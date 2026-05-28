# Round 8 test — VIP / Premium / İrəli çək

## Native app test

1. Kabinetə OTP ilə daxil ol.
2. Saytda olan aktiv elanda `Reklam` düyməsinə bas.
3. Xidmətlər görünməlidir: `Elanı irəli çəkin`, `VIP edin`, `Premium edin`.
4. Hər xidmətdə planlar açılmalıdır.
5. Internal testing-dən quraşdırılmış app-də `Google Play ilə al` düyməsini test et.
6. Debug APK-də Play product-lar görünməsə `Epoint` fallback düyməsi ilə serverdə mövcud `/api/promotions/create` flow-u yoxla.
7. Ödənişdən sonra app-ə qayıdanda kabinet elanları refresh olunmalıdır.

## Play Console product ID-lər

- bump_once / bump_three / bump_five / bump_ten
- vip_d1 / vip_d5 / vip_d15 / vip_d30
- premium_d1 / premium_d5 / premium_d15 / premium_d30

## Backend

Play Billing-in tam real aktivləşməsi üçün `cloudflare-googleplay-patch` qovluğundakı endpoint web backend-ə tətbiq olunmalıdır.
