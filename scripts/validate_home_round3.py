from pathlib import Path
root = Path(__file__).resolve().parents[1]
main = root / 'app/src/main/java/az/byqezi/app/MainActivity.java'
text = main.read_text(encoding='utf-8')
build = (root / 'app/build.gradle').read_text(encoding='utf-8')
workflow = (root / '.github/workflows/android-build.yml').read_text(encoding='utf-8')
checks = [
    ('No WebView', 'WebView' not in text and 'android.webkit' not in text),
    ('No TWA/browserhelper', 'TrustedWebActivity' not in text and 'androidbrowserhelper' not in text),
    ('Home ad 03 used', 'R.drawable.home_ad_03' in text),
    ('Banner height applied by parent layout params', 'new LinearLayout.LayoutParams(-1, dp(203))' in text and 'content.addView(ad, adLp)' in text),
    ('Old wrap-content ad call removed', 'content.addView(ad, topLp(14))' not in text and 'localImage(R.drawable.home_ad_03, dp(203))' not in text),
    ('Model disabled for non-exact brand', 'String exact = exactBrand(brandQuery)' in text and 'setModelEnabled(false, "")' in text and 'enabled ? modelsFor(brand) : new String[0]' in text),
    ('No all-model fallback', 'return all.toArray(new String[0])' not in text),
    ('Exact brand helper exists', 'private String exactBrand(String value)' in text),
    ('Card media 128dp', 'FrameLayout.LayoutParams(-1, dp(128))' in text),
    ('Cleaner font padding', 'setIncludeFontPadding(false)' in text),
    ('Logo returns home', 'logo.setOnClickListener(v -> showHome(false))' in text and 'brand.setOnClickListener(v -> showHome(false))' in text),
    ('Autocomplete present', 'AutoCompleteTextView' in text and 'showDropDown()' in text),
    ('Info pages present', 'showInfo(' in text and 'Məxfilik' in text and 'Haqqımızda' in text and 'Əlaqə' in text),
    ('API base https', '"https://www.byqezi.az"' in build),
    ('target SDK 35', 'targetSdk 35' in build),
    ('workflow uses round4 validator', 'validate_home_round4.py' in workflow),
]
failed = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(('OK: ' if ok else 'FAIL: ') + name)
if failed:
    raise SystemExit('Failed checks: ' + ', '.join(failed))
