from pathlib import Path
import sys
root = Path(__file__).resolve().parents[1]
main = (root / 'app/src/main/java/az/byqezi/app/MainActivity.java').read_text(encoding='utf-8')
gradle = (root / 'app/build.gradle').read_text(encoding='utf-8')
failures = []

def ok(name, cond):
    print(('OK: ' if cond else 'FAIL: ') + name)
    if not cond:
        failures.append(name)

ok('No WebView/TWA', 'WebView' not in main and 'android.webkit' not in main and 'TrustedWebActivity' not in main)
ok('Android 15 safe-area insets', 'setDecorFitsSystemWindows(false)' in main and 'WindowInsets.Type.systemBars()' in main and 'root.setPadding(0, systemTopInset, 0, systemBottomInset)' in main)
ok('Real OTP request endpoint wired', 'postJson("/api/auth/otp/request"' in main and 'SMS kod göndər' in main)
ok('Real OTP verify endpoint wired', 'postJson("/api/auth/otp/verify"' in main and 'challenge_id' in main)
ok('Real upload endpoint wired', 'absoluteApiUrl("/api/upload")' in main and 'multipart/form-data; boundary=' in main)
ok('Real listing submit endpoint wired', 'postJson("/api/listings", body)' in main and '_user_submit' in main)
ok('Cabinet real mine fetch', 'requestListings("/api/listings?mine=1&limit=100")' in main and 'refreshUserListings' in main)
ok('Startup demo rows removed', 'listings.addAll(Listing.demoRows())' not in main)
ok('Photo ordering controls', 'moveDraftImage' in main and 'makeDraftImageMain' in main and 'Ana et' in main)
ok('Relative backend image URLs supported', 'absoluteApiUrl(url)' in main and 'BuildConfig.API_BASE_URL + raw' in main)
ok('Version bumped to round 5+', ('versionCode 5' in gradle and "versionName '1.0.4-real-api-safearea'" in gradle) or ('versionCode 6' in gradle and "versionName '1.0.5-real-data-csrf-polish'" in gradle) or ('versionCode 7' in gradle and "versionName '1.0.6-session-persistence'" in gradle) or ('versionCode 8' in gradle and "versionName '1.0.7-promotions-billing'" in gradle) or ('versionCode 9' in gradle and "versionName '1.0.8-real-detail-no-demo'" in gradle))

if failures:
    print('\nFailed checks: ' + ', '.join(failures))
    sys.exit(1)
print('\nAll round 5 real API/safe-area checks passed.')
