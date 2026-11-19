# SAFEGUARD 0.7.3-rc1 — verificare RAM (20 noiembrie 2026)

## Schimbare

În `MainActivity.kt`, recomandarea RAM adaptată este afișată numai dacă `ActivityManager.MemoryInfo` există și `totalMem > 0`. Anterior, o citire cu `totalMem <= 0` putea afișa o recomandare bazată pe indicatorul `lowMemory`, deși măsurătoarea era invalidă. Eticheta „Date indisponibile / Data unavailable” rămâne vizibilă pentru această situație.

## Verificări efectuate

- Inspectat codul RAM, Cache Advisor și ghidarea animațiilor din `MainActivity.kt` pe ramura `main`.
- Confirmată static condiția de validare `memorySnapshot != null && memorySnapshot.totalMem > 0L` după modificare.
- Confirmat `versionName = "0.7.3-rc1"`, `versionCode = 10` în `app/build.gradle.kts`.
- Nicio schimbare de permisiuni, logo-uri, semnare APK sau comportament de ștergere a datelor.

## Teste încă necesare (NU sunt declarate trecute)

1. `bash ./gradlew assembleDebug` și `bash ./gradlew testDebugUnitTest` în CI cu verificarea logurilor.
2. Pe telefon: RAM disponibil și total valid; reîmprospătarea citirii; afișarea mesajului indisponibil în condiții de eșec (dacă poate fi simulată).
3. Dialogurile ⓘ RO/EN, Cache Advisor și ghidarea animațiilor; verificarea navigării către Setările Android.
4. Auditul semnăturii APK și comparația certificatului cu versiunea instalată înainte de orice actualizare; nu dezinstalați versiunea funcțională.

Nu s-a rulat un emulator, nu s-a instalat APK și nu se afirmă că build-ul sau testele au trecut.
