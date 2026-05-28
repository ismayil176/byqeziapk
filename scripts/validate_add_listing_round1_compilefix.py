from pathlib import Path
import sys

root = Path(__file__).resolve().parents[1]
main = (root / 'app/src/main/java/az/byqezi/app/MainActivity.java').read_text(encoding='utf-8')
workflow = (root / '.github/workflows/android-build.yml').read_text(encoding='utf-8')
failures = []

def ok(name, cond):
    print(('OK: ' if cond else 'FAIL: ') + name)
    if not cond:
        failures.append(name)

ok('modelsForBrand helper exists', 'private String[] modelsForBrand(String brand)' in main)
ok('modelsForBrand delegates to modelsFor', 'return modelsFor(brand);' in main)
ok('add brand model still uses helper', 'modelsForBrand(exact)' in main)
ok('existing modelsFor exists', 'private String[] modelsFor(String brand)' in main)
ok('workflow exists', (root / '.github/workflows/android-build.yml').exists())
ok('no WebView', 'WebView' not in main and 'android.webkit' not in main)

if failures:
    print('\nFailed checks: ' + ', '.join(failures))
    sys.exit(1)
print('\nAll add listing compile fix checks passed.')
