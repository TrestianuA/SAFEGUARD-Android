# FoniWise — Decizie de produs: trei zone, simplitate și fără duplicări

**Stare: APROBAT de utilizator.** Această decizie ghidează dezvoltarea FoniWise — „Know Your Phone” / „Cunoaște-ți telefonul”. Este o decizie de arhitectură a experienței și prioritizare, **nu o afirmație că interfața a fost deja restructurată**.

## Principiu central

**Fiecare funcție nouă trebuie să rezolve o problemă reală fără să dubleze ceea ce există deja.** Aplicația trebuie să fie inteligibilă în aproximativ 30 de secunde. Nu adăugăm funcții doar pentru că sunt interesante; preferăm o aplicație clară, sigură și ușor de întreținut.

## Cele trei zone principale aprobate

### 1. Telefonul meu / My Phone
Date reale și accesibile despre telefon: baterie, RAM, stocare, securitate și alte informații permise de Android. Răspunde la întrebarea: **„Ce se întâmplă pe telefonul meu?”** Nu promite diagnoze ori optimizări pe care Android nu le permite.

### 2. Învață despre telefon / Learn About Your Phone
**Un singur dicționar** cu căutare și categorii precum baterie, performanță, conectivitate, securitate, stocare și Opțiuni pentru dezvoltatori. Răspunde la: **„Ce înseamnă acest lucru?”** Articole bilingve RO/EN, utile și corecte. „Știai că?” se integrează în articole, nu devine modul separat.

### 3. Recomandări și acțiuni / Recommendations & Actions
Sfaturi practice, pași reversibili și butoane de deschidere a paginilor Android Settings **acolo unde există un mecanism compatibil**. Răspunde la: **„Ce pot face concret?”** Utilizatorul modifică manual setările; FoniWise nu le schimbă automat. Nu promitem deschiderea fiecărui comutator individual, în special în Developer Options.

**Cele trei zone reprezintă responsabilități logice, nu obligatoriu trei ecrane noi sau trei taburi suplimentare.** Refolosim navigarea existentă și evităm aglomerarea.

## Regula anti-duplicare

**O informație se redactează o singură dată și este reutilizată oriunde e necesară.** De exemplu, cardul RAM din dashboard afișează datele relevante și trimite prin „Află mai multe” la **unicul articol RAM** din dicționar. Acțiunea de deschidere a setărilor se afișează contextual, fără a copia explicația în mai multe locuri. Evităm versiuni divergente ale acelorași recomandări.

## Decizii privind ideile existente

| Idee | Decizie |
|---|---|
| Tabloul principal cu date despre telefon | Păstrăm |
| Dicționar bilingv RO / EN | Păstrăm și extindem gradual |
| Opțiuni pentru dezvoltatori | **Categorie în dicționar**, nu modul independent |
| Butoane către setările Android | Păstrăm unde funcționează; fără modificări automate |
| „Știai că?” / “Did you know?” | În interiorul articolelor, nu secțiune nouă |
| „Optimizare” automată, RAM booster, cache cleaner miraculos | **Nu adăugăm** |
| Ecrane noi pentru fiecare idee | Evităm |

Cele **111 noțiuni** din planul editorial nu reprezintă obligația de a publica imediat 111 articole. Prioritizăm câteva articole excelente, verificabile și utile; extindem după testare și feedback. Ideile suplimentare intră în backlog, nu direct în interfață.

## Priorități imediate și criterii de acceptare

1. **Stabilizare și build:** verificăm compilarea și funcțiile existente înainte de extinderi majore.
2. **Dicționar fără repetiții:** sursă unică pentru explicații, linkuri contextuale din dashboard și recomandări.
3. **Calitate înainte de volum:** finalizăm și testăm un număr mic de articole bine scrise RO/EN.
4. **Navigare și acțiuni:** verificăm traseul din dashboard către articol și butoanele Android Settings pe dispozitive reale, cu fallback clar.
5. **Controlul complexității:** înainte de fiecare funcție, verificăm problema reală, suprapunerea cu funcțiile existente, riscurile, mentenanța și impactul asupra navigării.
6. **Fără funcții noi neesențiale până la stabilizare**; ideile noi se păstrează pentru versiuni viitoare.

## Constrângeri de proiect păstrate

Păstrăm logo-ul original aprobat, identitatea verde pastel fără valuri decorative, RO/EN, `applicationId` existent, semnarea cu cheia originală, permisiunile, datele utilizatorilor și funcțiile reale deja existente. Nu modificăm `main` sau PR #19 prin merge fără acord explicit.

**Implementare:** acest document fixează direcția aprobată; modificările de UI, refactorizarea și testele sunt sarcini separate, de realizat gradual.
