# SAFEGUARD — criterii de acceptare pentru actualizarea 0.7.0 → 0.7.1

Document de lucru. Nu presupune că o compilare reușită garantează compatibilitatea instalării sau acceptarea de către Google Play Protect.

## Stare confirmată

- Versiunea instalată pe telefon: **0.7.0**, funcțională; nu o dezinstalați.
- Versiunea din `main`: **0.7.1**, `versionCode=8`, `applicationId=com.safeguard.android`.
- Certificatul versiunii 0.7.0: `B0:58:08:CC:91:0E:75:24:42:6D:E7:28:51:9D:F7:46:28:A0:45:62:98:42:AA:11:FB:DC:62:2A:10:4C:94:86`.
- Proprietarul a verificat că fișierul local `%USERPROFILE%\.android\debug.keystore` are aceeași amprentă.
- APK-ul debug produs de GitHub Actions este semnat cu **alt certificat**; nu îl instalați peste 0.7.0.
- Avertismentul Play Protect este o problemă distinctă; nu dezactivați protecția pentru a forța instalarea.

## Checklist înainte de actualizare

- [ ] Arhiva originală a surselor 0.7.0 este recuperată, inspectată și salvată pe o ramură dedicată; nu confundați o ramură creată din `main` cu arhivarea originalului.
- [ ] Modificările din `main` pentru 0.7.1 sunt revizuite.
- [ ] Testele unitare și `assembleDebug` se încheie cu succes pentru commitul ales.
- [ ] APK-ul este construit **local**, pe calculatorul cu cheia originală, fără a transmite `debug.keystore` către servicii externe.
- [ ] `apksigner verify --verbose --print-certs` confirmă semnătura APK-ului și certificatul SHA-256 de mai sus.
- [ ] APK-ul rezultat are identificatorul `com.safeguard.android` și `versionCode > 7`.
- [ ] Permisiunile și serviciile declarate sunt revizuite; în special `QUERY_ALL_PACKAGES` și serviciul de acces la notificări.
- [ ] Se păstrează o copie de rezervă a datelor relevante înainte de testare, dacă aplicația oferă o metodă sigură.
- [ ] Se testează actualizarea normală, fără dezinstalarea 0.7.0 și fără dezactivarea Play Protect.
- [ ] După actualizare se verifică pornirea aplicației, ecranele principale, permisiunile și funcțiile existente.

## Distribuție ulterioară

Cheia Android debug este o soluție temporară pentru testarea actualizărilor existente. Pentru distribuție publică trebuie stabilită o politică de semnare și migrare separată, fără a promite că schimbarea certificatului va permite actualizări directe.

Instrucțiuni: [Semnarea locală](SEMNARE_LOCALA_070_LA_071.md).
