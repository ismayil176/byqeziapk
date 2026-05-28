from pathlib import Path
root = Path(__file__).resolve().parents[1]
main = (root/'app/src/main/java/az/byqezi/app/MainActivity.java').read_text()
build = (root/'app/build.gradle').read_text()
checks = [
    ('No WebView', 'WebView' not in main and 'android.webkit' not in main),
    ('No TWA/browserhelper', 'androidbrowserhelper' not in main.lower() and 'trustedwebactivity' not in main.lower()),
    ('Home UI present', 'showHome' in main and 'searchCard' in main and 'carCard' in main),
    ('Brand model autocomplete', 'AutoCompleteTextView' in main and 'modelsFor' in main),
    ('Footer info pages', 'showInfo' in main and 'Məxfilik' in main and 'Haqqımızda' in main and 'Əlaqə' in main),
    ('target SDK 35', 'targetSdk 35' in build and 'compileSdk 35' in build),
]
for label, ok in checks:
    print(('OK: ' if ok else 'FAIL: ') + label)
if not all(ok for _, ok in checks):
    raise SystemExit(1)
