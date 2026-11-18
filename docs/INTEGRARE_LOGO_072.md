# Integrare logo-uri originale — SAFEGUARD 0.7.2

Codul Compose din `MainActivity.kt` caută acum două resurse drawable Android:

- `app/src/main/res/drawable-nodpi/safeguard_logo_simplu.png`
- `app/src/main/res/drawable-nodpi/safeguard_logo_complet.png`

Imaginile originale au fost păstrate, fără modificări, în pachetul persistent:
`/SAFEGUARD/Branding/SAFEGUARD_072_logo_originale.zip` din biblioteca ChatGPT.

**Stare actuală:** resursele binare nu au fost încă transferate în GitHub; codul folosește un simbol temporar dacă lipsesc. Compilarea CI poate reuși fără resurse, dar acest APK nu este vizual complet. Nu declarați integrarea finalizată până când ambele PNG-uri sunt în repository și un APK nou este compilat după acest commit.

Pe Windows, descărcați pachetul ZIP, extrageți și copiați directorul `app` peste proiect, apoi adăugați cele două PNG-uri în Git și generați APK-ul cu cheia locală originală, conform `docs/TEST_VIZUAL_072_WINDOWS.md`. Nu distribuiți cheia privată.
