from pathlib import Path
import sys

root = Path(__file__).resolve().parents[1]
main = (root / 'app/src/main/java/az/byqezi/app/MainActivity.java').read_text(encoding='utf-8')
workflow = (root / '.github/workflows/android-build.yml').read_text(encoding='utf-8')
checks = [
    ('No WebView', 'WebView' not in main and 'android.webkit' not in main),
    ('No TWA/browserhelper', 'TrustedWebActivity' not in main and 'androidbrowserhelper' not in main),
    ('Car cards open detail', 'card.setOnClickListener(v -> showDetail(listing));' in main),
    ('Detail screen method exists', 'private void showDetail(Listing listing)' in main),
    ('Detail gallery card exists', 'detailGalleryCard' in main and '(detailImageIndex + 1) + "/" + detailImageCount(listing)' in main),
    ('Detail info card exists', 'detailInfoCard' in main and 'Qiymət tarixçəsi' in main),
    ('Detail specs rows exist', 'specRow("Marka"' in main and 'specRow("Mühərrik"' in main and 'specRow("Sürətlər qutusu"' in main),
    ('Seller contact card exists', 'sellerCard' in main and 'Zəng edin' in main and 'WhatsApp' in main),
    ('Complaint card exists', 'complaintCard' in main and 'Şikayət et' in main),
    ('Related listings exists', 'Oxşar elanlar' in main and 'relatedRows' in main),
    ('Detail sticky contact hides bottom nav', 'bottomNav.setVisibility(detail ? View.GONE : View.VISIBLE)' in main and 'detailContactBar' in main),
    ('Back press returns home', 'public void onBackPressed()' in main and '"detail".equals(activeScreen)' in main),
    ('Listing detail fields exist', 'String sellerName;' in main and 'String bodyType;' in main and 'String horsepower;' in main),
    ('Workflow uses detail validator', 'validate_detail_round1.py' in workflow or 'validate_detail_round3.py' in workflow),
]
failed=[]
for name, ok in checks:
    print(('OK: ' if ok else 'FAIL: ') + name)
    if not ok:
        failed.append(name)
if failed:
    print('Failed checks: ' + ', '.join(failed))
    sys.exit(1)
print('All Detail Round 1 checks passed.')
