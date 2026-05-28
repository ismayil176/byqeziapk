#!/usr/bin/env python3
from pathlib import Path
import re, sys
root = Path(__file__).resolve().parents[1]
main = (root/'app/src/main/java/az/byqezi/app/MainActivity.java').read_text(encoding='utf-8')
workflow = (root/'.github/workflows/android-build.yml').read_text(encoding='utf-8')
gradle = (root/'app/build.gradle').read_text(encoding='utf-8')
checks = []
def ok(name, cond): checks.append((name, bool(cond)))
# no wrappers
ok('No WebView', 'WebView' not in main and 'android.webkit' not in main)
ok('No TWA/browserhelper', 'TrustedWebActivity' not in main and 'androidbrowserhelper' not in main)
# crash fix
ok('Brand onItemClick guarded', 'currentBrandInput.setOnItemClickListener' in main and 'try {' in main[main.find('currentBrandInput.setOnItemClickListener'):main.find('currentBrandInput.addTextChangedListener')])
ok('No auto model focus after brand selection', 'currentModelInput.requestFocus();' not in main)
ok('No auto model dropdown after brand selection', 'currentModelInput.showDropDown();\n        });\n        currentBrandInput.addTextChangedListener' not in main)
ok('Model disabled for non-exact brand', 'setModelEnabled(false, "");' in main and 'String exact = exactBrand(brandQuery);' in main)
# premium/vip separation
ok('Separate premium list exists', 'private final ArrayList<Listing> premiumListings' in main and 'private ArrayList<Listing> premiumRows()' in main)
ok('Separate vip list exists', 'private final ArrayList<Listing> vipListings' in main and 'private ArrayList<Listing> vipRows()' in main)
ok('Premium collection API fetched', 'collection=premium' in main)
ok('VIP collection API fetched', 'collection=vip' in main)
ok('Home premium section title', 'PREMİUM ELANLAR' in main)
ok('Home vip section uses vipRows only', 'ArrayList<Listing> vipRows = vipRows();' in main)
ok('VIP screen only active VIP', 'Yalnız aktiv VIP elanlar' in main and 'vipRows()' in main)
ok('No VIP or premium mixed filter', 'row.isVipActive() || row.isPremiumActive()' not in main)
ok('Premium parser supports featured/premium', 'obj.optBoolean("featured", false)' in main and 'obj.optBoolean("premium", false)' in main)
ok('Premium parser supports premium_until', 'premium_until' in main)
ok('VIP parser supports vip_until', 'vip_until' in main)
# build config
ok('Java regex escaped', '([+-]\\\\d{2}):(\\\\d{2})$' in main or '([+-]\\d{2}):(\\d{2})$' in main)
ok('target SDK 35', 'targetSdk 35' in gradle)
ok('workflow validator round5', 'validate_home_round5.py' in workflow)
failed = [name for name, cond in checks if not cond]
for name, cond in checks:
    print(('OK: ' if cond else 'FAIL: ') + name)
if failed:
    print('\nFailed checks:', ', '.join(failed))
    sys.exit(1)
