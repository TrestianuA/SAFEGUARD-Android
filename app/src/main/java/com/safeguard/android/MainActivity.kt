package com.safeguard.android

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    private val notificationAccess = mutableStateOf(false)
    private val events = mutableStateOf<List<NotificationEvent>>(emptyList())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SafeguardScreen(
                notificationAccess.value,
                events.value,
                ::refreshNotifications
            )
        }
    }

    override fun onResume() {
        super.onResume()
        refreshNotifications()
    }

    private fun refreshNotifications() {
        notificationAccess.value = NotificationAccess.isEnabled(this)
        events.value = NotificationLogStore(this).readRecent()
    }
}

private val green = Color(0xFF087C26)
private val navy = Color(0xFF064D2B)
private val paleBlue = Color(0xFFF3FBF2)
private val paleGreen = Color(0xFFE7F8DB)

@Composable
private fun SafeguardScreen(
    notificationAccessEnabled: Boolean,
    notificationEvents: List<NotificationEvent>,
    onRefreshNotifications: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var scanConsent by rememberSaveable { mutableStateOf(false) }
    var showConsent by remember { mutableStateOf(false) }
    var showNotificationConsent by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var scanning by remember { mutableStateOf(false) }
    var report by remember { mutableStateOf<ScanReport?>(null) }
    var error by remember { mutableStateOf<String?>(null) }
    var selectedTab by rememberSaveable { mutableIntStateOf(0) }
    var showHelp by remember { mutableStateOf(false) }
    var showCacheGuide by remember { mutableStateOf(false) }
    var helpEnglish by rememberSaveable { mutableStateOf(false) }

    fun startScan() {
        scanning = true
        error = null
        scope.launch {
            try {
                report = withContext(Dispatchers.IO) {
                    SecurityScanner(context.applicationContext).scan()
                }
            } catch (exception: Exception) {
                error = exception.localizedMessage ?: "Scanarea nu a putut fi finalizată."
            } finally {
                scanning = false
            }
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = green,
            secondary = navy,
            background = paleBlue,
            surface = Color.White,
            surfaceVariant = paleGreen
        )
    ) {
        Scaffold(
            bottomBar = {
                Surface(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 5.dp),
                    shape = RoundedCornerShape(32.dp),
                    color = navy,
                    shadowElevation = 7.dp,
                    border = BorderStroke(1.dp, green)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(5.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val tabs = if (helpEnglish) listOf("My Phone", "Learn", "Actions") else listOf("Telefonul", "Învață", "Acțiuni")
                        val icons = listOf(Icons.Filled.Home, Icons.Filled.Assessment, Icons.Filled.Settings)
                        tabs.forEachIndexed { index, title ->
                            val active = selectedTab == index || (index == 0 && (selectedTab == 3 || selectedTab == 4)) || (index == 1 && selectedTab == 5)
                            Surface(
                                onClick = { selectedTab = index },
                                modifier = Modifier.weight(1f).height(64.dp),
                                shape = RoundedCornerShape(28.dp),
                                color = if (active) Color(0xFF9EEA47) else Color.Transparent,
                                contentColor = if (active) navy else Color.White
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Icon(icons[index], contentDescription = null, modifier = Modifier.size(25.dp))
                                    Text(
                                        title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedTab == 0) FoniWiseHeader(onHelp = { showHelp = true }, onLanguage = { helpEnglish = !helpEnglish }, helpEnglish = helpEnglish)
                if (selectedTab == 0) {
                DashboardOverview(report != null, scanning)
                Text("Acces rapid", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = navy)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DashboardTile(Icons.Filled.Security, "Scanare\nsecuritate", "Locală", Modifier.weight(1f)) { selectedTab = 3 }
                    DashboardTile(Icons.Filled.Notifications, "Inspector\nnotificări", if (notificationAccessEnabled) "Activ" else "Opțional", Modifier.weight(1f)) { selectedTab = 4 }
                    DashboardTile(Icons.Filled.Apps, "Gestionare\naplicații", "Setări Android", Modifier.weight(1f)) {
                        launchSettings(context, Intent(Settings.ACTION_APPLICATION_SETTINGS))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DashboardTile(Icons.Filled.CleaningServices, "Curățare\nmanuală", "Cache Advisor", Modifier.weight(1f)) {
                        showCacheGuide = true
                    }
                    DashboardTile(Icons.Filled.BatteryChargingFull, "Setări\nbaterie", "Manual", Modifier.weight(1f)) {
                        launchSettings(context, Intent("android.intent.action.POWER_USAGE_SUMMARY"))
                    }
                    DashboardTile(Icons.Filled.Assessment, "Rapoarte\nși activitate", "Date locale", Modifier.weight(1f)) { selectedTab = 4 }
                }
                Card(
                    onClick = { selectedTab = 5 },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = paleGreen),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                        Text(if (helpEnglish) "Know Your Phone · Dictionary" else "Cunoaște-ți telefonul · Dicționar",
                            fontWeight = FontWeight.Bold, color = navy)
                        Text(if (helpEnglish) "Practical explanations and tips · RO / EN" else
                            "Explicații utile și recomandări practice · RO / EN",
                            style = MaterialTheme.typography.bodySmall)
                    }
                }
                Text("Starea dispozitivului", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = navy)
                DeviceHealthCards(context, helpEnglish)
                }
                if (selectedTab == 1 || selectedTab == 5) {
                    FoniWiseGlossary(
                        english = helpEnglish,
                        onBack = { selectedTab = 0 },
                        onLanguageChange = { helpEnglish = it }
                    )
                }
                if (selectedTab == 2) {
                    Text(if (helpEnglish) "Recommendations & Actions" else "Recomandări și acțiuni",
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = navy)
                    Text(if (helpEnglish) "Practical steps you control. FoniWise does not change Android settings automatically." else
                        "Pași practici pe care îi controlezi tu. FoniWise nu modifică automat setările Android.")
                    Card(colors = CardDefaults.cardColors(containerColor = paleGreen)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (helpEnglish) "How can it help you? · Manual cache cleaning" else "Cum te poate ajuta? · Curățare manuală cache",
                                fontWeight = FontWeight.Bold, color = navy)
                            Text(if (helpEnglish) "Checking an app's cache may help when it uses too much storage. Clearing cache differs from clearing app data." else
                                "Verificarea cache-ului poate ajuta când o aplicație ocupă prea mult spațiu. Ștergerea cache-ului diferă de ștergerea datelor aplicației.")
                            OutlinedButton(onClick = { showCacheGuide = true }) {
                                Text(if (helpEnglish) "Read steps and warnings" else "Vezi pașii și avertismentele")
                            }
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = paleGreen)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (helpEnglish) "How can it help you? · App permissions" else "Cum te poate ajuta? · Permisiunile aplicațiilor",
                                fontWeight = FontWeight.Bold, color = navy)
                            Text(if (helpEnglish) "Review permissions you no longer need. Changing them may affect app features." else
                                "Verifică permisiunile de care aplicațiile nu mai au nevoie. Modificarea lor poate afecta unele funcții.")
                            OutlinedButton(onClick = { launchSettings(context, Intent(Settings.ACTION_APPLICATION_SETTINGS)) }) {
                                Text(if (helpEnglish) "Open Apps settings" else "Deschide Setări aplicații")
                            }
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = paleGreen)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (helpEnglish) "How can it help you? · Animation scales" else "Cum te poate ajuta? · Scara animațiilor",
                                fontWeight = FontWeight.Bold, color = navy)
                            Text(if (helpEnglish) "At 0.5x, transitions can feel faster without improving CPU performance. Record original values first." else
                                "La 0,5x, tranzițiile pot părea mai rapide fără să crească performanța procesorului. Notează întâi valorile inițiale.")
                            OutlinedButton(onClick = { launchSettings(context, Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)) }) {
                                Text(if (helpEnglish) "Open Developer options page" else "Deschide Opțiuni pentru dezvoltatori")
                            }
                        }
                    }
                }
                if (selectedTab == 3) {
                Card(
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(containerColor = paleGreen)
                ) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("Scanner local – prototip 0.7.3-rc1", fontWeight = FontWeight.Bold, color = green)
                        Text("Verifică aplicațiile vizibile, permisiunile sensibile și câteva accesări speciale Android.")
                        Text("Nu este antivirus. Nu citește mesaje, fotografii, fișiere sau istoricul browserului și nu transmite date.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Button(
                    onClick = { if (scanConsent) startScan() else showConsent = true },
                    enabled = !scanning,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (scanning) "Se scanează…" else if (report == null) "Începe scanarea" else "Scanează din nou")
                }
                error?.let { InfoCard("Eroare: $it") }
                report?.let { scan ->
                    Card {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text("Dispozitiv verificat", color = green, fontWeight = FontWeight.Bold)
                            Text(scan.deviceModel, fontWeight = FontWeight.Bold)
                            Text("Android ${scan.androidRelease} (API ${scan.sdkInt})")
                            Text("Patch de securitate: ${scan.securityPatch}")
                            Text("${scan.installedApps} aplicații vizibile · ${scan.userApps} aplicații terțe")
                        }
                    }
                    Section("Accesări speciale", "Nu reprezintă automat amenințări. Verifică dacă recunoști aplicațiile.")
                    if (scan.specialAccess.isEmpty()) InfoCard("Nu au fost găsite accesări speciale în categoriile verificate.")
                    scan.specialAccess.forEach { item ->
                        Card {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(item.category, fontWeight = FontWeight.Bold, color = green)
                                Text(item.appLabel)
                                Text(item.packageName, style = MaterialTheme.typography.labelSmall)
                                Text(item.explanation, style = MaterialTheme.typography.bodySmall)
                                OutlinedButton(onClick = { openSettingsFor(context, item) }) { Text("Verifică în Setări") }
                            }
                        }
                    }
                    Section("Permisiuni sensibile acordate", "Unele permisiuni sunt necesare pentru funcționarea normală a aplicațiilor.")
                    if (scan.permissionApps.isEmpty()) InfoCard("Nu au fost găsite aplicații terțe cu permisiunile verificate.")
                    scan.permissionApps.take(40).forEach { app ->
                        Card {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(app.appLabel, fontWeight = FontWeight.Bold)
                                Text(app.grantedPermissions.joinToString(" • "), style = MaterialTheme.typography.bodySmall)
                                Text(app.packageName, style = MaterialTheme.typography.labelSmall)
                                TextButton(onClick = { openAppDetails(context, app.packageName) }) {
                                    Text("Detalii aplicație")
                                }
                            }
                        }
                    }
                    if (scan.permissionApps.size > 40) Text("Sunt afișate primele 40 din ${scan.permissionApps.size} aplicații.")
                    InfoCard("Acesta este un control de configurare, nu o scanare antivirus. Nu analizează codul aplicațiilor și nu poate confirma că dispozitivul este curat.")
                }

                }
                if (selectedTab == 4) {
                Section("Rapoarte și activitate", "Inspectorul notificărilor: identifică local sursele notificărilor.")
                Card {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (notificationAccessEnabled) "Acces la notificări activ" else "Acces la notificări dezactivat",
                            fontWeight = FontWeight.Bold,
                            color = green
                        )
                        Text("Păstrează local maximum 100 de înregistrări: numai pachetul-sursă și ora. Nu citește titlul, textul sau expeditorul notificării.", style = MaterialTheme.typography.bodySmall)
                        if (!notificationAccessEnabled) {
                            Button(onClick = { showNotificationConsent = true }) { Text("Configurează inspectorul") }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = onRefreshNotifications) { Text("Reîmprospătează") }
                                TextButton(onClick = {
                                    launchSettings(context, Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                }) { Text("Gestionează accesul") }
                            }
                        }
                        if (notificationEvents.isEmpty()) {
                            Text("Nu există înregistrări locale.")
                        } else {
                            Text("${notificationEvents.size} înregistrări locale")
                            TextButton(onClick = { showDeleteConfirmation = true }) { Text("Șterge istoricul") }
                            val sources = NotificationFrequencyAnalyzer.recentSources(notificationEvents, System.currentTimeMillis())
                            Text("Surse în ultimele 15 minute", fontWeight = FontWeight.Bold)
                            if (sources.isEmpty()) Text("Nu există evenimente recente.")
                            sources.take(5).forEach { source ->
                                Text("${source.packageName}: ${source.count} notificări" +
                                    if (source.count >= NotificationFrequencyAnalyzer.NOTICE_THRESHOLD) " · Frecvență ridicată" else "")
                            }
                            Text("Frecvența ridicată nu indică automat malware.", style = MaterialTheme.typography.bodySmall)
                            notificationEvents.take(20).forEach { event ->
                                Text("${event.packageName} · ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(event.postedAtMillis))}",
                                    style = MaterialTheme.typography.bodySmall)
                                TextButton(onClick = { openAppDetails(context, event.packageName) }) { Text("Detalii aplicație") }
                            }
                        }
                    }
                }
                }
                Text("FoniWise 0.7.3-rc1 • analiză locală • fără cont și fără reclame", style = MaterialTheme.typography.labelSmall)
            }
        }
    }

    if (showCacheGuide) {
        SafeguardInfoDialog(
            titleRo = "Cache Advisor",
            titleEn = "Cache Advisor",
            summaryRo = "Cache-ul este un spațiu în care aplicațiile păstrează temporar imagini și alte informații, ca să se deschidă mai repede. În timp, unele aplicații pot ocupa mult spațiu. Cum te poate ajuta: poți recupera spațiu pe telefon ștergând manual cache-ul unei aplicații, dacă Android permite.",
            summaryEn = "A cache is temporary storage where apps keep images and other information so they can open faster. Over time, some apps may use a lot of space. How it can help you: you may recover phone storage by manually clearing an app\'s cache, if Android allows it.",
            doesRo = "FoniWise îți explică pașii pentru a curăța manual cache-ul unei aplicații. Poți deschide lista aplicațiilor Android, alege aplicația dorită, apoi căuta Stocare → Șterge cache. Numele opțiunilor diferă între telefoane.",
            doesEn = "FoniWise explains how to clear an app's cache manually. Open the Android app list, choose an app, then look for Storage → Clear cache. The labels vary by phone.",
            limitsRo = "FoniWise nu șterge automat cache-ul altor aplicații și nu garantează că telefonul va deveni mai rapid. Atenție: «Șterge datele» sau «Golire stocare» este altă acțiune și poate elimina setări ori sesiuni de conectare.",
            limitsEn = "FoniWise does not automatically clear other apps' caches and cannot guarantee a faster phone. Warning: Clear data or Clear storage is a different action and may remove settings or signed-in sessions.",
            adviceRo = "Curăță cache-ul doar dacă o aplicație ocupă prea mult spațiu sau nu funcționează corect. Începe cu aplicația care are problema și verifică spațiul liber după aceea. Nu este necesar să ștergi cache-ul tuturor aplicațiilor în fiecare zi. Aplicația poate crea din nou fișiere temporare la următoarea utilizare, deci spațiul eliberat poate fi temporar.",
            adviceEn = "Clear cache only when an app uses too much storage or is not working correctly. Start with the affected app and check available storage afterward. You do not need to clear every app's cache each day. The app may recreate temporary files the next time you use it, so freed space may be temporary.",
            english = helpEnglish,
            onLanguageChange = { helpEnglish = it },
            onDismiss = { showCacheGuide = false },
            onSettings = {
                showCacheGuide = false
                launchSettings(context, Intent(Settings.ACTION_APPLICATION_SETTINGS))
            }
        )
    }

    if (showHelp) {
        SafeguardInfoDialog(
            titleRo = "Despre FoniWise",
            titleEn = "About FoniWise",
            summaryRo = "FoniWise te ajută să înțelegi anumite setări și riscuri ale telefonului. Verificările se fac pe dispozitiv, fără să fie nevoie de un cont.",
            summaryEn = "FoniWise helps you understand some phone settings and possible risks. Checks run on your device, without requiring an account.",
            doesRo = "Poate analiza aplicațiile pe care Android îi permite să le vadă și unele permisiuni importante. O permisiune este un drept acordat unei aplicații, de exemplu accesul la cameră sau locație. Rezultatele te ajută să decizi ce merită verificat în setările telefonului.",
            doesEn = "It can examine apps that Android allows it to see and some important permissions. A permission is access granted to an app, such as the camera or location. The results help you decide what to review in your phone settings.",
            limitsRo = "Nu este un antivirus: nu verifică fiecare fișier, nu poate garanta că telefonul este lipsit de amenințări și nu blochează automat aplicațiile. Nu oferă protecție antivirus în timp real.",
            limitsEn = "It is not an antivirus: it does not inspect every file, cannot guarantee your phone is threat-free and does not automatically block apps. It does not provide real-time antivirus protection.",
            adviceRo = "Rulează verificarea după instalarea unei aplicații noi sau când observi un comportament neobișnuit. Dacă apare o permisiune pe care nu o înțelegi, deschide detaliile aplicației în setările Android și verifică dacă are nevoie de ea. Nu elimina permisiuni la întâmplare: unele sunt necesare funcționării aplicației.",
            adviceEn = "Run a check after installing a new app or if your phone behaves unexpectedly. If a permission seems unfamiliar, open that app’s details in Android settings and decide whether it needs the access. Do not remove permissions at random: some are needed for the app to work.",
            english = helpEnglish,
            onLanguageChange = { helpEnglish = it },
            onDismiss = { showHelp = false }
        )
    }

    if (showConsent) {
        AlertDialog(
            onDismissRequest = { showConsent = false },
            title = { Text("Acord pentru scanarea locală") },
            text = { Text("FoniWise va citi local lista aplicațiilor vizibile, permisiunile acordate și unele setări de acces special. Inventarul aplicațiilor este sensibil. În această versiune nu este transmis sau salvat. Continui?") },
            confirmButton = {
                TextButton(onClick = { scanConsent = true; showConsent = false; startScan() }) { Text("Accept și scanează") }
            },
            dismissButton = { TextButton(onClick = { showConsent = false }) { Text("Anulează") } }
        )
    }
    if (showNotificationConsent) {
        AlertDialog(
            onDismissRequest = { showNotificationConsent = false },
            title = { Text("Acces special la notificări") },
            text = { Text("Android avertizează că serviciile de notificări pot vedea conținutul notificărilor. FoniWise folosește doar pachetul-sursă și ora, păstrate local, maximum 100 de evenimente. Accesul se activează numai din Setările Android.") },
            confirmButton = {
                TextButton(onClick = {
                    showNotificationConsent = false
                    launchSettings(context, Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }) { Text("Deschide Setări") }
            },
            dismissButton = { TextButton(onClick = { showNotificationConsent = false }) { Text("Renunță") } }
        )
    }
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Ștergi istoricul local?") },
            text = { Text("Vor fi șterse toate înregistrările păstrate de FoniWise.") },
            confirmButton = {
                TextButton(onClick = {
                    NotificationLogStore(context).clear()
                    showDeleteConfirmation = false
                    onRefreshNotifications()
                }) { Text("Șterge") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirmation = false }) { Text("Păstrează") } }
        )
    }
}

@Composable
private fun BrandLogo(size: androidx.compose.ui.unit.Dp) {
    Image(
        painter = painterResource(R.drawable.foniwise_symbol_original_approved),
        contentDescription = "Simbolul original FoniWise",
        modifier = Modifier.size(size),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun DashboardOverview(hasReport: Boolean, scanning: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = paleGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = BorderStroke(1.dp, Color(0xFFC5E9B6))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 18.dp, vertical = 22.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                "Bun venit în FoniWise!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = navy
            )
            Text(
                "Înțelege-ți telefonul, descoperă instrumentele disponibile și decide ce este potrivit pentru tine.",
                style = MaterialTheme.typography.bodyMedium,
                color = navy
            )
            Surface(
                color = Color.White,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Filled.Security, contentDescription = null, tint = green)
                    Column {
                        Text(
                            when {
                                scanning -> "Verificare locală în desfășurare"
                                hasReport -> "Verificare locală finalizată"
                                else -> "Verificare locală disponibilă"
                            },
                            fontWeight = FontWeight.SemiBold,
                            color = navy
                        )
                        Text(
                            if (hasReport) "Rezultatele sunt disponibile în Protecție."
                            else "Nu oferă protecție antivirus în timp real.",
                            style = MaterialTheme.typography.bodySmall,
                            color = navy
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun DashboardTile(
    symbol: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val narrowScreen = LocalConfiguration.current.screenWidthDp < 390
    Card(
        onClick = onClick,
        modifier = modifier.height(if (narrowScreen) 158.dp else 140.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = paleGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC5E9B6))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 5.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp, Alignment.CenterVertically)
        ) {
            Icon(imageVector = symbol, contentDescription = null, tint = green, modifier = Modifier.size(38.dp))
            Text(title, fontWeight = FontWeight.Bold, color = navy, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, lineHeight = 16.sp)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = green, textAlign = TextAlign.Center, maxLines = 2)
        }
    }
}

@Composable
private fun SafeguardInfoDialog(
    titleRo: String,
    titleEn: String,
    summaryRo: String,
    summaryEn: String,
    doesRo: String,
    doesEn: String,
    limitsRo: String,
    limitsEn: String,
    adviceRo: String,
    adviceEn: String,
    english: Boolean,
    onLanguageChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSettings: (() -> Unit)? = null
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Security, contentDescription = null, tint = green)
                    Spacer(Modifier.width(10.dp))
                    Text(if (english) titleEn else titleRo, color = navy, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge)
                    IconButton(onClick = onDismiss) {
                        Text("×", color = navy, style = MaterialTheme.typography.headlineMedium)
                    }
                }
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                    listOf(false to "RO", true to "EN").forEach { (isEnglish, label) ->
                        val selected = english == isEnglish
                        Surface(
                            onClick = { onLanguageChange(isEnglish) },
                            shape = RoundedCornerShape(20.dp),
                            color = if (selected) green else Color(0xFFF0F7EE),
                            border = BorderStroke(1.dp, if (selected) green else Color(0xFFC5E9B6)),
                            modifier = Modifier.padding(horizontal = 3.dp)
                        ) {
                            Text(label, modifier = Modifier.padding(horizontal = 22.dp, vertical = 8.dp),
                                color = if (selected) Color.White else navy, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Surface(color = paleGreen, shape = RoundedCornerShape(16.dp)) {
                    Text(if (english) summaryEn else summaryRo, modifier = Modifier.padding(14.dp), color = navy)
                }
                Text(if (english) "What does it do?" else "Ce face?", fontWeight = FontWeight.Bold, color = green)
                Text(if (english) doesEn else doesRo)
                Text(if (english) "What doesn't it do?" else "Ce NU face?", fontWeight = FontWeight.Bold, color = green)
                Text(if (english) limitsEn else limitsRo)
                Text(if (english) "Recommendation" else "Recomandare", fontWeight = FontWeight.Bold, color = green)
                Text(if (english) adviceEn else adviceRo)
            }
        },
        confirmButton = {
            Column(modifier = Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                if (onSettings != null) {
                    TextButton(onClick = onSettings, modifier = Modifier.fillMaxWidth()) {
                        Text(if (english) "Open Android app settings" else "Deschide setările aplicațiilor")
                    }
                }
                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = green)) {
                    Text(if (english) "Got it" else "Am înțeles")
                }
            }
        }
    )
}

@Composable
private fun FoniWiseHeader(onHelp: () -> Unit, onLanguage: () -> Unit, helpEnglish: Boolean) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = navy),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 14.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BrandLogo(54.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text("FoniWise", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text(if (helpEnglish) "Know Your Phone" else "Cunoaște-ți telefonul", style = MaterialTheme.typography.labelSmall, color = Color(0xFFD1F5C5))
            }
            IconButton(onClick = onHelp) { Icon(Icons.Filled.Settings, contentDescription = if (helpEnglish) "About and settings" else "Despre și setări", tint = Color.White, modifier = Modifier.size(30.dp)) }
            TextButton(onClick = onLanguage, contentPadding = PaddingValues(2.dp)) { Text(if (helpEnglish) "EN" else "RO", color = Color.White, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
private fun Section(title: String, subtitle: String) {
    Column(verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Text(subtitle, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun InfoCard(message: String) {
    Card { Text(message, Modifier.padding(14.dp)) }
}

private fun readDeviceMemory(context: Context): ActivityManager.MemoryInfo? =
    runCatching {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        ActivityManager.MemoryInfo().also { manager.getMemoryInfo(it) }
    }.getOrNull()

internal fun hasValidRamReading(totalBytes: Long, availableBytes: Long): Boolean =
    totalBytes > 0L && availableBytes >= 0L && availableBytes <= totalBytes

private fun hasValidRamReading(info: ActivityManager.MemoryInfo?): Boolean =
    info != null && hasValidRamReading(info.totalMem, info.availMem)

@Composable
private fun DeviceHealthCards(context: Context, helpEnglish: Boolean) {
    var memory by remember { mutableStateOf(readDeviceMemory(context)) }
    val memorySnapshot = memory
    var showRamInfo by remember { mutableStateOf(false) }
    var ramInfoEnglish by remember(helpEnglish) { mutableStateOf(helpEnglish) }
    var showAnimationInfo by remember { mutableStateOf(false) }
    var animationInfoEnglish by remember(helpEnglish) { mutableStateOf(helpEnglish) }
    val battery = remember {
        runCatching {
            val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            if (level < 0 || scale <= 0) null else level * 100 / scale
        }.getOrNull()
    }
    val storage = remember {
        runCatching {
            val fs = StatFs(Environment.getDataDirectory().absolutePath)
            Pair(fs.totalBytes, fs.availableBytes)
        }.getOrNull()
    }
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (helpEnglish) "RAM Monitor" else "Memorie RAM", fontWeight = FontWeight.Bold, color = green, modifier = Modifier.weight(1f))
                IconButton(onClick = { showRamInfo = true }) {
                    Text("ⓘ", color = navy, style = MaterialTheme.typography.titleLarge)
                }
            }
            if (memorySnapshot != null && hasValidRamReading(memorySnapshot)) {
                Text((if (helpEnglish) "Available: %.1f GB of %.1f GB" else "Disponibil: %.1f GB din %.1f GB").format(memorySnapshot.availMem / 1073741824.0, memorySnapshot.totalMem / 1073741824.0))
                Text(if (memorySnapshot.lowMemory) { if (helpEnglish) "Android reports memory pressure." else "Android raportează presiune asupra memoriei." } else { if (helpEnglish) "Android does not report critically low memory." else "Android nu raportează memorie critic de redusă." })
            } else Text(if (helpEnglish) "Data unavailable" else "Date indisponibile")
            Text(if (helpEnglish) "Tap ⓘ for a simple explanation and recommendations." else "Apasă ⓘ pentru explicații simple și recomandări.", style = MaterialTheme.typography.bodySmall)
            if (memorySnapshot != null && hasValidRamReading(memorySnapshot)) {
                Text(if (helpEnglish) "Advice based on this reading:" else "Sfat adaptat măsurătorii:", fontWeight = FontWeight.SemiBold, color = green)
                Text(
                    if (memorySnapshot.lowMemory)
                        if (helpEnglish) "Android reports memory pressure. Review resource-heavy apps and close only those you no longer need. If the phone stays slow, consider restarting it." else "Android semnalează presiune asupra memoriei. Verifică aplicațiile care consumă resurse și închide doar ce nu mai folosești. Dacă telefonul rămâne lent, poți încerca o repornire."
                    else
                        if (helpEnglish) "Android does not report critical memory pressure. There is no need to force-close apps just to increase available RAM." else "Android nu semnalează presiune critică asupra memoriei. Nu este necesar să forțezi închiderea aplicațiilor doar pentru a mări RAM-ul disponibil.",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            OutlinedButton(onClick = { memory = readDeviceMemory(context) }) { Text(if (helpEnglish) "Refresh RAM" else "Actualizează RAM") }
            Text(if (helpEnglish) "FoniWise does not stop processes automatically or force RAM to be freed." else "FoniWise nu oprește automat procese și nu eliberează forțat RAM.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { launchSettings(context, Intent(Settings.ACTION_APPLICATION_SETTINGS)) }) { Text(if (helpEnglish) "Review apps" else "Verifică aplicațiile") }
        }
    }
    if (showRamInfo) {
        SafeguardInfoDialog(
            titleRo = "ⓘ Memorie RAM",
            titleEn = "ⓘ RAM Monitor",
            summaryRo = "RAM este memoria de lucru temporară a telefonului. Android o folosește pentru aplicațiile deschise și pentru a le porni mai repede. Nu este același lucru cu spațiul pentru poze și fișiere.",
            summaryEn = "RAM is your phone's temporary working memory. Android uses it for open apps and to launch them faster. It is different from the storage used for photos and files.",
            doesRo = "FoniWise citește de la Android câtă memorie RAM există, câtă este disponibilă și dacă sistemul semnalează lipsă de memorie. Poți apăsa «Actualizează RAM» pentru o nouă citire.",
            doesEn = "FoniWise reads Android's total and available RAM and whether the system reports low memory. Tap Refresh RAM to take another reading.",
            limitsRo = "Memoria ocupată nu înseamnă automat o problemă: Android o gestionează singur. FoniWise nu închide aplicații în locul tău și nu poate elibera forțat RAM.",
            limitsEn = "Used memory does not automatically mean a problem: Android manages RAM itself. FoniWise does not close apps for you or forcibly free RAM.",
            adviceRo = if (memorySnapshot != null && hasValidRamReading(memorySnapshot) && memorySnapshot.lowMemory)
                "Android raportează presiune asupra memoriei. Dacă telefonul este lent, închide doar aplicațiile pe care nu le mai folosești și verifică dacă problema persistă. Poți reporni telefonul dacă încetinirea continuă."
                else if (!hasValidRamReading(memorySnapshot)) "Măsurarea RAM nu este disponibilă acum. Apasă «Actualizează RAM» pentru a încerca din nou. FoniWise nu poate aprecia presiunea asupra memoriei fără o citire validă." else "Android nu raportează acum lipsă critică de memorie. Dacă telefonul funcționează bine, nu trebuie să închizi aplicații doar pentru a vedea mai mult RAM liber. Dacă apar încetiniri, actualizează măsurătoarea.",
            adviceEn = if (memorySnapshot != null && hasValidRamReading(memorySnapshot) && memorySnapshot.lowMemory)
                "Android reports memory pressure. If your phone is slow, close only apps you no longer need and see whether the problem continues. Restarting the phone may help if it remains slow."
                else if (!hasValidRamReading(memorySnapshot)) "RAM readings are unavailable right now. Tap Refresh RAM to try again. FoniWise cannot assess memory pressure without a valid reading." else "Android is not currently reporting critically low memory. If your phone works well, there is no need to close apps just to show more free RAM. Refresh the reading if you notice slowdowns.",
            english = ramInfoEnglish,
            onLanguageChange = { ramInfoEnglish = it },
            onDismiss = { showRamInfo = false }
        )
    }
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    if (helpEnglish) "Android animations" else "Animații Android",
                    fontWeight = FontWeight.Bold, color = green, modifier = Modifier.weight(1f)
                )
                IconButton(onClick = { showAnimationInfo = true }) {
                    Text("ⓘ", color = navy, style = MaterialTheme.typography.titleLarge)
                }
            }
            Text(
                if (helpEnglish) "Animations are visual transitions between screens. Tap ⓘ for an explanation, safe steps and advice."
                else "Animațiile sunt efectele vizuale dintre ecrane. Apasă ⓘ pentru explicații, pași siguri și recomandări.",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedButton(onClick = { launchSettings(context, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
                Text(if (helpEnglish) "Open Accessibility settings" else "Deschide Accesibilitate")
            }
        }
    }
    if (showAnimationInfo) {
        SafeguardInfoDialog(
            titleRo = "ⓘ Animații Android",
            titleEn = "ⓘ Android animations",
            summaryRo = "Animațiile sunt efectele de mișcare pe care le vezi când deschizi o aplicație sau treci între ecrane. Ele fac schimbările mai ușor de urmărit, dar pe unele telefoane pot crea senzația de întârziere.",
            summaryEn = "Animations are the motion effects you see when opening an app or moving between screens. They can make changes easier to follow, but on some phones they may feel slow.",
            doesRo = "FoniWise îți arată unde poți căuta opțiunea Android pentru reducerea animațiilor: Setări → Accesibilitate → Culori și mișcare / Îmbunătățiri pentru vizibilitate → Elimină animațiile. Denumirile diferă după producător și versiunea Android.",
            doesEn = "FoniWise shows where to look for Android's animation reduction option: Settings → Accessibility → Color and motion / Visibility enhancements → Remove animations. Names vary by manufacturer and Android version.",
            limitsRo = "FoniWise nu schimbă automat setările, nu activează Opțiunile pentru dezvoltatori și nu mărește memoria RAM fizică. Reducerea animațiilor poate schimba doar felul în care se simte navigarea.",
            limitsEn = "FoniWise does not change settings automatically, enable Developer options or increase physical RAM. Reducing animations may only change how navigation feels.",
            adviceRo = "Încearcă reducerea animațiilor doar dacă mișcările te deranjează sau telefonul pare lent la schimbarea ecranelor. Verifică dacă îți place rezultatul. Dacă preferi efectele inițiale, revino în aceeași pagină de Accesibilitate și dezactivează opțiunea «Elimină animațiile». Dacă nu găsești opțiunea, lasă setările neschimbate.",
            adviceEn = "Try reducing animations if the motion bothers you or switching screens feels slow. Check whether you prefer the result. To restore the original effects, return to the same Accessibility page and turn off Remove animations. If you cannot find the option, leave the settings unchanged.",
            english = animationInfoEnglish,
            onLanguageChange = { animationInfoEnglish = it },
            onDismiss = { showAnimationInfo = false },
            onSettings = {
                showAnimationInfo = false
                launchSettings(context, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
            }
        )
    }
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Baterie", fontWeight = FontWeight.Bold, color = green)
            Text(if (battery == null) "Date indisponibile" else "Nivel baterie: $battery%")
            Text("FoniWise nu modifică automat setările de energie.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { launchSettings(context, Intent("android.intent.action.POWER_USAGE_SUMMARY")) }) { Text("Setările bateriei") }
        }
    }
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Stocare", fontWeight = FontWeight.Bold, color = green)
            if (storage != null && storage.first > 0L) {
                Text("Utilizați: %.1f GB din %.1f GB".format(
                    (storage.first - storage.second) / 1073741824.0,
                    storage.first / 1073741824.0
                ))
            } else Text("Date indisponibile")
            Text(if (helpEnglish) "ⓘ Cache contains temporary files used by apps. It is not the same as an app’s personal data." else "ⓘ Cache-ul conține fișiere temporare folosite de aplicații. Nu este același lucru cu datele personale ale aplicației.", style = MaterialTheme.typography.bodySmall)
            Text(if (helpEnglish) "Cache Advisor: if an app uses too much storage, open Android Settings → Apps → select the app → Storage → Clear cache, if available. Do not tap Clear data unless you understand the consequences: you may lose settings or sign-in information." else "Cache Advisor: dacă o aplicație ocupă prea mult spațiu, deschide Setări Android → Aplicații → alege aplicația → Stocare → Șterge memoria cache, dacă opțiunea este disponibilă. Nu apăsa «Șterge datele» fără să înțelegi consecințele: poți pierde setări sau autentificări.", style = MaterialTheme.typography.bodySmall)
            Text(if (helpEnglish) "FoniWise does not measure each app’s cache or delete files automatically." else "FoniWise nu măsoară cache-ul fiecărei aplicații și nu șterge automat fișiere.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { launchSettings(context, Intent(Settings.ACTION_APPLICATION_SETTINGS)) }) { Text(if (helpEnglish) "Cache Advisor · App settings" else "Cache Advisor · Setări aplicații") }
            Text(if (helpEnglish) "FoniWise does not read or delete personal files." else "FoniWise nu citește sau șterge fișiere personale.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { launchSettings(context, Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)) }) { Text("Setările stocării") }
        }
    }
}

private fun launchSettings(context: Context, intent: Intent) {
    runCatching { context.startActivity(intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)) }
        .onFailure {
            runCatching {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            }
        }
}

private fun openAppDetails(context: Context, packageName: String) {
    launchSettings(
        context,
        Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:$packageName"))
    )
}

private fun openSettingsFor(context: Context, item: SpecialAccessItem) {
    val intent = when (item.settingsTarget) {
        SettingsTarget.APP_DETAILS -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${item.packageName}"))
        SettingsTarget.ACCESSIBILITY -> Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)
        SettingsTarget.NOTIFICATION_LISTENERS -> Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
        SettingsTarget.DEVICE_SECURITY -> Intent(Settings.ACTION_SECURITY_SETTINGS)
        SettingsTarget.OVERLAY -> Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:${item.packageName}"))
        SettingsTarget.UNKNOWN_APP_SOURCES -> Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES, Uri.parse("package:${item.packageName}"))
        SettingsTarget.ALL_FILES_ACCESS -> Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION, Uri.parse("package:${item.packageName}"))
        SettingsTarget.USAGE_ACCESS -> Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
    }
    launchSettings(context, intent)
}
