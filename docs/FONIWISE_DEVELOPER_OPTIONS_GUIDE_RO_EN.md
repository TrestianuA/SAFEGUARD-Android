# FoniWise — Opțiuni pentru dezvoltatori explicate (RO / EN)

**Decizie aprobată:** introducem în dicționarul FoniWise o categorie educativă separată: **„Opțiuni pentru dezvoltatori — explicate pe înțelesul tuturor”** / **“Developer options explained”**. Obiectiv editorial: 25–40 de setări, explicate simplu, cu recomandări, riscuri și instrucțiuni de revenire. Acesta este un **plan de conținut aprobat**, nu o funcție Android deja implementată.

## Principii obligatorii

- Nu modificați automat setările de dezvoltator, nu solicitați acces privilegiat și nu promiteți „boost” de RAM, CPU sau baterie.
- Pentru fiecare opțiune: **Ce face? / What does it do?**, **De ce contează? / Why does it matter?**, **Merită modificată? / Is it worth changing?**, **Recomandare / Recommendation**, **Cum revii? / How to revert?**, **Atenționări / Cautions**.
- Clasificare **orientativă**, nu garanție de siguranță: **Risc redus / Low risk**, **Atenție / Caution**, **Avansat / Advanced**.
- Răspunsuri pentru „Merită modificată?”: **Da, opțional / Optionally**, **Doar pentru diagnostic / For diagnostics only**, **Păstrează implicit / Keep default**.
- Meniurile și efectele diferă între producători și versiuni Android. Recomandă notarea valorii inițiale înainte de schimbare. Dezactivarea comutatorului principal „Opțiuni pentru dezvoltatori” **nu garantează** restaurarea tuturor valorilor modificate.
- Nu confundați accelerarea animațiilor cu creșterea reală a performanței. Nu promiteți reduceri de expunere radio, optimizări sau beneficii fără date.

## Primele opțiuni și recomandări RO

| Opțiune | Nivel orientativ | Merită modificată? | Explicație și recomandare | Revenire |
|---|---|---|---|---|
| Scara animațiilor ferestrelor (Window animation scale) | Risc redus | Da, opțional | 0,5x poate face interfața să pară mai rapidă, fără a accelera procesorul | Notează valoarea inițială; revino de obicei la 1x |
| Scara animațiilor de tranziție (Transition animation scale) | Risc redus | Da, opțional | Ajustează viteza tranzițiilor între ecrane; testează 0,5x doar dacă îți place | Restabilește valoarea inițială |
| Scara duratei animatorului (Animator duration scale) | Risc redus | Da, opțional | Schimbă durata anumitor animații din aplicații; reducerea poate afecta confortul vizual | Restabilește valoarea inițială |
| Afișare rată de reîmprospătare (Show refresh rate) | Risc redus | Doar pentru diagnostic | Arată rata de reîmprospătare pe dispozitive compatibile; nu optimizează ecranul | Dezactivează indicatorul după verificare |
| Afișare atingeri (Show taps) | Risc redus | Doar pentru demonstrații | Afișează pozițiile atingerilor; utilă pentru tutoriale | Dezactivează după utilizare |
| Locația indicatorului (Pointer location) | Atenție | Doar pentru diagnostic | Suprapune informații tehnice despre gesturi; poate aglomera ecranul | Dezactivează după test |
| Servicii în execuție (Running services) | Atenție | Doar pentru diagnostic | Afișează anumite servicii și memoria lor, dacă meniul este disponibil | Nu opri la întâmplare servicii de sistem |
| Forțare MSAA 4x (Force 4x MSAA) | Atenție | Păstrează implicit | Poate influența grafica unor aplicații OpenGL ES și poate crește consumul | Păstrează dezactivat pentru utilizarea obișnuită |
| Nu păstra activitățile (Don't keep activities) | Avansat | Păstrează implicit | Închide activități când sunt părăsite; este destinată testării aplicațiilor | Las-o dezactivată |
| Limită procese în fundal (Background process limit) | Avansat | Păstrează implicit | Restricțiile artificiale pot cauza reîncărcări și comportamente neașteptate | Alege limita standard |
| Depanare USB (USB debugging) | Avansat | Doar când este necesar | Permite operații ADB de pe calculatoare autorizate | Dezactivează după utilizare și revocă autorizările necunoscute |
| Depanare wireless (Wireless debugging) | Avansat | Doar când este necesar | Permite depanarea prin rețea pe versiuni compatibile | Dezactivează după utilizare; folosește rețele de încredere |
| Deblocare OEM (OEM unlocking) | Avansat | Păstrează implicit | Nu este optimizare; deblocarea efectivă a bootloaderului poate șterge date și schimba protecții | Nu modifica fără un scop tehnic bine înțeles |

## Exemplu de articol bilingv: scara animațiilor

### RO — Scara animațiilor
**Ce face?** Modifică durata anumitor animații ale interfeței.

**De ce contează?** La 0,5x, tranzițiile pot părea mai rapide; performanța hardware nu crește.

**Merită modificată?** Da, opțional, dacă preferi animații mai scurte.

**Recomandare:** Notează valoarea inițială, testează 0,5x și vezi dacă îți place rezultatul.

**Cum revii?** Intră din nou în Opțiuni pentru dezvoltatori și restabilește valorile inițiale, adesea 1x.

**Atenționări:** Opțiunile diferă între modele; dacă animațiile devin incomode, revino.

### EN — Animation scales
**What does it do?** Changes the duration of certain interface animations.

**Why does it matter?** At 0.5x, transitions may feel faster; hardware performance does not increase.

**Is it worth changing?** Optionally, if you prefer shorter animations.

**Recommendation:** Record the original value, try 0.5x, and see whether you prefer it.

**How to revert?** Return to Developer options and restore the original values, often 1x.

**Cautions:** Options vary by device; revert if animations feel uncomfortable.

## Plan de extindere până la 25–40 de opțiuni

Documentați și verificați pe versiuni Android: afișare limite aspect (Show layout bounds), profilare randare GPU, suprapuneri hardware (Disable HW overlays), simulare spațiu de culoare, afișare actualizări de suprafață, selectare configurare USB implicită, raport de erori (Bug report), așteptare depanator (Wait for debugger), aplicație de depanare, verificare aplicații prin USB, monitorizare strict mode, afișare utilizare CPU (unde există), dimensiune buffer jurnal, setări codec Bluetooth, volum absolut Bluetooth, simulare afișaj secundar, lățime minimă (Smallest width), forțare aspect RTL, forțare redimensionare activități, comportament multi-window și preferințe drivere grafice.

**Nu atribuiți dinainte nivel de risc sau recomandări detaliate acestor opțiuni fără verificare.** Unele lipsesc ori au fost redenumite în versiunile recente Android.

## Integrare FoniWise

- Categorie separată în dicționar, cu căutare și explicații RO / EN.
- Carduri pastel verzi, etichete de risc orientative, pași de revenire și explicația „Merită modificată?”.
- Legătură către setările Android numai când există o destinație sigură și compatibilă; nu modificați automat nicio setare.
- Păstrați logo-ul original, `applicationId`, semnarea, permisiunile și funcțiile existente.
- Verificați fiecare recomandare în documentația oficială Android înainte de publicare și testați pe dispozitive reale.

**Stadiu:** document de cerințe și conținut inițial salvat; implementarea în aplicație și validarea CI urmează separat.
