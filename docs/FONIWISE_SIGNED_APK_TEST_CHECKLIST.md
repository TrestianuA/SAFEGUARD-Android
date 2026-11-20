# FoniWise — verificare locală a APK-ului semnat

**Scop:** testarea noii interfețe peste instalarea existentă SAFEGUARD, fără schimbarea identității aplicației sau expunerea cheii de semnare.

## Înainte de instalare

1. În Android Studio, selectați ramura `feat/foniwise-ui-phase1` și sincronizați proiectul.
2. Confirmați că `applicationId` este în continuare `com.safeguard.android`.
3. Identificați **cheia cu care este semnată versiunea instalată**. Un APK debug GitHub Actions poate avea o cheie diferită; nu presupuneți că este compatibil.
4. Generați local APK-ul cu **aceeași cheie de semnare compatibilă**. Nu încărcați fișiere `.jks`, parole, `keystore.properties` sau secrete în GitHub.
5. Verificați că `versionCode` al APK-ului nou este adecvat pentru actualizare, nu inferior celui instalat.
6. Comparați certificatele de semnare ale APK-ului existent și ale celui nou cu `apksigner verify --print-certs <cale-apk>` (Android SDK Build Tools). Nu distribuiți cheia privată.
7. Faceți o copie de siguranță a datelor importante înainte de test. **Nu dezinstalați aplicația existentă** pentru a rezolva o eroare de semnătură.

## Verificare vizuală și funcțională

- Pictograma din launcher folosește originalul FoniWise; verificați decupajul pe launcherul real, inclusiv masca rotunjită.
- Antetul arată simbolul original, nu sigla SAFEGUARD.
- Numele afișat este FoniWise, iar sloganul este „Know Your Phone”.
- Cardurile verde pastel, antetul și navigația păstrează contrastul și lizibilitatea.
- Dialogurile RO/EN pot fi deschise, comutate și închise.
- Verificați ecranele de protecție, RAM, cache, baterie și rapoarte, precum și permisiunile existente.
- Confirmați că datele și setările existente au rămas disponibile după actualizare.


## Condiții pentru un APK comun PR #18 + PR #19

- **Stare verificată la 2026-11-20:** ambele ramuri pornesc din `main`, dar sunt divergente; ambele modifică `MainActivity.kt`. Nu folosiți simpla reușită a buildului FoniWise ca dovadă că toate corecțiile din PR #18 sunt incluse.
- Creați o **ramură temporară de integrare** pornind de la capătul verificat al PR #19; nu modificați direct `main` sau ramurile de origine.
- Comparați explicit versiunile `MainActivity.kt` și `StorageReadingValidationTest.kt`; păstrați interfața FoniWise aprobată și integrați doar comportamentele lipsă din PR #18, fără suprascrierea integrală a ecranului.
- Verificați în cod: validarea stocării, traducerile RO/EN, citirea `ActivityManager.MemoryInfo`, recomandările RAM, dialogurile ⓘ, curățarea **manuală** a cache-ului și instrucțiunile **manuale** pentru animații.
- Rulați testele și compilarea pe **commitul final de integrare**; păstrați SHA-ul, URL-ul GitHub Actions, concluzia fiecărei etape și artefactul APK în raport. Un CI anterior nu validează noul commit.
- Nu modificați `applicationId`, `versionCode`, manifestul, permisiunile, cheile de semnare sau resursele grafice aprobate fără motiv verificat și revizuire separată.
- Testarea prin Android Studio pe telefon se face numai după verificarea compatibilității certificatului și a datelor existente.

## Test local prin Android Studio (fără pierderea datelor)

1. Deschideți proiectul pe ramura de test aleasă și verificați commitul exact înainte de sincronizare. PR #18 și PR #19 sunt ramuri distincte; nu presupuneți că APK-ul uneia include toate schimbările celeilalte.
2. În terminalul Android Studio, rulați `./gradlew :app:testDebugUnitTest :app:assembleDebug` (pe Windows, `gradlew.bat :app:testDebugUnitTest :app:assembleDebug`). Notați commitul, rezultatul fiecărei sarcini și orice eroare în raportul de test.
3. Înainte de `Run ▶`, verificați aplicația instalată și compatibilitatea certificatului. `Run` produce de regulă un APK debug: **nu îl instalați peste aplicația existentă** dacă semnăturile diferă. Nu ștergeți aplicația și nu eliminați datele ca soluție rapidă.
4. Dacă semnăturile nu sunt compatibile, opriți instalarea și pregătiți un APK de test semnat cu cheia existentă printr-o configurație locală sigură; nu încărcați cheia sau parolele în repository ori în conversație.
5. După instalarea compatibilă, verificați în RO și EN: logo și nume FoniWise, butoanele ⓘ și dialogurile, Cache Advisor (numai instrucțiuni manuale), RAM Monitor (valori Android și recomandări adaptate), ghidarea manuală a animațiilor, navigarea și persistența datelor.
6. Notați pentru fiecare probă: **trecut / eșuat / netestat**, versiunea Android, modelul telefonului, commitul și mesajele relevante din Logcat, fără date personale, identificatori de dispozitiv sau secrete.

**Criteriu de oprire:** orice conflict de semnătură, solicitare neașteptată de permisiuni, pierdere de date sau blocare a aplicației oprește testul până la investigare.

## Interpretarea erorilor

- `INSTALL_FAILED_UPDATE_INCOMPATIBLE` indică frecvent o semnătură incompatibilă; **nu dezinstalați** pentru a o ocoli.
- `INSTALL_FAILED_VERSION_DOWNGRADE` indică un `versionCode` mai mic.
- Play Protect este o verificare separată; cheia originală nu garantează absența avertismentelor.

**Nu integrați PR #19 în `main` până când CI și verificarea vizuală pe telefon nu sunt satisfăcătoare.**
