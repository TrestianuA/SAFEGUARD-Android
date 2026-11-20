# Integrarea imaginii originale FoniWise în Android

**Stadiu:** pregătire pentru fișierul binar. Până la încărcarea lui, manifestul folosește sigla veche pentru a menține compilarea funcțională.

## Un singur fișier de încărcat în ramura `feat/foniwise-ui-phase1`

- **Cale exactă:** `app/src/main/res/drawable-nodpi/foniwise_symbol_original_approved.png`
- **Sursă:** imaginea originală aprobată de utilizator, 512 × 512, PNG RGBA, cu telefon zâmbitor, cele patru simboluri, orbită și frunză
- **SHA-256 al originalului:** `58b6404d74aa5def2bbadd87f02ae922cfaf434240adcca6c5d4da345b365852`
- Nu încărcați preview-ul rotunjit sau alte versiuni reconstituite; păstrați imaginea originală.

## După încărcare

1. Verificați că fișierul binar este prezent în GitHub și că hash-ul coincide.
2. În `MainActivity.kt`, `BrandLogo("foniwise_symbol_original_approved", ...)` îl va încărca automat în antet; fallback-ul SAFEGUARD poate fi eliminat numai după verificare.
3. Pentru pictograma launcher, configurați un `adaptive-icon` care utilizează desenul original cu spațiu de siguranță și fundal verde/alb, în funcție de mascarea Android. Nu tăiați frunza sau orbita.
4. Modificați `android:icon` din manifest numai când resursa de launcher există efectiv, pentru a nu întrerupe CI.
5. Testați build, auditul APK și aspectul pe telefon. Păstrați `applicationId`, semnarea și permisiunile neschimbate.

Nu afirmați că fișierul este inclus în APK înainte de confirmarea încărcării și a compilării.
