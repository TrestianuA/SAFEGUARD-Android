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


## Clarificare aprobată: păstrăm funcțiile SAFEGUARD 0.7.2 și explicațiile complete

Utilizatorul a confirmat explicit că **simplificarea privește navigarea, nu conținutul**. Articolele trebuie să fie suficient de detaliate pentru începători: „Ce este?”, „De ce contează?”, **„Cum te poate ajuta?”**, „Ce poți face?”, limite/avertismente și, când este cazul, revenirea la setările inițiale. Exemple concrete, limbaj accesibil, RO/EN. Recomandările rămân vizibile atât în articole, cât și în zona „Recomandări și acțiuni”, prin reutilizarea aceleiași surse de conținut; nu copiem definiții în mai multe locuri.

### Inventarul aprobat pentru „Telefonul meu”

Păstrăm funcționalitățile din captura SAFEGUARD 0.7.2: **Scanare securitate, Inspector notificări, Baterie, Aplicații, Curățare manuală, Rapoarte**, plus **Starea dispozitivului / RAM** și celelalte date deja disponibile. „Curățare manuală” poate avea o recomandare contextuală în zona 3, iar rapoartele pot fi accesate din zona 1; **nu eliminăm funcțiile existente** prin reorganizare. Cele trei zone sunt responsabilități logice, nu trei module noi care să fragmenteze funcționalitatea.

### Regula „Cum te poate ajuta?”

Sub fiecare opțiune, utilizatorul trebuie să poată afla **beneficiul concret** printr-o explicație vizibilă și inteligibilă, nu doar un status sau un buton „Deschide”. Formula editorială aprobată este **„Cum te poate ajuta?” / “How can it help you?”**. Prezentarea poate fi compactă în card, cu explicație extinsă la accesare; nu înghesuim paragrafe lungi în dashboard.

Exemplu pentru **Inspector notificări**: permite examinarea notificărilor puse la dispoziție de Android, pentru observarea mesajelor importante, a aplicațiilor care trimit multe alerte sau a activităților de verificat. Necesită acces acordat explicit de utilizator. **Nu este antivirus** și nu garantează detectarea tuturor mesajelor periculoase. Păstrăm inspectorul distinct de scanarea locală de securitate și îl testăm pe dispozitiv.

### Criterii de implementare

1. Fiecare dintre cele șase opțiuni și cardurile relevante ale dispozitivului rămân accesibile după reorganizare.
2. Fiecare are explicație bilingvă „Cum te poate ajuta?” și recomandări practice adecvate, cu limite clare.
3. Articolele sunt complete, cu exemple și avertismente, nu reduse la o singură propoziție.
4. Recomandările sunt vizibile și contextual în articole; zona 3 le reunește fără duplicare editorială.
5. Butoanele către setările Android indică destinația reală și folosesc fallback; nu promit deschiderea fiecărui comutator.
6. Verificăm compilarea, navigarea și funcțiile existente înainte de a considera reorganizarea finalizată.

**Stare:** decizie editorială și de produs aprobată; nu afirmă că toate modificările de UI au fost deja implementate.
