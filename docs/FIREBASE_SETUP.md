# Firebase and credential setup

## Files that must never be committed

- `app/google-services.json` (real)
- Cloud Functions secrets
- Any Gemini API key
- Service account JSON

## Custom claims

Student clients cannot become admins by editing `users/{uid}`.

Security rules check `request.auth.token.role == 'admin'`.

## Cloud Functions

`functions/` is a Node.js codebase. Keep it outside the Android module so Gradle does not compile it.

Required callables:

- `setAdminRole`
- `submitTest`
- `askStudyAssistant`
- `createVoiceSession`

Scoring and AI usage accounting happen here so clients cannot trust themselves.

## Storage path

```
pdfs/{pdfId}/source.pdf
```

Failed uploads must not leave a published Firestore document. The Android uploader writes metadata only after `putFile` succeeds, and publish defaults to false.
