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

- V12 Inginerie socială: antet verde închis, nod Phishing verde, ramuri spre Smishing/SMS, Vishing/apel vocal, Quishing/cod QR, fundal verde pal.
- V13 Identitatea paginii: două machete identice, adrese fictive serviciu.example și imitatie.example, lacăt HTTPS la ambele, câmpuri nefuncționale. Aspectul și criptarea transportului nu dovedesc legitimitatea.
- V14 Detector deepfake: două grile de câte 100 celule, 5 alarme false (coral) și 90 sintetice detectate (verde) implicit. Două cursoare independente 0–100; nedetectate = 100 minus detectate. Nu este detector real.

- V15 Wi-Fi vs internet: telefon → router → internet; comutator conexiune din amonte, implicit activ; Wi-Fi rămâne activ, legătura internet devine coral și întreruptă când comutatorul este oprit.
- V16 GPS: patru sateliți, linii punctate către receptor, elipsă de incertitudine; poziție și eroare ceas, fără transmitere către sateliți.
- V17 Date mobile: cursoare 1–20 Mb/s (implicit 5), 1–60 minute (implicit 10); volum MB = debit × minute × 60 / 8; bară raportată la maximul simulării 9000 MB. Model ideal, nu măsurătoare.

- V18 Canale de notificări: dulap cu trei sertare Mesaje (3), Comenzi (2), Oferte (4), numere fictive; nivel aprofundat: aplicație → două canale Comenzi/cu sunet și Oferte/fără sunet. Selector de nivel. Analogia nu implică existența unor sertare fizice.
- V19 Push: două diagrame telefon/server, verificări periodice cu săgeți spre server, push cu săgeată spre telefon și plic; conexiunea și traficul de întreținere sunt omise din schema simplificată.

- V20 Actualizări de securitate: două uși identice, încuietoare defectă coral înainte și reparată verde după; selector nivel, nivel aprofundat două telefoane Android 14 cu niveluri de securitate exemplificate prin 01.09.2025 și 01.10.2025, fără etichete de tip «Dată ilustrativă». Nu implică protecție absolută.
- V21 Google Play Services: trei aplicații de exemplu → serviciu comun → localizare, autentificare, alte funcții; schema nu implică acces automat la toate funcțiile sau partajarea datelor între aplicații.

- V22 Dimensiunea textului: mesaj identic, cursor 100–200% în pași de 25%, textul se reașază, buton reset. Model liniar ilustrativ, nu reproducere a scalării Android.
- V23 Mărirea ecranului: peisaj vectorial cu indicator PARC 100 metri, cursor 1–3× în pași de 0,5; decupare centrală, suprafață vizibilă 100/z² procente. Imaginea originală rămâne neschimbată.

- V24 Animații: selector 0,5×/1×/2×, durată de bază 400 ms, încărcare ipotetică constantă 600 ms, bare separate; trei cadre de tranziție, nu benchmark și nu sumă obligatorie a duratelor.
- V25 Bootloader: patru etape conceptuale Buton de pornire → Componente de pornire → Bootloader → Android; evidențiere bootloader, ordine simplificată, fără instrucțiuni de deblocare.

- V26 RAM, stocare internă și cache: masa cu documente în lucru, dulap cu dosare și sertar cu obiecte la îndemână; cache-ul poate exista în RAM, stocare sau procesor. Analogie conceptuală, nu trei memorii fizice obligatoriu separate.

- V27 DNS: două rânduri comparative, agenda Maria → 07xx xxx xxx și DNS site.example → 192.0.2.10. Exemple fictive, adresă rezervată documentației. DNS poate returna mai multe adrese și nu descarcă pagina sau garantează legitimitatea.

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
