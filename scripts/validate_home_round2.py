from pathlib import Path
root = Path(__file__).resolve().parents[1]
main = root / 'app/src/main/java/az/byqezi/app/MainActivity.java'
text = main.read_text(encoding='utf-8')
checks = [
    ('No WebView', 'WebView' not in text and 'android.webkit' not in text),
    ('No TWA/browserhelper', 'TrustedWebActivity' not in text and 'androidbrowserhelper' not in text),
    ('Home ad 03 used', 'R.drawable.home_ad_03' in text),
    ('Ad height 203dp', 'localImage(R.drawable.home_ad_03, dp(203))' in text),
    ('Card media 128dp', 'FrameLayout.LayoutParams(-1, dp(128))' in text),
    ('Cleaner font padding', 'setIncludeFontPadding(false)' in text),
    ('Logo returns home', 'logo.setOnClickListener(v -> showHome(false))' in text and 'brand.setOnClickListener(v -> showHome(false))' in text),
    ('Autocomplete present', 'AutoCompleteTextView' in text and 'showDropDown()' in text),
    ('Info pages present', 'showInfo(' in text and 'Məxfilik' in text and 'Haqqımızda' in text and 'Əlaqə' in text),
    ('API base https', '"https://www.byqezi.az"' in (root / 'app/build.gradle').read_text(encoding='utf-8')),
    ('target SDK 35', 'targetSdk 35' in (root / 'app/build.gradle').read_text(encoding='utf-8')),
]
failed = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(('OK: ' if ok else 'FAIL: ') + name)
if failed:
    raise SystemExit('Failed checks: ' + ', '.join(failed))
