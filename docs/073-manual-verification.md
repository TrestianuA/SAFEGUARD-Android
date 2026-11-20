# SAFEGUARD 0.7.3-rc1 — verificare reproductibilă

Acest document descrie **teste de executat**, nu rezultate deja obținute. Data pregătirii: 2026-11-20.

## Înainte de test

- Păstrează instalarea funcțională 0.7.0. Nu o dezinstala pentru a instala un APK nou.
- Verifică SHA-256 al certificatului APK-ului nou și al celui original cu `apksigner verify --print-certs`; dacă diferă, nu încerca actualizarea peste aplicația existentă.
- Verifică separat în GitHub Actions `assembleDebug`, `testDebugUnitTest` și auditul semnăturii/permisunilor. Un build verde nu confirmă testarea pe telefon.
- Nu dezactiva Google Play Protect pentru a forța instalarea.

## Scenarii de acceptare pe telefon

| Componentă | Acțiune | Rezultat așteptat |
| --- | --- | --- |
| Cache Advisor | Acasă → Curățare manuală | Explicație RO/EN; avertisment distinct pentru „Șterge datele”; deschiderea setărilor aplicațiilor; nicio ștergere automată |
| RAM Monitor | Notează RAM total/disponibil, apoi reîmprospătează | Valori furnizate de Android prin `ActivityManager.MemoryInfo`; recomandarea respectă indicatorul `lowMemory`; nicio promisiune de eliberare forțată |
| Animații | Deschide explicația ⓘ și ghidarea | Pași manuali RO/EN; fără modificarea automată a setărilor de dezvoltator; avertizare că denumirile diferă între producători |
| Dialoguri | Schimbă RO/EN și închide/redeschide | Text lizibil, fără trunchiere pe ecran mic; butoane accesibile |
| Navigare | Deschide Acasă, Protecție, Rapoarte | Fără blocări, fără pierderea datelor locale existente |
| Confidențialitate | Refuză scanarea și accesul la notificări | Fără colectare de inventar înainte de acord; fără activarea automată a accesului special |

## Raportarea rezultatului

Pentru fiecare scenariu notează: modelul telefonului, versiunea Android, versiunea APK, **PASS / FAIL / NETESTAT**, pașii de reproducere și eventualul mesaj de eroare. Nu încărca în issue-uri loguri cu notificări, nume de aplicații private, parole, chei de semnare sau date personale.

## Limite

Acest checklist nu dovedește că testele au rulat. Validarea vizuală a logo-urilor aprobate, compatibilitatea semnăturii cu 0.7.0 și comportamentul pe dispozitiv necesită verificare separată.
