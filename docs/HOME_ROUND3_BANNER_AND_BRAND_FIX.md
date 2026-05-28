# HOME ROUND 3 — Banner ölçüsü + marka/model düzəlişi

Bu round yalnız Home ekranına aiddir.

## Dəyişikliklər

1. Ortadakı reklam/banner şəkli artıq `wrap_content` ilə böyümür.
   - Əvvəl `ImageView` üçün 203dp hündürlük localImage daxilində verilirdi, amma `content.addView(ad, topLp(14))` həmin ölçünü əvəz edirdi.
   - İndi 203dp hündürlük parent `LayoutParams` ilə verilir: `content.addView(ad, adLp)`.

2. Marka/model dropdown bug düzəldildi.
   - Marka sahəsinə `bye` kimi səhv/yarımçıq dəyər yazanda model siyahısı açılmamalıdır.
   - Model yalnız marka tam olaraq tanınanda aktiv olur: məsələn `BYD`, `BMW`, `Kia`.
   - `modelsFor()` artıq bütün modelləri fallback kimi qaytarmır.

## Bu roundda test ediləcək

- Ana səhifə ilk açılış: banner artıq çox böyük olmamalıdır.
- Marka sahəsinə `bye` yaz: model siyahısı açılmamalıdır.
- Marka sahəsində `BYD` seç: model siyahısında yalnız BYD modelləri görünməlidir.
- Search nəticələrində kartlar və footer əvvəlki round kimi qalmalıdır.
