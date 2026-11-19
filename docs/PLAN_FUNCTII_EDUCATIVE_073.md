# SAFEGUARD — funcții educative pentru următoarea versiune (țintă 0.7.3)

Acest document este un plan de implementare, **nu** o declarație că funcțiile au fost livrate sau testate. Versiunea instalată/testată 0.7.2 nu este modificată prin acest document.

## Principii
- Arată numai date măsurate și acțiuni posibile în limitele Android.
- Explică în limbaj simplu, în română și engleză; nu eticheta drept „amenințare” o utilizare normală a resurselor.
- Nu cere permisiuni inutile, nu citește fișiere personale și nu șterge date automat.
- În interfață apar doar acțiunile implementate, fără butoane decorative pentru funcții inexistente.

## 1. Cache Advisor
- Explicație RO/EN: cache-ul păstrează date temporare pentru ca aplicațiile să pornească mai repede.
- Recomandare prudentă: ștergerea cache-ului poate elibera temporar spațiu, dar acesta se poate reface; nu este necesară în mod regulat.
- Buton numai pentru deschiderea setărilor Android ale aplicațiilor sau a unei aplicații selectate, cu confirmare explicită. Utilizatorul alege manual dacă șterge cache-ul.
- Nu pretinde că SAFEGUARD poate măsura cache-ul fiecărei aplicații sau îl poate șterge automat.

## 2. Definiții simple prin iconița ⓘ, RO/EN
- Pentru RAM, memorie disponibilă, cache, stocare, permisiuni, acces la notificări, patch de securitate și animații.
- Dialog cu explicație scurtă: „Ce este?”, „De ce contează?” și „Ce pot face?”.
- RO/EN trebuie să traducă întregul dialog și etichetele relevante, nu doar antetul.
- Limbaj accesibil, fără promisiuni de protecție antivirus.

## 3. RAM Monitor și sfaturi adaptate
- Folosește Android ActivityManager.MemoryInfo: totalMem, availMem, lowMemory; afișează ora ultimei citiri.
- Buton „Actualizează”; nu pretinde monitorizare continuă dacă nu există eșantionare.
- Calculează procentul disponibil = availMem / totalMem * 100 numai când totalMem > 0.
- Recomandări determinate de date:
  - Dacă lowMemory este true: „Android semnalează presiune asupra memoriei. Închide doar aplicațiile care nu răspund sau pe care nu le mai folosești; repornește telefonul dacă problemele persistă.”
  - Dacă lowMemory este false: „Android nu semnalează presiune critică. RAM-ul utilizat este normal; nu este necesară golirea lui.”
  - Dacă citirea e indisponibilă: „Nu putem evalua RAM-ul acum.”
- Procentul este orientativ, nu diagnostic și nu justifică singur închiderea aplicațiilor.
- Nu promite „RAM boost”, oprirea proceselor altor aplicații sau economii garantate de baterie.

## 4. Reducerea animațiilor Android
- Explică faptul că reducerea animațiilor poate face tranzițiile să pară mai rapide, fără să accelereze procesorul sau să crească RAM-ul.
- Ghid pas cu pas pentru Opțiuni pentru dezvoltatori, doar dacă utilizatorul dorește; recomandă păstrarea valorilor implicite dacă nu înțelege efectele.
- SAFEGUARD nu modifică automat scalele animațiilor; evită permisiuni privilegiate.
- Deschiderea unei pagini de setări trebuie să aibă fallback la Setările generale când acțiunea specifică nu este disponibilă.

## Verificări înainte de livrare
1. Build Gradle și teste; nu confunda compilarea CI cu testarea pe telefon.
2. Verifică toate acțiunile de deschidere a setărilor pe Samsung Galaxy A13 / Android 14.
3. Verifică RO/EN și fonturile la dimensiuni mari.
4. Confirmă că datele RAM se reîmprospătează la cerere și că sfaturile corespund indicatorului lowMemory.
5. Compară vizual cu designul aprobat și păstrează logo-urile originale.
6. Pentru instalarea peste versiunea existentă, verifică aceeași semnătură de debug; nu dezinstala versiunea funcțională.

## Stare la 19 noiembrie 2026
- Există deja un card RAM în MainActivity care citește totalMem, availMem și lowMemory, dar nu este încă un monitor complet cu actualizare și explicații adaptate.
- Există un dialog informativ RO/EN în antet, dar nu există încă explicații contextuale pentru fiecare termen.
- Cache Advisor și ghidul pentru animații sunt propuse, nu implementate.
- versionName rămâne 0.7.2 până la implementare, compilare și verificare.
