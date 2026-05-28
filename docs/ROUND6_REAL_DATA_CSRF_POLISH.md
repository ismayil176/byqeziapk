# Round 6 — Real data / CSRF / UI polish

- Android native unsafe API requests now send the `byqezi_csrf` cookie value as `x-csrf-token` and `x-byqezi-csrf`.
- Upload and listing submit keep using real `/api/upload` and `/api/listings`.
- Login helper/demo text removed.
- Price history popup no longer says demo data.
- Home card favorite heart reduced and polished.
- Cabinet refresh is forced after first native login if no listings are returned immediately.
- Brand/model catalog synced from the current web project.
