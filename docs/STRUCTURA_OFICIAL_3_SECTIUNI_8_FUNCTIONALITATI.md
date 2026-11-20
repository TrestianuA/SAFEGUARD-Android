# FoniWise — Structura oficială: 3 secțiuni pentru cele 8 funcționalități

**Decizie aprobată de utilizator la 20 noiembrie 2026.**

Această structură stabilește unde vor fi prezentate cele opt funcționalități din [planul original](PLAN_ORIGINAL_8_FUNCTIONALITATI.md). Nu modifică lista originală, nu reprezintă confirmarea implementării complete și nu autorizează schimbarea identității vizuale aprobate.

## Cele trei secțiuni principale rămân neschimbate

### 1. Telefonul — „Văd”
Funcționalități principale:
- **#3 Asistent pentru spațiul de stocare** — spațiu disponibil, explicații, acces la setări; fără ștergere automată.
- **#4 Consilier pentru baterie** — informații disponibile și explicații despre consum.
- **#6 Android și actualizări de securitate** — versiune Android, patch de securitate, explicații și acces la setări.
- **#8 Raport de sănătate a telefonului** — sinteză inteligibilă: bine / atenție / verificare manuală; date locale.

### 2. Învață — „Înțeleg”
Funcționalități principale:
- **#2 Ghid pentru permisiunile aplicațiilor** — cameră, microfon, locație, contacte, notificări și exemple practice.
- **#5 Ghid antifraudă și antiphishing** — mesaje, linkuri, apeluri, coduri bancare și listă de verificare; fără citirea mesajelor personale.

### 3. Acțiuni — „Rezolv”
Funcționalitate principală:
- **#1 Asistent de securitate pas cu pas** — blocarea ecranului, actualizări și accesări speciale, cu pași manuali.
- Pașii practici ai celorlalte ghiduri, recomandări și scurtături către setările Android. Acestea pot fi accesate contextual și din „Telefonul” sau „Învață”.

## #7 Mod simplificat pentru începători — transversal
**#7** nu devine a patra secțiune. Se aplică tuturor celor trei secțiuni:
- **Simplu**: texte mai mari, explicații scurte, butoane vizibile, pași de tip „Pasul 1 din 3”.
- **Detaliat**: informații și explicații suplimentare.
- Identitatea vizuală FoniWise aprobată rămâne aceeași în ambele moduri.

## Reguli de integrare
1. Navigația principală rămâne **Telefonul | Învață | Acțiuni**; nu adăugăm opt butoane principale sau o a patra secțiune.
2. „Telefonul = văd; Învață = înțeleg; Acțiuni = rezolv” este principiul de organizare.
3. Ghidurile pentru permisiuni și antifraudă au fost introduse inițial în „Acțiuni”; mutarea/prezentarea lor principală în „Învață” este **de implementat**, nu realizată prin această decizie.
4. Datele inaccesibile Android nu sunt prezentate ca verificate. Nu există modificări automate riscante, ștergere automată sau citire a mesajelor personale.
5. Implementarea se face incremental pe ramura de integrare, cu CI și ulterior testare reală pe Samsung prin Android Studio. Nu se face merge în `main` fără acord.
6. Planul original al celor opt funcționalități și planul tehnic 0.7.3 sunt documente distincte.

## Stare
**Decizie de arhitectură informațională aprobată și salvată.** Aceasta nu reprezintă un raport de funcționalități terminate sau validate pe dispozitiv.
