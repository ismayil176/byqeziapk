# ROUND3 Production Readiness

## What changed
- Removed the top-right header dropdown / hamburger button from the native Android shell.
- Kept BYQEZI.AZ logo/brand as the only header action and it returns to Home.
- Aligned native add-listing image validation with the Cloudflare backend: minimum 3, maximum 15 images.
- Added optional GitHub Actions secret `BYQEZI_API_BASE_URL` for build-time API base override. Default remains `https://www.byqezi.az`.
- Added a validator to ensure no WebView/TWA, no header dropdown, target SDK 35, cleartext traffic disabled, and no backend secrets in Android code.

## Still not production-complete
The native app is visually and structurally prepared, but production release requires these integrations before public Play launch:
1. Real OTP login against `/api/auth/otp/request` and `/api/auth/otp/verify`.
2. Real image upload against `/api/upload`, then listing submit to `/api/listings`.
3. Play Billing for VIP/Premium/Bump digital promotion products.
4. AdMob integration with test ads first, then real ad unit IDs.
5. Internal testing on Google Play with signed AAB before production rollout.

## Where secrets belong
- Android app: no D1, 1SMS, Epoint, Cloudflare, or private API secrets.
- Cloudflare Pages/Workers: production variables/secrets such as `SESSION_SECRET`, `ONESMS_API_KEY`, `EPOINT_PRIVATE_KEY`, `TURNSTILE_SECRET_KEY`.
- GitHub Actions: only Android build/signing secrets such as upload keystore and optional public API base URL.
