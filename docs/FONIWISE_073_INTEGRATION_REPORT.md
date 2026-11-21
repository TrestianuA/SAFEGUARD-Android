# FoniWise 0.7.3 — raport inițial de integrare PR #18 / PR #19

Data: 2026-11-20

## Ramură izolată

- Ramură de test: `test/foniwise-073-integration`, pornită de la commitul FoniWise `54d92209ed3c9777c9125b7c70a9f119e3e3bca9`.
- Ramuri sursă: `fix/storage-reading-validation-073` (PR #18) și `feat/foniwise-ui-phase1` (PR #19).
- Nu se modifică `main`; nu se modifică semnarea, `applicationId`, `versionCode` sau resursele grafice.

## Reconciliere verificată în cod

Am inspectat `MainActivity.kt` din ambele ramuri și am constatat că ramura FoniWise conține deja comportamentele esențiale verificate pentru stocare în PR #18:

- `hasValidStorageReading(totalBytes, availableBytes)` este prezent.
- Titlul Stocare/Storage, mesajul Date indisponibile/Data unavailable și valoarea utilizată au variante RO/EN.
- Cache Advisor oferă doar instrucțiuni manuale, avertizează despre „Șterge datele” și deschide setările aplicațiilor.
- Butonul de setări pentru stocare este bilingv în FoniWise; nu este necesară suprascrierea lui `MainActivity.kt` cu varianta vizuală veche.
- Ambele ramuri conțin `StorageReadingValidationTest.kt`, conform comparației GitHub.

**Decizie:** nu înlocuim `MainActivity.kt` FoniWise cu varianta PR #18; ar elimina modificări importante de interfață. Acest raport nu reprezintă o dovadă că fiecare modificare a PR #18 a fost integrată; verificarea diferențelor și testele rămân obligatorii.

## Verificări necesare

1. Compilare și teste unitare pe commitul final al acestei ramuri.
2. Auditul APK, logo-urilor și permisiunilor din GitHub Actions.
3. Verificare pe telefon prin Android Studio: semnătură compatibilă, UI FoniWise, RO/EN, RAM, cache, animații și persistența datelor.
4. Păstrarea PR #18 și PR #19 nemodificate până la rezultatul testării.

Nu au fost efectuate teste pe dispozitiv.
