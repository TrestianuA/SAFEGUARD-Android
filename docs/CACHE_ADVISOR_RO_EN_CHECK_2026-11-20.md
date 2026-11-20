# SAFEGUARD 0.7.3 — verificare Cache Advisor RO/EN (2026-11-20)

## Modificare
În dialogul Cache Advisor s-a clarificat că datele cache pot fi reconstruite după ștergere și că ștergerea periodică nu este necesară. Textul este prezent atât în română, cât și în engleză. Nu s-au adăugat permisiuni, servicii sau operații de ștergere automată.

## Verificări efectuate
- Inspectat `app/src/main/java/com/safeguard/android/MainActivity.kt` pe ramura `fix/storage-reading-validation-073`: dialogul Cache Advisor descrie curățarea manuală și distinge explicit „Șterge cache” de „Șterge datele”.
- Inspectat `app/build.gradle.kts`: `applicationId = "com.safeguard.android"`, `versionName = "0.7.3-rc1"`, `versionCode = 10`.
- Workflow-ul anterior pentru commit `d54b2be`, run #104, a avut concluzia `success`: https://github.com/TrestianuA/SAFEGUARD-Android/actions/runs/44698910704
- La verificare, noul commit `18a4122` nu avea încă un workflow PR returnat de GitHub. **Nu se declară compilare sau teste trecute pentru noul commit.**

## Testare recomandată
1. CI: `./gradlew testDebugUnitTest assembleDebug`.
2. Pe telefon: deschide Cache Advisor, verifică dialogul în RO și EN, lizibilitatea pe ecran mic și avertismentul despre „Șterge datele”.
3. Confirmă că deschiderea setărilor Android nu șterge nimic automat și că dialogul nu promite accelerarea telefonului.
4. Pentru actualizare peste aplicația instalată, folosește local semnătura compatibilă cu APK-ul existent; nu încărca keystore sau parole în GitHub.

Nu s-au schimbat designul, logo-urile, `applicationId`, semnarea sau permisiunile.
