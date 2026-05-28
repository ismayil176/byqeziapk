# Round 5 — Real API + Safe Area + Photo Ordering

- Removed startup demo dependency: home uses live /api/listings data only.
- Added native OTP request/verify screens calling /api/auth/otp/request and /api/auth/otp/verify.
- Added cookie-based user session support for Cloudflare Pages APIs.
- Cabinet fetches real /api/listings?mine=1 data after login.
- Listing submit uploads selected images to /api/upload, then posts to /api/listings with _user_submit=true.
- Added status/navigation bar insets handling for Android 15 / Samsung devices so header and bottom navigation do not overlap system bars.
- Added visible photo controls: move left, move right, make main, delete.
- Supports relative /api/images/... URLs from backend.

Note: If Cloudflare Turnstile is enabled on OTP/listing APIs and the native app is not given a native Turnstile/Play Integrity path, server may reject requests with security-check errors.
