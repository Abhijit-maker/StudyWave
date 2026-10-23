const { onCall, HttpsError } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const admin = require("firebase-admin");

admin.initializeApp();
const db = admin.firestore();
const geminiKey = defineSecret("GEMINI_API_KEY");

function requireAuth(request) {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Sign in required.");
  }
  return request.auth.uid;
}

function requireAdmin(request) {
  const uid = requireAuth(request);
  if (request.auth.token.role !== "admin") {
    throw new HttpsError("permission-denied", "Administrator claim required.");
  }
  return uid;
}

exports.setAdminRole = onCall(async (request) => {
  const actor = requireAdmin(request);
  const targetUid = request.data && request.data.uid;
  if (!targetUid) {
    throw new HttpsError("invalid-argument", "uid is required.");
  }
  await admin.auth().setCustomUserClaims(targetUid, { role: "admin" });
  await db.collection("auditLogs").add({
    actorUid: actor,
    action: "setAdminRole",
    target: targetUid,
    createdAt: Date.now()
  });
  return { ok: true };
});

exports.submitTest = onCall(async (request) => {
  const uid = requireAuth(request);
  const attemptId = request.data && request.data.attemptId;
  if (!attemptId) {
    throw new HttpsError("invalid-argument", "attemptId is required.");
  }
  const attemptRef = db.collection("testAttempts").doc(attemptId);
  const attemptSnap = await attemptRef.get();
  if (!attemptSnap.exists) {
    throw new HttpsError("not-found", "Attempt not found.");
  }
  const attempt = attemptSnap.data();
  if (attempt.uid !== uid) {
    throw new HttpsError("permission-denied", "This attempt belongs to another user.");
  }
  if (attempt.submittedAt) {
    return attempt;
  }
  const keySnap = await db.collection("mockTests").doc(attempt.testId).collection("answerKey").get();
  const answers = attempt.answers || {};
  let correct = 0;
  let incorrect = 0;
  let unanswered = 0;
  let score = 0;
  keySnap.forEach((doc) => {
    const key = doc.data();
    const given = answers[doc.id];
    if (given == null || given === "") {
      unanswered += 1;
      return;
    }
    if (String(given) === String(key.correctAnswer)) {
      correct += 1;
      score += Number(key.marks || 1);
    } else {
      incorrect += 1;
      score -= Number(key.negative || 0);
    }
  });
  const payload = {
    submittedAt: Date.now(),
    score,
    correct,
    incorrect,
    unanswered,
    status: "GRADED"
  };
  await attemptRef.update(payload);
  return Object.assign({ id: attemptId, testId: attempt.testId, uid }, payload);
});

exports.askStudyAssistant = onCall({ secrets: [geminiKey] }, async (request) => {
  const uid = requireAuth(request);
  const conversationId = request.data && request.data.conversationId;
  const text = request.data && request.data.text;
  if (!conversationId || !text) {
    throw new HttpsError("invalid-argument", "conversationId and text are required.");
  }
  const usageRef = db.collection("aiUsage").doc(uid);
  await db.runTransaction(async (tx) => {
    const snap = await tx.get(usageRef);
    const used = snap.exists ? Number(snap.data().requestsToday || 0) : 0;
    if (used >= 40) {
      throw new HttpsError("resource-exhausted", "Daily AI limit reached.");
    }
    tx.set(usageRef, { requestsToday: used + 1, updatedAt: Date.now() }, { merge: true });
  });

  const key = geminiKey.value();
  if (!key) {
    throw new HttpsError("failed-precondition", "GEMINI_API_KEY is not configured in Secret Manager.");
  }

  const body = {
    contents: [{ role: "user", parts: [{ text: String(text) }] }]
  };
  const response = await fetch(
    "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + encodeURIComponent(key),
    {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(body)
    }
  );
  if (!response.ok) {
    throw new HttpsError("unavailable", "Gemini request failed.");
  }
  const json = await response.json();
  const answer = (((json.candidates || [])[0] || {}).content || {}).parts
    ? json.candidates[0].content.parts.map((part) => part.text || "").join("\n")
    : "I could not generate a response.";

  await db.collection("conversations").doc(conversationId).collection("messages").add({
    role: "ASSISTANT",
    text: answer,
    createdAt: Date.now(),
    status: "COMPLETE"
  });
  await db.collection("conversations").doc(conversationId).update({ updatedAt: Date.now() });
  return { ok: true };
});

exports.createVoiceSession = onCall({ secrets: [geminiKey] }, async (request) => {
  requireAuth(request);
  const key = geminiKey.value();
  if (!key) {
    throw new HttpsError("failed-precondition", "Live Voice is pending GEMINI_API_KEY configuration.");
  }
  return {
    expiresInSec: 60,
    note: "Issue a short-lived Live API credential here. Do not ship the secret to the APK."
  };
});
