from pathlib import Path
root = Path(__file__).resolve().parents[1]
main = (root / 'app/src/main/java/az/byqezi/app/MainActivity.java').read_text()
gradle = (root / 'app/build.gradle').read_text()

def ok(name, cond):
    if not cond:
        raise SystemExit(f'FAIL: {name}')
    print(f'OK: {name}')

ok('SharedPreferences session store exists', 'SESSION_PREFS' in main and 'PREF_LOGGED_IN' in main and 'PREF_COOKIES' in main)
ok('Session restored on app startup', 'restoreNativeSession();' in main and 'getSharedPreferences(SESSION_PREFS, MODE_PRIVATE)' in main)
ok('Cookies serialized for persistent login', 'cookiesToJson()' in main and 'restoreCookiesFromJson' in main and 'nativeCookieManager.getCookieStore().add(baseUri, cookie)' in main)
ok('OTP verification saves session', 'userLoggedIn = true;' in main and 'saveNativeSession();' in main)
ok('Logout clears stored session', 'getSharedPreferences(SESSION_PREFS, MODE_PRIVATE).edit().clear().apply()' in main)
ok('Version round 7+', ('versionCode 7' in gradle and "versionName '1.0.6-session-persistence'" in gradle) or ('versionCode 8' in gradle and "versionName '1.0.7-promotions-billing'" in gradle) or ('versionCode 9' in gradle and "versionName '1.0.8-real-detail-no-demo'" in gradle))
print('All round 7 session persistence checks passed.')
