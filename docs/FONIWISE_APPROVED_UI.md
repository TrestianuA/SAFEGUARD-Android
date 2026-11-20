# FoniWise — interfața vizuală aprobată

**Status:** direcție vizuală aprobată explicit de proprietarul proiectului, 20 noiembrie 2026. **Referință pentru implementare**, nu implementare în Android.

## Imagini aprobate

Copiile PNG sunt păstrate în biblioteca FoniWise, în `/FoniWise/Branding/`:

- `FoniWise_UI_Ecran1_Aprobat.png`: ultima previzualizare a ecranului de încărcare, cu antet verde închis, bandă verde jos, logo central și textul „Se încarcă...”.
- `FoniWise_UI_Ecrane_2_3_4_Aprobate.png`: imaginea cu patru telefoane din iterația precedentă; **numai ecranele 2, 3 și 4** din această imagine sunt aprobate. **Ecranul 1 din această imagine este înlocuit** de referința separată de mai sus.
- Logo-ul original oficial: `FoniWise_logo_aprobat.png`.
- Pictograma aprobată: varianta **cu colțuri rotunjite** (documentată în `docs/FONIWISE_APPROVED_LOGO.md`).

**Important:** fișierele PNG ale mockupurilor sunt salvate în biblioteca proiectului, nu în acest PR GitHub. Nu recreați logo-ul oficial pornind de la imaginile generate de concept; folosiți originalul aprobat.

## Reguli vizuale obligatorii

- Identitate: **FoniWise — Know Your Phone**.
- Fundaluri albe sau verde foarte deschis în zonele cu informații; verde închis pentru antete și navigație.
- Carduri rotunjite, verde pastel, cu iconografie verde și text cu contrast ridicat.
- **Fără valuri decorative** în niciun ecran.
- Evitați atât suprafețele excesiv de albe, cât și un fundal verde intens pe toată pagina de conținut.
- Navigația și cardurile trebuie să rămână accesibile, lizibile și consecvente între ecrane.

## Ecrane

1. **Pornire / încărcare:** antet verde cu marca FoniWise, zonă centrală luminoasă cu logo mare, indicator „Se încarcă...”, bandă verde jos, fără valuri. Repetarea mărcii în antet și central a fost acceptată.
2. **Acasă:** antet verde cu marcă și slogan, mesaj „Bun venit!”, patru carduri principale (Performanță, Hardware, Sistem, Setări utile), navigație verde jos.
3. **Performanță:** antet verde drept, card de introducere verde deschis, secțiuni CPU, RAM și stocare, recomandare contextuală.
4. **Instrumente:** antet verde drept, carduri verde pastel pentru instrumentele disponibile, navigație verde jos. Instrumentele din mockup sunt **ilustrative**, nu o listă confirmată de funcții existente.

## Limite de implementare

- Valorile CPU, RAM și stocare din mockup sunt exemple, nu date reale.
- Nu introduceți afirmații despre curățare automată de cache, creștere forțată a RAM sau antivirus dacă funcționalitatea nu există.
- Păstrați funcțiile existente, `applicationId`, semnarea APK, compatibilitatea actualizărilor și versiunea existentă până la o modificare verificată separat.
- Implementați incremental, într-o ramură de test, cu build și teste CI. Verificați pe dispozitiv contrastul, fonturile, accesibilitatea și comportamentul pe ecrane mici.
- **Acest document nu schimbă interfața Android în cod și nu certifică un APK testat.**

## Explicații bilingve RO / EN — aprobat

**Status:** aprobat explicit de proprietarul proiectului, 20 noiembrie 2026.

**Referință vizuală:** `/FoniWise/Branding/FoniWise_UI_Explicatii_RO_EN_Aprobat.png` din biblioteca proiectului. Această imagine conține trei ecrane: pagina „Explicații”, dicționarul de termeni și dialogul de explicație pentru „Procesor (CPU)”.

- Păstrăm **comutatorul RO / EN din SAFEGUARD** în interiorul ferestrei de explicație; se afișează o singură limbă la un moment dat, iar selecția este evidențiată în verde.
- Comutarea schimbă **toate textele explicației**: titlu, descriere, „Ce face?” / „What does it do?”, „Ce NU face?” / „What doesn't it do?”, „Recomandare” / „Recommendation” și „Am înțeles” / „Got it”.
- Dialogul are fundal alb, colțuri rotunjite, pictogramă verde, buton de închidere și un fundal al aplicației estompat.
- Secțiunile „Explicații” și „Dicționar de termeni” respectă antetul verde închis, cardurile pastel, navigația verde și regula **fără valuri decorative**.
- Textele din mockup sunt exemple de design; înainte de implementare trebuie verificate față de comportamentul real al funcțiilor și limitele lor.
- Păstrăm originalul logo-ului și pictograma cu colțuri rotunjite; **nu** modificăm prin acest document codul Android, `applicationId`, semnarea sau funcțiile existente.

**Notă:** imaginea este salvată în biblioteca FoniWise, nu ca fișier binar în acest PR GitHub.
