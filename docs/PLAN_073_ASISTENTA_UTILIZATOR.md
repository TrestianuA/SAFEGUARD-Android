# SAFEGUARD 0.7.3 — asistență și explicații

Plan de implementare, nu dovadă că funcțiile sunt deja livrate.

## Reguli
- Funcții reale, locale și transparente. Nu promitem antivirus, curățare automată sau optimizare RAM forțată.
- Nu solicităm acces la fișiere personale pentru recomandări.
- Nu modificăm automat opțiunile de dezvoltator sau setările altor aplicații.
- Explicațiile sunt scurte, în română și engleză, și accesibile prin ⓘ.

## 1. Cache Advisor
- Explică diferența dintre cache și datele aplicației, avertizând că „Șterge datele” poate elimina configurări și autentificări.
- Oferă navigare către pagina de informații a aplicației selectate (ACTION_APPLICATION_DETAILS_SETTINGS), unde utilizatorul poate decide manual.
- Nu afirmă că poate măsura cache-ul fiecărei aplicații fără API/permisiuni potrivite; nu execută ștergerea cache-ului.

## 2. Dicționar ⓘ RO / EN
- Definiții clare pentru RAM, memorie disponibilă, cache, stocare, permisiuni, notificări și patch de securitate.
- Comutator RO / EN pentru explicații; traducerea completă a tuturor ecranelor este o sarcină separată.
- Testare pentru dimensiunea textului și cititoarele de ecran.

## 3. RAM Monitor
- Folosește ActivityManager.MemoryInfo pentru totalMem, availMem, lowMemory și threshold, cu etichetarea sursei și momentului măsurării.
- Actualizare numai cât ecranul este vizibil sau la cererea utilizatorului; fără monitorizare ascunsă continuă.
- Nu confundă RAM-ul utilizat de sistem pentru cache cu o problemă.

## 4. Recomandări RAM în funcție de măsurători
- Dacă lowMemory=true: explică presiunea raportată de Android și recomandă verificarea aplicațiilor solicitante și repornirea numai când este justificată.
- Dacă lowMemory=false: informează că situația nu indică presiune critică și descurajează închiderea forțată a aplicațiilor.
- Recomandările sunt informative, nu diagnostice și nu promit creșteri de performanță.

## 5. Animații Android
- Explică efectele setărilor pentru animații asupra senzației de viteză și accesibilității.
- Oferă pași manuali către setările Android/Opțiuni dezvoltator, fără să schimbe valorile automat.
- Avertizează că denumirile și accesul diferă după versiunea Android și producător.

## Criterii de acceptare
1. Fiecare card vizibil are acțiune reală sau explicație utilă.
2. Nicio acțiune nu șterge date și nu schimbă setări fără intervenția explicită a utilizatorului.
3. Toate afirmațiile din interfață corespund capabilităților implementate.
4. Compilarea și testarea pe dispozitiv se confirmă separat, după implementare.
