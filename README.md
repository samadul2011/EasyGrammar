# EasyGrammar

EasyGrammar is an offline Android app that teaches English grammar aligned with the Bangladesh (NCTB) curriculum for Class 8, SSC and HSC students. It includes lessons, examples, topic-wise tests and full practice tests designed for exam preparation.

## Key Features

- 100% offline and lightweight (<10MB)
- Curriculum-aligned content for Class 8, SSC (Class 9-10) and HSC (Class 11-12)
- 1000+ practice questions and topic tests
- Save words with `My Learning Words`
- Text-to-Speech support (device TTS)
- No ads, simple UI

## Repo Structure (high level)

- `app/` — Android app module
- `release/app-release.aab` — prebuilt release bundle (if present)
- `extracted_apk/` — extracted resources from an APK
- utility scripts: `find_duplicates.py`, `fix_all_duplicates.py`, etc.
- `PLAY_STORE_LISTING.md` — Play Store description and ASO guidance

## Getting Started

Prerequisites:
- JDK 11+
- Android Studio (recommended) or Gradle CLI

To open and run the app:

1. Clone the repository:

```powershell
git clone https://github.com/samadul2011/EasyGrammar.git
cd EasyGrammar
```

2. Open the project in Android Studio and let it sync Gradle.
3. Build and run on an emulator or device via Android Studio's Run actions.

Or build from command line:

```powershell
.\\gradlew assembleDebug
.\\gradlew assembleRelease
```

The generated APK/AAB will be in `app/build/outputs/` or the included `release/` folder.

## Branches & Remote

This local code was pushed to a branch named `import-local` on the remote repository to avoid overwriting existing history. You can create a pull request on GitHub to merge it into `main`.

## Contributing

- Open an issue or fork the repo and submit a pull request.
- Please avoid committing large generated build outputs and consider adding or updating `.gitignore` for build artifacts.

## Contact

Created by Md Samadul Hoque
- Email: samadul2011@gmail.com
- Phone: +968-97550832

## License

This repository does not include a license file. Add a license (for example MIT) if you want others to reuse the code.
