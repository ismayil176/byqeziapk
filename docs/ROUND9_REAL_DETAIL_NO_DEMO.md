# Round 9 — Real detail data and no demo placeholders

This round removes the remaining fake/default detail information from the native Android app and makes listing details refresh from the real Cloudflare API.

## Changes

- Detail page now calls `GET /api/listings/{id}` when opened.
- The real detail response is used to refresh the selected listing in memory.
- Opening an active listing through the native app now triggers the server-side views increment used by the web project.
- Listing cards and detail page use `created_at`/`updated_at` from the API for real listing time.
- Detail page shows real `views`, `view_count`, or `views_count` instead of hardcoded `0`.
- Removed fake seller fallback data such as test names and fake phone numbers.
- Removed fake detail defaults for description, color, body type, transmission, market, seats, and accident/paint state.
- Credit/barter chips only appear when the listing actually has those flags.
- Equipment chips are rendered from the listing's real `equipment` / `equipment_json` values.
- Public listings are no longer marked as the current user's listing based on `owner_id == 1`; only real `mine` / `is_mine` or the cabinet endpoint marks ownership.

## Notes

If the views counter still stays at zero in production, verify that the D1 `views` column exists by running the existing web migration `0006_listing_views.sql` on the production D1 database.
