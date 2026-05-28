from pathlib import Path
import re, sys
root = Path(__file__).resolve().parents[1]
java = (root / 'app/src/main/java/az/byqezi/app/MainActivity.java').read_text(encoding='utf-8')
workflow = (root / '.github/workflows/android-build.yml').read_text(encoding='utf-8')
checks = []

def ok(name, cond):
    checks.append((name, bool(cond)))

ok('Cabinet UI round marker doc', (root / 'docs/CABINET_UI_ROUND1_POLISH.md').exists())
ok('Workflow uses cabinet UI validator', 'validate_cabinet_ui_round1.py' in workflow)
ok('Cabinet content safe padding', 'content.setPadding(dp(14), dp(14), dp(14), dp(38));' in java)
ok('Hero card 22dp radius', 'hero.setBackground(round(Color.WHITE, BORDER, dp(22), 1));' in java)
ok('Hero active cabinet badge', 'Aktiv kabinet' in java)
ok('Hero add button fixed height', 'new LinearLayout.LayoutParams(-1, dp(46))' in java)
ok('Support service badge exists', '09:00–19:00' in java)
ok('Support card 22dp radius', 'card.setBackground(round(Color.WHITE, BORDER, dp(22), 1));' in java)
ok('Stats 82dp cards', 'new LinearLayout.LayoutParams(0, dp(82), 1)' in java)
ok('Stat number 23sp', 'text(String.valueOf(count), 23, PURPLE, true)' in java)
ok('Notice polished background', 'Color.rgb(250, 247, 255)' in java and 'dp(18), 1));' in java)
ok('Listing card vertical layout', 'card.setOrientation(LinearLayout.VERTICAL);' in java and 'LinearLayout top = new LinearLayout(this);' in java)
ok('Listing image 124x92', 'new LinearLayout.LayoutParams(dp(124), dp(92))' in java)
ok('Listing actions 42dp', 'new LinearLayout.LayoutParams(0, dp(42), 1)' in java)
ok('Cabinet functions single definition', java.count('private LinearLayout cabinetHeroCard()') == 1 and java.count('private LinearLayout cabinetListingCard(Listing listing)') == 1)
ok('No WebView', 'WebView' not in java and 'android.webkit' not in java)
ok('No TWA', 'TrustedWebActivity' not in java and 'androidbrowserhelper' not in java)

failed = [name for name, status in checks if not status]
for name, status in checks:
    print(('OK: ' if status else 'FAIL: ') + name)
if failed:
    print('\nFailed checks: ' + ', '.join(failed))
    sys.exit(1)
