# SAFEGUARD 0.7.3 — verificări CI și testare manuală

## Corecție verificare logo-uri (2026-11-19)

În `.github/workflows/android-debug.yml`, semnăturile binare sunt comparate cu `bytes.fromhex("89504e470d0a1a0a")` (PNG) și `bytes.fromhex("ffd8ff")` (JPEG). Anterior, literalii Python conțineau bare oblice inverse escapate dublu și puteau respinge imagini valide.

Verificare statică: cele două comparații sunt acum definite cu octeții corecți; workflow-ul cere exact o variantă PNG sau JPG pentru fiecare logo. Nu s-au schimbat imaginile, designul, configurația de semnare sau permisiunile Android.

**Neconfirmat:** rularea completă GitHub Actions, `assembleDebug`, `testDebugUnitTest`, instalarea pe dispozitiv și semnătura APK rezultată. Descărcarea logo-urilor de la Canva poate încă eșua până când fișierele originale sunt prezente în `app/src/main/res/drawable-nodpi/`.

## Verificări necesare după încărcarea logo-urilor

1. Confirmă existența fișierelor `safeguard_logo_simplu.jpg` și `safeguard_logo_complet.jpg` (sau a variantelor PNG autentice), fără dubluri cu același nume de resursă.
2. Verifică în GitHub Actions că pașii de validare a imaginilor, `assembleDebug` și `testDebugUnitTest` trec.
3. Verifică raportul `SAFEGUARD-apk-audit` pentru hash, permisiuni și certificatul semnatarului; nu presupune că semnătura debug este aceeași cu cea a aplicației deja instalate.
4. Pe telefon: verifică logo-urile, explicațiile RO/EN, Cache Advisor (doar deschide setările), actualizarea manuală a RAM și ghidul de animații. Nu solicita ștergerea datelor aplicațiilor.
