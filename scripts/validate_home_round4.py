from pathlib import Path
src = Path('app/src/main/java/az/byqezi/app/MainActivity.java').read_text()
checks = {
    'No WebView': 'WebView' not in src and 'android.webkit' not in src,
    'No TWA': 'TrustedWebActivity' not in src and 'androidbrowserhelper' not in src,
    'Filtered brands helper': 'filteredBrands(String query)' in src,
    'Maybe show brand dropdown': 'maybeShowBrandDropdown()' in src,
    'Model enabled helper': 'setModelEnabled(boolean enabled, String brand)' in src,
    'Brand watcher suppression': 'suppressBrandWatcher' in src,
    'No stale full dropdown on exact': 'currentBrandInput.dismissDropDown();' in src,
    'Active premium badge': 'isPremiumActive()' in src,
    'Active vip badge': 'isVipActive()' in src,
    'Promotion until fields': 'premiumUntil' in src and 'vipUntil' in src,
    'targetSdk 35': 'targetSdk 35' in Path('app/build.gradle').read_text(),
    'Java regex double-escaped': 'replaceAll("([+-]\\\\d{2}):(\\\\d{2})$", "$1$2")' in src,
}

failed = [k for k,v in checks.items() if not v]
for k,v in checks.items():
    print(('OK: ' if v else 'FAIL: ') + k)
if failed:
    raise SystemExit(1)
