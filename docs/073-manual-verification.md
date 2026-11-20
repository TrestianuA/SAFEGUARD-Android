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

## Audit static al implementării (2026-11-20)

Inspecție a `app/src/main/java/com/safeguard/android/MainActivity.kt`, fără executarea aplicației:

- RAM: `readDeviceMemory()` interoghează `ActivityManager.getMemoryInfo()`; ecranul arată `totalMem`, `availMem` și `lowMemory`. Butonul „Actualizează RAM” face o nouă citire, nu o eliberare de memorie.
- Pentru valori lipsă sau `totalMem <= 0`, cardul afișează „Date indisponibile” și nu afișează sfaturi adaptate. Dialogul ⓘ oferă explicit explicația indisponibilității. De testat separat cazul `availMem > totalMem`, dacă un dispozitiv îl raportează.
- Cache Advisor: butonul deschide setările Android; nu există în această interfață apel pentru ștergerea cache-ului altor aplicații. Textul distinge „Șterge cache” de „Șterge datele”.
- Animații: butonul deschide `Settings.ACTION_ACCESSIBILITY_SETTINGS`; interfața nu modifică automat setările animațiilor.
- Dialogurile RAM și animații au selecție RO/EN și conținut derulabil; verificarea lizibilității pe telefon rămâne necesară.

### Cazuri suplimentare de verificat

1. RAM: fără citire validă, confirmă absența recomandării bazate pe `lowMemory` și afișarea mesajului de indisponibilitate în dialog.
2. RAM: după „Actualizează RAM”, confirmă că cifrele reflectă noua citire, nu un procent estimat sau o promisiune de optimizare.
3. Cache Advisor: pe două versiuni diferite de Android, confirmă că ghidul nu îndeamnă la „Șterge datele”.
4. Animații: dacă pagina Accesibilitate nu există pe dispozitiv, verifică revenirea sigură la Setări generale; nu modifica automat Opțiunile pentru dezvoltatori.
5. Localizare: comută RO/EN în dialog, închide-l și redeschide-l; verifică titlurile, explicațiile și etichetele butoanelor.

**Stare:** audit static efectuat; compilare, teste automate, verificare vizuală și testare pe telefon **NETESTATE** în această etapă.
