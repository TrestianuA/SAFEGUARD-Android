# Verificare Cache Advisor — 2026-11-20

## Modificare
În `MainActivity.kt`, recomandările Cache Advisor RO/EN explică acum că aplicațiile pot recrea fișierele temporare și că unele resurse pot fi descărcate din nou, eventual folosind date mobile. Nu s-a introdus curățare automată.

## Verificare efectuată
- Inspecție statică a implementării din ramura `test/foniwise-073-integration`.
- Confirmat că dialogul Cache Advisor folosește `Settings.ACTION_APPLICATION_SETTINGS` pentru ghidarea manuală.
- Confirmat că avertismentul distinge între «Șterge cache» și «Șterge datele / Golire stocare».
- Confirmat că `onLanguageChange` folosește `chooseLanguage`.
- Modificare exclusiv textuală RO/EN; fără modificări ale manifestului, permisiunilor, logo-urilor, culorilor sau configurării semnării.

## Teste rămase
- Așteaptă rezultatul GitHub Actions pentru commitul de cod `8a720997eb7ab4d6bf9cef34cbb0ffed80b1b5e4`; verificarea statică nu este echivalentă cu o compilare.
- Pe Samsung: deschide Cache Advisor, verifică textul RO și EN, deschide Setări aplicații și revino fără a șterge date.
- Verifică afișarea textului cu font mare și pe ecran îngust.

## Limite
Nu s-a testat pe dispozitiv fizic; nu s-a efectuat nicio curățare de cache. Datele mobile pot fi consumate numai dacă aplicația redescărcă resurse prin conexiunea mobilă, în funcție de comportamentul ei.
