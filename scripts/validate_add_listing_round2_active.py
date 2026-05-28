from pathlib import Path
import sys

root = Path(__file__).resolve().parents[1]
main = (root / 'app/src/main/java/az/byqezi/app/MainActivity.java').read_text(encoding='utf-8')
workflow = (root / '.github/workflows/android-build.yml').read_text(encoding='utf-8')
failures = []

def ok(name, cond):
    print(('OK: ' if cond else 'FAIL: ') + name)
    if not cond:
        failures.append(name)

ok('No WebView', 'WebView' not in main and 'android.webkit' not in main and 'TrustedWebActivity' not in main)
ok('Add draft state exists', 'private AddListingDraft addDraft = new AddListingDraft();' in main and 'private static class AddListingDraft' in main)
ok('Vehicle cards active', 'choice.setOnClickListener' in main and 'addDraft.vehicleCategory = value' in main)
ok('Select dialogs active', 'showOptionSheet' in main and 'addSelectValue' in main)
ok('Pills active', 'addPillWrap(String[] values, String selected, ChoiceHandler handler)' in main and 'handler.onChoice(value)' in main)
ok('Checkbox rows active and taller', 'addCheckLike(String label, boolean selected, Runnable action)' in main and 'setMinHeight(dp(42))' in main)
ok('Color palette active', 'addDraft.color = COLOR_NAMES[index]' in main)
ok('Image picker active', 'ACTION_OPEN_DOCUMENT' in main and 'REQ_ADD_IMAGES' in main and 'onActivityResult' in main)
ok('Image previews active', 'addPhotoPreviewGrid' in main and 'imageUris' in main and 'ANA' in main)
ok('Submit validates and creates pending listing', 'submitAddListing()' in main and 'validateAddDraft()' in main and 'listingFromDraft()' in main and 'status = "pending"' in main)
ok('Local images work in cards/detail', 'isLocalImageUri' in main and 'setImageURI' in main and 'detailImageCount' in main)
ok('Fuller brand list included', 'Anyang Kinland' in main and 'ZX Auto' in main)
ok('Workflow uses round2 validator', 'validate_add_listing_round2_active.py' in workflow)

if failures:
    print('\nFailed checks: ' + ', '.join(failures))
    sys.exit(1)
print('\nAll add listing round 2 active-selection checks passed.')
