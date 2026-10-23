# GitHub then Google AI Studio

StudyWave is a native Android project. After this folder is on GitHub, open it in Google AI Studio as an Android app.

## 1. Put the project on GitHub

From a machine with `git` and your GitHub account:

```bash
cd /path/to/StudyWave

git add .
git status
```

Confirm `app/google-services.json` is not staged.

```bash
git commit -m "feat: add StudyWave Android app foundation"
git branch -M main
git remote add origin https://github.com/YOUR_USER/YOUR_REPO.git
git push -u origin main
```

## 2. Import in Google AI Studio

1. Open Google AI Studio.
2. Import the GitHub repository.
3. Choose the Android / Kotlin project at the repo root.
4. Add `app/google-services.json` only on your local or Studio secrets storage.
5. Sync Gradle, then run on an emulator or device.

## 3. Do not paste secrets into Studio chat

Gemini keys belong in Cloud Functions Secret Manager. Firebase config belongs in `google-services.json`, which stays out of git.
