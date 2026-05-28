# Round 4 — Add Listing Final UI Polish

This round keeps the Android app native. No WebView/TWA was added.

Changes:
- Removed the top add-listing hero title/subtitle (`Elan əlavə edin`, `Məlumatları bölmə-bölmə...`).
- Removed the visible `Əlavə texniki məlumatlar` toggle from the add-listing form.
- Preserved scroll position when selecting dropdown/pill/checkbox values so the page no longer jumps to the top after every choice.
- Engine volume field now displays the `L` suffix after the typed numeric value.
- Description textarea padding/height/gravity fixed so `Təsvir yazın` sits correctly.
- Submit button background/height increased for a stronger touch target.
- Version bumped to `1.0.3-final-add-listing-polish` / `versionCode 4`.

Production notes:
- Backend secrets remain in Cloudflare only.
- Android build uses `BYQEZI_API_BASE_URL`, defaulting to `https://www.byqezi.az`.
- Existing GitHub Actions still produces debug APK and release AAB.
