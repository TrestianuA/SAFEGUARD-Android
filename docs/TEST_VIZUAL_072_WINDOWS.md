# Test SAFEGUARD 0.7.2 — Android Studio / Windows

## Pregătire
- Pe telefon rămâne instalată versiunea 0.7.0. **Nu o dezinstalați**; nu dezactivați Play Protect.
- Folosiți același cont Windows în care se află `%USERPROFILE%\.android\debug.keystore` original.
- Codul sursă trebuie să fie actualizat la ultimul commit din ramura `main`.
- Pentru testul vizual aprobat, consultați `docs/INTERFATA_APROBATA.md`. Logo-urile originale sunt păstrate separat în biblioteca SAFEGUARD/Branding și nu trebuie confundate cu emoji-urile temporare din interfață.

## 1. Verificați cheia locală (PowerShell)
```powershell
keytool -list -v -keystore "$env:USERPROFILE\.android\debug.keystore" -alias androiddebugkey -storepass android -keypass android
```
Amprenta SHA-256 trebuie să fie exact:
`B0:58:08:CC:91:0E:75:24:42:6D:E7:28:51:9D:F7:46:28:A0:45:62:98:42:AA:11:FB:DC:62:2A:10:4C:94:86`.

Dacă diferă, **opriți instalarea** și investigați. Nu încărcați cheia în GitHub.

## 2. Compilați local
Din terminalul Android Studio, în directorul proiectului:
```powershell
.\gradlew.bat --no-daemon testDebugUnitTest assembleDebug
```
APK așteptat: `app\build\outputs\apk\debug\app-debug.apk`.
În `app/build.gradle.kts` versiunea trebuie să fie `0.7.2`, `versionCode = 9`, iar applicationId `com.safeguard.android`.

## 3. Verificați APK-ul înainte de instalare
Din Android SDK Build Tools (calea diferă în funcție de instalare):
```powershell
& "$env:LOCALAPPDATA\Android\Sdk\build-tools\35.0.0\apksigner.bat" verify --verbose --print-certs "app\build\outputs\apk\debug\app-debug.apk"
```
Dacă versiunea Build Tools diferă, adaptați directorul. Verificați că SHA-256 al certificatului din APK este cel de mai sus. APK-ul generat de GitHub Actions folosește altă cheie și **nu poate actualiza** direct instalarea existentă.

## 4. Test vizual și funcțional
- Porniți aplicația și comparați culorile, antetul, cardurile rotunjite și navigarea Acasă / Protecție / Rapoarte cu macheta.
- Verificați că fiecare tab poate fi selectat și că funcțiile existente rămân accesibile.
- Verificați dialogul de consimțământ la scanare, scanarea locală și afișarea rezultatelor.
- Verificați inspectorul notificărilor, accesul opțional, reîmprospătarea și ștergerea istoricului.
- Nu interpretați cardurile de navigare ca protecție antivirus, curățare automată sau blocare reală de aplicații.
- **Limitare cunoscută:** logo-urile originale încă nu sunt incluse ca resurse binare în APK. Integrarea lor trebuie finalizată separat înainte de a declara aspectul identic cu macheta.

Dacă Play Protect sau Android avertizează, nu forțați instalarea. Notați mesajul și investigați semnătura, proveniența APK-ului și permisiunile.
