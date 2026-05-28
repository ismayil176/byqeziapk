from pathlib import Path
root = Path(__file__).resolve().parents[1]
main = (root / 'app/src/main/java/az/byqezi/app/MainActivity.java').read_text()
gradle = (root / 'app/build.gradle').read_text()

def ok(name, cond):
    if not cond:
        raise SystemExit(f'FAIL: {name}')
    print(f'OK: {name}')

ok('CSRF helper exists', 'csrfTokenFromCookies' in main and 'attachCsrfHeader' in main and 'ensureCsrfCookie' in main)
ok('Unsafe API requests attach CSRF', 'conn.setRequestProperty("x-csrf-token", token)' in main and 'x-byqezi-client' in main)
ok('Upload attaches CSRF', 'multipart/form-data; boundary=' in main and 'attachCsrfHeader(conn);' in main)
ok('Login demo/helper text removed', 'Kod real 1SMS servisi ilə göndərilir' not in main)
ok('Price history helper text removed', 'Qiymət dəyişiklikləri demo məlumat əsasında göstərilir' not in main and 'Qiymət dəyişiklikləri olduqda burada görünəcək' not in main)
ok('Favorite heart polished', 'FrameLayout.LayoutParams(dp(34), dp(34)' in main and 'text(isFavorite(listing) ? "♥" : "♡", 20' in main)
ok('Full model catalog synced', 'put("Toyota", modelList(' in main and 'Land Cruiser Prado' in main and 'put("Abarth", modelList(' in main and 'put("Mercedes-Benz", modelList(' in main)
ok('Version round 6+', ('versionCode 6' in gradle and "versionName '1.0.5-real-data-csrf-polish'" in gradle) or ('versionCode 7' in gradle and "versionName '1.0.6-session-persistence'" in gradle) or ('versionCode 8' in gradle and "versionName '1.0.7-promotions-billing'" in gradle) or ('versionCode 9' in gradle and "versionName '1.0.8-real-detail-no-demo'" in gradle))
print('All round 6 checks passed.')
