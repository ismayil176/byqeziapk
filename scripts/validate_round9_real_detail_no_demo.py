from pathlib import Path
root = Path(__file__).resolve().parents[1]
java = (root / 'app/src/main/java/az/byqezi/app/MainActivity.java').read_text(encoding='utf-8')
gradle = (root / 'app/build.gradle').read_text(encoding='utf-8')
required = [
    'requestListingDetail',
    '"/api/listings/" + Uri.encode(id)',
    'Baxışların sayı " + formatCount(listing.views)',
    'Yerləşdirildi: " + listing.fullDateText()',
    'l.createdAt = firstNonEmpty(obj.optString("created_at", "")',
    'l.views = intValue(obj, "views", "view_count", "views_count")',
    'if (!listing.equipment.isEmpty())',
    'if (listing.credit)',
    'if (listing.barter)',
    'l.mine = boolValue(obj, "mine", "is_mine");',
]
missing = [x for x in required if x not in java]
if missing:
    raise SystemExit('Missing round9 real-detail markers: ' + ', '.join(missing))
for forbidden in [
    'Baxışların sayı 0',
    'Rauf Əliyev',
    '+994559999999',
    'Full komplektasiya, ideal vəziyyət',
    'owner_id", -100) == 1',
    'user_id", -100) == 1',
]:
    if forbidden in java:
        raise SystemExit('Forbidden demo/fallback marker still present: ' + forbidden)
if 'versionCode 9' not in gradle or "versionName '1.0.8-real-detail-no-demo'" not in gradle:
    raise SystemExit('Round9 version markers are missing')
print('round9 real detail/no-demo validation passed')
