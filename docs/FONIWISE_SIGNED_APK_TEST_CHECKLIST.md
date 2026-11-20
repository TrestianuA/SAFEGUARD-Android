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

## Interpretarea erorilor

- `INSTALL_FAILED_UPDATE_INCOMPATIBLE` indică frecvent o semnătură incompatibilă; **nu dezinstalați** pentru a o ocoli.
- `INSTALL_FAILED_VERSION_DOWNGRADE` indică un `versionCode` mai mic.
- Play Protect este o verificare separată; cheia originală nu garantează absența avertismentelor.

**Nu integrați PR #19 în `main` până când CI și verificarea vizuală pe telefon nu sunt satisfăcătoare.**
