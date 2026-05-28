#!/usr/bin/env python3
from pathlib import Path
import sys
root = Path(__file__).resolve().parents[1]
main = (root/'app/src/main/java/az/byqezi/app/MainActivity.java').read_text(encoding='utf-8')
workflow = (root/'.github/workflows/android-build.yml').read_text(encoding='utf-8')
gradle = (root/'app/build.gradle').read_text(encoding='utf-8')
checks = []
def ok(name, cond): checks.append((name, bool(cond)))
# wrappers/build
ok('No WebView', 'WebView' not in main and 'android.webkit' not in main)
ok('No TWA/browserhelper', 'TrustedWebActivity' not in main and 'androidbrowserhelper' not in main)
ok('target SDK 35', 'targetSdk 35' in gradle)
ok('workflow validator round6', 'validate_home_round6.py' in workflow)
# brand selection state
ok('Logo resets search before home', 'logo.setOnClickListener(v -> resetSearchAndShowHome())' in main and 'brand.setOnClickListener(v -> resetSearchAndShowHome())' in main)
ok('Reset helper clears brand/model', 'private void resetSearchAndShowHome()' in main and 'brandQuery = "";' in main and 'modelQuery = "";' in main)
ok('Brand selection helper exists', 'private void applyBrandSelection(String value)' in main)
ok('Brand onItemClick applies helper', 'applyBrandSelection(String.valueOf(raw))' in main)
ok('Old brand retry toast removed', 'Marka seçimi yenidən sınayın' not in main)
ok('Selection keeps brand in state', 'brandQuery = exact;' in main and 'currentBrandInput.setText(exact, false)' in main)
ok('Model disabled for non-exact brand', 'setModelEnabled(false, "");' in main and 'currentModelInput.setText("", false);' in main)
# reset button
ok('Reset button exists', 'button("Sıfırla"' in main)
ok('Reset button clears brand/model', 'reset.setOnClickListener' in main and 'brandQuery = "";' in main and 'modelQuery = "";' in main)
# premium/vip duplicate separation
ok('Regular home rows helper exists', 'private ArrayList<Listing> regularRows()' in main)
ok('Regular rows exclude premium and vip', 'row.isPremiumActive() || row.isVipActive()' in main and 'continue;' in main)
ok('Home first row uses regularRows', 'ArrayList<Listing> regularRows = regularRows();' in main and 'regularRows.get(i)' in main)
ok('Home visible all rows still available', 'ArrayList<Listing> visible = visibleRows(false);' in main and 'BÜTÜN ELANLAR' in main)
ok('Premium rows remain separate', 'private final ArrayList<Listing> premiumListings' in main and 'collection=premium' in main and 'premiumRows()' in main)
ok('VIP rows remain separate', 'private final ArrayList<Listing> vipListings' in main and 'collection=vip' in main and 'vipRows()' in main)
ok('VIP screen only active VIP', 'Yalnız aktiv VIP elanlar' in main and 'row.isVipActive() && matchesCurrentSearch(row)' in main)
failed = [name for name, cond in checks if not cond]
for name, cond in checks:
    print(('OK: ' if cond else 'FAIL: ') + name)
if failed:
    print('\nFailed checks:', ', '.join(failed))
    sys.exit(1)
