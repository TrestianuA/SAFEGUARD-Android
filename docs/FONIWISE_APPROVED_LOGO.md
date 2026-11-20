# FoniWise — logo aprobat

**Status:** aprobat de proprietarul proiectului, 20 noiembrie 2026.

**Sursă vizuală oficială:** imaginea cu telefonul verde zâmbitor trimisă și aprobată explicit în conversația proiectului. Copia PNG este păstrată în biblioteca proiectului, la `/FoniWise/Branding/FoniWise_logo_aprobat.png` (1536 × 1536). Fișierul binar nu este inclus în acest commit.

## Elemente obligatorii

- Telefon verde zâmbitor, cu patru pictograme pe ecran (semnal, procesor, stocare, setări).
- Orbită verde, frunză și accente luminoase.
- Textul exact **FoniWise** în două nuanțe de verde.
- Sloganul exact **Know Your Phone**.
- Fundal alb/deschis, conform originalului aprobat.

## Reguli de integrare

- Nu înlocuiți logo-ul SAFEGUARD existent până la pregătirea și testarea redenumirii.
- Nu recreați imaginea dintr-un prompt dacă originalul aprobat este disponibil; folosiți originalul.
- Pentru pictograma Android, realizați separat o adaptare lizibilă fără slogan, numai după aprobare.
- Păstrați `applicationId`, configurația de semnare, funcțiile și compatibilitatea de actualizare.
- Acest document nu confirmă o marcă înregistrată și nu reprezintă o lansare publică.

## Verificare integritate și integrare viitoare

- Original aprobat: `FoniWise_logo_aprobat.png`, PNG RGB, **1536 × 1536 px**.
- SHA-256 al originalului: `cbdcf2f9156dee84c068249acaf56128f58d2add8528bcb9a55e380b001d9e1c`.
- Imaginea aprobată a fost comparată pixel cu pixel cu atașamentul de referință al utilizatorului: **identică la nivelul pixelilor** (atașamentul inițial era codificat JPEG, chiar dacă avea extensia `.png`).
- La integrarea în GitHub, copiați exact fișierul PNG din bibliotecă și verificați SHA-256 înainte de commit. Nu folosiți o variantă generată prin căutare de imagini.
- Păstrați fișierul original într-un director de branding separat; nu înlocuiți automat resursele `mipmap`/`drawable` existente.
- Pentru o pictogramă Android adaptivă, creați o variantă derivată cu simbolul telefonului lizibil la dimensiuni mici, validați decuparea circulară și cea cu colțuri rotunjite, apoi solicitați aprobare înainte de înlocuire.
- După integrare, rulați `testDebugUnitTest` și `assembleDebug`, verificați auditul de semnare/permisiuni și efectuați un test vizual pe dispozitiv.

**Limitare:** acest PR conține doar documentație; nu adaugă încă binarul PNG în GitHub și nu redenumește aplicația.

## Previzualizare pictogramă (neaprobată)

- Am pregătit local o **previzualizare pătrată de 512 × 512 px**, decupată din originalul aprobat, fără regenerare AI și fără slogan.
- Fișierul de previzualizare este salvat separat în biblioteca proiectului: `/FoniWise/Branding/FoniWise_icon_preview_neaprobat.png`.
- Previzualizarea **nu este pictograma Android finală**: trebuie verificată la dimensiuni mici și în măștile adaptive Android înainte de aprobare.
- În `AndroidManifest.xml`, eticheta rămâne `SAFEGUARD`, iar pictograma rămâne `@drawable/safeguard_logo_simplu`; în `app/build.gradle.kts`, `applicationId` rămâne `com.safeguard.android` și versiunea `0.7.3-rc1`.
- Nu se activează nicio resursă FoniWise în aplicație prin acest PR.

## Previzualizări pentru măști Android (neaprobate)

Au fost create două fișiere PNG cu transparență, pornind de la previzualizarea pătrată de 512 × 512 px și aplicând numai măști geometrice, fără modificarea desenului:

- `/FoniWise/Branding/FoniWise_icon_round_preview_neaprobat.png` — mască circulară.
- `/FoniWise/Branding/FoniWise_icon_rounded_preview_neaprobat.png` — mască pătrată cu colțuri rotunjite.

Acestea sunt doar simulări vizuale, **nu** resurse Android adaptive definitive. Originalul complet aprobat rămâne separat și nemodificat. Nu activați aceste fișiere în manifest fără evaluarea lizibilității la dimensiuni mici și aprobarea utilizatorului.
