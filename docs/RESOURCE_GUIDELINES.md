# Resource & UI organization guidelines

This document explains how we structure resources, styles and UI assets to avoid merge conflicts and keep the project consistent.

1) Themes and styles
- Put application-level Theme definitions in: res/values/themes.xml only.
- Use res/values/styles.xml for smaller widget styles and component overrides.
- Avoid defining the same style name in multiple files.

2) Strings and localization
- All user-facing text must go in res/values/strings.xml.
- Spanish translations go in res/values-es/strings.xml.
- Do not hardcode strings in layout XML or code. Use @string/ keys.

3) Drawables and glass effect
- Keep glass blur/shape drawables in res/drawable/bg_glass_*.xml
- Large image assets and Lottie JSON files go into app/src/main/assets/ (create assets/lottie/ for animations)

4) Motion & animations
- MotionScene XML files live under res/xml/ (e.g. motion_scene_main.xml)
- MotionLayout layouts reference scenes using app:layoutDescription
- Lottie files loaded from assets/lottie/*.json; add workers to pre-load if needed

5) Layout naming
- Prefix fragments and activities: activity_main.xml, fragment_dashboard.xml
- Keep ids lowercase with underscores

6) Code style
- Use viewBinding or dataBinding instead of findViewById
- Avoid using non-null assertions (!!) — prefer safe-calls and let/also

7) CI / resource checks
- Add a CI step to run: ./gradlew :app:lint && ./gradlew assembleDebug
- Fail CI early on resource merge errors

Follow these rules to minimize conflicts and keep UI consistent.
