from pathlib import Path
import sys
root = Path(__file__).resolve().parents[1]
java = root / 'app/src/main/java/az/byqezi/app/MainActivity.java'
wf = root / '.github/workflows/android-build.yml'
text = java.read_text(encoding='utf-8')
w = wf.read_text(encoding='utf-8')
checks = [
    ('No giant price placeholder', 'new View(this)' not in text[text.find('private void showPriceHistory'):text.find('private void addPriceRow')]),
    ('Price history list container', 'LinearLayout list = new LinearLayout(this);' in text and ('addPriceRow(list, "İndi"' in text or 'addPriceRow(list, "Cari qiymət"' in text)),
    ('Price history close button', 'TextView close = button("Bağla"' in text and 'close.setOnClickListener(v -> dismissActiveDialog())' in text),
    ('Report selected reason state', 'final String[] selectedReason = {""};' in text),
    ('Report option views array', 'final TextView[] reasonViews = new TextView[reasons.length];' in text),
    ('Report active visual state', 'active ? Color.rgb(245, 236, 255)' in text and 'active ? PURPLE : BORDER' in text),
    ('Report send requires selection', 'Əvvəl şikayət səbəbini seçin' in text),
    ('Report send includes selected reason', 'Şikayət göndərildi: ' in text),
    ('Workflow uses detail round3 validator', 'validate_detail_round3.py' in w),
    ('No WebView', 'WebView' not in text and 'android.webkit' not in text),
]
failed=[]
for name, ok in checks:
    print(('OK' if ok else 'FAIL') + ': ' + name)
    if not ok:
        failed.append(name)
if failed:
    print('Failed checks: ' + ', '.join(failed))
    sys.exit(1)
