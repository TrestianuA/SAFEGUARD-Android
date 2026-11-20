# FoniWise — Aplicații uitate (propunere de funcționalitate)

**Status:** idee acceptată pentru păstrare în planul FoniWise; **neimplementată**. Această specificație nu afirmă că Android oferă toate datele necesare pe orice dispozitiv.

## Scop

Ajutăm utilizatorul să identifice aplicații instalate pe care le folosește rar sau deloc și să decidă informat dacă le păstrează. FoniWise **recomandă și explică**, nu dezinstalează automat.

## Experiență propusă

- Secțiune nouă în **Instrumente**, intitulată **Aplicații uitate**.
- Listă de aplicații candidate pentru revizuire, cu ultima utilizare **doar dacă informația este disponibilă și accesul a fost acordat**.
- Spațiu potențial recuperabil **doar dacă poate fi măsurat/estimat credibil**, cu explicație că datele, cache-ul și comportamentul dezinstalării pot influența rezultatul; altfel afișăm „Indisponibil”.
- Acțiuni orientative: **Păstrează**, **Ignoră / Amintește-mi mai târziu**, **Revizuiește**. Eventuala dezinstalare se inițiază numai prin fluxul explicit Android și cu confirmarea utilizatorului.
- Buton ⓘ cu explicații **RO / EN** în dialogul aprobat pentru FoniWise, inclusiv „Ce face?”, „Ce NU face?” și „Recomandare”.

## Mesaje responsabile

- Text preferat: **„Mai mult spațiu, mai puține aplicații de care să ai grijă.”**
- O aplicație instalată, dar nefolosită, **nu consumă neapărat RAM**. Nu promitem că dezinstalarea eliberează RAM, accelerează telefonul sau îmbunătățește bateria.
- Nu prezentăm aplicațiile rareori folosite drept periculoase; utilizatorul poate avea motive să le păstreze.
- Nu facem afirmații de tip antivirus, curățare automată de cache ori optimizare forțată a memoriei.

## Cercetare tehnică înainte de implementare

1. Evaluați disponibilitatea și limitele statisticilor de utilizare Android (`UsageStatsManager` și accesul acordat explicit în setările sistemului), inclusiv aplicațiile fără istoric vizibil.
2. Verificați limitările de vizibilitate a pachetelor și regulile Google Play; evitați permisiunile largi dacă nu sunt justificate.
3. Evaluați accesul la dimensiunea aplicațiilor și la datele asociate pe versiunile Android suportate; nu inventați estimări.
4. Stabiliți filtre de protecție pentru aplicațiile de sistem, esențiale, cu roluri speciale sau utilizate rar din motive legitime.
5. Prelucrați datele local, minimizați colectarea și explicați clar de ce este solicitat accesul.
6. Proiectați stările „fără permisiune”, „date indisponibile”, „nicio aplicație de revizuit” și „aplicație păstrată”.
7. Implementați incremental într-o ramură de test; rulați teste și CI înainte de integrare.

## Design

Respectați `docs/FONIWISE_APPROVED_UI.md`: antet verde închis, carduri verde pastel cu colțuri rotunjite, navigație verde, fără valuri decorative. Folosiți dialogul RO / EN aprobat, cu selecția activă verde.

**Nu modificați** `applicationId`, semnarea APK, funcțiile existente sau iconografia aprobată doar pentru a adăuga această specificație.
