# Login/Cabinet Round 1 Compile Fix

Fixed the GitHub Actions Java compile error:

`cannot find symbol: method money(int,String)`

The cabinet listing card now uses the existing `formatMoney(listing.price, listing.currency)` helper. No UI/layout changes were made.
