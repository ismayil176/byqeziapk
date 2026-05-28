# Home Round 4

Focus: Home screen only.

Fixes:
- Brand autocomplete no longer keeps stale full-list popup after exact brand text.
- Invalid text such as `bye` does not open unrelated brand/model lists.
- Exact brand text such as `BYD` enables model input and keeps brand dropdown closed.
- Model input receives only the selected brand's models.
- Badge rendering uses active VIP/Premium state with `vip_until` / `premium_until` awareness.
