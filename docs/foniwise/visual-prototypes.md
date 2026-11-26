# FoniWise — Catalog de prototipuri vizuale

Status: referință pentru etapa vizuală; nu este cod Android implementat.
Identitate: verde închis #064D2B, verde #087C26, verde pal #E7F8DB, fundal #F3FBF2, accent #B5F52C, verde grafic #72B94C; Nunito Sans. Texte RO/EN, offline, fără permisiuni suplimentare, TalkBack, adaptare la Galaxy A13. Integrare în paginile lecțiilor, la nivelul potrivit, fără schimbarea primei pagini.

## Prototipuri păstrate
- V01 Baterie: două baterii verticale, nouă 100% și degradată; capacitate inițială 5000 mAh, cursor 40–100%, implicit 80%; capacitate rămasă = 5000 × procent / 100; pierdută = 5000 minus rămasă. Verde la >=80%, ocru #D6A32F la >=60%, coral #C96846 sub 60%. Simulare, nu măsurătoare.
- V02 Procesor: bare orizontale 10, 7, 5,5 secunde pentru 1, 2, 4 nuclee; segment secvențial 4 s verde închis, segment paralel 6/n verde grafic. Model ideal, nu benchmark.
- V03 Dezinstalare: două coloane înainte/după, 80 MB cod (#72B94C), 120 MB date private (#087C26), 300 MB fotografii partajate (#D1E6C5); după, primii 200 MB reprezentați prin contur punctat, fotografiile rămân. Exemplu ipotetic.
- V04 Criptare: document cu linii, săgeată, lacăt verde-lime pe fond verde închis, săgeată, text cifrat ilustrativ A9 F2 8C / 7D 3B E1 / C4 0A 6F / 2E 9B 5D. Etichete date originale / criptare / date criptate.
- V05 Comunicații: două diagrame cu telefoane la capete și server central. TLS: două legături criptate distincte, serverul poate citi mesajul. E2EE: conținut protejat între capete, serverul retransmite text cifrat. Nu sugera că toate metadatele sunt ascunse.
- V06 PIN: selector 4/6/8 cifre, implicit 6; căsuțe cu puncte, combinații 10^n, entropie ideală n × log2(10), două zecimale. Nu colectează PIN real, nu estimează timpul de compromitere.
- V07 Ecran tactil: straturi afișaj/digitizor, două contacte; 120 Hz tactil (8,3 ms) vs 60 Hz afișaj (16,7 ms), intervale distincte, nu latență totală fixă.
- V08 Cameră: obiectiv/senzor/ISP; comparație lumină suficientă vs redusă, zgomot și mișcare; exemplele fotografice din conversație sunt referințe conceptuale, nu active salvate.
- V09 Microfon: membrană MEMS, conversie în eșantioane; PCM mono 48 kHz × 16 biți = 96.000 octeți/s, 5,76 MB/minut.
- V10 Degradare: grafic conceptual descendent 100% spre 80%, fără a pretinde o curbă universală.
- V11 Permisiuni: aplicație → Android → componentă; diferențiază autorizare, utilizare efectivă și transfer de date.

## Referință de aspect
Pagina lecției: antet verde închis, săgeată înapoi, titlu; două selectoare de nivel, activ verde, inactiv verde pal; explicație, schemă pe fond pal cu colțuri rotunjite, legendă, explicație și exemplu. Nu este o aprobare pentru modificarea navigației existente.
Implementare propusă: Compose Canvas, VectorDrawable, componente Compose, Slider și stare locală; descrieri accesibile și echivalente textuale. Verificare editorială și pe dispozitiv înainte de finalizare.

## Ordine
Finalizarea conținutului celor 55 de lecții; selectarea schemelor utile; componente reutilizabile; integrare RO/EN; legături între concepte; testare. Catalogul este extensibil. Nu se adaugă lecții noi în această etapă.
