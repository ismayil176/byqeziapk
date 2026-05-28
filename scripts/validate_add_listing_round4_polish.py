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
ok('Top add hero title removed from code', 'Elan əlavə edin' not in main and 'Məlumatları bölmə-bölmə' not in main)
ok('Extra technical info toggle removed from code', 'Əlavə texniki məlumat' not in main)
ok('Add screen preserves scroll on selection refresh', 'boolean preserveScroll = "add".equals(activeScreen)' in main and 'restoreScrollY' in main)
ok('Engine volume custom input exists', 'addEngineVolumeInput()' in main and 'engineVolumeDisplay(addDraft.engineVolume)' in main)
ok('Engine volume appends L suffix', 'return clean + " L";' in main and 'input.setSelection(Math.min(clean.length()' in main)
ok('Description textarea polished', 'desc.setPadding(dp(18), dp(14), dp(18), dp(14))' in main and 'new LinearLayout.LayoutParams(-1, dp(132))' in main)
ok('Submit button enlarged', 'submit.setMinHeight(dp(52))' in main and 'submitLp.height = dp(52)' in main)
ok('Version bumped', ('versionCode 4' in gradle and "versionName '1.0.3-final-add-listing-polish'" in gradle) or ('versionCode 5' in gradle and "versionName '1.0.4-real-api-safearea'" in gradle) or ('versionCode 6' in gradle and "versionName '1.0.5-real-data-csrf-polish'" in gradle) or ('versionCode 7' in gradle and "versionName '1.0.6-session-persistence'" in gradle) or ('versionCode 8' in gradle and "versionName '1.0.7-promotions-billing'" in gradle) or ('versionCode 9' in gradle and "versionName '1.0.8-real-detail-no-demo'" in gradle))

if failures:
    print('\nFailed checks: ' + ', '.join(failures))
    sys.exit(1)
print('\nAll add listing round 4 polish checks passed.')
