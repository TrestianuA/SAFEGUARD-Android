# Semnarea locală a SAFEGUARD pentru actualizarea versiunii 0.7.0

Acest ghid este pentru proprietarul instalării SAFEGUARD 0.7.0. Nu publicați și nu încărcați fișierul `debug.keystore` în GitHub, conversații sau servicii de partajare.

## Identitatea versiunii instalate

- Application ID: `com.safeguard.android`
- Certificat SHA-256 al versiunii 0.7.0 instalate: `B0:58:08:CC:91:0E:75:24:42:6D:E7:28:51:9D:F7:46:28:A0:45:62:98:42:AA:11:FB:DC:62:2A:10:4C:94:86`
- Cheia locală `%USERPROFILE%\.android\debug.keystore` a fost verificată de proprietar și certificatul coincide.
- APK-ul de test generat de GitHub Actions pentru 0.7.1 folosește alt certificat; **nu** este o actualizare compatibilă cu instalarea 0.7.0.

## Construire și semnare pe calculatorul proprietarului (Windows)

1. Păstrați o copie de rezervă privată și criptată a `debug.keystore`.
2. Descărcați proiectul SAFEGUARD de pe ramura `main` și deschideți-l în Android Studio pe **același cont Windows** în care se află cheia originală.
3. În Android Studio, selectați varianta **debug** și folosiți **Build > Build APK(s)** (sau rulați `gradlew.bat assembleDebug` în terminalul proiectului). Compilarea locală debug utilizează, în configurația Android Gradle Plugin standard, `%USERPROFILE%\.android\debug.keystore`. Dacă există configurări de semnare personalizate, verificați-le înainte.
4. APK-ul rezultat se află de regulă la `app\build\outputs\apk\debug\app-debug.apk`.
5. Verificați certificatul **APK-ului rezultat**, nu doar al keystore-ului, cu `apksigner verify --print-certs --verbose app-debug.apk` din Android SDK Build Tools. SHA-256 trebuie să fie identic cu cel de mai sus. Verificați și că `applicationId` este `com.safeguard.android`, iar `versionCode` este mai mare decât cel instalat.
6. Abia după aceste verificări, testați instalarea ca actualizare. Nu dezinstalați versiunea 0.7.0 și nu dezactivați Play Protect. O semnătură corectă nu garantează acceptarea de către Play Protect.

**Notă:** cheia debug este adecvată pentru testare locală, nu reprezintă o strategie recomandată pentru distribuția publică. Migrarea la o cheie de producție necesită un plan separat; schimbarea cheii nu permite în mod obișnuit actualizarea directă a instalărilor existente.

## De făcut înainte de testare

- Verificați că sursele din `main` sunt cele dorite pentru 0.7.1.
- Rulați testele și compilarea locală.
- Verificați amprenta APK-ului semnat, versiunea și permisiunile.
- Păstrați cheia privată exclusiv pe dispozitivele proprietarului.
