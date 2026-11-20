package com.safeguard.android

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

private data class GlossaryEntry(
    val titleRo: String, val titleEn: String,
    val whatRo: String, val whatEn: String,
    val whyRo: String, val whyEn: String,
    val tipRo: String, val tipEn: String,
    val cautionRo: String, val cautionEn: String
)

private val glossaryEntries = listOf(
    GlossaryEntry("Semnal slab", "Weak signal", "Semnalul mobil slab îngreunează legătura cu operatorul.", "A weak mobile signal makes it harder to communicate with your carrier.", "Telefonul poate crește puterea de transmisie și poate consuma mai multă energie.", "Your phone may increase transmit power and use more energy.", "În timpul apelurilor, poți folosi difuzorul sau căștile pentru a ține telefonul departe de cap.", "During calls, consider speakerphone or headphones to keep the phone farther from your head.", "Semnalul slab nu dovedește un pericol; aplicația nu măsoară expunerea radio.", "Weak signal does not prove danger; this app does not measure radio exposure."),
    GlossaryEntry("SAR", "SAR", "SAR descrie energia radio absorbită de țesuturi în teste standardizate, în W/kg.", "SAR describes radiofrequency energy absorbed by tissue in standardized tests, in W/kg.", "Este utilizat în evaluarea conformității dispozitivelor.", "It is used in device compliance testing.", "Consultă documentația oficială pentru modelul exact.", "Check official documentation for your exact model.", "Nu este o măsurare în timp real; FoniWise nu măsoară SAR.", "It is not a live reading; FoniWise does not measure SAR."),
    GlossaryEntry("RAM", "RAM", "RAM este memoria temporară folosită de Android și aplicații.", "RAM is temporary memory used by Android and apps.", "Android folosește RAM și pentru a deschide mai rapid aplicațiile.", "Android also uses RAM to reopen apps faster.", "Verifică întâi dacă există blocări reale înainte de a închide aplicații.", "Look for actual slowdowns before closing apps.", "RAM ocupat nu înseamnă automat telefon lent.", "Used RAM does not automatically mean a slow phone."),
    GlossaryEntry("Cache", "Cache", "Cache-ul păstrează fișiere temporare pentru acces mai rapid.", "Cache stores temporary files for faster access.", "Unele aplicații îl refac după ștergere.", "Some apps rebuild it after clearing.", "Șterge cache-ul unei aplicații doar când ai o problemă sau ai nevoie de spațiu.", "Clear an app's cache when troubleshooting or needing space.", "Ștergerea cache-ului nu este o optimizare obligatorie zilnică.", "Daily cache clearing is not required optimization."),
    GlossaryEntry("Șterge date", "Clear app data", "Această comandă poate reseta aplicația și elimina informații locale.", "This action can reset an app and remove local information.", "Este diferită de ștergerea cache-ului.", "It differs from clearing cache.", "Verifică backupul și contul înainte să confirmi.", "Check your backup and account before confirming.", "Nu presupune că toate datele pot fi recuperate.", "Do not assume all data can be recovered."),
    GlossaryEntry("Temperatura bateriei", "Battery temperature", "Bateria se poate încălzi în timpul încărcării sau utilizării intensive.", "A battery can warm up during charging or intensive use.", "Căldura excesivă poate accelera uzura.", "Excessive heat can accelerate wear.", "Nu acoperi telefonul la încărcare și evită soarele direct.", "Do not cover the phone while charging; avoid direct sunlight.", "FoniWise nu poate diagnostica defecte doar din temperatură.", "FoniWise cannot diagnose faults from temperature alone."),
    GlossaryEntry("Încărcare adaptivă", "Adaptive charging", "Unele telefoane ajustează finalizarea încărcării în funcție de rutină.", "Some phones adjust charging completion based on your routine.", "Poate reduce timpul petrecut la nivel maxim de încărcare.", "It can reduce time spent at full charge.", "Activeaz-o dacă este disponibilă și utilă pentru programul tău.", "Enable it if available and useful for your schedule.", "Nu toate telefoanele au aceeași funcție.", "Not all phones offer the same feature."),
    GlossaryEntry("Cicluri baterie", "Battery cycles", "Un ciclu este consumul cumulat echivalent cu 100% din capacitate.", "One cycle is cumulative use equal to 100% of battery capacity.", "Nu înseamnă neapărat o singură încărcare de la 0 la 100%.", "It does not necessarily mean one charge from 0 to 100%.", "Evită căldura excesivă și folosește funcțiile de protecție a bateriei.", "Avoid excess heat and use available battery protection features.", "Numărul de conectări la încărcător nu este numărul de cicluri.", "Plugging in is not the same as a full cycle."),
    GlossaryEntry("5G", "5G", "5G este o tehnologie de rețea mobilă; experiența depinde de acoperire.", "5G is a mobile network technology; experience depends on coverage.", "Viteza și consumul variază în funcție de condiții.", "Speed and power use vary with conditions.", "Compară modul automat cu alte setări doar dacă întâmpini probleme.", "Compare automatic mode with other settings only if you have issues.", "5G nu este întotdeauna mai rapid în orice loc.", "5G is not always faster everywhere."),
    GlossaryEntry("Apel Wi-Fi", "Wi-Fi calling", "Unele telefoane pot efectua apeluri printr-o conexiune Wi-Fi.", "Some phones can place calls over Wi-Fi.", "Poate ajuta în clădiri cu semnal mobil slab.", "It can help indoors with poor cellular reception.", "Verifică dacă operatorul și telefonul acceptă funcția.", "Check whether your carrier and phone support it.", "Disponibilitatea diferă între operatori și modele.", "Availability varies by carrier and device."),
    GlossaryEntry("NFC", "NFC", "NFC permite comunicarea la distanță foarte mică, inclusiv pentru plăți.", "NFC enables very short-range communication, including payments.", "Este folosit și la anumite accesorii și carduri.", "It is also used with some accessories and cards.", "Protejează ecranul cu PIN sau biometrie și verifică aplicațiile de plată.", "Use a screen lock and review your payment apps.", "NFC activat nu înseamnă automat o plată efectuată.", "Having NFC on does not mean a payment occurred."),
    GlossaryEntry("Permisiuni", "App permissions", "Permisiunile controlează accesul aplicațiilor la funcții și date.", "Permissions control app access to features and data.", "Unele permisiuni pot expune informații sensibile.", "Some permissions can expose sensitive information.", "Acordă doar accesul necesar și revizuiește-l periodic.", "Grant only necessary access and review it periodically.", "O permisiune solicitată nu dovedește singură că aplicația este periculoasă.", "A permission request alone does not prove an app is dangerous."),
    GlossaryEntry("Actualizări securitate", "Security updates", "Actualizările pot remedia vulnerabilități cunoscute.", "Updates can fix known vulnerabilities.", "Versiunea Android și nivelul patch-ului sunt informații diferite.", "Android version and security patch level are different.", "Instalează actualizările oficiale și păstrează backup pentru date importante.", "Install official updates and back up important data.", "O dată recentă a patch-ului nu garantează absența oricărui risc.", "A recent patch does not guarantee zero risk."),
    GlossaryEntry("Phishing", "Phishing", "Mesaje sau pagini false încearcă să obțină date sau bani.", "Deceptive messages or pages try to obtain information or money.", "Pot imita bănci, curieri sau servicii cunoscute.", "They can impersonate banks, delivery services, or familiar brands.", "Deschide separat aplicația sau site-ul oficial, nu linkul suspect.", "Open the official app or website separately, not the suspicious link.", "Lacătul HTTPS nu dovedește că site-ul este legitim.", "HTTPS alone does not prove a site is legitimate."),
    GlossaryEntry("Backup", "Backup", "Backupul este o copie a anumitor date, locală sau în cloud.", "A backup is a copy of selected data, locally or in the cloud.", "Nu toate aplicațiile salvează automat toate informațiile.", "Not every app automatically backs up all information.", "Verifică periodic ce se salvează și dacă restaurarea funcționează.", "Check what is backed up and whether restore works.", "Sincronizarea nu este întotdeauna echivalentă cu o copie recuperabilă.", "Sync is not always the same as a recoverable backup."),
)

@Composable
internal fun FoniWiseGlossary(english: Boolean, onBack: () -> Unit, onLanguageChange: (Boolean) -> Unit) {
    var query by remember { mutableStateOf("") }
    var expanded by remember { mutableStateOf<String?>(null) }
    val matching = remember(query, english) {
        glossaryEntries.filter {
            query.isBlank() || it.titleRo.contains(query, ignoreCase = true) ||
                it.titleEn.contains(query, ignoreCase = true) ||
                it.whatRo.contains(query, ignoreCase = true) ||
                it.whatEn.contains(query, ignoreCase = true)
        }
    }
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = onBack) { Text(if (english) "← Home" else "← Acasă") }
        Text(if (english) "Know Your Phone" else "Cunoaște-ți telefonul",
            style = MaterialTheme.typography.headlineSmall, color = Color(0xFF064D2B), fontWeight = FontWeight.Bold)
        Text(if (english) "FoniWise explanatory dictionary" else "Dicționarul explicativ FoniWise",
            style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = !english, onClick = { onLanguageChange(false) }, label = { Text("RO") })
            FilterChip(selected = english, onClick = { onLanguageChange(true) }, label = { Text("EN") })
        }
        OutlinedTextField(
            value = query, onValueChange = { query = it },
            label = { Text(if (english) "Search terms" else "Caută un termen") },
            singleLine = true, modifier = Modifier.fillMaxWidth()
        )
        Text(if (english) "${matching.size} terms · tap to read" else "${matching.size} termeni · apasă pentru explicații",
            style = MaterialTheme.typography.bodySmall)
        matching.forEach { entry ->
            val title = if (english) entry.titleEn else entry.titleRo
            Card(
                onClick = { expanded = if (expanded == entry.titleRo) null else entry.titleRo },
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F8DB)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(7.dp)) {
                    Text(title, fontWeight = FontWeight.Bold, color = Color(0xFF064D2B))
                    if (expanded == entry.titleRo) {
                        GlossarySection(if (english) "What is it?" else "Ce este?", if (english) entry.whatEn else entry.whatRo)
                        GlossarySection(if (english) "Why does it matter?" else "De ce contează?", if (english) entry.whyEn else entry.whyRo)
                        GlossarySection(if (english) "What can you do?" else "Ce poți face?", if (english) entry.tipEn else entry.tipRo)
                        GlossarySection(if (english) "What doesn't it mean?" else "Ce NU înseamnă?", if (english) entry.cautionEn else entry.cautionRo)
                    } else {
                        Text(if (english) entry.whatEn else entry.whatRo, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
        if (matching.isEmpty()) Text(if (english) "No matching terms." else "Nu am găsit termeni.")
        Text(if (english) "Educational information, not a medical or security diagnosis." else
            "Informații educative, nu diagnostic medical sau de securitate.",
            style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun GlossarySection(heading: String, content: String) {
    Text(heading, fontWeight = FontWeight.SemiBold, color = Color(0xFF087C26))
    Text(content, style = MaterialTheme.typography.bodyMedium)
}
