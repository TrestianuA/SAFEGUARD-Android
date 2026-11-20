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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
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
    var showPermissionsGuide by remember { mutableStateOf(false) }
    var showFraudGuide by remember { mutableStateOf(false) }
    var showSecuritySteps by remember { mutableStateOf(false) }
    var showDeveloperGuide by remember { mutableStateOf(false) }
    val languagePreferences = remember(context) {
        context.applicationContext.getSharedPreferences("foniwise_language", Context.MODE_PRIVATE)
    }
    val savedLanguage = remember(languagePreferences) { languagePreferences.getString("language", null) }
    var chosenLanguage by remember { mutableStateOf(savedLanguage) }
    var helpEnglish by rememberSaveable { mutableStateOf(savedLanguage == "en") }
    fun chooseLanguage(english: Boolean) {
        helpEnglish = english
        chosenLanguage = if (english) "en" else "ro"
        languagePreferences.edit().putString("language", chosenLanguage).apply()
    }
    if (chosenLanguage == null) {
        FoniWiseLanguageWelcome(onChooseLanguage = ::chooseLanguage)
        return
    }

    fun startScan() {
        scanning = true
        error = null
        scope.launch {
            try {
                report = withContext(Dispatchers.IO) {
                    SecurityScanner(context.applicationContext).scan()
                }
            } catch (exception: Exception) {
                error = exception.localizedMessage
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
                        val tabs = if (helpEnglish) listOf("My phone", "Learn about\nyour phone", "Recommendations\nand actions") else listOf("Telefonul meu", "Învață despre\ntelefon", "Recomandări\nși acțiuni")
                        val icons = listOf(Icons.Filled.Home, Icons.Filled.Assessment, Icons.Filled.Settings)
                        tabs.forEachIndexed { index, title ->
                            val active = selectedTab == index || (index == 0 && (selectedTab == 3 || selectedTab == 4)) || (index == 1 && selectedTab == 5)
                            Surface(
                                onClick = { selectedTab = index },
                                modifier = Modifier.weight(1f).height(78.dp),
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
                                        maxLines = 2,
                                        textAlign = TextAlign.Center,
                                        lineHeight = 14.sp
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
                FoniWiseHeader(onHelp = { showHelp = true }, onLanguage = { chooseLanguage(!helpEnglish) }, helpEnglish = helpEnglish)
                if (selectedTab == 0) {
                DashboardOverview(report != null, scanning, helpEnglish)
                Text(if (helpEnglish) "Quick access" else "Acces rapid", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = navy)
                BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                    if (maxWidth < 360.dp) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            DashboardTile(Icons.Filled.Security, if (helpEnglish) "Security scan" else "Scanare securitate",
                            if (helpEnglish) "Local check" else "Verificare locală",
                            if (helpEnglish) "Helps you review visible apps, sensitive permissions and special access. Not an antivirus." else "Te ajută să verifici aplicații vizibile, permisiuni sensibile și accesări speciale. Nu este antivirus.",
                            helpEnglish, Modifier.fillMaxWidth()) { selectedTab = 3 }
                            DashboardTile(Icons.Filled.Notifications, if (helpEnglish) "Notification inspector" else "Inspector notificări",
                            if (notificationAccessEnabled) (if (helpEnglish) "Active" else "Activ") else (if (helpEnglish) "Optional access" else "Acces opțional"),
                            if (helpEnglish) "Helps you review notifications made available by Android. Requires your permission." else "Te ajută să examinezi notificările oferite de Android. Necesită acordul tău.",
                            helpEnglish, Modifier.fillMaxWidth()) { selectedTab = 4 }
                            DashboardTile(Icons.Filled.BatteryChargingFull, if (helpEnglish) "Battery" else "Baterie",
                            if (helpEnglish) "Android settings" else "Setări Android",
                            if (helpEnglish) "Helps you check battery usage and decide which settings deserve attention." else "Te ajută să verifici consumul bateriei și să decizi ce setări merită atenție.",
                            helpEnglish, Modifier.fillMaxWidth()) {
                            launchSettings(context, Intent("android.intent.action.POWER_USAGE_SUMMARY"))
                            }
                            DashboardTile(Icons.Filled.Apps, if (helpEnglish) "Apps" else "Aplicații",
                            if (helpEnglish) "Android settings" else "Setări Android",
                            if (helpEnglish) "Helps you review installed apps, their permissions and available controls." else "Te ajută să verifici aplicațiile instalate, permisiunile și controalele disponibile.",
                            helpEnglish, Modifier.fillMaxWidth()) {
                            launchSettings(context, Intent(Settings.ACTION_APPLICATION_SETTINGS))
                            }
                            DashboardTile(Icons.Filled.CleaningServices, if (helpEnglish) "Manual cleaning" else "Curățare manuală",
                            "Cache Advisor",
                            if (helpEnglish) "Shows when clearing an app's cache may help and how to avoid deleting its data." else "Îți arată când poate ajuta ștergerea cache-ului și cum eviți ștergerea datelor.",
                            helpEnglish, Modifier.fillMaxWidth()) { showCacheGuide = true }
                        }
                    } else {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DashboardTile(Icons.Filled.Security, if (helpEnglish) "Security scan" else "Scanare securitate",
                            if (helpEnglish) "Local check" else "Verificare locală",
                            if (helpEnglish) "Helps you review visible apps, sensitive permissions and special access. Not an antivirus." else "Te ajută să verifici aplicații vizibile, permisiuni sensibile și accesări speciale. Nu este antivirus.",
                            helpEnglish, Modifier.weight(1f)) { selectedTab = 3 }
                        DashboardTile(Icons.Filled.Notifications, if (helpEnglish) "Notification inspector" else "Inspector notificări",
                            if (notificationAccessEnabled) (if (helpEnglish) "Active" else "Activ") else (if (helpEnglish) "Optional access" else "Acces opțional"),
                            if (helpEnglish) "Helps you review notifications made available by Android. Requires your permission." else "Te ajută să examinezi notificările oferite de Android. Necesită acordul tău.",
                            helpEnglish, Modifier.weight(1f)) { selectedTab = 4 }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        DashboardTile(Icons.Filled.BatteryChargingFull, if (helpEnglish) "Battery" else "Baterie",
                            if (helpEnglish) "Android settings" else "Setări Android",
                            if (helpEnglish) "Helps you check battery usage and decide which settings deserve attention." else "Te ajută să verifici consumul bateriei și să decizi ce setări merită atenție.",
                            helpEnglish, Modifier.weight(1f)) {
                            launchSettings(context, Intent("android.intent.action.POWER_USAGE_SUMMARY"))
                        }
                        DashboardTile(Icons.Filled.Apps, if (helpEnglish) "Apps" else "Aplicații",
                            if (helpEnglish) "Android settings" else "Setări Android",
                            if (helpEnglish) "Helps you review installed apps, their permissions and available controls." else "Te ajută să verifici aplicațiile instalate, permisiunile și controalele disponibile.",
                            helpEnglish, Modifier.weight(1f)) {
                            launchSettings(context, Intent(Settings.ACTION_APPLICATION_SETTINGS))
                        }
                    }
                    DashboardTile(Icons.Filled.CleaningServices, if (helpEnglish) "Manual cleaning" else "Curățare manuală",
                        "Cache Advisor",
                        if (helpEnglish) "Shows when clearing an app's cache may help and how to avoid deleting its data." else "Îți arată când poate ajuta ștergerea cache-ului și cum eviți ștergerea datelor.",
                        helpEnglish, Modifier.fillMaxWidth()) { showCacheGuide = true }
                    }
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
                Text(if (helpEnglish) "Device status" else "Starea dispozitivului", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = navy)
                DeviceHealthCards(context, helpEnglish, onLanguageChange = { chooseLanguage(it) })
                }
                if (selectedTab == 1 || selectedTab == 5) {
                    Text(if (helpEnglish) "Learn about your phone" else "Învață despre telefon", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = navy)
                    Text(if (helpEnglish) "Clear explanations and practical examples. Choose a topic when you need it." else "Explicații clare și exemple practice. Alege subiectul care te interesează.")
                    Card(colors = CardDefaults.cardColors(containerColor = paleGreen)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (helpEnglish) "App permissions explained" else "Permisiunile aplicațiilor, pe înțelesul tău", fontWeight = FontWeight.Bold, color = navy)
                            Text(if (helpEnglish) "Why apps ask for camera, microphone, location or contacts, with examples." else "De ce cer aplicațiile acces la cameră, microfon, locație sau contacte, cu exemple.")
                            OutlinedButton(onClick = { showPermissionsGuide = true }) {
                                Text(if (helpEnglish) "Read the guide" else "Citește ghidul")
                            }
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = paleGreen)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (helpEnglish) "Fraud and phishing safety" else "Protecție antifraudă și antiphishing",
                                fontWeight = FontWeight.Bold, color = navy)
                            Text(if (helpEnglish) "Learn warning signs in messages, links and calls. No private messages are read." else
                                "Recunoaște semnele de alarmă din mesaje, linkuri și apeluri. Nu sunt citite mesaje personale.")
                            OutlinedButton(onClick = { showFraudGuide = true }) {
                                Text(if (helpEnglish) "Open safety checklist" else "Deschide lista de verificare")
                            }
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = paleGreen)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (helpEnglish) "Developer options explained" else "Opțiuni pentru dezvoltatori, explicate", fontWeight = FontWeight.Bold, color = navy)
                            Text(if (helpEnglish) "Understand advanced Android settings, risks and safe defaults before making changes."
                                else "Înțelege setările Android avansate, riscurile și valorile recomandate înainte să modifici ceva.")
                            OutlinedButton(onClick = { showDeveloperGuide = true }) {
                                Text(if (helpEnglish) "Read the guide" else "Citește ghidul")
                            }
                        }
                    }
                    FoniWiseGlossary(
                        english = helpEnglish,
                        onBack = { selectedTab = 0 },
                        onLanguageChange = { chooseLanguage(it) }
                    )
                }
                if (selectedTab == 2) {
                    Text(if (helpEnglish) "Recommendations & Actions" else "Recomandări și acțiuni",
                        style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = navy)
                    Text(if (helpEnglish) "Practical steps you control. FoniWise does not change Android settings automatically." else
                        "Pași practici pe care îi controlezi tu. FoniWise nu modifică automat setările Android.")
                    Card(colors = CardDefaults.cardColors(containerColor = paleGreen)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (helpEnglish) "Step-by-step security assistant" else "Asistent de securitate pas cu pas",
                                fontWeight = FontWeight.Bold, color = navy)
                            Text(if (helpEnglish) "Three manual checks: screen lock, Android updates and special access. FoniWise does not claim to read inaccessible settings." else
                                "Trei verificări manuale: blocarea ecranului, actualizările Android și accesările speciale. FoniWise nu pretinde că citește setări inaccesibile.")
                            OutlinedButton(onClick = { showSecuritySteps = true }) {
                                Text(if (helpEnglish) "Start the 3 steps" else "Începe cei 3 pași")
                            }
                        }
                    }
                    Card(colors = CardDefaults.cardColors(containerColor = paleGreen)) {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            Text(if (helpEnglish) "Manual cache cleaning" else "Curățare manuală cache",
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
                            Text(if (helpEnglish) "App permissions" else "Permisiunile aplicațiilor",
                                fontWeight = FontWeight.Bold, color = navy)
                            Text(if (helpEnglish) "Review permissions you no longer need. Changing them may affect app features." else
                                "Verifică permisiunile de care aplicațiile nu mai au nevoie. Modificarea lor poate afecta unele funcții.")
                            OutlinedButton(onClick = { showPermissionsGuide = true }) {
                                Text(if (helpEnglish) "Read permissions guide" else "Citește ghidul permisiunilor")
                            }
                            OutlinedButton(onClick = { launchSettings(context, Intent(Settings.ACTION_APPLICATION_SETTINGS)) }) {
                                Text(if (helpEnglish) "Open Apps settings" else "Deschide Setări aplicații")
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
                        Text(if (helpEnglish) "Local security scan" else "Scanare de securitate locală", fontWeight = FontWeight.Bold, color = green)
                        Text(if (helpEnglish) "Checks visible apps, sensitive permissions and some special Android access settings." else "Verifică aplicațiile vizibile, permisiunile sensibile și câteva accesări speciale Android.")
                        Text(if (helpEnglish) "This is not an antivirus. It does not read messages, photos, files or browsing history, and does not transmit data." else "Nu este antivirus. Nu citește mesaje, fotografii, fișiere sau istoricul browserului și nu transmite date.", style = MaterialTheme.typography.bodySmall)
                    }
                }
                Button(
                    onClick = { if (scanConsent) startScan() else showConsent = true },
                    enabled = !scanning,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (scanning) (if (helpEnglish) "Scanning…" else "Se scanează…") else if (report == null) (if (helpEnglish) "Start scan" else "Începe scanarea") else (if (helpEnglish) "Scan again" else "Scanează din nou"))
                }
                error?.let { InfoCard((if (helpEnglish) "Scan failed. Please try again." else "Scanarea nu a putut fi finalizată. Încearcă din nou.") + (if (it.isNotBlank()) " ($it)" else "")) }
                report?.let { scan ->
                    Card {
                        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(if (helpEnglish) "Device information" else "Informații despre dispozitiv", color = green, fontWeight = FontWeight.Bold)
                            Text(scan.deviceModel, fontWeight = FontWeight.Bold)
                            Text("Android ${scan.androidRelease} (API ${scan.sdkInt})")
                            Text((if (helpEnglish) "Security patch: " else "Patch de securitate: ") + scan.securityPatch)
                            Text(if (helpEnglish) "${scan.installedApps} visible apps · ${scan.userApps} third-party apps" else "${scan.installedApps} aplicații vizibile · ${scan.userApps} aplicații terțe")
                        }
                    }
                    Section(if (helpEnglish) "Special access" else "Accesări speciale", if (helpEnglish) "These are not automatically threats. Check whether you recognize the apps." else "Nu reprezintă automat amenințări. Verifică dacă recunoști aplicațiile.")
                    if (scan.specialAccess.isEmpty()) InfoCard(if (helpEnglish) "No special access found in the checked categories." else "Nu au fost găsite accesări speciale în categoriile verificate.")
                    scan.specialAccess.forEach { item ->
                        Card {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(if (helpEnglish) translateScanLabel(item.category) else item.category, fontWeight = FontWeight.Bold, color = green)
                                Text(item.appLabel)
                                Text(item.packageName, style = MaterialTheme.typography.labelSmall)
                                Text(if (helpEnglish) translateScanLabel(item.explanation) else item.explanation, style = MaterialTheme.typography.bodySmall)
                                OutlinedButton(onClick = { openSettingsFor(context, item) }) { Text(if (helpEnglish) "Review in Settings" else "Verifică în Setări") }
                            }
                        }
                    }
                    Section(if (helpEnglish) "Granted sensitive permissions" else "Permisiuni sensibile acordate", if (helpEnglish) "Some permissions are necessary for apps to function normally." else "Unele permisiuni sunt necesare pentru funcționarea normală a aplicațiilor.")
                    if (scan.permissionApps.isEmpty()) InfoCard(if (helpEnglish) "No third-party apps found with the checked permissions." else "Nu au fost găsite aplicații terțe cu permisiunile verificate.")
                    scan.permissionApps.take(40).forEach { app ->
                        Card {
                            Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
                                Text(app.appLabel, fontWeight = FontWeight.Bold)
                                Text(app.grantedPermissions.joinToString(" • ") { if (helpEnglish) translateScanLabel(it) else it }, style = MaterialTheme.typography.bodySmall)
                                Text(app.packageName, style = MaterialTheme.typography.labelSmall)
                                TextButton(onClick = { openAppDetails(context, app.packageName) }) {
                                    Text(if (helpEnglish) "App details" else "Detalii aplicație")
                                }
                            }
                        }
                    }
                    if (scan.permissionApps.size > 40) Text(if (helpEnglish) "Showing the first 40 of ${scan.permissionApps.size} apps." else "Sunt afișate primele 40 din ${scan.permissionApps.size} aplicații.")
                    InfoCard(if (helpEnglish) "This checks configuration, not malware. It does not analyze app code and cannot confirm that a device is malware-free." else "Acesta este un control de configurare, nu o scanare antivirus. Nu analizează codul aplicațiilor și nu poate confirma că dispozitivul este curat.")
                }

                }
                if (selectedTab == 4) {
                Section(if (helpEnglish) "Notification activity" else "Activitatea notificărilor", if (helpEnglish) "Notification inspector: identifies notification sources locally." else "Inspectorul notificărilor: identifică local sursele notificărilor.")
                Card {
                    Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text(
                            if (notificationAccessEnabled) (if (helpEnglish) "Notification access enabled" else "Acces la notificări activ") else (if (helpEnglish) "Notification access disabled" else "Acces la notificări dezactivat"),
                            fontWeight = FontWeight.Bold,
                            color = green
                        )
                        Text(if (helpEnglish) "Stores up to 100 local records: only the source package and time. It does not read notification titles, message text or senders." else "Păstrează local maximum 100 de înregistrări: numai pachetul-sursă și ora. Nu citește titlul, textul sau expeditorul notificării.", style = MaterialTheme.typography.bodySmall)
                        if (!notificationAccessEnabled) {
                            Button(onClick = { showNotificationConsent = true }) { Text(if (helpEnglish) "Set up inspector" else "Configurează inspectorul") }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(onClick = onRefreshNotifications) { Text(if (helpEnglish) "Refresh" else "Reîmprospătează") }
                                TextButton(onClick = {
                                    launchSettings(context, Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                                }) { Text(if (helpEnglish) "Manage access" else "Gestionează accesul") }
                            }
                        }
                        if (notificationEvents.isEmpty()) {
                            Text(if (helpEnglish) "No local records." else "Nu există înregistrări locale.")
                        } else {
                            Text(if (helpEnglish) "${notificationEvents.size} local records" else "${notificationEvents.size} înregistrări locale")
                            TextButton(onClick = { showDeleteConfirmation = true }) { Text(if (helpEnglish) "Clear history" else "Șterge istoricul") }
                            val sources = NotificationFrequencyAnalyzer.recentSources(notificationEvents, System.currentTimeMillis())
                            Text(if (helpEnglish) "Sources in the last 15 minutes" else "Surse în ultimele 15 minute", fontWeight = FontWeight.Bold)
                            if (sources.isEmpty()) Text(if (helpEnglish) "No recent events." else "Nu există evenimente recente.")
                            sources.take(5).forEach { source ->
                                Text("${source.packageName}: ${source.count} " + (if (helpEnglish) "notifications" else "notificări") +
                                    if (source.count >= NotificationFrequencyAnalyzer.NOTICE_THRESHOLD) (if (helpEnglish) " · High frequency" else " · Frecvență ridicată") else "")
                            }
                            Text(if (helpEnglish) "High notification frequency does not automatically indicate malware." else "Frecvența ridicată nu indică automat malware.", style = MaterialTheme.typography.bodySmall)
                            notificationEvents.take(20).forEach { event ->
                                Text("${event.packageName} · ${java.text.DateFormat.getDateTimeInstance().format(java.util.Date(event.postedAtMillis))}",
                                    style = MaterialTheme.typography.bodySmall)
                                TextButton(onClick = { openAppDetails(context, event.packageName) }) { Text(if (helpEnglish) "App details" else "Detalii aplicație") }
                            }
                        }
                    }
                }
                }
                Text(if (helpEnglish) "FoniWise 0.7.3-rc1 • local analysis • no account or ads" else "FoniWise 0.7.3-rc1 • analiză locală • fără cont și fără reclame", style = MaterialTheme.typography.labelSmall)
            }
        }
    }

    if (showDeveloperGuide) {
        DeveloperOptionsGuideDialog(
            english = helpEnglish,
            onDismiss = { showDeveloperGuide = false },
            onSettings = { launchSettings(context, Intent(Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS)) }
        )
    }
    if (showSecuritySteps) {
        SecurityStepsDialog(
            english = helpEnglish,
            onLanguageChange = { chooseLanguage(it) },
            onDismiss = { showSecuritySteps = false },
            onSettings = { step ->
                val action = when (step) {
                    0 -> Settings.ACTION_SECURITY_SETTINGS
                    1 -> "android.settings.SYSTEM_UPDATE_SETTINGS"
                    2 -> Settings.ACTION_APPLICATION_SETTINGS
                    else -> Settings.ACTION_SETTINGS
                }
                launchSettings(context, Intent(action))
            }
        )
    }

    if (showFraudGuide) {
        SafeguardInfoDialog(
            titleRo = "Ghid antifraudă și antiphishing",
            titleEn = "Fraud and phishing safety guide",
            summaryRo = "Escrocii pot imita bănci, curieri, instituții sau persoane cunoscute prin SMS, e-mail, mesaje și apeluri. Un mesaj urgent, o amenințare sau o ofertă prea bună pot fi semne de alarmă.",
            summaryEn = "Scammers may impersonate banks, delivery services, institutions or people you know via texts, emails, messages and calls. Urgency, threats or offers that seem too good to be true can be warning signs.",
            doesRo = "Înainte de a deschide un link: 1. Oprește-te dacă mesajul cere acțiune imediată. 2. Verifică atent adresa și expeditorul; numele afișat poate fi falsificat. 3. Intră separat în aplicația sau site-ul oficial, fără să folosești linkul primit. 4. Nu introduce parole, PIN-uri sau coduri de autentificare dintr-un mesaj neașteptat.",
            doesEn = "Before opening a link: 1. Pause if the message demands immediate action. 2. Check the address and sender carefully; displayed names can be spoofed. 3. Open the official app or website independently, not through the received link. 4. Do not enter passwords, PINs or verification codes requested by an unexpected message.",
            limitsRo = "FoniWise nu citește automat SMS-uri, conversații sau e-mailuri și nu poate garanta că un mesaj ori un link este sigur. Acest ghid este educativ, nu un detector automat de fraude.",
            limitsEn = "FoniWise does not automatically read texts, chats or emails and cannot guarantee that a message or link is safe. This is an educational guide, not an automatic fraud detector.",
            adviceRo = "Dacă cineva îți cere un cod bancar sau să transferi urgent bani, închide apelul și contactează instituția prin numărul din aplicația oficială sau de pe card. Dacă ai introdus deja date sensibile, schimbă parola prin canalul oficial și contactează imediat banca atunci când sunt implicate date bancare. Nu instala aplicații de control la distanță la cererea unui apelant necunoscut.",
            adviceEn = "If someone asks for a banking code or an urgent transfer, end the call and contact the institution using the number in its official app or on your card. If you already shared sensitive information, change the password through the official channel and contact your bank immediately if banking data is involved. Never install remote-control apps at an unknown caller's request.",
            english = helpEnglish,
            onLanguageChange = { chooseLanguage(it) },
            onDismiss = { showFraudGuide = false },
            onSettings = null
        )
    }

    if (showPermissionsGuide) {
        SafeguardInfoDialog(
            titleRo = "Ghid pentru permisiunile aplicațiilor",
            titleEn = "App permissions guide",
            summaryRo = "O permisiune permite unei aplicații să folosească anumite funcții sau date. Camera face fotografii, microfonul înregistrează sunete, locația indică poziția, contactele oferă acces la agenda ta, iar notificările permit afișarea de alerte. Accesul la citirea notificărilor este separat de permisiunea de a le afișa.",
            summaryEn = "Permissions allow an app to use features or data. Camera captures images, microphone records audio, location indicates position, contacts provide access to your address book, and notification permission allows alerts. Reading other apps' notifications requires separate special access.",
            doesRo = "FoniWise te ajută să înțelegi ce să verifici. Deschide Setări → Aplicații → alege o aplicație → Permisiuni. Verifică dacă fiecare acces este necesar pentru funcția folosită.",
            doesEn = "FoniWise helps explain what to review. Open Settings → Apps → choose an app → Permissions. Consider whether each access is needed for the features you use.",
            limitsRo = "O permisiune acordată nu dovedește că aplicația este periculoasă. FoniWise nu revocă automat permisiuni și nu poate vedea toate aplicațiile sau toate utilizările unei permisiuni pe orice telefon.",
            limitsEn = "A granted permission does not prove an app is dangerous. FoniWise does not revoke permissions automatically and cannot see every app or every use of a permission on all phones.",
            adviceRo = "Exemplu: dacă o aplicație de lanternă cere microfonul, întreabă-te ce funcție îl justifică. Pentru cameră și microfon verifică dacă sunt necesare; pentru locație poți alege accesul doar în timpul folosirii, dacă Android oferă opțiunea; pentru contacte și notificări verifică dacă recunoști motivul. Nu dezactiva la întâmplare permisiuni necesare funcționării.",
            adviceEn = "Example: if a flashlight app asks for microphone access, consider which feature needs it. Review camera and microphone access; for location, choose while-in-use access when Android offers it; for contacts and notifications, check whether the reason makes sense. Do not randomly disable permissions needed for an app to work.",
            english = helpEnglish,
            onLanguageChange = { chooseLanguage(it) },
            onDismiss = { showPermissionsGuide = false },
            onSettings = {
                showPermissionsGuide = false
                launchSettings(context, Intent(Settings.ACTION_APPLICATION_SETTINGS))
            }
        )
    }

    if (showCacheGuide) {
        SafeguardInfoDialog(
            titleRo = "Cache Advisor",
            titleEn = "Cache Advisor",
            summaryRo = "Cache-ul este un spațiu în care aplicațiile păstrează temporar imagini și alte informații, ca să se deschidă mai repede. În timp, unele aplicații pot ocupa mult spațiu. Poți recupera spațiu pe telefon ștergând manual cache-ul unei aplicații, dacă Android permite.",
            summaryEn = "A cache is temporary storage where apps keep images and other information so they can open faster. Over time, some apps may use a lot of space. You may recover phone storage by manually clearing an app\'s cache, if Android allows it.",
            doesRo = "FoniWise îți explică pașii pentru a curăța manual cache-ul unei aplicații. Poți deschide lista aplicațiilor Android, alege aplicația dorită, apoi căuta Stocare → Șterge cache. Numele opțiunilor diferă între telefoane.",
            doesEn = "FoniWise explains how to clear an app's cache manually. Open the Android app list, choose an app, then look for Storage → Clear cache. The labels vary by phone.",
            limitsRo = "FoniWise nu șterge automat cache-ul altor aplicații și nu garantează că telefonul va deveni mai rapid. Atenție: «Șterge datele» sau «Golire stocare» este altă acțiune și poate elimina setări ori sesiuni de conectare.",
            limitsEn = "FoniWise does not automatically clear other apps' caches and cannot guarantee a faster phone. Warning: Clear data or Clear storage is a different action and may remove settings or signed-in sessions.",
            adviceRo = "Curăță cache-ul doar dacă o aplicație ocupă prea mult spațiu sau nu funcționează corect. Începe cu aplicația care are problema și verifică spațiul liber după aceea. Nu este necesar să ștergi cache-ul tuturor aplicațiilor în fiecare zi. Aplicația poate crea din nou fișiere temporare la următoarea utilizare, iar unele imagini sau fișiere vor trebui descărcate din nou, folosind date mobile dacă nu ești conectat la Wi-Fi. Spațiul eliberat poate fi temporar.",
            adviceEn = "Clear cache only when an app uses too much storage or is not working correctly. Start with the affected app and check available storage afterward. You do not need to clear every app's cache each day. The app may recreate temporary files, and some images or files may need to download again, using mobile data if you are not on Wi-Fi. Freed space may be temporary.",
            english = helpEnglish,
            onLanguageChange = { chooseLanguage(it) },
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
            onLanguageChange = { chooseLanguage(it) },
            onDismiss = { showHelp = false }
        )
    }

    if (showConsent) {
        AlertDialog(
            onDismissRequest = { showConsent = false },
            title = { Text(if (helpEnglish) "Consent for local scan" else "Acord pentru scanarea locală") },
            text = { Text(if (helpEnglish) "FoniWise will locally check visible apps, granted permissions and certain special access settings. Your app inventory is sensitive. This version does not transmit or save it. Continue?" else "FoniWise va citi local lista aplicațiilor vizibile, permisiunile acordate și unele setări de acces special. Inventarul aplicațiilor este sensibil. În această versiune nu este transmis sau salvat. Continui?") },
            confirmButton = {
                TextButton(onClick = { scanConsent = true; showConsent = false; startScan() }) { Text(if (helpEnglish) "Accept and scan" else "Accept și scanează") }
            },
            dismissButton = { TextButton(onClick = { showConsent = false }) { Text(if (helpEnglish) "Cancel" else "Anulează") } }
        )
    }
    if (showNotificationConsent) {
        AlertDialog(
            onDismissRequest = { showNotificationConsent = false },
            title = { Text(if (helpEnglish) "Special notification access" else "Acces special la notificări") },
            text = { Text(if (helpEnglish) "Android warns that notification services may access notification content. FoniWise uses only the source package and time, stored locally for up to 100 events. You enable access only through Android Settings." else "Android avertizează că serviciile de notificări pot vedea conținutul notificărilor. FoniWise folosește doar pachetul-sursă și ora, păstrate local, maximum 100 de evenimente. Accesul se activează numai din Setările Android.") },
            confirmButton = {
                TextButton(onClick = {
                    showNotificationConsent = false
                    launchSettings(context, Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                }) { Text(if (helpEnglish) "Open Settings" else "Deschide Setări") }
            },
            dismissButton = { TextButton(onClick = { showNotificationConsent = false }) { Text(if (helpEnglish) "Not now" else "Renunță") } }
        )
    }
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(if (helpEnglish) "Delete local history?" else "Ștergi istoricul local?") },
            text = { Text(if (helpEnglish) "All records stored by FoniWise will be deleted." else "Vor fi șterse toate înregistrările păstrate de FoniWise.") },
            confirmButton = {
                TextButton(onClick = {
                    NotificationLogStore(context).clear()
                    showDeleteConfirmation = false
                    onRefreshNotifications()
                }) { Text(if (helpEnglish) "Delete" else "Șterge") }
            },
            dismissButton = { TextButton(onClick = { showDeleteConfirmation = false }) { Text(if (helpEnglish) "Keep" else "Păstrează") } }
        )
    }
}

@Composable
private fun FoniWiseLanguageWelcome(onChooseLanguage: (Boolean) -> Unit) {
    var english by rememberSaveable { mutableStateOf(false) }
    MaterialTheme(colorScheme = lightColorScheme(primary = green, background = paleBlue, surface = Color.White)) {
        Column(
            modifier = Modifier.fillMaxSize().background(paleBlue).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = green),
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.fillMaxWidth().padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    BrandLogo(108.dp)
                    Spacer(Modifier.height(10.dp))
                    Text("FoniWise", color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
                    Text(if (english) "Know Your Phone" else "Cunoaște-ți telefonul", color = Color.White)
                }
            }
            Spacer(Modifier.height(28.dp))
            Text("Alege limba / Choose your language", style = MaterialTheme.typography.titleLarge, color = navy, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Text(if (english) "You can change your language at any time." else "Poți schimba limba oricând.", color = navy, textAlign = TextAlign.Center)
            Spacer(Modifier.height(22.dp))
            listOf(false to "🇷🇴 Română", true to "🇬🇧 English").forEach { (optionEnglish, label) ->
                Surface(
                    onClick = { english = optionEnglish },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = if (english == optionEnglish) paleGreen else Color.White,
                    border = BorderStroke(if (english == optionEnglish) 2.dp else 1.dp, green)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = english == optionEnglish, onClick = { english = optionEnglish })
                        Spacer(Modifier.width(10.dp))
                        Text(label, color = navy, style = MaterialTheme.typography.titleMedium)
                    }
                }
            }
            Spacer(Modifier.height(22.dp))
            Button(onClick = { onChooseLanguage(english) }, modifier = Modifier.fillMaxWidth()) {
                Text(if (english) "Continue" else "Continuă")
            }
        }
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
private fun DashboardOverview(hasReport: Boolean, scanning: Boolean, english: Boolean) {
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
                if (english) "Welcome to FoniWise!" else "Bun venit în FoniWise!",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = navy
            )
            Text(
                if (english) "Understand your phone, discover available tools and decide what works for you." else "Înțelege-ți telefonul, descoperă instrumentele disponibile și decide ce este potrivit pentru tine.",
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
                                scanning -> if (english) "Local check in progress" else "Verificare locală în desfășurare"
                                hasReport -> if (english) "Local check completed" else "Verificare locală finalizată"
                                else -> if (english) "Local check available" else "Verificare locală disponibilă"
                            },
                            fontWeight = FontWeight.SemiBold,
                            color = navy
                        )
                        Text(
                            if (hasReport) (if (english) "Results are available in the security scan." else "Rezultatele sunt disponibile în scanarea de securitate.")
                            else (if (english) "Does not provide real-time antivirus protection." else "Nu oferă protecție antivirus în timp real."),
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
    howItHelps: String,
    english: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 218.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = paleGreen),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFC5E9B6))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp, Alignment.Top)
        ) {
            Icon(imageVector = symbol, contentDescription = null, tint = green, modifier = Modifier.size(38.dp))
            Text(title, fontWeight = FontWeight.Bold, color = navy, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, lineHeight = 16.sp)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = green, textAlign = TextAlign.Center, maxLines = 2)
            Text(howItHelps, style = MaterialTheme.typography.labelSmall, color = navy, textAlign = TextAlign.Center)
        }
    }
}

@Composable
private fun DeveloperOptionsGuideDialog(
    english: Boolean,
    onDismiss: () -> Unit,
    onSettings: () -> Unit
) {
    val topics = if (english) listOf(
        "What is this menu?" to "Developer options are advanced Android controls intended mainly for testing and troubleshooting. They are usually hidden. You do not need to enable them for everyday phone use.",
        "How to find it" to "On many phones, go to Settings → About phone → Software information → Build number and tap it seven times. Enter your screen lock if asked. On Samsung, Developer options usually appears near the bottom of Settings. Paths differ by model; do not activate this menu without a reason.",
        "Animation scales" to "Window, transition and animator duration scales control interface animation length. At 0.5x, transitions may feel faster, but CPU power and physical RAM do not increase. Write down the original values and restore them if needed.",
        "USB and wireless debugging" to "These features allow development tools to communicate with the phone after authorization or pairing. Keep them off unless actively using trusted tools. Never approve an unfamiliar computer or pairing request; turn debugging off when finished.",
        "Background process limit" to "Restricting background processes can interrupt notifications, music, syncing and switching between apps. Android already manages memory. Keep the default unless you are deliberately testing an app.",
        "Graphics and GPU settings" to "Options such as Force 4x MSAA may affect battery use, heat, graphics quality or compatibility. They are not universal performance boosts. Leave defaults unchanged without a specific reason.",
        "OEM unlocking" to "This advanced option relates to bootloader unlocking on supported phones. Actually unlocking a bootloader can erase data and change security protections. Seeing the option does not mean the phone is unlocked. Do not use it as a routine optimization.",
        "Stay awake while charging" to "Keeps the display on while the phone is charging, mainly for testing. It can increase power use and leave private content visible. Leave it off unless you need it briefly.",
        "Running services" to "Shows services and processes currently running, which can help developers investigate resource use. A running service is not automatically suspicious. Avoid stopping unfamiliar system services.",
        "Memory statistics" to "Shows memory use over time for apps and the system. High RAM usage alone does not mean the phone is slow: Android uses memory for caching. Investigate only alongside a real problem.",
        "Show taps" to "Displays a visual marker wherever you touch the screen, useful for demonstrations and recordings. It does not improve touch accuracy. Turn it off after testing if distracting.",
        "Pointer location" to "Overlays touch coordinates and gesture traces for input testing. It can clutter the screen and reveal interaction patterns in recordings. Keep it off for normal use.",
        "Show layout bounds" to "Draws outlines around interface elements so developers can inspect layouts. It does not repair app display problems. Disable it when you finish inspecting.",
        "Simulate color space" to "Previews how an interface may look with different color perception conditions. It changes the display preview, not the actual colors stored in photos. Restore Disabled after testing.",
        "Force dark mode" to "May attempt to apply a dark appearance to apps that do not support it, depending on Android version. Some text or controls may become hard to read. Revert it if an app looks wrong.",
        "Default USB configuration" to "Controls the initial USB connection mode, such as charging or file transfer, when supported. File transfer can expose phone files to a connected trusted computer after unlocking. Prefer charging by default.",
        "Bluetooth audio codec" to "Lets compatible devices negotiate audio formats. Available codecs depend on both phone and headphones. Forcing an unsupported choice may revert or interrupt audio; Auto/default is usually best.",
        "Bluetooth sample rate" to "Changes an audio parameter only when the selected codec and connected device support it. Higher numbers do not guarantee better sound and can affect stability. Keep the default unless troubleshooting.",
        "Smallest width" to "Changes the display density layout measured in dp, affecting the apparent size of text and controls. Extreme values can make the interface difficult to use. Record the original number before any experiment.",
        "Show refresh rate" to "Displays the screen refresh rate for testing smoothness. It is an indicator, not a speed boost, and may vary with content and device. Turn off the overlay after checking.",
        "Force peak refresh rate" to "On supported devices, may keep the display at a higher refresh rate more often. This can increase battery use and may not affect every app. Prefer the regular display settings unless testing.",
        "Bluetooth HCI snoop log" to "Records technical Bluetooth traffic for troubleshooting. Logs may contain sensitive connection information. Do not enable it routinely or share logs publicly.",
        "Select mock location app" to "Lets a designated testing app provide simulated locations. Useful for developers, but navigation and location-based services may show misleading results. Disable the test setup afterwards.",
        "USB debugging authorizations" to "Lets you revoke previously trusted computers for USB debugging. Useful after using a shared or untrusted computer. Revoking access does not erase your files or automatically fix other security issues.",
        "Wireless display certification" to "Adds testing controls for wireless displays on supported phones. It does not improve Wi-Fi speed or picture quality by itself. Keep it disabled unless diagnosing casting problems.",
        "Strict mode enabled" to "May flash the screen when an app performs certain slow operations on its main thread. This is a developer diagnostic, not a phone acceleration feature. Turn it off outside testing.",
        "Do not keep activities" to "Destroys app screens when you leave them to test lifecycle behavior. It can cause frequent reloads or lost unsaved state. Leave it off for daily use.",
        "Safe approach" to "Change only one setting at a time, note its original value and observe the result. If unsure, leave it alone. FoniWise does not enable Developer options or change these settings automatically."
    ) else listOf(
        "Ce este acest meniu?" to "Opțiunile pentru dezvoltatori sunt controale Android avansate, destinate în principal testării și depanării. De obicei sunt ascunse. Nu trebuie activate pentru utilizarea obișnuită a telefonului.",
        "Cum îl găsești" to "Pe multe telefoane: Setări → Despre telefon → Informații software → Număr versiune (Build number), apoi apasă de șapte ori. Introdu codul de blocare dacă este cerut. Pe Samsung, meniul apare de obicei spre finalul Setărilor. Traseul diferă după model; nu activa meniul fără un motiv.",
        "Scara animațiilor" to "Scara animațiilor ferestrelor, tranzițiilor și animatorului controlează durata efectelor vizuale. La 0,5x, trecerile pot părea mai rapide, dar puterea procesorului și memoria RAM fizică nu cresc. Notează valorile inițiale și revino la ele dacă este nevoie.",
        "Depanare USB și wireless" to "Aceste funcții permit instrumentelor de dezvoltare să comunice cu telefonul după autorizare sau asociere. Lasă-le dezactivate dacă nu folosești activ instrumente de încredere. Nu aproba computere sau cereri de asociere necunoscute; oprește depanarea când termini.",
        "Limita proceselor în fundal" to "Restricționarea proceselor poate întrerupe notificările, muzica, sincronizarea și trecerea între aplicații. Android gestionează deja memoria. Păstrează valoarea implicită dacă nu testezi intenționat o aplicație.",
        "Setări grafice și GPU" to "Opțiuni precum Forțează 4x MSAA pot afecta consumul bateriei, temperatura, calitatea grafică sau compatibilitatea. Nu sunt soluții universale pentru performanță. Păstrează valorile implicite fără un motiv concret.",
        "Deblocare OEM" to "Această opțiune avansată privește deblocarea bootloaderului pe telefoanele compatibile. Deblocarea efectivă poate șterge date și modifica protecțiile de securitate. Prezența opțiunii nu înseamnă că telefonul este deblocat. Nu o folosi pentru optimizări obișnuite.",
        "Ecran activ la încărcare" to "Menține ecranul aprins în timpul încărcării, mai ales pentru testare. Poate crește consumul și poate lăsa informații personale la vedere. Lasă opțiunea dezactivată dacă nu ai nevoie temporar de ea.",
        "Servicii în execuție" to "Arată servicii și procese active, utile dezvoltatorilor pentru investigarea resurselor. Un serviciu activ nu este automat suspect. Nu opri servicii de sistem necunoscute.",
        "Statistici de memorie" to "Prezintă utilizarea memoriei de către aplicații și sistem în timp. Mult RAM ocupat nu înseamnă automat telefon lent: Android folosește memoria și pentru cache. Investighează doar dacă există o problemă reală.",
        "Afișare atingeri" to "Arată un marcaj acolo unde atingi ecranul, util în demonstrații și înregistrări. Nu îmbunătățește precizia ecranului tactil. Dezactivează-l după test dacă te deranjează.",
        "Locația indicatorului" to "Suprapune coordonatele atingerilor și traseele gesturilor pentru testare. Poate aglomera ecranul și dezvălui gesturi în înregistrări. Păstreaz-o dezactivată în utilizarea obișnuită.",
        "Afișare limite aspect" to "Desenează contururi în jurul elementelor interfeței pentru verificarea aspectului aplicațiilor. Nu repară problemele de afișare. Dezactivează opțiunea după verificare.",
        "Simulare spațiu de culoare" to "Permite previzualizarea interfeței pentru diferite moduri de percepție a culorilor. Schimbă previzualizarea ecranului, nu culorile stocate în fotografii. Revino la Dezactivat după test.",
        "Forțare mod întunecat" to "Poate încerca să aplice o temă întunecată aplicațiilor care nu o oferă, în funcție de versiunea Android. Unele texte sau butoane pot deveni greu de citit. Revino la setarea inițială dacă apar probleme.",
        "Configurație USB implicită" to "Stabilește modul inițial al conexiunii USB, precum încărcare sau transfer de fișiere, când telefonul permite. Transferul poate expune fișiere unui computer de încredere conectat după deblocare. Preferă încărcarea implicită.",
        "Codec audio Bluetooth" to "Permite alegerea formatelor audio compatibile. Codecurile disponibile depind de telefon și căști. Forțarea unei opțiuni incompatibile poate fi ignorată sau întrerupe sunetul; setarea automată este de obicei potrivită.",
        "Rată de eșantionare Bluetooth" to "Schimbă un parametru audio numai dacă dispozitivul și codecul îl acceptă. Valori mai mari nu garantează sunet mai bun și pot afecta stabilitatea. Păstrează setarea implicită fără un motiv concret.",
        "Lățime minimă" to "Schimbă densitatea logică a interfeței, exprimată în dp, influențând mărimea aparentă a textelor și butoanelor. Valorile extreme pot face telefonul greu de folosit. Notează cifra inițială înainte de test.",
        "Afișare rată de reîmprospătare" to "Afișează frecvența de actualizare a ecranului pentru verificarea fluidității. Este un indicator, nu o accelerare a telefonului, și poate varia după conținut. Oprește afișajul după verificare.",
        "Forțare rată maximă de reîmprospătare" to "Pe telefoanele compatibile poate menține mai des o rată de reîmprospătare ridicată. Poate crește consumul bateriei și nu afectează orice aplicație. Preferă setările normale ale ecranului.",
        "Jurnalizare Bluetooth HCI" to "Înregistrează trafic Bluetooth tehnic pentru depanare. Jurnalele pot conține informații sensibile despre conexiuni. Nu activa funcția permanent și nu publica jurnalele.",
        "Selectare aplicație pentru locații simulate" to "Permite unei aplicații de test să furnizeze locații simulate. Este utilă dezvoltatorilor, dar navigația și serviciile bazate pe locație pot afișa rezultate greșite. Dezactivează configurația după test.",
        "Revocare autorizări depanare USB" to "Îți permite să retragi încrederea acordată anterior computerelor pentru depanare USB. Util după conectarea la un computer comun sau nesigur. Nu șterge fișiere și nu rezolvă automat alte riscuri.",
        "Certificare afișaj wireless" to "Oferă controale de test pentru afișaje wireless pe telefoanele compatibile. Nu mărește automat viteza Wi-Fi sau calitatea imaginii. Las-o dezactivată dacă nu investighezi probleme de transmitere.",
        "Mod strict activat" to "Poate face ecranul să clipească atunci când o aplicație execută operații lente pe firul principal. Este un instrument de diagnostic, nu o metodă de accelerare. Dezactivează-l în afara testelor.",
        "Nu păstra activitățile" to "Închide ecranele aplicațiilor când le părăsești pentru testarea comportamentului acestora. Poate provoca reîncărcări frecvente sau pierderea datelor nesalvate. Lasă opțiunea dezactivată în utilizarea zilnică.",
        "Cum procedezi în siguranță" to "Schimbă o singură setare pe rând, notează valoarea inițială și observă rezultatul. Dacă nu ești sigur, las-o neschimbată. FoniWise nu activează Opțiunile pentru dezvoltatori și nu modifică automat aceste setări."
    )
    val groups = if (english) listOf(
        "Start here" to listOf(0, 1, 27),
        "Screen and interface" to listOf(2, 7, 10, 11, 12, 13, 14, 18, 19, 20),
        "Performance and memory" to listOf(4, 5, 8, 9, 25, 26),
        "Connections and debugging" to listOf(3, 15, 16, 17, 21, 23, 24),
        "Security and location" to listOf(6, 22)
    ) else listOf(
        "Începe de aici" to listOf(0, 1, 27),
        "Ecran și interfață" to listOf(2, 7, 10, 11, 12, 13, 14, 18, 19, 20),
        "Performanță și memorie" to listOf(4, 5, 8, 9, 25, 26),
        "Conexiuni și depanare" to listOf(3, 15, 16, 17, 21, 23, 24),
        "Securitate și locație" to listOf(6, 22)
    )
    var selectedGroup by rememberSaveable { mutableIntStateOf(-1) }
    var selectedTopic by rememberSaveable { mutableIntStateOf(-1) }
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = { Text(if (english) "Developer options explained" else "Opțiuni pentru dezvoltatori, explicate", color = navy) },
        text = {
            key(selectedGroup, selectedTopic) {
                Column(modifier = Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    if (selectedTopic >= 0) {
                        OutlinedButton(onClick = { selectedTopic = -1 }) {
                            Text(if (english) "Back to category" else "Înapoi la categorie")
                        }
                        Text(topics[selectedTopic].first, color = green, fontWeight = FontWeight.Bold)
                        Text(topics[selectedTopic].second, color = navy)
                        Text(if (english) "FoniWise cannot check or change this setting for you."
                            else "FoniWise nu poate verifica sau modifica această setare în locul tău.",
                            color = navy, style = MaterialTheme.typography.bodySmall)
                    } else if (selectedGroup >= 0) {
                        OutlinedButton(onClick = { selectedGroup = -1 }) {
                            Text(if (english) "All categories" else "Toate categoriile")
                        }
                        Text(groups[selectedGroup].first, color = green, fontWeight = FontWeight.Bold)
                        groups[selectedGroup].second.forEach { index ->
                            OutlinedButton(onClick = { selectedTopic = index }, modifier = Modifier.fillMaxWidth()) {
                                Text(topics[index].first)
                            }
                        }
                    } else {
                        Text(if (english) "Choose a category. Some settings may be absent or named differently on your phone."
                            else "Alege o categorie. Unele setări pot lipsi sau pot avea alte denumiri pe telefonul tău.",
                            color = navy, style = MaterialTheme.typography.bodySmall)
                        groups.forEachIndexed { index, group ->
                            OutlinedButton(onClick = { selectedGroup = index }, modifier = Modifier.fillMaxWidth()) {
                                Text(group.first + " (" + group.second.size + ")")
                            }
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(if (english) "Close" else "Închide") } },
        dismissButton = { TextButton(onClick = onSettings) { Text(if (english) "Open Android settings" else "Deschide setările Android") } }
    )
}

@Composable
private fun SecurityStepsDialog(
    english: Boolean,
    onLanguageChange: (Boolean) -> Unit,
    onDismiss: () -> Unit,
    onSettings: (Int) -> Unit
) {
    var step by rememberSaveable { mutableIntStateOf(0) }
    val headingsRo = listOf("Blocarea ecranului", "Actualizări Android", "Accesări speciale")
    val headingsEn = listOf("Screen lock", "Android updates", "Special access")
    val instructionsRo = listOf(
        "Deschide Setări → Securitate sau Ecran de blocare. Verifică dacă folosești un PIN, o parolă ori o metodă de blocare potrivită. Denumirile meniurilor diferă între telefoane.",
        "Deschide Setări → Actualizare software (sau Sistem → Actualizări) și verifică manual dacă există actualizări. Versiunea Android și data patch-ului afișate de FoniWise nu dovedesc singure că telefonul este sigur sau nesigur.",
        "În Setări → Aplicații, caută meniul Acces special (pe unele telefoane este în meniul cu trei puncte). Verifică separat accesul la notificări și dreptul de a instala aplicații din surse necunoscute. Pentru fiecare aplicație, întreabă-te dacă recunoști scopul accesului și dacă mai ai nevoie de el. Nu modifica setări dacă nu înțelegi consecințele."
    )
    val instructionsEn = listOf(
        "Open Settings → Security or Lock screen. Check whether you use a PIN, password or suitable screen lock. Menu names vary between devices.",
        "Open Settings → Software update (or System → Updates) and manually check for updates. The Android version and patch date shown by FoniWise alone do not prove a device safe or unsafe.",
        "In Settings → Apps, look for Special app access (on some phones it is under the three-dot menu). Review notification access and permission to install unknown apps separately. For each app, consider whether you recognize why it needs that access and whether you still need it. Do not change settings if you are unsure of the consequences."
    )
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White,
        title = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(if (english) "Security assistant" else "Asistent de securitate",
                    color = navy, style = MaterialTheme.typography.titleLarge)
                Text(if (english) "Step ${step + 1} of 3" else "Pasul ${step + 1} din 3",
                    color = green, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(false to "RO", true to "EN").forEach { (isEnglish, label) ->
                        OutlinedButton(onClick = { onLanguageChange(isEnglish) },
                            enabled = english != isEnglish) { Text(label) }
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(if (english) headingsEn[step] else headingsRo[step],
                    color = navy, fontWeight = FontWeight.Bold)
                Text(if (english) instructionsEn[step] else instructionsRo[step])
                Surface(color = paleGreen, shape = RoundedCornerShape(12.dp)) {
                    Text(
                        if (english) when (step) {
                            0 -> "Example: if the screen unlocks without a PIN, password or other lock, review the available screen-lock methods. FoniWise cannot read every lock setting automatically."
                            1 -> "Tip: on Samsung phones, look for Software update in Settings. Available updates depend on the manufacturer, model and region; the patch date alone is not a safety verdict."
                            else -> "Example: an app you no longer use may still have notification access. First consider which feature would stop working if you removed it; do not disable permissions indiscriminately. FoniWise cannot verify every special access setting automatically."
                        } else when (step) {
                            0 -> "Exemplu: dacă ecranul se deblochează fără PIN, parolă sau altă metodă de blocare, verifică opțiunile disponibile. FoniWise nu poate citi automat toate setările de blocare."
                            1 -> "Sfat: pe telefoanele Samsung, caută Actualizare software în Setări. Actualizările disponibile depind de producător, model și regiune; data patch-ului nu este singură un verdict de siguranță."
                            else -> "Exemplu: o aplicație pe care nu o mai folosești poate avea încă acces la notificări. Verifică întâi ce funcție ar pierde dacă ai retrage accesul; nu dezactiva permisiuni la întâmplare. FoniWise nu poate confirma automat toate accesările speciale."
                        },
                        modifier = Modifier.padding(12.dp), color = navy,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
                Text(
                    if (english) when (step) {
                        0 -> "After checking the screen-lock options, return to FoniWise and choose Next step. This is a manual check, not an automatic confirmation."
                        1 -> "If the update page does not open directly, find Software update in Android Settings. Return to FoniWise afterwards; the app does not install updates for you."
                        else -> "After reviewing each special access, return here and choose Finish. Finishing this guide does not certify the phone as secure."
                    } else when (step) {
                        0 -> "După verificarea opțiunilor de blocare, revino în FoniWise și apasă Pasul următor. Verificarea este manuală, nu o confirmare automată."
                        1 -> "Dacă pagina de actualizări nu se deschide direct, caută Actualizare software în Setările Android. Revino apoi în FoniWise; aplicația nu instalează actualizări în locul tău."
                        else -> "După verificarea fiecărui acces special, revino aici și apasă Finalizează. Încheierea ghidului nu certifică telefonul ca fiind sigur."
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = navy
                )
                OutlinedButton(onClick = { onSettings(step) }, modifier = Modifier.fillMaxWidth()) {
                    Text(if (english) (if (step == 0) "Open Security settings" else if (step == 1) "Open system updates" else "Open Apps settings")
                        else (if (step == 0) "Deschide setările de securitate" else if (step == 1) "Deschide actualizările de sistem" else "Deschide setările aplicațiilor"))
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { if (step < 2) step++ else onDismiss() }) {
                Text(if (step == 2) (if (english) "Finish" else "Finalizează")
                    else (if (english) "Next step" else "Pasul următor"))
            }
        },
        dismissButton = {
            TextButton(onClick = { if (step > 0) step-- else onDismiss() }) {
                Text(if (step > 0) (if (english) "Previous" else "Înapoi")
                    else (if (english) "Close" else "Închide"))
            }
        }
    )
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
                        Text(if (english) "Open Android settings" else "Deschide setările Android")
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

internal fun hasValidStorageReading(totalBytes: Long, availableBytes: Long): Boolean =
    totalBytes > 0L && availableBytes >= 0L && availableBytes <= totalBytes

private fun readDeviceMemory(context: Context): ActivityManager.MemoryInfo? =
    runCatching {
        val manager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        ActivityManager.MemoryInfo().also { manager.getMemoryInfo(it) }
    }.getOrNull()

internal fun hasValidRamReading(totalBytes: Long, availableBytes: Long): Boolean =
    totalBytes > 0L && availableBytes >= 0L && availableBytes <= totalBytes

private fun hasValidRamReading(info: ActivityManager.MemoryInfo?): Boolean =
    info != null && hasValidRamReading(info.totalMem, info.availMem)

/** Returns a percentage only for valid Android battery level/scale readings. */
internal fun batteryPercentage(level: Int, scale: Int): Int? =
    if (level < 0 || scale <= 0 || level > scale) null
    else (level.toLong() * 100L / scale).toInt()

@Composable
private fun DeviceHealthCards(context: Context, helpEnglish: Boolean, onLanguageChange: (Boolean) -> Unit) {
    var memory by remember { mutableStateOf(readDeviceMemory(context)) }
    val memorySnapshot = memory
    var showRamInfo by remember { mutableStateOf(false) }
    fun readBatteryLevel(): Int? = runCatching {
        val intent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = intent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = intent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        batteryPercentage(level, scale)
    }.getOrNull()
    var battery by remember { mutableStateOf(readBatteryLevel()) }
    fun readInternalStorage(): Pair<Long, Long>? = runCatching {
        val fs = StatFs(Environment.getDataDirectory().absolutePath)
        Pair(fs.totalBytes, fs.availableBytes)
    }.getOrNull()
    var storage by remember { mutableStateOf(readInternalStorage()) }
    val storageSnapshot = storage
    var showUpdateInfo by remember { mutableStateOf(false) }
    var showBatteryGuide by remember { mutableStateOf(false) }
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (helpEnglish) "Android and security updates" else "Android și actualizări de securitate",
                    fontWeight = FontWeight.Bold, color = green, modifier = Modifier.weight(1f))
                IconButton(onClick = { showUpdateInfo = true },
                    modifier = Modifier.semantics {
                        contentDescription = if (helpEnglish) "Information about Android updates" else "Informații despre actualizările Android"
                    }) {
                    Text("ⓘ", color = navy, style = MaterialTheme.typography.titleLarge)
                }
            }
            Text((if (helpEnglish) "Android version: " else "Versiune Android: ") + Build.VERSION.RELEASE)
            val patch = Build.VERSION.SECURITY_PATCH.takeIf { it.isNotBlank() }
            Text((if (helpEnglish) "Security patch: " else "Patch de securitate: ") +
                (patch ?: if (helpEnglish) "Not reported by Android" else "Neraportat de Android"))
            OutlinedButton(onClick = { launchSettings(context, Intent(Settings.ACTION_SETTINGS)) }) {
                Text(if (helpEnglish) "Open Android settings" else "Deschide setările Android")
            }
        }
    }
    if (showUpdateInfo) {
        SafeguardInfoDialog(
            titleRo = "ⓘ Android și securitate",
            titleEn = "ⓘ Android and security",
            summaryRo = "Versiunea Android identifică sistemul instalat. Data patch-ului indică nivelul actualizărilor de securitate raportat de dispozitiv.",
            summaryEn = "The Android version identifies the installed system. The security patch date is the security update level reported by the device.",
            doesRo = "FoniWise afișează aceste valori oferite de Android și deschide Setările Android, unde poți căuta opțiunea de actualizare.",
            doesEn = "FoniWise displays the values provided by Android and opens Android Settings, where you can look for the software update option.",
            limitsRo = "O dată mai veche nu înseamnă automat că telefonul este nesigur. FoniWise nu poate garanta securitatea dispozitivului și nu instalează actualizări.",
            limitsEn = "An older date does not automatically mean the phone is unsafe. FoniWise cannot guarantee device security and does not install updates.",
            adviceRo = "Verifică periodic actualizările oferite de producător. Dacă nu se deschide pagina dedicată, caută «Actualizare software» în Setările Android.",
            adviceEn = "Check periodically for updates from your manufacturer. If the dedicated page does not open, look for Software update in Android Settings.",
            english = helpEnglish,
            onLanguageChange = onLanguageChange,
            onDismiss = { showUpdateInfo = false },
            onSettings = {
                showUpdateInfo = false
                launchSettings(context, Intent(Settings.ACTION_SETTINGS))
            }
        )
    }
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(if (helpEnglish) "RAM Monitor" else "Memorie RAM", fontWeight = FontWeight.Bold, color = green, modifier = Modifier.weight(1f))
                IconButton(onClick = { showRamInfo = true }, modifier = Modifier.semantics { contentDescription = if (helpEnglish) "RAM Monitor information" else "Informații despre memoria RAM" }) {
                    Text("ⓘ", color = navy, style = MaterialTheme.typography.titleLarge)
                }
            }
            if (memorySnapshot != null && hasValidRamReading(memorySnapshot)) {
                Text((if (helpEnglish) "Available: %.1f GB of %.1f GB" else "Disponibil: %.1f GB din %.1f GB").format(memorySnapshot.availMem / 1073741824.0, memorySnapshot.totalMem / 1073741824.0))
                Text(if (memorySnapshot.lowMemory) { if (helpEnglish) "Android reports memory pressure." else "Android raportează presiune asupra memoriei." } else { if (helpEnglish) "Android does not report critically low memory." else "Android nu raportează memorie critic de redusă." })
            } else Text(if (helpEnglish) "Data unavailable" else "Date indisponibile")
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
            english = helpEnglish,
            onLanguageChange = onLanguageChange,
            onDismiss = { showRamInfo = false }
        )
    }
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (helpEnglish) "Battery" else "Baterie", fontWeight = FontWeight.Bold, color = green)
            Text(if (battery == null) (if (helpEnglish) "Data unavailable" else "Date indisponibile") else (if (helpEnglish) "Battery level: $battery%" else "Nivel baterie: $battery%"))
            Text(if (helpEnglish) "The battery percentage helps you decide when to charge. For unusual drain, check Android battery usage before changing settings." else "Procentul bateriei te ajută să decizi când să încarci telefonul. Dacă bateria se consumă neobișnuit, verifică utilizarea în Android înainte să schimbi setări.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { showBatteryGuide = true }) {
                Text(if (helpEnglish) "Battery saving guide" else "Ghid pentru economisirea bateriei")
            }
            OutlinedButton(onClick = { battery = readBatteryLevel() }) { Text(if (helpEnglish) "Refresh battery" else "Actualizează bateria") }
            OutlinedButton(onClick = { launchSettings(context, Intent("android.intent.action.POWER_USAGE_SUMMARY")) }) { Text(if (helpEnglish) "Battery settings" else "Setările bateriei") }
        }
    }
    if (showBatteryGuide) {
        SafeguardInfoDialog(
            titleRo = "Consilier pentru baterie",
            titleEn = "Battery advisor",
            summaryRo = "Bateria alimentează telefonul. Procentul afișat de Android arată încărcarea curentă, nu gradul exact de uzură.",
            summaryEn = "The battery powers your phone. Android's percentage shows the current charge, not precise battery wear.",
            doesRo = "Pentru a găsi aplicațiile cu consum ridicat, deschide Setări → Baterie → Utilizarea bateriei și verifică aplicațiile din listă. Denumirile diferă între telefoane.",
            doesEn = "To find apps using more power, open Settings → Battery → Battery usage and review the listed apps. Menu names vary between phones.",
            limitsRo = "FoniWise nu măsoară precis uzura bateriei pe orice telefon, nu poate identifica singur aplicațiile care consumă cel mai mult și nu promite creșteri miraculoase ale autonomiei.",
            limitsEn = "FoniWise cannot precisely measure battery wear on every phone, cannot independently identify the highest-drain apps and does not promise dramatic battery-life gains.",
            adviceRo = "Dacă ai nevoie de autonomie suplimentară, caută în Setări → Baterie opțiunea Economisire energie. Poate reduce activitatea în fundal și unele funcții. Activeaz-o când ai baterie puțină sau nu poți încărca telefonul; dezactiveaz-o dacă limitează funcții de care ai nevoie. Nu restricționa aplicații importante fără să înțelegi efectele.",
            adviceEn = "If you need longer battery life, look for Power saving in Settings → Battery. It may limit background activity and some features. Enable it when charge is low or charging is unavailable; turn it off if it limits features you need. Do not restrict important apps without understanding the effects.",
            english = helpEnglish,
            onLanguageChange = onLanguageChange,
            onDismiss = { showBatteryGuide = false },
            onSettings = {
                showBatteryGuide = false
                launchSettings(context, Intent("android.intent.action.POWER_USAGE_SUMMARY"))
            }
        )
    }
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(if (helpEnglish) "Storage" else "Stocare", fontWeight = FontWeight.Bold, color = green)
            if (storageSnapshot != null && hasValidStorageReading(storageSnapshot.first, storageSnapshot.second)) {
                Text((if (helpEnglish) "Used: %.1f GB of %.1f GB" else "Utilizați: %.1f GB din %.1f GB").format(
                    (storageSnapshot.first - storageSnapshot.second) / 1073741824.0,
                    storageSnapshot.first / 1073741824.0
                ))
            } else Text(if (helpEnglish) "Data unavailable" else "Date indisponibile")
            Text(if (helpEnglish) "Shows used and total internal storage so you can notice when space is running low. The reading does not identify which app or file uses the space." else "Arată spațiul intern utilizat și total, ca să observi când rămâne puțin spațiu liber. Măsurarea nu identifică aplicația sau fișierul care ocupă spațiul.", style = MaterialTheme.typography.bodySmall)
            Text(if (helpEnglish) "ⓘ Cache contains temporary files used by apps. It is not the same as an app’s personal data." else "ⓘ Cache-ul conține fișiere temporare folosite de aplicații. Nu este același lucru cu datele personale ale aplicației.", style = MaterialTheme.typography.bodySmall)
            Text(if (helpEnglish) "Cache Advisor: if an app uses too much storage, open Android Settings → Apps → select the app → Storage → Clear cache, if available. Do not tap Clear data unless you understand the consequences: you may lose settings or sign-in information." else "Cache Advisor: dacă o aplicație ocupă prea mult spațiu, deschide Setări Android → Aplicații → alege aplicația → Stocare → Șterge memoria cache, dacă opțiunea este disponibilă. Nu apăsa «Șterge datele» fără să înțelegi consecințele: poți pierde setări sau autentificări.", style = MaterialTheme.typography.bodySmall)
            Text(if (helpEnglish) "FoniWise does not measure each app’s cache or delete files automatically." else "FoniWise nu măsoară cache-ul fiecărei aplicații și nu șterge automat fișiere.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { launchSettings(context, Intent(Settings.ACTION_APPLICATION_SETTINGS)) }) { Text(if (helpEnglish) "Cache Advisor · App settings" else "Cache Advisor · Setări aplicații") }
            Text(if (helpEnglish) "FoniWise does not read or delete personal files." else "FoniWise nu citește sau șterge fișiere personale.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { storage = readInternalStorage() }) { Text(if (helpEnglish) "Refresh storage" else "Actualizează stocarea") }
            OutlinedButton(onClick = { launchSettings(context, Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS)) }) { Text(if (helpEnglish) "Storage settings" else "Setările stocării") }
        }
    }
}


// Display-only translation: scanner findings remain unchanged and are never rescanned on language switch.
private val scanLabelEnglish = mapOf(
    "Cameră" to "Camera",
    "Microfon" to "Microphone",
    "Locație precisă" to "Precise location",
    "Locație aproximativă" to "Approximate location",
    "Locație în fundal" to "Background location",
    "Contacte" to "Contacts",
    "Modificare contacte" to "Edit contacts",
    "Citire SMS" to "Read SMS",
    "Primire SMS" to "Receive SMS",
    "Primire MMS" to "Receive MMS",
    "Primire mesaje WAP Push" to "Receive WAP Push messages",
    "Trimitere SMS" to "Send SMS",
    "Citire calendar" to "Read calendar",
    "Modificare calendar" to "Edit calendar",
    "Istoric apeluri" to "Call history",
    "Modificare istoric apeluri" to "Edit call history",
    "Stare telefon" to "Phone state",
    "Numere de telefon" to "Phone numbers",
    "Inițiere apeluri" to "Make phone calls",
    "Preluare apeluri" to "Answer phone calls",
    "Senzori corporali" to "Body sensors",
    "Senzori corporali în fundal" to "Background body sensors",
    "Recunoașterea activității fizice" to "Physical activity recognition",
    "Scanare dispozitive Bluetooth" to "Scan Bluetooth devices",
    "Conectare dispozitive Bluetooth" to "Connect Bluetooth devices",
    "Detectabilitate Bluetooth" to "Bluetooth discoverability",
    "Dispozitive Wi-Fi din apropiere" to "Nearby Wi-Fi devices",
    "Citire stocare partajată (vechi)" to "Read shared storage (legacy)",
    "Modificare stocare partajată (vechi)" to "Modify shared storage (legacy)",
    "Fotografii" to "Photos",
    "Videoclipuri" to "Videos",
    "Fișiere audio" to "Audio files",
    "Fotografii/video selectate" to "Selected photos/videos",
    "Trimitere notificări (nu citire)" to "Post notifications (not read them)",
    "Serviciu Accessibility activ" to "Active accessibility service",
    "Acces la notificări activ" to "Notification access enabled",
    "Administrator dispozitiv activ" to "Active device administrator",
    "Afișare peste alte aplicații" to "Display over other apps",
    "Permisiune de solicitare instalare APK" to "Permission to request APK installation",
    "Acces la toate fișierele" to "All-files access",
    "Acces la datele de utilizare" to "Usage access",
    "Un serviciu Accessibility poate observa sau interacționa cu interfața. Unele servicii legitime au nevoie de acest acces." to "An accessibility service can observe or interact with the interface. Some legitimate services need this access.",
    "Aplicația poate primi notificările afișate de sistem. Verifică dacă recunoști aplicația și motivul accesului." to "The app can receive notifications shown by Android. Check whether you recognize the app and why it has access.",
    "Administratorii dispozitivului pot avea drepturi speciale de administrare. Nu dezactiva un profil de serviciu sau de securitate fără să știi ce controlează." to "Device administrators can have special management rights. Do not disable a work or security profile without understanding what it controls.",
    "Permite afișarea de ferestre peste alte aplicații. Este folosit și legitim de unele aplicații; verifică dacă îl recunoști." to "Allows windows to appear over other apps. Some legitimate apps use this; check whether you recognize it.",
    "Aplicația poate solicita instalarea altor pachete. Această setare nu înseamnă că a instalat ceva fără acord." to "The app can request installation of other packages. This does not mean it installed anything without your consent.",
    "Aplicația poate gestiona o mare parte din fișierele partajate. Unele aplicații de fișiere sau backup au motive legitime; verifică dacă recunoști aplicația." to "The app can manage much of shared storage. File managers and backup apps may have legitimate reasons; check whether you recognize the app.",
    "Aplicația poate vedea informații despre utilizarea altor aplicații. Accesul poate fi legitim pentru control parental, automatizare sau monitorizare acceptată." to "The app can see information about other apps' usage. This may be legitimate for parental controls, automation or monitoring you have agreed to."
)

private fun translateScanLabel(label: String): String = scanLabelEnglish[label] ?: label

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
