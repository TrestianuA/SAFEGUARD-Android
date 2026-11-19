# SAFEGUARD — un beneficiu real pentru fiecare funcție

Stare: propunere de formulări RO/EN pentru explicațiile ⓘ, bazată pe codul actual din `MainActivity.kt` și pe `docs/PLAN_FUNCTII_INCEPATORI.md`. **Documentul nu implementează funcții noi.**

## A. Funcții existente sau acțiuni disponibile în 0.7.3-rc1

| Funcție | Beneficiu RO | Benefit EN | Limită importantă |
| --- | --- | --- | --- |
| Scanare locală a configurației | Înțelegi ce accesări și permisiuni merită revizuite. | Understand which access and permissions deserve review. | Nu este antivirus și nu garantează detectarea amenințărilor. |
| Inspector notificări | Vezi sursele și frecvența notificărilor înregistrate local. | See locally recorded notification sources and frequency. | Necesită acces opțional; nu colectează conținutul mesajelor în jurnalul implementat. |
| Gestionare aplicații | Ajungi mai ușor la setările aplicațiilor Android. | Reach Android app settings more easily. | Administrarea efectivă este manuală în Android. |
| Cache Advisor | Poți recupera temporar spațiu prin ștergerea manuală a cache-ului unei aplicații. | You may temporarily reclaim space by manually clearing an app's cache. | Nu șterge automat și nu măsoară cache-ul fiecărei aplicații. |
| Setări baterie | Găsești mai ușor opțiunile Android de gestionare a consumului. | Find Android's battery management options more easily. | Nu optimizează automat bateria. |
| RAM Monitor | Înțelegi când Android raportează presiune asupra memoriei. | Understand when Android reports memory pressure. | Nu eliberează forțat RAM. |
| Reducerea animațiilor | Navigarea poate părea mai rapidă și mai confortabilă. | Navigation may feel faster and more comfortable. | Nu accelerează CPU și nu eliberează RAM; schimbarea este manuală. |
| Stocare | Observi spațiul disponibil și poți decide când să organizezi fișierele. | See available storage and decide when to organize files. | Nu citește sau șterge fișiere personale. |
| Informații dispozitiv | Găsești într-un loc modelul, versiunea Android și patch-ul raportat. | Find the reported device model, Android version and security patch in one place. | Nu certifică singur securitatea telefonului. |
| Permisiuni și accesări speciale | Identifici ce aplicații merită verificate în Setări. | Identify apps worth reviewing in Settings. | O permisiune acordată nu înseamnă automat un pericol. |
| Rapoarte și activitate | Poți consulta rezultatele locale disponibile și istoricul limitat al notificărilor. | Review available local results and limited notification history. | Nu implică monitorizare permanentă sau sincronizare cloud. |
| Explicații ⓘ RO/EN | Înțelegi termenii tehnici înainte să iei o decizie. | Understand technical terms before making a decision. | Nu toate ecranele sunt traduse integral. |

## B. Cele opt funcții aprobate pentru începători — plan, nu implementare

Ordinea și denumirile sunt preluate din `docs/PLAN_FUNCTII_INCEPATORI.md`.

| Nr. | Funcție planificată | Beneficiu RO | Benefit EN |
| --- | --- | --- | --- |
| 1 | Asistent de securitate pas cu pas | Înțelegi ce setări să verifici și de ce, fără termeni complicați. | Understand which settings to review and why, without jargon. |
| 2 | Ghid pentru permisiunile aplicațiilor | Poți decide mai informat ce acces la cameră, microfon sau locație păstrezi. | Make better-informed choices about camera, microphone and location access. |
| 3 | Asistent pentru spațiul de stocare | Găsești pași siguri pentru a gestiona spațiul fără ștergeri automate. | Find safe steps to manage space without automatic deletion. |
| 4 | Consilier pentru baterie | Înțelegi opțiunile de economisire a energiei și când să le folosești. | Understand power-saving options and when to use them. |
| 5 | Ghid antifraudă și antiphishing — prioritate specială | Recunoști mai ușor semnele unei înșelătorii și știi cum să verifici o solicitare suspectă. | Better recognize scam warning signs and know how to verify suspicious requests. |
| 6 | Verificarea actualizărilor de securitate | Știi unde să verifici actualizările Android și de ce contează. | Know where to check Android updates and why they matter. |
| 7 | Mod simplificat pentru începători | Poți urma instrucțiuni clare, pas cu pas, cu text mai lizibil. | Follow clear step-by-step guidance with more readable text. |
| 8 | Raport de sănătate a telefonului | Primești un rezumat ușor de înțeles al lucrurilor care merită atenție. | Get an easy-to-understand summary of items worth attention. |

## Ordinea dezvoltării

1. Validează 0.7.3-rc1 pe dispozitiv și compatibilitatea semnăturii, fără a dezinstala versiunea funcțională.
2. Introdu treptat beneficiile în explicațiile ⓘ existente, cu verificare RO/EN, lizibilitate și build după fiecare modificare mică.
3. Pentru cele opt funcții: întâi structură RO/EN și mod simplificat (7), apoi ghidul antifraudă (5), apoi 1–2, apoi 6–3–4, la final raportul agregat (8).
4. Nu afișa ca funcțional niciun modul din plan înainte de implementare și testare. Fără promisiuni antivirus, detectare perfectă a fraudei, ștergeri automate, optimizare forțată RAM sau economii garantate.
