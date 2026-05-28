# Home Round 5 — Crash + Premium/VIP separation

Bu round yalnız üç problemi düzəldir:

1. Marka seçəndə app-in crash edib bağlanması.
2. Premium bölməsində bütün aktiv Premium elanların görünməməsi.
3. VIP bölməsində VIP olmayan, sadəcə Premium olan elanların görünməsi.

## Dəyişikliklər

- Marka dropdown `onItemClick` guard edildi və model dropdown-u artıq avtomatik zorla açılmır.
- Model sahəsi yalnız exact marka seçiləndə aktiv olur.
- `/api/listings?collection=premium&limit=100` ayrıca çəkilir.
- `/api/listings?collection=vip&limit=100` ayrıca çəkilir.
- `PREMİUM ELANLAR` yalnız aktiv premium elanları göstərir.
- `VIP ELANLAR` və bottom `VIP` tabı yalnız aktiv VIP elanları göstərir.
- `featured`, `premium`, `is_premium`, `vip`, `is_vip`, `premium_until`, `featured_until`, `vip_until` field-ləri dəstəklənir.

## Qeyd

Bu round-da UI spacing/card ölçüləri dəyişdirilməyib. Yalnız bug fix-lər edilib.
