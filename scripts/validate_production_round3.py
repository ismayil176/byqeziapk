from pathlib import Path
import sys
root = Path(__file__).resolve().parents[1]
main = (root / 'app/src/main/java/az/byqezi/app/MainActivity.java').read_text(encoding='utf-8')
gradle = (root / 'app/build.gradle').read_text(encoding='utf-8')
manifest = (root / 'app/src/main/AndroidManifest.xml').read_text(encoding='utf-8')
failures=[]
def ok(name, cond):
    print(('OK: ' if cond else 'FAIL: ') + name)
    if not cond: failures.append(name)

ok('No WebView/TWA imports', 'WebView' not in main and 'android.webkit' not in main and 'TrustedWebActivity' not in main)
ok('Header dropdown/menu removed', '☰' not in main and 'showMenu()' not in main)
ok('Production image minimum aligned to backend', 'MIN_ADD_IMAGES = 3' in main and 'Ən azı 3 şəkil seçin' in main)
ok('Maximum images constant is 15', 'MAX_ADD_IMAGES = 15' in main and 'MAX_ADD_IMAGES' in main)
ok('API base URL is build-configurable', 'BYQEZI_API_BASE_URL' in gradle and 'API_BASE_URL' in gradle)
ok('Target SDK is Play-ready API 35', 'targetSdk 35' in gradle)
ok('Cleartext traffic disabled', 'android:usesCleartextTraffic="false"' in manifest)
ok('Backup disabled', 'android:allowBackup="false"' in manifest)
ok('No hardcoded secret placeholders in Android code', 'ONESMS_API_KEY' not in main and 'D1_DATABASE' not in main and 'EPOINT_PRIVATE_KEY' not in main)
if failures:
    print('\nFailed checks: ' + ', '.join(failures))
    sys.exit(1)
print('\nAll production-prep checks passed.')
