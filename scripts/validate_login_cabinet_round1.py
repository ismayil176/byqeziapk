from pathlib import Path
import re
root = Path(__file__).resolve().parents[1]
main = root / 'app/src/main/java/az/byqezi/app/MainActivity.java'
workflow = root / '.github/workflows/android-build.yml'
gradle = root / 'app/build.gradle'
text = main.read_text(encoding='utf-8')
wf = workflow.read_text(encoding='utf-8')
gradle_text = gradle.read_text(encoding='utf-8')
checks = []

def check(name, ok):
    checks.append((name, bool(ok)))

check('No WebView', 'WebView' not in text and 'android.webkit' not in text)
check('No TWA/browserhelper', 'TrustedWebActivity' not in text and 'androidbrowserhelper' not in text)
check('Login state exists', 'private boolean userLoggedIn' in text)
check('Pending after login exists', 'pendingAfterLogin' in text)
check('Add requires login', 'requireLoginFor("add")' in text)
check('Cabinet requires login', 'requireLoginFor("cabinet")' in text)
check('Login screen exists', 'private void showLogin(String targetScreen)' in text)
check('Login completion exists', 'private void completeNativeLogin(String phone)' in text or 'private void verifyOtpCode(String code)' in text)
check('Cabinet screen exists', 'private void showCabinet()' in text)
check('Cabinet hero exists', 'cabinetHeroCard()' in text)
check('Cabinet support exists', 'cabinetSupportCard()' in text)
check('Cabinet stats exists', 'cabinetStatsGrid()' in text)
check('Cabinet listing cards exist', 'cabinetListingCard(Listing listing)' in text)
check('No stale money helper call', 'money(listing.price, listing.currency)' not in text)
check('Cabinet listing uses formatMoney', 'formatMoney(listing.price, listing.currency)' in text)
check('formatMoney helper exists', 'private String formatMoney(int amount, String currency)' in text)
check('Add placeholder exists', 'private void showAddPlaceholder()' in text)
check('Phone validation exists', 'private String normalizeAzPhone(String raw)' in text)
check('User listings list exists', 'private final ArrayList<Listing> userListings' in text)
check('Listing mine flag exists', 'boolean mine;' in text)
check('Sync user listings exists', 'private void syncUserListingsFromLocal()' in text)
check('Cabinet does not add all listings', 'userListings.addAll(listings)' not in text)
check('Cabinet fallback limited', ('userListings.add(listings.get(0))' in text and 'if (listings.size() > 2) userListings.add(listings.get(2))' in text) or 'listings.addAll(Listing.demoRows())' not in text)
check('Logout exists', 'Çıxış edildi' in text)
check('Back handles login/cabinet/add', '"login".equals(activeScreen)' in text and '"cabinet".equals(activeScreen)' in text and '"add".equals(activeScreen)' in text)
check('Workflow uses login/cabinet validator', 'validate_login_cabinet_round1.py' in wf)
check('target SDK 35', re.search(r'targetSdk\s+35', gradle_text) is not None)
check('compile SDK 35', re.search(r'compileSdk\s+35', gradle_text) is not None)

for name, ok in checks:
    print(('OK: ' if ok else 'FAIL: ') + name)
failed = [name for name, ok in checks if not ok]
if failed:
    print('\nFailed checks: ' + ', '.join(failed))
    raise SystemExit(1)
print('\nAll login/cabinet round 1 checks passed.')
