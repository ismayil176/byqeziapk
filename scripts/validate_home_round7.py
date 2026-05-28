#!/usr/bin/env python3
from pathlib import Path
import sys
root = Path(__file__).resolve().parents[1]
main = (root/'app/src/main/java/az/byqezi/app/MainActivity.java').read_text(encoding='utf-8')
workflow = (root/'.github/workflows/android-build.yml').read_text(encoding='utf-8')
gradle = (root/'app/build.gradle').read_text(encoding='utf-8')
checks = []
def ok(name, cond): checks.append((name, bool(cond)))
ok('No WebView', 'WebView' not in main and 'android.webkit' not in main)
ok('No TWA/browserhelper', 'TrustedWebActivity' not in main and 'androidbrowserhelper' not in main)
ok('target SDK 35', 'targetSdk 35' in gradle)
ok('workflow validator round7', 'validate_home_round7.py' in workflow)
ok('Duplicate top preview grid removed', 'firstTwo' not in main and 'regularRows.get(i)' not in main)
ok('Regular listings stay in BÜTÜN ELANLAR', 'BÜTÜN ELANLAR' in main and 'visibleRows(false)' in main)
ok('Premium block still separate', 'PREMİUM ELANLAR' in main and 'premiumRows()' in main and 'collection=premium' in main)
ok('VIP block still separate', 'VIP ELANLAR' in main and 'vipRows()' in main and 'collection=vip' in main)
ok('Brand item click protected', 'setOnItemClickListener' in main and 'try {' in main and 'Never let brand selection close the app' in main)
ok('Brand exact branch does not force dismiss during watcher', 'currentBrandInput.dismissDropDown();\n                    setModelEnabled(true, exact);\n                    return;' not in main)
ok('Brand selection avoids clearFocus keyboard crash', 'currentBrandInput.clearFocus()' not in main and 'currentModelInput.clearFocus()' not in main and 'hideKeyboard();\n        } catch' not in main)
ok('Safe adapter guard exists', 'Adapter changes must never crash' in main and 'options == null ? new String[0]' in main)
ok('Reset button still exists', 'button("Sıfırla"' in main)
ok('Model enabled only with exact brand', 'setModelEnabled(!TextUtils.isEmpty(initialExactBrand), initialExactBrand)' in main and 'modelsFor(String brand)' in main)
failed = [name for name, cond in checks if not cond]
for name, cond in checks:
    print(('OK: ' if cond else 'FAIL: ') + name)
if failed:
    print('\nFailed checks:', ', '.join(failed))
    sys.exit(1)
