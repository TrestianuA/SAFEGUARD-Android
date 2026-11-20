# Audit UX FoniWise — claritate, fără repetiții, cu explicații detaliate pentru toți

Data: 20 noiembrie 2026. Ramură: `test/foniwise-073-integration`.
**Tip:** audit static inițial al `app/src/main/java/com/safeguard/android/MainActivity.kt`, confruntat cu `docs/PLAN_ORIGINAL_8_FUNCTIONALITATI.md` și `docs/STRUCTURA_OFICIAL_3_SECTIUNI_8_FUNCTIONALITATI.md`. **Nu este test pe Samsung și nu certifică UX-ul final.**

## Principiu central
**O informație are un singur loc principal; în alte zone apare o trimitere contextuală, nu o copie a explicației.**

Denumirile oficiale aprobate:
1. **Telefonul meu** — informații reale despre dispozitiv.
2. **Învață despre telefon** — explicații complete, clare și cu exemple practice.
3. **Recomandări și acțiuni** — sfaturi vizibile și butoane către setările Android, unde este posibil.

## Constatări concrete în cod
| Prioritate | Constatare verificabilă | Risc UX | Acțiune |
|---|---|---|---|
| P0 | Bara de jos afișează încă `Telefonul / Învață / Acțiuni`, nu denumirile oficiale complete | Neconcordanță cu decizia aprobată | Păstrează titlurile complete în pagini; proiectează etichetele de navigație pentru ecran îngust **cu aprobare**, fără a redenumi unilateral |
| P0 | Ghidurile pentru permisiuni și antifraudă sunt accesate în `selectedTab == 2` (Acțiuni), iar `selectedTab == 1` afișează dicționarul | Educație dispersată, navigare neintuitivă | Intrare principală în „Învață despre telefon”; „Acțiuni” păstrează numai pasul concret/scurtătura |
| P0 | `DeviceHealthCards` prezintă informații și explicații ample în pagina principală | Derulare lungă, oboseală | Date reale + o propoziție scurtă; „De ce contează?” deschide explicația completă |
| P1 | `Dashboard` are acces rapid pentru scanare, notificări, baterie, aplicații, cache și rapoarte, iar alte carduri repetă unele teme | Aglomerație, dublarea intrărilor | O singură acțiune principală per subiect, restul acces contextual |
| P1 | Ghidurile pentru baterie, permisiuni, antifraudă și securitate sunt dialoguri cu paragrafe lungi | Citire greoaie pe telefon | Titluri scurte, pași numerotați vizual, avertismente separate; explicații detaliate la cerere |
| P1 | Asistentul „3 pași” prezintă toate cele trei etape în același dialog | Nu este flux progresiv real | Câte un pas pe ecran, Înainte/Înapoi, progres 1/3, fără verdict fals de verificare |
| DECIZIE | Utilizatorul a respins modul Simplu/Detaliat | Nu trebuie introdus un selector de nivel | Un singur set de explicații detaliate, clare, cu exemple practice, pentru toți utilizatorii |
| P1 | Scanarea și rapoartele apar în mai multe puncte de intrare | Posibilă confuzie între „scanare”, „raport de scanare” și „sănătatea telefonului” | Definire clară a scopului fiecărui raport; evită prezentarea unui raport de scanare ca raport complet de sănătate |
| P2 | Instrucțiunile de setări depind de producător/versiune Android | Utilizatorul poate să nu găsească meniul exact | Text „denumirile pot diferi”; deschidere sigură a setărilor, fără promisiune de deep-link garantat |
| P2 | Conținut RO/EN există în multe dialoguri | Posibile discrepanțe între limbi | Revizie de limbaj și teste RO/EN pentru fiecare flux |

## Reguli editoriale obligatorii
- **Un singur mod de prezentare:** explicații detaliate, complete și corecte, redactate în limbaj ușor de înțeles, cu exemple practice, limitele citirii Android și consecințele acțiunii.
- **Structură lizibilă:** „Ce arată?”, „Ce înseamnă?”, „Ce poți face?”, fără a duplica articolele între secțiuni. Nu există comutator Simplu/Detaliat.
- Utilizatorul experimentat poate accesa direct detaliile și setările, fără a parcurge lecții obligatorii.
- **Nu repetăm același paragraf** în „Telefonul meu”, „Învață despre telefon” și „Recomandări și acțiuni”.
- **Nu eliminăm avertismentele esențiale** doar pentru a scurta ecranul. Scurtăm formularea, nu adevărul.
- Nu etichetăm automat dispozitivul „sigur/nesigur”; nu inventăm date, nu pretindem acces la setări inaccesibile, nu citim mesaje personale.
- Contrast, font scalabil, butoane suficient de mari, fără text tăiat; verificare în română și engleză.

## Hartă unică de conținut pentru cele 8 funcții
| Funcție | Loc principal | Unde apare doar acțiunea/referința |
|---|---|---|
| #1 Securitate pas cu pas | Recomandări și acțiuni | Trimiteri contextuale din Telefonul meu / Învață |
| #2 Permisiuni | Învață despre telefon | Recomandări: „Verifică permisiunile” + Setări |
| #3 Stocare | Telefonul meu | Învață: explicație detaliată; Recomandări: Setări stocare |
| #4 Baterie | Telefonul meu | Învață: explicație detaliată; Recomandări: Setări baterie |
| #5 Antifraudă | Învață despre telefon | Recomandări: checklist scurt, fără articol duplicat |
| #6 Android / patch | Telefonul meu | Recomandări: verificare manuală actualizări |
| #7 Accesibilitatea explicațiilor (decizie actualizată) | Transversal, în toate trei | Explicații detaliate pentru toți, fără mod Simplu/Detaliat |
| #8 Raport sănătate | Telefonul meu | Recomandări: doar acțiunile rezultate |

## Exemple de text, orientative (nu afirmații despre date reale)
- **Baterie, afișare principală:** „Baterie: 68%. Vezi ce aplicații consumă energie.” (68% este exemplu, nu citire reală)
- **Baterie, explicație completă:** „Procentul arată încărcarea curentă, nu uzura bateriei. Android poate afișa consumul pe aplicații în Setări.”
- **Permisiuni, introducere:** „O aplicație îți cere acces la microfon? Verifică de ce are nevoie.”
- **Permisiuni, explicație completă:** „Accesul la microfon poate fi justificat pentru înregistrare sau apeluri; simpla cerere nu dovedește un pericol.”
- **Antifraudă, recomandare:** „Nu comunica nimănui codurile bancare primite prin SMS. Verifică cererea prin aplicația oficială.”
- **Actualizări:** „Patch de securitate: [valoare raportată de Android]. Verifică manual actualizările disponibile.” Nu declarăm telefonul nesigur numai pe baza datei.

## Ordine de lucru înainte de testul Samsung
1. Inventariere și eliminare a duplicărilor de conținut / mutarea ghidurilor educative în zona corectă.
2. Consolidarea cardurilor din „Telefonul meu”: date înaintea explicațiilor; detalii la cerere.
3. Revizuirea tuturor explicațiilor într-un singur stil detaliat și accesibil, cu exemple și pași clari; fără selector Simplu/Detaliat și fără schimbări de branding.
4. Corectarea fluxului real al asistentului în trei pași și verificarea navigației.
5. Testare de compilare + teste automate, apoi verificare pe Samsung prin Android Studio: ecran îngust, text mare, RO/EN, navigare, back, dialoguri, acces la setări, valori lipsă, accesibilitate.
6. Actualizarea auditului după fiecare schimbare; `main` rămâne neatins până la acord explicit.

## Stare la redactare
Auditul este **documentat**, nu toate remedierile sunt implementate. Ultima compilare verificată pentru commitul `0b5684f3ea71862d045f084bfd936f6dc60fca04`: GitHub Actions #161, succes. Testul Samsung: neefectuat.

## Verificare incrementală — 20 noiembrie 2026, 18:04 (Europe/Bucharest)

**Cod inspectat:** `app/src/main/java/com/safeguard/android/MainActivity.kt`, ramura `test/foniwise-073-integration`.
- Cache Advisor: ghid de curățare **manuală**, avertismente și intrări către setările Android; fără ștergere automată.
- RAM Monitor: citire `readDeviceMemory(context)`, verificare `hasValidRamReading`, recomandare diferențiată după `lowMemory`, actualizare manuală, fără oprire forțată de procese.
- Animații Android: ⓘ cu explicații RO/EN și trimitere către Accesibilitate; fără schimbări automate.
- **Defect UX corectat:** comutarea limbii din dialogurile ⓘ pentru RAM și animații modifica doar o variabilă locală; după închidere limba generală a aplicației rămânea neschimbată. Dialogurile folosesc acum `helpEnglish` și `onLanguageChange` ale ecranului, ca celelalte explicații. Nu s-a introdus mod Simplu/Detaliat.

**Verificare statică:** au fost confirmate patru înlocuiri exacte în sursă; nu au fost schimbate citirile RAM, comportamentul Android, logo-urile, semnarea APK sau permisiunile. **Compilarea CI pentru commitul `2b00ea4223f44389fff726bbdc19e1aaac6006f7` nu avea încă un rezultat la verificare.** Nu s-a efectuat testare pe Samsung.

**Test manual necesar:** schimbă limba din ⓘ RAM și verifică dacă ecranul și ⓘ animații folosesc aceeași limbă; repetă invers. Verifică RO/EN, închiderea dialogurilor și revenirea la ecranul principal. Următorul pas: CI și test de navigare pe Samsung, apoi reducerea textelor duplicate fără pierderea explicațiilor detaliate.
