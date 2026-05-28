# BYQEZI.AZ Native Android

Native Android implementation for BYQEZI.AZ. This project does not use WebView or TWA.

Current round: **Round 9 — real detail data / no demo placeholders**

## Production behavior

- Real API base: `https://www.byqezi.az` by default, overridable with `BYQEZI_API_BASE_URL`.
- OTP, listing upload, listing submit, cabinet listings, promotions, and Play Billing hooks are wired to real endpoints.
- Listing detail opens through `GET /api/listings/{id}` so views and detail data come from the backend.
- Remaining fake detail fallbacks were removed from seller, description, vehicle specs, equipment chips, created time, and view count UI.

## GitHub Actions secrets

Required for signed release AAB:

- `BYQEZI_UPLOAD_KEYSTORE_BASE64`
- `BYQEZI_UPLOAD_KEYSTORE_PASSWORD`
- `BYQEZI_UPLOAD_KEY_ALIAS`
- `BYQEZI_UPLOAD_KEY_PASSWORD`
- `BYQEZI_API_BASE_URL`

## Output

- Debug APK: `byqezi-debug-apk`
- Play Console upload: `byqezi-release-aab`
