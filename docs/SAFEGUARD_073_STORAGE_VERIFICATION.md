# SAFEGUARD 0.7.3 — verificare stocare și Cache Advisor

Data: 2026-11-20

## Schimbare

În cardul de stocare, titlul, valoarea utilizată și mesajul pentru citiri invalide respectă acum limba RO/EN selectată. Logica de citire și validare a stocării nu s-a schimbat. Cache Advisor continuă să ofere doar pași manuali pentru Android Settings; nu șterge cache automat.

## Verificări efectuate

- Inspecție statică a `MainActivity.kt` în ramura `fix/storage-reading-validation-073`: `hasValidStorageReading` respinge total <= 0, disponibil negativ și disponibil > total.
- Inspecție statică: RAM Monitor folosește `ActivityManager.MemoryInfo`, iar ghidul de animații deschide setările Android, fără modificare automată.
- Execuția GitHub Actions #93 pentru commitul `40499df9078952d0894d5466c634a06b5b2e02a7` a fost raportată `success`. Aceasta **nu** validează automat commitul ulterior cu traducerile.
- Testele unitare din `StorageReadingValidationTest.kt` acoperă valori valide, invalide și limite `Long`.

## Verificări încă necesare

1. `./gradlew testDebugUnitTest assembleDebug` pentru ultimul commit.
2. Pe telefon: comutare RO/EN în cardul Stocare; confirmarea afișării corecte pentru citiri reale.
3. Pe telefon: pașii manuali Cache Advisor și ghidul animațiilor, care pot diferi după producător.

Nu s-au schimbat `applicationId`, semnarea, permisiunile, logo-urile sau datele utilizatorului.
