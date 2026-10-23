# StudyWave QA checklist

Run these on a device or emulator after Firebase is configured. This environment did not assemble an APK.

## Launch and navigation

- [ ] App launches to splash, then onboarding for signed-out users
- [ ] Authenticated users skip onboarding
- [ ] Bottom navigation: Home, Library, Tests, AI, Profile
- [ ] Android back button returns to the previous screen
- [ ] No dead navigation items

## Authentication

- [ ] Register with email and password
- [ ] Sign in success and invalid-credential error
- [ ] Password reset sends email
- [ ] Sign out returns to onboarding
- [ ] Passwords are never written to Firestore

## Data and permissions

- [ ] Without `google-services.json`, screens show setup-required, not fake PDFs
- [ ] Students cannot open Admin Mode
- [ ] Admin Mode appears only after custom claim `role=admin`
- [ ] Student cannot become admin by editing a profile document

## PDFs

- [ ] Admin document picker uploads to `pdfs/{id}/source.pdf`
- [ ] Progress is visible during upload
- [ ] Failed upload does not publish a document
- [ ] Student library shows only published PDFs
- [ ] Authorized user can open a real PDF from Storage
- [ ] File larger than configured limit is rejected

## Tests

- [ ] Published test loads questions
- [ ] Answers autosave
- [ ] Submit goes through `submitTest`
- [ ] Answer keys are not on the student-readable document
- [ ] Duplicate submit returns the existing result

## Notices and AI

- [ ] New notice appears without a new APK
- [ ] Notices remain visible if FCM is disabled
- [ ] AI chat calls `askStudyAssistant` when Functions are deployed
- [ ] AI errors do not crash the app
- [ ] Live Voice shows missing-configuration instead of a fake conversation

## Security review

- [ ] No Gemini key in Kotlin, BuildConfig, assets, or git
- [ ] Firestore rules deployed, not the default open rules
- [ ] Storage writes limited to admins and 50 MB PDFs
