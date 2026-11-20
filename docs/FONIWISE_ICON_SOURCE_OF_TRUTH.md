# FoniWise — sursa corectă pentru pictograma Android

**Status:** sursă vizuală confirmată de utilizator; resurse pregătite local, încă neintegrate în APK.

## Sursa originală

Utilizatorul a reconfirmat explicit pictograma cu telefon verde zâmbitor, patru simboluri pe ecran, orbită verde și frunză verde. Nu se folosește nicio variantă redesenată ori extrasă aproximativ din mockupuri.

- Imagine furnizată de utilizator: `1000035036.png`, 512 × 512, RGBA.
- SHA-256: `58b6404d74aa5def2bbadd87f02ae922cfaf434240adcca6c5d4da345b365852`.
- Copia originalului în pachetul local `FoniWise_Logo_Original_Resurse_Android.zip` este identică la nivel de bytes.
- Pachetul conține: `foniwise_symbol_original_approved.png`, `ic_foniwise_foreground.png`, `ic_foniwise.xml`, `colors_foniwise_icon.xml`, previzualizare rotunjită și instrucțiuni.
- Acest pachet este disponibil ca atașament în conversația de lucru, **nu este inclus în repository sau în APK**.

## Reguli pentru integrare

1. Folosiți imaginea originală de mai sus pentru simbolul FoniWise, fără redesenare.
2. Android adaptive icon poate adăuga margini transparente și poate aplica masca de sistem; nu modificați elementele artistice.
3. Pentru ecranul de pornire, folosiți logo-ul complet separat, cu textul FoniWise / Know Your Phone, conform `docs/FONIWISE_APPROVED_UI.md`.
4. Nu reutilizați fișierele `FoniWise_icon_*neaprobat*` sau alte preview-uri respinse.
5. Înainte de a schimba resursele de icon, verificați referințele actuale din manifest și compatibilitatea Android; rulați CI și testați pe telefon.
6. Păstrați `applicationId`, semnarea, permisiunile și logica existentă.

**Acest document nu certifică integrarea fișierelor binare.**
