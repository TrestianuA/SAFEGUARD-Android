# FoniWise — Catalog de prototipuri vizuale

Status: referință pentru etapa vizuală; nu este cod Android implementat.
Identitate: verde închis #064D2B, verde #087C26, verde pal #E7F8DB, fundal #F3FBF2, accent #B5F52C, verde grafic #72B94C; Nunito Sans. Texte RO/EN, offline, fără permisiuni suplimentare, TalkBack, adaptare la Galaxy A13. Integrare în paginile lecțiilor, la nivelul potrivit, fără schimbarea primei pagini.

## Prototipuri păstrate
- V01 Baterie: două baterii verticale, nouă și degradată, ambele încărcate la 100%; sănătatea afișată separat; capacitate inițială 5000 mAh, cursor 40–100%, implicit 80%; capacitate rămasă = 5000 × procent / 100; pierdută = 5000 minus rămasă. Verde la >=80%, ocru #D6A32F la >=60%, coral #C96846 sub 60%. Simulare, nu măsurătoare.
- V02 Procesor: selector 1/2/4/8 nuclee, implicit 2, reset; durată 4 + 6/n secunde; comparație statică păstrată.  bare orizontale 10, 7, 5,5 secunde pentru 1, 2, 4 nuclee; segment secvențial 4 s verde închis, segment paralel 6/n verde grafic. Model ideal, nu benchmark.
- V03 Dezinstalare: două coloane înainte/după, 80 MB cod (#72B94C), 120 MB date private (#087C26), 300 MB fotografii partajate (#D1E6C5); după, primii 200 MB reprezentați prin contur punctat, fotografiile rămân. Exemplu ipotetic.
- V04 Criptare: document cu linii, săgeată, lacăt verde-lime pe fond verde închis, săgeată, text cifrat ilustrativ A9 F2 8C / 7D 3B E1 / C4 0A 6F / 2E 9B 5D. Etichete date originale / criptare / date criptate.
- V05 Comunicații: două diagrame cu telefoane la capete și server central. TLS: două legături criptate distincte, serverul poate citi mesajul. E2EE: conținut protejat între capete, serverul retransmite text cifrat. Nu sugera că toate metadatele sunt ascunse.
- V06 PIN: selector 4/6/8 cifre, implicit 6; căsuțe cu puncte, combinații 10^n, entropie ideală n × log2(10), două zecimale. Nu colectează PIN real, nu estimează timpul de compromitere.
- V07 Ecran tactil: straturi afișaj/digitizor, două contacte; 120 Hz tactil (8,3 ms) vs 60 Hz afișaj (16,7 ms), intervale distincte, nu latență totală fixă.
- V08 Cameră: obiectiv/senzor/ISP; comparație lumină suficientă vs redusă, zgomot și mișcare; exemplele fotografice din conversație sunt referințe conceptuale, nu active salvate.
- V09 Microfon: membrană MEMS, conversie în eșantioane; PCM mono 48 kHz × 16 biți = 96.000 octeți/s, 5,76 MB/minut.
- V10 Degradare: grafic conceptual descendent 100% spre 80%, fără a pretinde o curbă universală.
- V11 Permisiuni: aplicație → Android → componentă; diferențiază autorizare, utilizare efectivă și transfer de date.

- V12 Inginerie socială: antet verde închis, nod Phishing verde, ramuri spre Smishing/SMS, Vishing/apel vocal, Quishing/cod QR, fundal verde pal. HTML RO/EN: patru selecții exclusive, evidențiere a nodului, explicație și reset. SVG RO/EN: cinci cadre statice per limbă (inițial și patru selecții). Mărire opțională la 420 px, derulare locală și reset inclusiv mărirea; al șaselea cadru SVG per limbă reprezintă vederea mărită derulată la dreapta.
- V13 Identitatea paginii: două machete identice, adrese fictive serviciu.example și imitatie.example, lacăt HTTPS la ambele, câmpuri nefuncționale. Aspectul și criptarea transportului nu dovedesc legitimitatea. HTML RO/EN: trei selecții Aspect/Domeniu/HTTPS, evidențiere simultană a ambelor machete, explicații și reset. SVG RO/EN: patru cadre per limbă, machete vectoriale reconstruite, nu capturi pixel-perfect. Domeniile afișate consecvent pe două rânduri (nume și .example), inclusiv în cele patru cadre SVG.
- V14 Detector deepfake: două grile de câte 100 celule, 5 alarme false (coral) și 90 sintetice detectate (verde) implicit. Două cursoare independente 0–100; nedetectate = 100 minus detectate. Nu este detector real. HTML RO/EN: grile 10×10, rezultat textual și reset 5/90. SVG RO/EN: șase cadre per limbă, perechi alarme false/detectate (5,90), (0,90), (100,90), (100,0), (100,100), (0,100); câte 100 celule în fiecare grilă, culori și avertisment de simulare păstrate. Cursoare HTML cu înălțime CSS 44 px; verificarea tactilă fizică rămâne deschisă.

- V15 Wi-Fi vs internet: telefon → router → internet; comutator conexiune din amonte, implicit activ; Wi-Fi rămâne activ, legătura internet devine coral și întreruptă când comutatorul este oprit. HTML RO/EN: reset la conexiune activă, avertisment simulare; SVG: două stări per limbă (internet disponibil/indisponibil). Revizie vizuală aprobată: router cu simbol Wi-Fi și indicatori, etichete și stare evidențiate, legenda «Exemplu informativ»/«Illustrative example».
- V16 GPS: patru sateliți, linii punctate către receptor, elipsă de incertitudine; poziție și eroare ceas, fără transmitere către sateliți. HTML RO/EN: explorare sateliți, semnale, receptor, incertitudine, evidențieri și reset. SVG: cinci cadre per limbă. Revizie vizuală aprobată: eticheta semnalelor pe fundal opac, deasupra liniilor inclusiv în starea evidențiată.
- V17 Date mobile: cursoare 1–20 Mb/s (implicit 5), 1–60 minute (implicit 10); volum MB = debit × minute × 60 / 8; bară raportată la maximul simulării 9000 MB. Model ideal, nu măsurătoare. HTML RO/EN: cursoare de 44 px și reset 5 Mb/s, 10 minute. SVG: șase cadre per limbă, perechi viteză/durată (5,10), (1,1), (20,1), (1,60), (20,60), (5,11), cu rezultate și bară. Revizie vizuală aprobată: rezultat evidențiat, spațiere, durată cu singular/plural și «Exemplu informativ»/«Illustrative example»; calculul ideal neschimbat.

- V18 Canale de notificări: dulap cu trei sertare Mesaje (3), Comenzi (2), Oferte (4), numere fictive; nivel aprofundat: aplicație → două canale Comenzi/cu sunet și Oferte/fără sunet. Selector de nivel. Analogia nu implică existența unor sertare fizice. HTML RO/EN: butoane de nivel, trei sertare și două canale explorabile, evidențieri, explicații, reset la nivel simplu; SVG: șapte cadre per limbă.
- V19 Push: două diagrame telefon/server, verificări periodice cu săgeți spre server, push cu săgeată spre telefon și plic; conexiunea și traficul de întreținere sunt omise din schema simplificată. HTML RO/EN: două metode explorabile, evidențieri, explicații, reset; SVG: trei cadre per limbă. Legendă Exemplu informativ / Illustrative example.

- V20 Actualizări de securitate: două uși identice, încuietoare defectă coral înainte și reparată verde după; selector nivel, nivel aprofundat două telefoane Android 14 cu niveluri de securitate exemplificate prin 01.09.2025 și 01.10.2025, fără etichete de tip «Dată ilustrativă». Nu implică protecție absolută.
- V21 Google Play Services: trei aplicații de exemplu → serviciu comun → localizare, autentificare, alte funcții; schema nu implică acces automat la toate funcțiile sau partajarea datelor între aplicații.

- V22 Dimensiunea textului: mesaj identic, cursor 100–200% în pași de 25%, textul se reașază, buton reset. Model liniar ilustrativ, nu reproducere a scalării Android.
- V23 Mărirea ecranului: peisaj vectorial cu indicator PARC 100 metri, cursor 1–3× în pași de 0,5; decupare centrală, suprafață vizibilă 100/z² procente. Imaginea originală rămâne neschimbată.

- V24 Animații: selector 0,5×/1×/2×, durată de bază 400 ms, încărcare ipotetică constantă 600 ms, bare separate; trei cadre de tranziție, nu benchmark și nu sumă obligatorie a duratelor.
- V25 Bootloader: patru etape conceptuale Buton de pornire → Componente de pornire → Bootloader → Android; evidențiere bootloader, ordine simplificată, fără instrucțiuni de deblocare.

- V26 RAM, stocare internă și cache: masa cu documente în lucru, dulap cu dosare și sertar cu obiecte la îndemână; cache-ul poate exista în RAM, stocare sau procesor. Analogie conceptuală, nu trei memorii fizice obligatoriu separate.

- V27 DNS: două rânduri comparative, agenda Maria → 07xx xxx xxx și DNS site.example → 192.0.2.10. Exemple fictive, adresă rezervată documentației. DNS poate returna mai multe adrese și nu descarcă pagina sau garantează legitimitatea. HTML RO/EN: explorare agendă, nume de domeniu și adresă IP, evidențieri și reset. SVG: patru cadre per limbă. Revizie vizuală aprobată: titluri delimitate, săgeți accentuate și legendă «Exemplu informativ»/«Illustrative example».

- V28: analogie atelier, comenzi șofer vs instrumente mecanic; setări obișnuite vs diagnosticare.
- V29: selector încărcare / fișiere / depanare USB; energie vs transfer vs operații ADB autorizate. Funcțiile pot coexista.
- V30: blocarea ecranului vs blocarea bootloaderului, acces utilizator vs software la pornire.
- V31: cache vs alte date ale aplicației; copii reutilizabile vs preferințe și sesiuni; ștergerea tuturor datelor poate include cache.

- V32 Cicluri de încărcare: două jumătăți de sticlă consumate după reumplere, 50% + 50% = aproximativ un ciclu echivalent. Nu reprezintă numărul conectărilor la priză.

## Referință de aspect
Pagina lecției: antet verde închis, săgeată înapoi, titlu; două selectoare de nivel, activ verde, inactiv verde pal; explicație, schemă pe fond pal cu colțuri rotunjite, legendă, explicație și exemplu. Nu este o aprobare pentru modificarea navigației existente.
Implementare propusă: Compose Canvas, VectorDrawable, componente Compose, Slider și stare locală; descrieri accesibile și echivalente textuale. Verificare editorială și pe dispozitiv înainte de finalizare.

## Ordine
Finalizarea conținutului celor 55 de lecții; selectarea schemelor utile; componente reutilizabile; integrare RO/EN; legături între concepte; testare. Catalogul este extensibil. Nu se adaugă lecții noi în această etapă.

- V33: stocare 128 GB, 104 GB ocupați inițial, filmare selectabilă 0–24 GB, liber = 24 − filmare; reset la 4 GB.
- V34: cache, rată de găsire 0–100%, timp mediu = p × 10 + (1 − p) × 200 ms; reset la 80%. Model cu durate fixe, nu măsurătoare.

## Revizie V07–V09
V07 straturi conceptuale, selectoare 60/120/240 Hz atingere și 60/90/120 Hz afișaj, intervale 1000/f ms. V08 obiectiv, senzor, peisaj ilustrativ cu lumină și zgomot variabile, 100–400 fotoni, SNR sqrt(N) în model fotonic. V09 undă, membrană, eșantioane, selector 16/44,1/48 kHz, durată 1–10 min, PCM mono 16 biți. Reset pentru fiecare; fără acces hardware.

## V35–V37 și V03 revizuit
V35 APK: cutie, conținut dezvăluit la apăsare. V36 fundal: cronologie 60 min, 0–12 sincronizări de 5 s, reset. V37 sistem: două zone conceptuale, selector explicații, reset. V03: două telefoane, simulare 80+120 MB eliberați, 300 MB fotografii partajate rămase, reset. V11: selector microfon/cameră/locație, acord și solicitare distincte. Toate offline, fără acces real.

## V38 și V04 — reconciliere
- V38 Blocarea ecranului / Screen lock: telefon cu lacăt, notificare și șase pictograme, trei funcții posibile, selector blocat/deblocat și trei opțiuni notificări, explicație dinamică și reset.
- V04 Criptare / Encryption: trei panouri, document cu colț pliat, cheie și lacăt, rezultat cu patru rânduri demonstrative, selecție și explicații, comutare criptare/decriptare.
- HTML RO și SVG RO: detalii recuperate din conversație; validare vizuală și funcțională încă necesară. Traduceri EN pregătite pentru aceste două scheme; restul catalogului necesită reconciliere.

## V05/V06 — revizie bilingvă
V05: păstrează ambele desene istorice TLS și E2EE; selector tip, explorare expeditor/server/destinatar cu evidențiere, explicații și reset. TLS terminat la server intermediar în model; E2EE poate utiliza TLS. V06: lacăt vectorial, 4/6/8 cifre, 10^n combinații, n log2(10) biți, reset la 6. HTML și SVG RO/EN actualizate; versiunile istorice RO păstrate. Testarea browser și reconcilierea tuturor stărilor SVG rămân deschise.

## V39 / V40 — RO/EN
V39: două verificări, parolă și confirmare pe telefon, selecție individuală, evidențiere, bife și rezultat, reset. V40: senzor cu amprentă vectorială, model numeric, comparator, scor fix 78/100, prag 0–100 implicit 70, rezultat și reset. HTML și SVG RO/EN arhivate. SVG stare inițială; verificare browser necesară.

### V06 — completare grafică RO/EN
Grafic logaritmic cu 3 bare, evidențiere dinamică și raport față de 4 cifre; SVG include stările 4/6/8 și graficul aferent. Scriptul nou verificat sintactic, nu în browser.

### Stări alternative V38/V04/V39/V40
SVG RO/EN: V38 patru stări vizuale distincte; V04 șase combinații criptare/selecție; V39 opt combinații bife/selecție; V40 cincisprezece combinații praguri reprezentative 0/70/78/79/100 și selecție. Total 33 cadre per limbă, extrase din geometria HTML existentă. Pragurile intermediare rămân interactive în HTML. Validarea vizuală și funcțională în browser rămâne necesară.

### V05 — stări și controale RO/EN
Șase cadre SVG per limbă (TLS/E2EE × expeditor/server/destinatar), extrase fără redesenare. HTML: butoane segmentate cu selecție lime și aria-pressed, reset și explicații existente. Sintaxă JS verificată; browser nevalidat.

### Uniformizare controale securitate
V38/V04/V06/V39/V40: șase selectoare transformate în butoane segmentate RO/EN, aria-pressed, înălțime minimă 44px, culori lime/pale, reset suplimentar V04. Geometria SVG inline păstrată identic; sintaxa scripturilor modificate verificată. Validare browser și TalkBack neefectuată.

### Audit securitate RO/EN
Etichetele vizibile ale celor șase prototipuri curente comparate RO/EN. Test de execuție JS cu DOM simulat: 12 prototipuri, 42 butoane de selecție, reseturi, comutare criptare, rezultat V39 cu ambele verificări și V40 la prag 79; fără excepții, fără referințe literale e(id) lipsă. Testul nu verifică randarea, CSS, geometria sau toate rezultatele. Trei secțiuni istorice RO marcate explicit, fără eliminare. Browser real, TalkBack și Android rămân nevalidate.

## Remediere accesibilitate V39/V40 — 27.11.2026
HTML RO/EN: căsuțele V39 au lățime automată și etichete flex cu înălțime minimă 44 px; V39/V40 au mesaje de stare HTML role=status, actualizate numai când rezultatul se schimbă. SVG inline păstrat identic prin comparație textuală. Commit RO 55f3109465211cdf6069e6eb3819e2bcc27a19af; EN d59fc95bff6e2522041b61609f927f85b5210771. Browser real, TalkBack, fidelitate grafică și Android încă nevalidate. Categoria rămâne deschisă; 26/55 final textual, 40 concepte.

### V05 — uniformizare accesibilitate
RO/EN: cinci butoane de selecție cu înălțime minimă 44 px și împachetarea cuvintelor la nevoie; două grupuri cu nume accesibile specifice. Toate SVG inline comparate textual și păstrate identic. Browser Chromium disponibil local, dar descărcarea directă GitHub în mediul local a eșuat; randarea reală nu este confirmată. Categoria rămâne deschisă.

### Uniformizare etichete pe ecrane înguste
V38/V04/V06/V39/V40: 16 butoane per limbă cu overflow-wrap:anywhere, în concordanță cu V05. Audit sursă: toate cele opt grupuri de selecție per limbă au denumiri specifice traduse; SVG inline identic textual înainte/după. Nu constituie test de randare sau TalkBack. Categoria rămâne deschisă.

## Internet și conectivitate — V41–V43 RO/EN
- V41 Bluetooth: telefon cu melodie locală, legătură radio, căști; trei componente explorabile, evidențiere, explicație și reset; patru cadre SVG per limbă.
- V42 NFC: telefon cititor, câmp magnetic, etichetă pasivă cu antenă și cip; trei componente explorabile, evidențiere, explicație și reset; patru cadre SVG per limbă. Nu reprezintă o tranzacție reală.
- V43 Hotspot: stație mobilă, telefon cu Wi-Fi, laptop și tabletă; trei componente explorabile, evidențiere, explicație și reset; patru cadre SVG per limbă. Capacitatea partajată nu se multiplică.
Legendă RO «Exemplu informativ», EN «Illustrative example»; controale minimum 44 px, aria-pressed, role=status. Toate offline, fără permisiuni sau modificări reale. 43 concepte catalogate, 55 lecții neschimbate. Randarea și validarea funcțională necesare.

## Notificări — rafinare și V44
- V18: contur lime pentru sertarul selectat, contrast îmbunătățit față de dulapul verde închis.
- V19: titluri și texte centrale mai mari, etichete Telefon/Phone și Server pentru ambele fluxuri; geometria săgeților păstrată.
- V44 Notificare și sunet / Notification and sound: telefon cu memento calendar la 14:30 pentru programarea de la 15:00, trei stări Cu sunet/Fără sunet/Îndepărtată, pictograme sonore, evenimentul rămâne salvat în toate stările, reset la Cu sunet. HTML RO/EN, trei cadre SVG per limbă; legendă Exemplu informativ / Illustrative example. Nu emite sunet real, nu modifică notificările sau calendarul telefonului. 44 concepte, fără lecții noi.
