# SAFEGUARD 0.7.3 — verificare PR #9 și PR #10

Data verificării: 2026-11-20. Document de verificare; nu modifică aplicația sau semnarea APK.

## Dovezi verificate în GitHub

- PR #9 (`0568889fcd47a1b3507d156764e66ec489a26f75`): două texte RO/EN din `MainActivity.kt` diferențiază măsurarea RAM indisponibilă de lipsa presiunii critice raportate de Android.
- PR #10 (`d94a03305a9ba7953f0aba34d4d8ea7a8b4e9923`): două texte RO/EN explică faptul că reducerea animațiilor nu accelerează CPU și nu eliberează RAM.
- GitHub Actions Android Debug APK #72 pentru PR #10: `completed/success` (run `44623822355`).
- PR-urile #9 și #10 erau deschise la verificare; nu presupune integrarea în `main`.

## Matrice de verificare înainte de integrare

| Test | Pași | Rezultat așteptat | Stare |
| --- | --- | --- | --- |
| RAM RO | Deschide ⓘ RAM în română, apasă Actualizează RAM | Valori raportate de Android; mesajul de indisponibilitate nu afirmă că RAM este suficient | Netestat pe dispozitiv |
| RAM EN | Schimbă limba dialogului în engleză și repetă | Explicații corecte în engleză; fără afirmații despre eliberare forțată | Netestat pe dispozitiv |
| RAM indisponibil | Simulează controlat o citire invalidă într-un test, fără a modifica date reale | Instrucțiune de reîmprospătare; fără diagnostic nefondat | Netestat |
| Animații RO/EN | Deschide ⓘ animații în ambele limbi | Ghid manual și precizarea că nu crește CPU/RAM; nicio schimbare automată de setări | Netestat pe dispozitiv |
| Cache Advisor RO/EN | Deschide ⓘ și navighează la setările Android | Curățare numai manuală, explicație că fișierele cache pot reapărea | Netestat pe dispozitiv |
| Compilare după integrare | Rulează `assembleDebug` și `testDebugUnitTest` | Ambele reușite | Neexecutat pentru combinația PR #9 + #10 |
| APK și identitate | Verifică raportul de audit, permisiunile și certificatul APK | Fără permisiuni noi; semnarea existentă nu este înlocuită | Neverificat pentru combinație |

## Reguli de integrare

1. Revizuiește diferențele PR #9 și #10 față de `main`; ambele ating `MainActivity.kt`, dar în zone diferite.
2. Integrează controlat, apoi verifică din nou CI pe commitul rezultat; succesul CI al PR-urilor separat nu dovedește succesul combinației.
3. Nu modifica logo-urile aprobate, configurația de semnare sau permisiunile; nu prezenta aplicația drept antivirus, curățător automat sau eliberator forțat de RAM.
4. Testarea pe telefon și identitatea certificatului de distribuție rămân neconfirmate până la verificare explicită.
