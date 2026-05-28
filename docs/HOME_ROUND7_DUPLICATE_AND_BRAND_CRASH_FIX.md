# Home Round 7 — Duplicate Listing and Brand Crash Fix

Düzəlişlər:

1. `Avtomobil almaq` altında çıxan 2 əlavə kart bloku silindi.
   - Regular elanlar yalnız `BÜTÜN ELANLAR` bölməsində göstərilir.
   - Premium/VIP elanlar yalnız öz bölmələrində qalır.

2. Marka seçimi crash verməsin deyə AutoComplete callback-ləri qorundu.
   - Brand selection adapter/popup callback try/catch ilə qorundu.
   - `afterTextChanged` içində dropdown-u zorla bağlama davranışı silindi.
   - Seçim zamanı `clearFocus()`/keyboard müdaxiləsi çıxarıldı.

Bu round-da UI dizaynına başqa dəyişiklik edilməyib.
