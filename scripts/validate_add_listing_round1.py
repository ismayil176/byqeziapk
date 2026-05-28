from pathlib import Path
import sys

root = Path(__file__).resolve().parents[1]
main = (root / 'app/src/main/java/az/byqezi/app/MainActivity.java').read_text(encoding='utf-8')
workflow = (root / '.github/workflows/android-build.yml').read_text(encoding='utf-8')

def ok(name, cond):
    print(('OK: ' if cond else 'FAIL: ') + name)
    if not cond:
        failures.append(name)

failures = []
ok('Add screen real form replaces placeholder', 'Bu mərhələdə yalnız login və kabinet axını' not in main and 'addVehicleTypeSection()' in main)
ok('Add requires login', 'if (!userLoggedIn)' in main and 'showLogin("add")' in main)
ok('Add active screen set', 'activeScreen = "add";' in main)
ok('Add progress strip exists', 'addProgressStrip()' in main and '0 / 5 bölmə tamamlandı' in main)
ok('Vehicle type section exists', 'addVehicleTypeSection()' in main and 'Nə satırsınız?' in main)
ok('Technical section exists', 'addTechnicalSection()' in main and 'Avtomobil haqqında' in main)
ok('Brand/model autocomplete exists', 'wireAddBrandModel' in main and 'updateAddModelForBrand' in main and 'modelsForBrand(exact)' in main)
ok('Model disabled for non-exact brand', 'model.setEnabled(false);' in main and 'Əvvəl marka seçin' in main)
ok('Photos section exists', 'addPhotoSection()' in main and 'Şəkil seçin' in main)
ok('Price condition section exists', 'addPriceConditionSection()' in main and 'Qiymət və vəziyyət' in main)
ok('Contact section exists', 'addContactSection()' in main and 'Əlaqə məlumatları' in main)
ok('Color palette exists', 'addColorPalette()' in main and 'Rəng seçin' in main)
ok('Submit safely placeholder only', 'Submit növbəti mərhələdə qoşulacaq' in main)
ok('No WebView', 'WebView' not in main and 'android.webkit' not in main)
ok('Workflow uses add listing validator', 'validate_add_listing_round1.py' in workflow)

if failures:
    print('\nFailed checks: ' + ', '.join(failures))
    sys.exit(1)
print('\nAll add listing round 1 checks passed.')
