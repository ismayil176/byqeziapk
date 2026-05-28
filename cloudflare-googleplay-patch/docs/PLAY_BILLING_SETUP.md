# BYQEZI Android VIP/Premium/İrəli çək — Play Billing setup

Bu patch native Android tətbiqindəki Google Play Billing alışlarını Cloudflare backend-də yoxlamaq və paketi D1-də aktivləşdirmək üçündür.

## 1) Play Console-da one-time products yarat

Product ID-lər dəqiq belə olmalıdır:

- `bump_once`, `bump_three`, `bump_five`, `bump_ten`
- `vip_d1`, `vip_d5`, `vip_d15`, `vip_d30`
- `premium_d1`, `premium_d5`, `premium_d15`, `premium_d30`

Hamısı **one-time product / in-app product** kimi yaradılır. Qiymətləri webdəki planlarla eyni saxla.

## 2) Cloudflare web project-ə patch əlavə et

Bu qovluqdakı faylı web project-də eyni yerə kopyala:

`functions/api/promotions/googleplay/confirm.js`

Migration faylını D1-də işlə:

`migrations/0013_google_play_promotions.sql`

## 3) Google service account secret

Play Console → API access bölməsindən Google Play Developer API üçün service account yarat və ona app üzrə maliyyə/order yoxlama icazəsi ver. JSON açarını Cloudflare Pages/Workers production secret kimi əlavə et:

`GOOGLE_PLAY_SERVICE_ACCOUNT_JSON`

Dəyər tam JSON olmalıdır.

## 4) Test axını

1. Android AAB-ni Internal testing track-ə yüklə.
2. License tester əlavə et.
3. Product-ları aktiv et.
4. APK/AAB Play vasitəsilə quraşdırıldıqdan sonra kabinetdə aktiv elan üçün Reklam → Google Play ilə al test et.

