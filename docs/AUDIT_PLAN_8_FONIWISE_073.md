# Audit inițial — planul celor 8 vs FoniWise 0.7.3

Data: 20 noiembrie 2026. Ramură analizată: `test/foniwise-073-integration`.

**Sursă de adevăr:** `docs/PLAN_ORIGINAL_8_FUNCTIONALITATI.md`. Audit static al `MainActivity.kt` și `AndroidManifest.xml`; **nu** reprezintă testare pe Samsung. „Parțial” nu înseamnă „gata”.

| # | Direcție | Stare constatată în cod | Ce lipsește pentru a respecta planul |
|---|---|---|---|
| 1 | Asistent de securitate pas cu pas | **Parțial**: scanare locală cu consimțământ, aplicații vizibile, permisiuni sensibile și acces special; linkuri către setările relevante | Flux ghidat „pasul 1/3”, verificări explicite pentru blocarea ecranului și actualizări, distingerea clară între setări citite automat și verificări manuale |
| 2 | Ghid pentru permisiuni | **Parțial**: lista permisiunilor acordate pentru aplicațiile vizibile, explicații generale și deschiderea detaliilor aplicației | Explicații didactice distincte pentru cameră, microfon, locație, contacte și notificări; exemple contextualizate |
| 3 | Asistent stocare | **Parțial avansat**: citire StatFs, validare total/disponibil, buton de reîmprospătare, explicații Cache Advisor, setări aplicații/stocare | Verificare UX și navigare pe Samsung; eventual afișarea explicită a spațiului liber și recomandări în funcție de praguri |
| 4 | Consilier baterie | **Parțial**: nivel real din ACTION_BATTERY_CHANGED, validare, actualizare manuală, scurtătură către consumul bateriei | Ghid explicit pentru economisirea energiei și identificarea aplicațiilor consumatoare, fără a pretinde măsurarea uzurii |
| 5 | Antifraudă / antiphishing | **Neidentificat în ecranul principal auditat** | Secțiune educativă dedicată, exemple de fraudă și checklist; fără acces la mesaje |
| 6 | Android și patch de securitate | **Neidentificat în ecranul principal auditat** | Afișarea versiunii Android și Build.VERSION.SECURITY_PATCH, explicații neutre și scurtătură către actualizări, cu fallback la Setări |
| 7 | Mod simplificat | **Neimplementat ca mod distinct în ecranul auditat**: există RO/EN, dialoguri ⓘ și design cu carduri | Comutator Simplu/Detaliat, fonturi și pași adaptați, persistența preferinței, verificări accesibilitate |
| 8 | Raport sănătate telefon | **Parțial, dar nu echivalent**: raport de scanare și istoric local al surselor notificărilor | Rezumat transversal „în regulă / atenție / verifică”, criterii transparente, istoric local opțional al recomandărilor; nu confunda istoricul notificărilor cu istoricul sănătății |

## Ordine propusă pentru implementare incrementală

1. **#6** Android + patch de securitate: citiri Android standard, explicații RO/EN, scurtătură sigură către actualizări.
2. **#4** Consilier baterie: completarea ghidului de economisire și consum, păstrând citirile existente.
3. **#2** Permisiuni: mini-dicționar cu exemple și navigare spre setări.
4. **#5** Antifraudă: checklist educativ local, fără citire de mesaje.
5. **#1** Asistent securitate: flux ghidat cu stări „verificat / necesită verificare manuală”.
6. **#7** Simplu/Detaliat: proiectare și teste de accesibilitate, fără schimbarea brandului.
7. **#8** Raport de sănătate: doar după ce sursele de date și limitele celorlalte carduri sunt clare.
8. **#3** Stocare: testare pe Samsung și rafinarea pragurilor/explicațiilor în paralel.

## Condiții de siguranță

- Fără modificări automate ale setărilor sau ștergere de date.
- Fără verdict global „telefon sigur/nesigur” dintr-un singur indicator.
- Fără permisiuni suplimentare nejustificate.
- Păstrăm logo-ul FoniWise original aprobat, semnarea aplicației și datele instalării existente.
- Orice schimbare de cod se verifică prin CI și apoi pe Samsung; build verde ≠ testare pe telefon.
- Ramura `main` rămâne neatinsă; raportul și schimbările experimentale stau în PR #20.
