# Test this round — BYQEZI native Android round 9

Round 9 focuses on removing the last fake/default detail data and using the real listing detail API.

## Must test on device

1. Open the app and wait for real listings to load.
2. Open an active listing.
3. Confirm the detail page calls real data and shows the real created date/time.
4. Confirm `Baxışların sayı` is not hardcoded to `0`; it should come from the backend.
5. Go back and open the same listing again; the backend should increment views according to `/api/listings/{id}` behavior.
6. Check that no fake seller name, fake phone, fake detail description, or fixed equipment chips appear when they are not in the API response.
7. Open a listing with credit/barter enabled and confirm those chips only show when real flags are present.
8. Open your own active cabinet listing and confirm the promotion/VIP/Premium actions still appear.
9. Test gallery arrows and thumbnails; moving between images should not add extra fake view increments.

## Build

Use GitHub Actions. Artifacts should be:

- `byqezi-debug-apk`
- `byqezi-release-aab`
