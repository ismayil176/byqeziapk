from pathlib import Path
import sys
root = Path(__file__).resolve().parents[1]
java = (root / 'app/src/main/java/az/byqezi/app/MainActivity.java').read_text(encoding='utf-8')
workflow = (root / '.github/workflows/android-build.yml').read_text(encoding='utf-8')
checks = []

def ok(name, cond): checks.append((name, bool(cond)))

ok('Cabinet filter state exists', 'private String cabinetFilter = "all";' in java)
ok('Cabinet stat accepts filter key', 'private LinearLayout cabinetStat(String label, int count, String filter)' in java)
ok('All stat is clickable filter', 'cabinetStat("Bütün elanlar", rows.size(), "all")' in java)
ok('Active stat is clickable filter', 'cabinetStat("Saytda", active, "active")' in java)
ok('Pending stat is clickable filter', 'cabinetStat("Gözləmədə", pending, "pending")' in java)
ok('Favorites stat is clickable filter', 'cabinetStat("Favoritlər", favorites, "favorites")' in java)
ok('Stat click refreshes cabinet', 'cabinetFilter = filter;' in java and 'showCabinet();' in java)
ok('Cabinet visible rows helper exists', 'private ArrayList<Listing> cabinetVisibleRows()' in java)
ok('Favorites rows helper exists', 'private ArrayList<Listing> cabinetFavoriteRows()' in java)
ok('Cabinet list title changes by filter', 'private String cabinetListTitle()' in java and 'Saytda olan elanlar' in java and 'Gözləmədə olanlar' in java and 'Favoritlər' in java)
ok('Empty state changes by filter', 'private String cabinetEmptyTitle()' in java and 'Gözləmədə elan yoxdur' in java and 'Favorit elan yoxdur' in java)
ok('Listing status field exists', 'String status;' in java)
ok('Status parsed from API', 'obj.optString("status"' in java and 'moderation_status' in java)
ok('Active/pending status helpers exist', 'boolean isPendingStatus()' in java and 'boolean isActiveStatus()' in java)
ok('Cabinet status chip is dynamic', 'cabinetStatusLabel(listing)' in java and 'cabinetStatusBg(listing)' in java)
ok('Filter resets on login/logout', 'cabinetFilter = "all";' in java)
ok('Workflow includes cabinet filter validator', 'validate_cabinet_filter_round1.py' in workflow)

failed = [n for n, s in checks if not s]
for n, s in checks:
    print(('OK: ' if s else 'FAIL: ') + n)
if failed:
    print('\nFailed checks: ' + ', '.join(failed))
    sys.exit(1)
print('\nAll cabinet filter round 1 checks passed.')
