# SAFEGUARD — Plan de funcții pentru utilizatori începători

Stare: idei aprobate pentru dezvoltare treptată; **nu sunt încă implementate prin acest document**.
Principii: explicații RO/EN accesibile, instrucțiuni pas cu pas, fără promisiuni false, fără permisiuni inutile, procesare locală unde este posibil, păstrarea designului aprobat. Testarea funcțiilor noi în Android Studio pe telefon înainte de a le declara validate.

## Cele 8 funcții aprobate

1. **Asistent de securitate pas cu pas** — verificări ale setărilor disponibile, explicații „ce înseamnă / de ce contează / ce pot face”, deschiderea setărilor Android relevante. Nu pretinde că detectează orice amenințare.
2. **Ghid pentru permisiunile aplicațiilor** — explică accesul la cameră, microfon, locație, contacte și notificări; îndrumă utilizatorul spre revizuirea manuală în Android.
3. **Asistent pentru spațiul de stocare** — arată spațiul disponibil, explică opțiuni de eliberare a spațiului, trimite la setările Android; fără ștergeri automate.
4. **Consilier pentru baterie** — explică utilizarea bateriei, economisirea energiei și setările relevante; fără afirmații nefondate despre sănătatea bateriei sau optimizare automată.
5. **Ghid antifraudă și antiphishing — PRIORITATE SPECIALĂ** — educație clară pentru persoane fără experiență despre SMS-uri, WhatsApp, e-mailuri, linkuri false, apeluri frauduloase, coduri OTP și solicitări bancare. Exemple ușor de înțeles, semnale de avertizare, listă de verificare înainte de a apăsa un link, pași de urmat dacă utilizatorul a interacționat deja cu o fraudă. Evităm citirea automată a mesajelor și colectarea datelor sensibile; nu promitem detectare perfectă.
6. **Verificarea actualizărilor de securitate** — afișează versiunea Android și nivelul patch-ului de securitate când API-ul permite; explică și deschide setările de actualizare. Nu declară automat nesigur un telefon doar din vechimea patch-ului.
7. **Mod simplificat pentru începători** — text mai lizibil, instrucțiuni „Pasul 1 din 3”, butoane clare; moduri Simplu/Detaliat, respectând identitatea vizuală SAFEGUARD.
8. **Raport de sănătate a telefonului** — rezumat ușor de înțeles cu ce este în regulă, ce necesită atenție și ce poate verifica utilizatorul; eventual istoric local, fără server implicit.

## Prioritizare propusă

- Etapa 0: validarea versiunii curente 0.7.3-rc1 prin Android Studio pe telefon; păstrarea instalării 0.7.2 până la verificarea semnăturii.
- Etapa 1: structură comună de explicații RO/EN și mod simplificat (funcția 7), pe care se bazează restul.
- Etapa 2: **Ghid antifraudă și antiphishing (funcția 5)**, ca modul educativ fără permisiuni noi.
- Etapa 3: asistentul de securitate (1) și ghidul permisiunilor (2).
- Etapa 4: actualizări Android (6), stocare (3) și baterie (4).
- Etapa 5: raportul agregat (8), după ce există date verificate din module.

Ordinea poate fi ajustată împreună cu utilizatorul. Fiecare etapă: implementare mică, build CI, verificare statică, apoi testare reală în Android Studio înainte de a considera funcția finalizată.

## Format recomandat pentru fiecare explicație

- Ce am observat?
- De ce contează?
- Ce pot face, pas cu pas?
- Ce nu poate face SAFEGUARD?
- Opțional: deschide setările Android relevante.

## Notă specială pentru antifraudă

Folosim limbaj calm, exemple cotidiene și avertismente clare: nu comunica parole, PIN-uri sau coduri de verificare; nu instala aplicații la cererea unui apelant necunoscut; verifică instituția printr-un canal oficial ales independent. Exemplele sunt educative, nu verdict automat că un anumit mesaj este fraudulos.
