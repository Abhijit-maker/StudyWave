# StudyWave

Native Android study companion built with Kotlin and Jetpack Compose.

White and lavender UI, Firebase backend, role-protected admin tools, PDF library, mock tests, AI study room, and a Live Voice screen with an explicit pending adapter.

This is not a website, WebView shell, or static prototype.

## What is already in the project

- Jetpack Compose student app: splash, onboarding, auth, home, library, PDF viewer, tests, AI, voice, notices, profile
- Admin Mode: overview, PDF manager, tests, notices, users, AI config, logs, settings
- Firebase Auth / Firestore / Storage / Functions / FCM adapters
- Security rules that keep answer keys and roles off ordinary clients
- Cloud Functions source in `functions/` (separate from the Android Gradle build)
- Design tokens from the attached UI references (`#FFFFFF`, `#F2EEFF`, `#7657E8`, `#4C8DFF`)

## What you must configure

The Android sources do not contain a Gemini key, Firebase secrets, or `google-services.json`.

Until those are added, the app launches and shows setup-required states instead of fake data.

### 1. Firebase

1. Create a Firebase project.
2. Add an Android app with package name `com.studywave.app`.
3. Download `google-services.json` into `app/google-services.json`.
4. Enable Email/Password authentication.
5. Create Firestore and Storage.
6. Deploy rules:

```bash
firebase deploy --only firestore:rules,storage
```

7. Copy `app/google-services.json.example` only as a shape reference. Never commit a real file with live keys if your repository is public.

### 2. First administrator

Do not write `role: admin` on a Firestore user document. That is ignored by security rules.

After you create your own Auth user, run a trusted command from a machine that already has Firebase admin access:

```bash
# Deploy functions first, then call setAdminRole for your uid
firebase deploy --only functions
```

Use the Firebase console or a one-time Admin SDK script to set custom claim `{ "role": "admin" }` on your uid, then sign out and sign in again so the ID token refreshes.

### 3. Gemini / AI

Store `GEMINI_API_KEY` in Cloud Functions Secret Manager, never in Kotlin, BuildConfig, Firestore, or the APK.

```bash
firebase functions:secrets:set GEMINI_API_KEY
firebase deploy --only functions
```

AI chat calls `askStudyAssistant`. Live Voice calls `createVoiceSession` and stays marked pending until that function returns a real short-lived credential.

### 4. PDF uploads

Only admins can upload. Files go to `pdfs/{pdfId}/source.pdf`. Metadata is stored in `pdfs/{pdfId}` and stays unpublished until an admin toggles publish. Default limit is 50 MB.

## Open in Android Studio or Google AI Studio

1. Push this repository to GitHub.
2. In Google AI Studio, import the GitHub repo as an Android project.
3. Add `app/google-services.json` locally (do not paste secrets into chat).
4. Sync Gradle, then Run.

Local build:

```bash
# Generate the wrapper jar once if Android Studio has not done it
gradle wrapper --gradle-version 8.9

# Debug APK
./gradlew :app:assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/`.

## Firestore collections

- `users`
- `subjects`
- `pdfs`
- `mockTests` plus `questions` and protected `answerKey`
- `testAttempts`
- `notices`
- `conversations` / `messages`
- `readingProgress`
- `bookmarks`
- `appConfig/public`
- `aiUsage`
- `auditLogs`

## QA status

| Check | Status |
| --- | --- |
| Project structure, theme, navigation, student and admin screens | Implemented |
| Firebase repositories and setup-required UI | Implemented |
| Firestore / Storage rules | Written, not deployed from this environment |
| Cloud Functions source | Written in `functions/`, not deployed from this environment |
| Gemini key in Android source | Not present |
| Live Voice real-time session | UI complete, adapter reports missing backend credential |
| Gradle assemble in this environment | Not claimed. Run `./gradlew :app:assembleDebug` on a machine with Android SDK |

## GitHub upload

```bash
git init
git add .
git commit -m "feat: add StudyWave Android app"
git branch -M main
git remote add origin https://github.com/YOUR_USER/studywave.git
git push -u origin main
```

Do not add `google-services.json`, `.env`, or API keys to the commit.
