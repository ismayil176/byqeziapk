from pathlib import Path
root = Path(__file__).resolve().parents[1]
java = root / 'app/src/main/java/az/byqezi/app/MainActivity.java'
workflow = root / '.github/workflows/android-build.yml'
text = java.read_text(encoding='utf-8')
checks = []
checks.append(('No money helper call remains', 'money(listing.price, listing.currency)' not in text))
checks.append(('formatMoney used in cabinet listing card', 'formatMoney(listing.price, listing.currency)' in text))
checks.append(('formatMoney helper exists', 'private String formatMoney(int amount, String currency)' in text))
checks.append(('Login screen present', 'showLogin(' in text or 'showLogin()' in text))
checks.append(('Cabinet screen present', 'showCabinet' in text))
checks.append(('No WebView', 'WebView' not in text and 'android.webkit' not in text))
checks.append(('Workflow present', workflow.exists()))
failed = [name for name, ok in checks if not ok]
for name, ok in checks:
    print(('OK: ' if ok else 'FAIL: ') + name)
if failed:
    raise SystemExit('Failed checks: ' + ', '.join(failed))
