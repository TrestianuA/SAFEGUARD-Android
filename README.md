# SAFEGUARD Android

Aplicație Android experimentală pentru informații despre RAM, baterie și stocare, inventarierea aplicațiilor și inspectarea locală a notificărilor.

## Starea versiunilor (verificată la 17 noiembrie 2026)

- **0.7.0**: ultima versiune confirmată ca funcțională pe Samsung Galaxy A13 (Android 14). Arhivarea surselor originale pe ramura `archive/safeguard-0.7.0` este încă incompletă; existența ramurii nu demonstrează că aceasta conține integral sursele originale.
- **0.7.1**: `versionCode = 8`, `versionName = "0.7.1"`. Proiectul din `main` a generat cu succes un APK debug în [GitHub Actions, execuția #18](https://github.com/TrestianuA/SAFEGUARD-Android/actions/runs/44156277981), pentru commitul [93753a7](https://github.com/TrestianuA/SAFEGUARD-Android/commit/93753a78967c008ddbdfdf55ef34f393c1a33fd9). Execuția include verificarea semnăturii și un artifact `SAFEGUARD-apk-audit`.
- **Proveniența codului**: fișierele prezente pe `main` nu trebuie prezentate automat ca o copie exactă a arhivei originale 0.7.1. Unele implementări au fost reconstruite/adaptate. Comparația fișier-cu-fișier cu arhiva originală rămâne de făcut.
- **Testare pe dispozitiv**: 0.7.1 nu este confirmată ca instalată și testată pe telefon. Google Play Protect a afișat un avertisment la încercarea de instalare. Nu dezactivați Play Protect pentru a ocoli avertismentul.


## Starea pregătirii 0.7.2 (18 noiembrie 2026)

- Commitul [f623d9c](https://github.com/TrestianuA/SAFEGUARD-Android/commit/f623d9ccfb0eb3bec178598459d1fc37b63a30fa) configurează preferința pentru logo-urile originale comise în proiect față de previzualizările temporare. **Acest commit nu dovedește că PNG-urile originale au fost încărcate**.
- Până la verificarea resurselor binare din APK, previzualizările Canva nu sunt echivalente cu logo-urile originale aprobate. Nu declarați integrarea grafică finalizată doar pe baza unui build verde.
- Pentru actualizarea instalării 0.7.0, comparați amprenta SHA-256 a certificatului de semnare al APK-ului instalat/original cu cea a APK-ului nou, folosind instrumente locale de audit (de exemplu `apksigner verify --print-certs`). Nu încărcați cheia privată sau parolele în GitHub, în loguri ori în conversații.
- Dacă certificatele diferă, **nu** încercați actualizarea peste instalarea existentă; păstrați 0.7.0 și investigați proveniența semnăturii. Nu dezinstalați versiunea funcțională pentru a forța testul.
- Un APK debug din GitHub Actions nu garantează compatibilitatea semnăturii cu instalarea de pe telefon. Verificarea efectivă pe Samsung Galaxy A13 rămâne separată de compilarea CI.

## Principii

- Scanarea este o revizuire locală a configurației, nu un motor antivirus; accesările speciale sau aplicațiile instalate nu reprezintă automat amenințări.
- Datele de notificări sunt tratate local și cu minimizarea informațiilor.
- Nicio ștergere de fișiere fără confirmare explicită.
- Fără chei de semnare, tokenuri, parole sau date personale în depozitul public.

## Construire și verificare

Proiect Android cu Java 17 și Gradle Wrapper. Compilarea de test se face prin `bash ./gradlew assembleDebug`; testele unitare pot fi lansate separat prin `bash ./gradlew testDebugUnitTest`. **Un build reușit nu demonstrează automat executarea testelor unitare sau validarea pe telefon.**

## Sarcini deschise

1. Compararea surselor din `main` cu arhiva originală 0.7.1 și marcarea diferențelor.
2. Completarea arhivei istorice 0.7.0 cu fișierele originale, fără suprascrieri neverificate.
3. Verificarea certificatului instalării 0.7.0 înainte de alegerea unei strategii de semnare a actualizărilor.
4. Rularea explicită a testelor unitare și testarea pe dispozitiv, când există condiții sigure.

Designurile Canva aprobate sunt materiale de design; nu presupuneți că sunt integrate în APK până când resursele Android corespunzătoare sunt comise și verificate.

## SAFEGUARD 0.7.3-rc1 — verificare interfață și Cache Advisor (19 noiembrie 2026)

- Versiunea declarată în `app/build.gradle.kts` este `0.7.3-rc1` (`versionCode = 10`); nu este o versiune finală validată pe dispozitiv.
- Ecranul Acasă folosește pictograme Compose vectoriale și păstrează logo-ul configurat; simpla compilare nu certifică fidelitatea vizuală față de macheta aprobată.
- Cardul **Curățare manuală / Cache Advisor** deschide lista de aplicații din setările Android, de unde utilizatorul poate alege aplicația și, dacă este disponibilă, opțiunea de ștergere cache. Nu șterge fișiere și nu promite curățare automată.
- Cardul pentru baterie este etichetat **Setări baterie / Manual**, deoarece deschide setările sistemului și nu optimizează automat consumul.
- RAM Monitor folosește `ActivityManager.MemoryInfo` pentru memoria totală, disponibilă și indicatorul Android `lowMemory`; recomandările depind de acest indicator, nu de un prag inventat. Măsurătorile trebuie verificate pe telefon.
- Explicațiile pentru cache, RAM și animațiile Android includ îndrumări manuale RO/EN. Denumirile meniurilor pot varia în funcție de producător.
- Verificare statică: traseele butoanelor și textele au fost inspectate în `MainActivity.kt`; nu au fost executate aici un emulator sau teste instrumentate.
- Verificare CI necesară după fiecare commit: în GitHub Actions verificați separat pașii `Build debug APK`, `Run debug unit tests`, `Audit generated APK signature and permissions` și artefactele. Nu presupuneți rezultat verde înainte de finalizarea execuției.
- Testare manuală ulterioară: deschiderea celor șase carduri, RAM refresh, dialogurile ⓘ/limbă, navigarea, lizibilitatea pe ecran mic și compatibilitatea certificatului APK cu instalarea existentă. Nu dezinstalați aplicația funcțională pentru test.
