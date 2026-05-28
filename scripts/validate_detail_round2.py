from pathlib import Path
import sys
root = Path(__file__).resolve().parents[1]
main = (root / 'app/src/main/java/az/byqezi/app/MainActivity.java').read_text(encoding='utf-8')
workflow = (root / '.github/workflows/android-build.yml').read_text(encoding='utf-8')
checks = [
    ('No WebView', 'WebView' not in main and 'android.webkit' not in main),
    ('No TWA/browserhelper', 'TrustedWebActivity' not in main and 'androidbrowserhelper' not in main),
    ('Detail screen still exists', 'private void showDetail(Listing listing)' in main and 'detailGalleryCard(listing)' in main),
    ('Favorite works on cards and detail', 'favoriteState' in main and 'toggleFavorite' in main and 'isFavorite(listing)' in main),
    ('Lightbox/gallery works', 'showLightbox' in main and 'presentDialog(overlay)' in main and 'updateLightboxImage' in main),
    ('Detail image next/prev works', 'moveDetailImage' in main and 'galleryNavButton("‹")' in main and 'galleryNavButton("›")' in main),
    ('Thumbnails switch main image', 'thumb.setOnClickListener(v -> { detailImageIndex = index; showDetail(listing); });' in main),
    ('Price history modal works', 'showPriceHistory' in main and 'addPriceRow' in main and 'Qiymət tarixçəsi' in main),
    ('Report modal works', 'showReportDialog' in main and 'Şikayət göndərin' in main and 'Şikayət göndərildi' in main),
    ('Back closes modal first', 'activeDialog != null && activeDialog.isShowing()' in main and 'activeDialog.dismiss()' in main),
    ('Workflow uses round2 validator', 'validate_detail_round2.py' in workflow),
]
failed=[]
for name, ok in checks:
    print(('OK: ' if ok else 'FAIL: ') + name)
    if not ok: failed.append(name)
if failed:
    print('Failed checks: ' + ', '.join(failed))
    sys.exit(1)
print('All Detail Round 2 checks passed.')
