from pathlib import Path
root = Path(__file__).resolve().parents[1]
main = (root / 'app/src/main/java/az/byqezi/app/MainActivity.java').read_text()
gradle = (root / 'app/build.gradle').read_text()
manifest = (root / 'app/src/main/AndroidManifest.xml').read_text()
patch = root / 'cloudflare-googleplay-patch/functions/api/promotions/googleplay/confirm.js'

def ok(name, cond):
    if not cond:
        raise SystemExit(f'FAIL: {name}')
    print(f'OK: {name}')

ok('Billing dependency exists', "com.android.billingclient:billing:8.3.0" in gradle)
ok('Billing permission exists', 'com.android.vending.BILLING' in manifest)
ok('Version round 8+', ('versionCode 8' in gradle and "versionName '1.0.7-promotions-billing'" in gradle) or ('versionCode 9' in gradle and "versionName '1.0.8-real-detail-no-demo'" in gradle))
ok('Promotion services seeded', 'seedPromotionServices()' in main and 'bump_once' not in main and 'serviceKey + "_" + key' in main)
ok('Cabinet reklam opens promotion UI', 'promo.setOnClickListener(v -> showPromotionServices(listing))' in main)
ok('Google Play Billing flow exists', 'launchBillingFlow' in main and 'confirmGooglePlayPromotion' in main and '/api/promotions/googleplay/confirm' in main)
ok('Epoint fallback exists', 'startEpointPromotion' in main and '/api/promotions/create' in main and 'openEpointPostBridge' in main)
ok('Promotion plans fetched from backend', '/api/promotions/plans' in main and 'fetchPromotionPlans();' in main)
ok('Cloudflare Google Play confirm patch included', patch.exists() and 'androidpublisher.googleapis.com' in patch.read_text())
print('All round 8 promotions billing checks passed.')
