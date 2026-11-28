# FoniWise — plan de testare manuală Samsung (RO/EN)

**Ramură:** `test/foniwise-073-integration`  
**Stare:** checklist pregătit pe baza structurii oficiale și a auditului UX; **niciun test de mai jos nu este declarat efectuat**. Nu este raport de certificare.

## Pregătire
- Instalează doar un APK rezultat dintr-o compilare reușită a commitului ales; notează SHA-ul, versiunea Android și modelul Samsung.
- Păstrează aplicația funcțională existentă și datele ei; nu dezinstala sau șterge date pentru a testa.
- Testează cu limba română, apoi engleză; repetă verificările vizuale cu font Android mărit și pe ecran îngust.
- Notează pentru fiecare scenariu: **Trecut / Eșuat / Netestat**, pașii, comportamentul observat și captură (fără date personale).

## 1. Telefonul meu — informații raportate de Android
- [ ] La prima deschidere, alegerea limbii funcționează; bara de jos are trei zone: „Telefonul meu”, „Învață despre telefon”, „Recomandări și acțiuni”.
- [ ] Android/patch: versiunea și data sunt afișate fără a declara telefonul sigur/nesigur. „Verifică actualizările în setările Android” deschide Setări; revenirea în FoniWise funcționează. Caută manual „Actualizare software” dacă ecranul dedicat nu apare.
- [ ] RAM: totalul și disponibilul sunt afișate în **GiB**; „Actualizează RAM” recitește valoarea. Textul explicativ nu repetă verdictul; ⓘ clarifică faptul că FoniWise nu eliberează RAM automat.
- [ ] Baterie: procentul afișat este plauzibil comparativ cu Android; ghidul distinge încărcarea de uzură. „Actualizează bateria” și „Setările bateriei” funcționează sau se deschid Setările generale prin fallback.
- [ ] Stocare: utilizat/total în **GiB**; „Actualizează stocarea” funcționează. Ghidul cache se deschide; avertismentul „Șterge datele” este vizibil; nicio ștergere automată.
- [ ] Scanare: necesită consimțământ; rezultatele nu sunt prezentate ca antivirus. Legătura către ghidul permisiunilor deschide zona „Învață despre telefon”.
- [ ] Inspector notificări: accesul este opțional; fără permisiune nu se afirmă că notificările au fost citite.

## 2. Învață despre telefon — explicații într-un singur loc
- [ ] Ghidul permisiunilor se deschide atât din zona educativă, cât și din scanare/recomandări, fără copii diferite ale articolului.
- [ ] Ghidul antifraudă explică exemple de mesaje, linkuri, apeluri și coduri; nu pretinde acces la mesaje private.
- [ ] Opțiuni pentru dezvoltatori: 5 categorii, 28 subiecte, navigare categorie → subiect → explicație; căutare fără diacritice, ștergere căutare, anterior/următor, revenire.
- [ ] Instrucțiunile „Cum verific pe telefon” pot fi extinse/ascunse; FoniWise nu modifică automat opțiunile Android.
- [ ] În RO și EN, titlurile, exemplele, limitele și butoanele sunt inteligibile; nu există selector „Simplu/Detaliat”.

## 3. Recomandări și acțiuni — acțiuni manuale
- [ ] Asistentul de securitate are trei pași și progres coerent; Înainte/Înapoi nu pierd orientarea.
- [ ] Fiecare scurtătură deschide pagina Android potrivită sau Setările generale; FoniWise nu afirmă că utilizatorul a modificat setarea.
- [ ] Ghidul de cache avertizează asupra diferenței dintre „Șterge memoria cache” și „Șterge datele”.
- [ ] Revenirea din Setări păstrează o navigare utilizabilă, fără a pretinde că o verificare manuală a fost confirmată automat.

## Verificări transversale
- [ ] Navigare între cele trei secțiuni, revenire din scanare/inspector/dicționar și închidere dialoguri.
- [ ] Font Android mărit: niciun text esențial sau buton inaccesibil/tăiat; derularea rămâne posibilă.
- [ ] Schimbare RO ↔ EN din antet și din dialogurile care o permit; limba rămâne consecventă după închiderea dialogului și repornirea aplicației.
- [ ] Fără permisiuni noi neexplicate, fără acces la mesaje personale, fără curățare automată sau afirmații de protecție garantată.

## Raportul testului
| Scenariu | Rezultat | Observații / pași de reproducere |
|---|---|---|
| Telefonul meu | Netestat | |
| Învață despre telefon | Netestat | |
| Recomandări și acțiuni | Netestat | |
| RO/EN, font mărit, revenire | Netestat | |

**Regulă de lansare:** compilarea CI este necesară, dar nu înlocuiește verificarea efectivă pe Samsung. Nu se face merge în `main` fără acord explicit.
