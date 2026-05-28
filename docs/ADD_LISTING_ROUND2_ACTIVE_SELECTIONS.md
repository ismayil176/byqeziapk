# Add Listing Round 2 — Active Native Selections

Scope: BYQEZI native Android app, no WebView/TWA.

What changed:

- Add Listing form is now stateful with `AddListingDraft`.
- Vehicle category cards are clickable and update the selected category.
- Brand/model selection is safer and more flexible:
  - web brand list was expanded into native Android;
  - exact brands show model suggestions;
  - custom brand text still enables model entry.
- Select-like fields now open native bottom-style option dialogs:
  - year, body type, drivetrain, transmission, seats, color, condition, city, sale type.
- Pills are active:
  - fuel, currency, market.
- Check rows are active and fixed visually:
  - credit, barter, no accident, not repainted, damaged, VIN.
- Color swatches are active and update the selected color.
- Photo picker is active with Android `ACTION_OPEN_DOCUMENT`:
  - multiple images supported;
  - max 15 images;
  - preview grid shown;
  - first image is main;
  - tap a non-main image to make it main;
  - delete button removes image.
- Submit now validates required fields and creates a local pending listing in Cabinet.
- Newly created listing opens in detail with selected local images.

Notes:

- This round keeps submission local/pending in the native app so the user can test the complete flow safely.
- Cloudflare live `/api/listings` submit with uploaded image ownership and Turnstile/user session should be connected in the next backend-auth round.
