# SAFEGUARD 0.7.1 — pregătit pentru testare

- Iconița ⓘ pentru RAM, baterie și stocare, cu explicații în română urmate de engleză.
- Dialogul se poate închide fără schimbarea permisiunilor sau a datelor.
- Indicatorul de stocare este etichetat ca **partiție de date Android**; `StatFs(Environment.getDataDirectory())` nu măsoară neapărat întreaga capacitate comercială Samsung.
- Nu au fost schimbate scanarea, istoricul notificărilor sau permisiunile.

## Verificare necesară

Deschide proiectul în Android Studio, așteaptă Gradle Sync, rulează `app` pe Samsung Galaxy A13. Apasă ⓘ în cele trei carduri, verifică ordinea română/engleză, închiderea dialogului și faptul că butoanele către Setări funcționează în continuare. Compară valorile stocării fără a le interpreta ca fiind aceeași capacitate. Versiunea 0.7.1 nu este încă compilată/testată pe telefon.
