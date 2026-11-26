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
import java.text.Normalizer
import java.util.Locale
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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Accessibility
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
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

private val nunitoSans = FontFamily(Font(R.font.nunito_sans))

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
    var libraryCategory by rememberSaveable { mutableIntStateOf(-1) }
    var libraryTopic by rememberSaveable { mutableStateOf(-1) }
    androidx.activity.compose.BackHandler(enabled = selectedTab == 1 && libraryCategory >= 0) {
        if (libraryTopic >= 0) libraryTopic = -1 else libraryCategory = -1
    }
    var showHelp by remember { mutableStateOf(false) }
    var showCacheGuide by remember { mutableStateOf(false) }
    var showPermissionsGuide by remember { mutableStateOf(false) }
    var showFraudGuide by remember { mutableStateOf(false) }
    var showSecuritySteps by remember { mutableStateOf(false) }
    var showDeveloperGuide by remember { mutableStateOf(false) }
    val languagePreferences = remember(context) {
        context.applicationContext.getSharedPreferences("foniwise_language", Context.MODE_PRIVATE)
    }
    var showLaunchScreen by remember { mutableStateOf(true) }
    LaunchedEffect(Unit) {
        delay(900L)
        showLaunchScreen = false
    }
    if (showLaunchScreen) {
        FoniWiseLaunchScreen()
        return
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
        typography = MaterialTheme.typography.let { base ->
            androidx.compose.material3.Typography(
                displayLarge = base.displayLarge.copy(fontFamily = nunitoSans),
                displayMedium = base.displayMedium.copy(fontFamily = nunitoSans),
                displaySmall = base.displaySmall.copy(fontFamily = nunitoSans),
                headlineLarge = base.headlineLarge.copy(fontFamily = nunitoSans),
                headlineMedium = base.headlineMedium.copy(fontFamily = nunitoSans),
                headlineSmall = base.headlineSmall.copy(fontFamily = nunitoSans),
                titleLarge = base.titleLarge.copy(fontFamily = nunitoSans),
                titleMedium = base.titleMedium.copy(fontFamily = nunitoSans),
                titleSmall = base.titleSmall.copy(fontFamily = nunitoSans),
                bodyLarge = base.bodyLarge.copy(fontFamily = nunitoSans),
                bodyMedium = base.bodyMedium.copy(fontFamily = nunitoSans),
                bodySmall = base.bodySmall.copy(fontFamily = nunitoSans),
                labelLarge = base.labelLarge.copy(fontFamily = nunitoSans),
                labelMedium = base.labelMedium.copy(fontFamily = nunitoSans),
                labelSmall = base.labelSmall.copy(fontFamily = nunitoSans)
            )
        },
        colorScheme = lightColorScheme(
            primary = green,
            secondary = navy,
            background = paleBlue,
            surface = Color.White,
            surfaceVariant = paleGreen
        )
    ) {
        Scaffold(
            containerColor = Color.White,
            bottomBar = {
                if (selectedTab in 0..2 || selectedTab == 6) Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(0.dp),
                    color = navy,
                    shadowElevation = 7.dp,
                    border = BorderStroke(1.dp, green)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(5.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        val tabs = if (helpEnglish) listOf("My phone", "Learn about\nyour phone", "Recommendations\nand actions") else listOf("Telefonul meu", "Învață despre\ntelefon", "Recomandări\nși acțiuni")
                        val icons = listOf(Icons.Filled.Smartphone, Icons.Filled.Assessment, Icons.Filled.Settings)
                        val originalIcons = listOf("foniwise_nav_phone", "foniwise_nav_learn", "foniwise_nav_recommendations")
                        tabs.forEachIndexed { index, title ->
                            val active = selectedTab == index || (index == 0 && (selectedTab == 3 || selectedTab == 4)) || (index == 1 && selectedTab == 5) || (index == 2 && selectedTab == 6)
                            Surface(
                                onClick = { selectedTab = index; if (index == 1) { libraryCategory = -1; libraryTopic = -1 } },
                                modifier = Modifier.weight(1f).heightIn(min = 78.dp),
                                shape = RoundedCornerShape(0.dp),
                                color = Color.Transparent,
                                contentColor = if (active) Color(0xFFB5F52C) else Color.White
                            ) {
                                Column(
                                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    if (active) Box(Modifier.width(32.dp).height(4.dp).background(Color(0xFFB5F52C), RoundedCornerShape(3.dp)))
                                    else Spacer(Modifier.height(4.dp))
                                    if (index == 0) ApprovedCanvaIcon(originalIcons[index], icons[index], Modifier.size(25.dp), tintFallback = if (active) Color(0xFFB5F52C) else Color.White)
                                    else Icon(icons[index], contentDescription = null, modifier = Modifier.size(25.dp), tint = if (active) Color(0xFFB5F52C) else Color.White)
                                    Text(
                                        title,
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = if (active) FontWeight.Bold else FontWeight.Medium,
                                        // Let the label wrap naturally when Android font scaling is large.
                                        // A fixed two-line limit could hide the third navigation title.
                                        textAlign = TextAlign.Center
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
                if (selectedTab in 0..2) {
                    FoniWiseHeader(onHelp = { showHelp = true }, onLanguage = { chooseLanguage(!helpEnglish) }, helpEnglish = helpEnglish, showLanguage = selectedTab == 0)
                } else {
                    Surface(color = navy, shape = RoundedCornerShape(18.dp), modifier = Modifier.fillMaxWidth()) {
                        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp)) {
                            IconButton(onClick = { selectedTab = when (selectedTab) { 5 -> 1; 6 -> 2; else -> 0 } }) {
                                Icon(Icons.Filled.ArrowBack, contentDescription = if (helpEnglish) "Back" else "Înapoi", tint = Color.White)
                            }
                            Text(
                                when (selectedTab) {
                                    3 -> if (helpEnglish) "Security scan" else "Scanare de securitate"
                                    4 -> if (helpEnglish) "Notification inspector" else "Inspector notificări"
                                    5 -> if (helpEnglish) "Dictionary" else "Dicționar"
                                    6 -> if (helpEnglish) "Performance" else "Performanță"
                                    else -> "FoniWise"
                                },
                                modifier = Modifier.weight(1f), color = Color.White,
                                style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                if (selectedTab == 0) {
                DashboardOverview(helpEnglish)
                Text(if (helpEnglish) "Quick access" else "Acces rapid", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = navy)
                // Five full-width quick-access cards, in a stable single-column layout.
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                            DashboardTile(Icons.Filled.Security, "foniwise_icon_security", if (helpEnglish) "Security scan" else "Scanare securitate",
                            if (helpEnglish) "Review apps and permissions with your consent" else "Verifică aplicațiile și permisiunile, cu acordul tău",
                            if (helpEnglish) "Helps you review visible apps, sensitive permissions and special access. Not an antivirus." else "Te ajută să verifici aplicații vizibile, permisiuni sensibile și accesări speciale. Nu este antivirus.",
                            helpEnglish, Modifier.fillMaxWidth()) { selectedTab = 3 }
                            DashboardTile(Icons.Filled.Notifications, "foniwise_icon_notifications", if (helpEnglish) "Notification inspector" else "Inspector notificări",
                            if (helpEnglish) "Optional access" else "Acces opțional",
                            if (helpEnglish) "Helps you review notifications made available by Android. Requires your permission." else "Te ajută să examinezi notificările oferite de Android. Necesită acordul tău.",
                            helpEnglish, Modifier.fillMaxWidth()) { selectedTab = 4 }
                            DashboardTile(Icons.Filled.BatteryChargingFull, "foniwise_icon_battery", if (helpEnglish) "Battery" else "Baterie",
                            if (helpEnglish) "Android information and settings" else "Informații și setări Android",
                            if (helpEnglish) "Helps you check battery usage and decide which settings deserve attention." else "Te ajută să verifici consumul bateriei și să decizi ce setări merită atenție.",
                            helpEnglish, Modifier.fillMaxWidth()) {
                            launchSettings(context, Intent("android.intent.action.POWER_USAGE_SUMMARY"))
                            }
                            DashboardTile(Icons.Filled.Apps, "foniwise_icon_apps", if (helpEnglish) "Apps" else "Aplicații",
                            if (helpEnglish) "Permissions and settings" else "Permisiuni și setări",
                            if (helpEnglish) "Helps you review installed apps, their permissions and available controls." else "Te ajută să verifici aplicațiile instalate, permisiunile și controalele disponibile.",
                            helpEnglish, Modifier.fillMaxWidth()) {
                            launchSettings(context, Intent(Settings.ACTION_APPLICATION_SETTINGS))
                            }
                            DashboardTile(Icons.Filled.CleaningServices, "foniwise_icon_cleaning", if (helpEnglish) "Manual cleaning" else "Curățare manuală",
                            "Cache Advisor",
                            if (helpEnglish) "Shows when clearing an app's cache may help and how to avoid deleting its data." else "Îți arată când poate ajuta ștergerea cache-ului și cum eviți ștergerea datelor.",
                            helpEnglish, Modifier.fillMaxWidth()) { showCacheGuide = true }
                }
                }
                if (selectedTab == 1 || selectedTab == 5) {
                    EducationalLibrary(helpEnglish, libraryCategory, libraryTopic,
                        onCategory = { libraryCategory = it; libraryTopic = -1 },
                        onTopic = { libraryTopic = it })
                }
                if (selectedTab == 2) {
                    Card(onClick = { selectedTab = 6 }, colors = CardDefaults.cardColors(containerColor = paleGreen), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            Text(if (helpEnglish) "Phone performance" else "Performanța telefonului", fontWeight = FontWeight.Bold, color = navy)
                            Text(if (helpEnglish) "RAM · Storage · Battery · Android" else "RAM · Stocare · Baterie · Android", style = MaterialTheme.typography.bodySmall)
                        }
                    }
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
                            OutlinedButton(onClick = {
                                selectedTab = 1
                                libraryCategory = 5
                                libraryTopic = 0
                            }) {
                                Text(if (helpEnglish) "Understand app permissions" else "Înțelege permisiunile aplicațiilor")
                            }
                            OutlinedButton(onClick = { launchSettings(context, Intent(Settings.ACTION_APPLICATION_SETTINGS)) }) {
                                Text(if (helpEnglish) "Open Apps settings" else "Deschide Setări aplicații")
                            }
                        }
                    }
                }
                if (selectedTab == 6) {
                    Text(if (helpEnglish) "Android readings are informational, not a diagnosis." else "Valorile raportate de Android sunt orientative, nu un diagnostic.", color = navy)
                    DeviceHealthCards(context, helpEnglish, onLanguageChange = { chooseLanguage(it) }, onCacheGuide = { showCacheGuide = true })
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
                    OutlinedButton(onClick = {
                        selectedTab = 1
                                libraryCategory = 5
                                libraryTopic = 0
                    }) {
                        Text(if (helpEnglish) "Understand what permissions mean" else "Înțelege ce înseamnă permisiunile")
                    }
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
private fun FoniWiseLaunchScreen() {
    // Brief branded transition after the native Android launch window.
    // Uses the exact same approved drawable as the app icon and header.
    Column(
        modifier = Modifier.fillMaxSize().background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.fillMaxWidth().weight(0.16f).background(navy)
        )
        Surface(
            modifier = Modifier.fillMaxWidth().weight(0.64f).padding(horizontal = 16.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color(0xFFF0FCEB)
        ) {
            Column(
                modifier = Modifier.fillMaxSize().padding(18.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                BrandLogo(172.dp)
                Spacer(Modifier.height(20.dp))
                Text("FoniWise", style = MaterialTheme.typography.headlineLarge, fontWeight = FontWeight.ExtraBold, color = navy)
                Spacer(Modifier.height(6.dp))
                Text("Know Your Phone", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = navy)
            }
        }
        Column(
            modifier = Modifier.fillMaxWidth().weight(0.20f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(color = green, strokeWidth = 3.dp, modifier = Modifier.size(34.dp))
            Spacer(Modifier.height(12.dp))
            Text("Se încarcă… / Loading…", color = navy, style = MaterialTheme.typography.bodyMedium)
        }
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
            Text("Alege limba / Choose your language", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = navy, textAlign = TextAlign.Center)
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
            Button(
                onClick = { onChooseLanguage(english) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF9EEA47), contentColor = navy)
            ) {
                Text(if (english) "Continue" else "Continuă", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun BrandLogo(size: androidx.compose.ui.unit.Dp) {
    Image(
        painter = painterResource(R.drawable.foniwise_symbol_original_approved),
        contentDescription = "Simbolul original FoniWise",
        modifier = Modifier.size(size).clip(RoundedCornerShape(12.dp)),
        contentScale = ContentScale.Fit
    )
}

@Composable
private fun DashboardOverview(english: Boolean) {
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
                if (english) "Understand your phone, step by step." else "Înțelege-ți telefonul, pas cu pas.",
                style = MaterialTheme.typography.bodyMedium,
                color = navy
            )

        }
    }
}

/**
 * Prefer the eight original Canva PNGs when present in drawable-nodpi.
 * The fallback keeps the test branch buildable until the exact exports arrive.
 * Original artwork is rendered without tint or cropping.
 */
@Composable
private fun ApprovedCanvaIcon(
    drawableName: String,
    fallback: ImageVector,
    modifier: Modifier,
    tintFallback: Color
) {
    val context = LocalContext.current
    @Suppress("DEPRECATION")
    val resourceId = remember(context, drawableName) {
        context.resources.getIdentifier(drawableName, "drawable", context.packageName)
    }
    if (resourceId != 0) {
        Image(
            painter = painterResource(resourceId),
            contentDescription = null,
            modifier = modifier,
            contentScale = ContentScale.Fit
        )
    } else {
        Icon(fallback, contentDescription = null, modifier = modifier, tint = tintFallback)
    }
}

@Composable
private fun DashboardTile(
    symbol: ImageVector,
    originalIconName: String,
    title: String,
    subtitle: String,
    howItHelps: String,
    english: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.heightIn(min = 78.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE7F8DB)),
        border = BorderStroke(1.dp, Color(0xFFC5E9B6))
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(color = Color(0xFF064D2B), shape = CircleShape) {
                Box(Modifier.size(48.dp), contentAlignment = Alignment.Center) {
                    ApprovedCanvaIcon(originalIconName, symbol, Modifier.size(40.dp), tintFallback = Color.White)
                }
            }
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(title, fontWeight = FontWeight.Bold, color = navy, style = MaterialTheme.typography.titleMedium)
                Text(subtitle, color = Color(0xFF4D4D4D), style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = navy)
        }
    }
}

private fun normalizeGuideSearch(value: String): String =
    Normalizer.normalize(value.lowercase(Locale.ROOT), Normalizer.Form.NFD)
        .replace(Regex("[\\u0300-\\u036f]"), "")
        .replace("ș", "s").replace("ş", "s")
        .replace("ț", "t").replace("ţ", "t")

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
    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showSettingsInstructions by rememberSaveable { mutableStateOf(false) }
    val categoryIndices = if (selectedGroup >= 0) groups[selectedGroup].second else emptyList()
    val normalizedQuery = normalizeGuideSearch(searchQuery.trim())
    val searchResults = if (normalizedQuery.isBlank()) emptyList() else topics.indices.filter { index ->
        normalizeGuideSearch(topics[index].first).contains(normalizedQuery) ||
            normalizeGuideSearch(topics[index].second).contains(normalizedQuery)
    }
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
                            Text(if (english) "Back to topics" else "Înapoi la subiecte")
                        }
                        val activeGroup = groups.getOrNull(selectedGroup)
                        val activePosition = activeGroup?.second?.indexOf(selectedTopic) ?: -1
                        if (activeGroup != null && activePosition >= 0) {
                            Text(
                                activeGroup.first + " • " +
                                    (if (english) "Topic " else "Subiectul ") +
                                    (activePosition + 1) + "/" + activeGroup.second.size,
                                color = green, style = MaterialTheme.typography.labelSmall
                            )
                        }
                        Text(topics[selectedTopic].first, color = green, fontWeight = FontWeight.Bold)
                        Text(topics[selectedTopic].second, color = navy)
                        Text(if (english) "FoniWise cannot check or change this setting for you."
                            else "FoniWise nu poate verifica sau modifica această setare în locul tău.",
                            color = navy, style = MaterialTheme.typography.bodySmall)
                        TextButton(onClick = { showSettingsInstructions = !showSettingsInstructions }) {
                            Text(if (english)
                                (if (showSettingsInstructions) "Hide Android settings steps" else "How to check this on my phone")
                                else (if (showSettingsInstructions) "Ascunde pașii pentru setările Android" else "Cum verific pe telefon"))
                        }
                        if (showSettingsInstructions) {
                            HorizontalDivider()
                            Text(if (english) "If you want to check it on your phone:"
                                else "Dacă vrei să verifici opțiunea pe telefon:",
                                color = green, fontWeight = FontWeight.Bold)
                        Text(if (english)
                            "1. Read the explanation and note any original value before changing anything.\n2. Use Open Android settings below. If your phone does not open Developer options directly, look for the menu in Settings; names vary by model.\n3. Return to FoniWise with Back or Recent apps. This explanation should still be here. Compare what you saw; FoniWise cannot confirm that a change was made."
                            else
                            "1. Citește explicația și notează valoarea inițială înainte să modifici ceva.\n2. Folosește Deschide setările Android de mai jos. Dacă telefonul nu deschide direct Opțiunile pentru dezvoltatori, caută meniul în Setări; denumirile diferă după model.\n3. Revino în FoniWise folosind Înapoi sau Aplicații recente. Explicația ar trebui să rămână aici. Compară ce ai observat; FoniWise nu poate confirma că ai schimbat o setare.",
                            color = navy, style = MaterialTheme.typography.bodySmall)
                        }
                        val navigationIndices = if (searchQuery.isNotBlank()) searchResults else categoryIndices
                        val position = navigationIndices.indexOf(selectedTopic)
                        if (position >= 0) {
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                                OutlinedButton(
                                    onClick = { navigationIndices.getOrNull(position - 1)?.let { selectedTopic = it } },
                                    enabled = position > 0,
                                    modifier = Modifier.weight(1f)
                                ) { Text(if (english) "Previous" else "Anterior") }
                                OutlinedButton(
                                    onClick = { navigationIndices.getOrNull(position + 1)?.let { selectedTopic = it } },
                                    enabled = position < navigationIndices.lastIndex,
                                    modifier = Modifier.weight(1f)
                                ) { Text(if (english) "Next" else "Următor") }
                            }
                        }
                    } else {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            label = { Text(if (english) "Search topics and explanations" else "Caută subiecte și explicații") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        if (searchQuery.isNotBlank()) {
                            TextButton(onClick = { searchQuery = ""; selectedGroup = -1 }) {
                                Text(if (english) "Clear search" else "Șterge căutarea")
                            }
                            Text(if (english) "Matching topics: ${searchResults.size}" else "Subiecte găsite: ${searchResults.size}",
                                color = navy, style = MaterialTheme.typography.bodySmall)
                            searchResults.forEach { index ->
                                OutlinedButton(onClick = {
                                    selectedGroup = groups.indexOfFirst { (_, indices) -> index in indices }
                                    selectedTopic = index
                                }, modifier = Modifier.fillMaxWidth()) {
                                    Column {
                                        Text(topics[index].first)
                                        val groupName = groups.firstOrNull { (_, indices) -> index in indices }?.first
                                        if (groupName != null) {
                                            Text(groupName, style = MaterialTheme.typography.labelSmall)
                                        }
                                    }
                                }
                            }
                            if (searchResults.isEmpty()) {
                                Text(if (english) "No matching option. Try another word."
                                    else "Nu am găsit opțiunea. Încearcă alt cuvânt.", color = navy)
                            }
                        } else if (selectedGroup >= 0) {
                            OutlinedButton(onClick = { selectedGroup = -1 }) {
                                Text(if (english) "All categories" else "Toate categoriile")
                            }
                            Text(groups[selectedGroup].first, color = green, fontWeight = FontWeight.Bold)
                            categoryIndices.forEach { index ->
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
private fun FoniWiseHeader(onHelp: () -> Unit, onLanguage: () -> Unit, helpEnglish: Boolean, showLanguage: Boolean = true) {
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
            if (showLanguage) TextButton(onClick = onLanguage, contentPadding = PaddingValues(2.dp)) { Text(if (helpEnglish) "EN" else "RO", color = Color.White, fontWeight = FontWeight.Bold) }
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
private fun DeviceHealthCards(
    context: Context,
    helpEnglish: Boolean,
    onLanguageChange: (Boolean) -> Unit,
    onCacheGuide: () -> Unit
) {
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
            Text(if (helpEnglish) "These values are reported by Android; they do not by themselves confirm whether the phone is secure." else "Aceste valori sunt raportate de Android; ele nu confirmă singure dacă telefonul este sigur.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { launchSettings(context, Intent(Settings.ACTION_SETTINGS)) }) {
                Text(if (helpEnglish) "Check for updates in Android settings" else "Verifică actualizările în setările Android")
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
                Text((if (helpEnglish) "Available: %.1f GiB of %.1f GiB" else "Disponibil: %.1f GiB din %.1f GiB").format(memorySnapshot.availMem / 1073741824.0, memorySnapshot.totalMem / 1073741824.0))
            } else Text(if (helpEnglish) "Data unavailable" else "Date indisponibile")
            if (memorySnapshot != null && hasValidRamReading(memorySnapshot)) {
                Text(if (helpEnglish) "What this reading means:" else "Ce înseamnă această măsurătoare:", fontWeight = FontWeight.SemiBold, color = green)
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
            summaryRo = "RAM este memoria de lucru temporară a telefonului. Android o folosește pentru aplicațiile deschise și pentru a le porni mai repede. Nu este același lucru cu spațiul pentru poze și fișiere. Valorile sunt afișate în GiB (1 GiB = 1.024 MiB).",
            summaryEn = "RAM is your phone's temporary working memory. Android uses it for open apps and to launch them faster. It is different from the storage used for photos and files. Values are shown in GiB (1 GiB = 1,024 MiB).",
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
            Text(if (helpEnglish) "The percentage shows current charge, not battery health or which app uses the most power." else "Procentul arată încărcarea curentă, nu sănătatea bateriei sau ce aplicație consumă cel mai mult.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { showBatteryGuide = true }) {
                Text(if (helpEnglish) "Understand battery usage and power saving" else "Înțelege consumul și economisirea bateriei")
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
                Text((if (helpEnglish) "Used: %.1f GiB of %.1f GiB" else "Utilizați: %.1f GiB din %.1f GiB").format(
                    (storageSnapshot.first - storageSnapshot.second) / 1073741824.0,
                    storageSnapshot.first / 1073741824.0
                ))
            } else Text(if (helpEnglish) "Data unavailable" else "Date indisponibile")
            Text(if (helpEnglish) "Shows used and total internal storage so you can notice when space is running low. The reading does not identify which app or file uses the space." else "Arată spațiul intern utilizat și total, ca să observi când rămâne puțin spațiu liber. Măsurarea nu identifică aplicația sau fișierul care ocupă spațiul.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = onCacheGuide) {
                Text(if (helpEnglish) "Understand storage and safe cache cleaning" else "Înțelege stocarea și curățarea sigură a cache-ului")
            }
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

private data class EducationEntry(val icon: ImageVector, val ro: List<String>, val en: List<String>)
private val educationEntries = listOf(
    EducationEntry(Icons.Filled.Smartphone, listOf("Cum funcționează telefonul", "Android", "Android este sistemul de operare al telefonului.", "Android coordonează hardware-ul și aplicațiile prin servicii de sistem.", "Ecranul principal este o interfață Android."), listOf("How your phone works", "Android", "Android is the phone's operating system.", "Android coordinates hardware and apps through system services.", "The home screen is an Android interface.")),
    EducationEntry(Icons.Filled.Security, listOf("Securitate și confidențialitate", "Blocarea ecranului", "Blocarea ecranului limitează accesul la telefon.", "Android verifică acreditările înainte de a permite accesul la sesiune.", "Un cod PIN poate debloca ecranul."), listOf("Security and privacy", "Screen lock", "Screen lock restricts access to the phone.", "Android verifies credentials before allowing session access.", "A PIN can unlock the screen.")),
    EducationEntry(Icons.Filled.Warning, listOf("Fraude și înșelătorii", "Phishing", "Phishingul este o tentativă de a obține date prin înșelăciune.", "Atacatorii imită servicii legitime pentru a determina divulgarea datelor.", "Un mesaj fals poate imita o bancă."), listOf("Fraud and scams", "Phishing", "Phishing is an attempt to obtain data through deception.", "Attackers impersonate legitimate services to induce disclosure.", "A fake message may impersonate a bank.")),
    EducationEntry(Icons.Filled.BatteryChargingFull, listOf("Baterie și autonomie", "Bateria", "Bateria stochează energia folosită de telefon.", "Celulele litiu-ion transformă energia chimică în energie electrică.", "Ecranul aprins consumă energie."), listOf("Battery and endurance", "Battery", "The battery stores energy used by the phone.", "Lithium-ion cells convert chemical energy into electrical energy.", "An illuminated screen uses energy.")),
    EducationEntry(Icons.Filled.Storage, listOf("Memorie și stocare", "Memoria RAM", "RAM este memoria temporară folosită de aplicații.", "RAM este volatilă; Android gestionează alocarea și eliberarea memoriei proceselor.", "O aplicație recentă poate rămâne în memorie."), listOf("Memory and storage", "RAM", "RAM is temporary memory used by apps.", "RAM is volatile; Android manages process memory allocation and reclamation.", "A recent app may remain in memory.")),
    EducationEntry(Icons.Filled.Apps, listOf("Aplicații și permisiuni", "Permisiunile aplicațiilor", "Permisiunile controlează accesul aplicațiilor la funcții și date.", "Android aplică reguli de acces, inclusiv permisiuni acordate în timpul utilizării.", "O aplicație foto poate cere acces la cameră."), listOf("Apps and permissions", "App permissions", "Permissions control app access to features and data.", "Android enforces access rules, including runtime permissions.", "A photo app may request camera access.")),
    EducationEntry(Icons.Filled.Wifi, listOf("Internet și conectivitate", "Wi-Fi", "Wi-Fi conectează telefonul la o rețea fără cablu.", "Wi-Fi transmite date prin unde radio conform standardelor IEEE 802.11.", "Telefonul se poate conecta la routerul de acasă."), listOf("Internet and connectivity", "Wi-Fi", "Wi-Fi connects the phone to a network wirelessly.", "Wi-Fi transmits data over radio using IEEE 802.11 standards.", "The phone can connect to a home router.")),
    EducationEntry(Icons.Filled.Notifications, listOf("Notificări", "Notificarea", "O notificare este o informare afișată de sistem sau aplicație.", "Android organizează notificările aplicațiilor în canale configurabile.", "Un calendar poate afișa un memento."), listOf("Notifications", "Notification", "A notification is a message displayed by the system or an app.", "Android organizes app notifications into configurable channels.", "A calendar can display a reminder.")),
    EducationEntry(Icons.Filled.Refresh, listOf("Sistem și actualizări", "Actualizările Android", "Actualizările modifică software-ul telefonului.", "Actualizările pot include remedieri de securitate și modificări ale componentelor sistemului.", "O actualizare poate corecta o eroare."), listOf("System and updates", "Android updates", "Updates change the phone's software.", "Updates can include security fixes and system component changes.", "An update may fix a bug.")),
    EducationEntry(Icons.Filled.Accessibility, listOf("Accesibilitate", "Dimensiunea textului", "Dimensiunea textului modifică mărimea literelor afișate.", "Android oferă scalare tipografică pe baza preferințelor utilizatorului.", "Un text mai mare poate fi mai ușor de citit."), listOf("Accessibility", "Text size", "Text size changes the size of displayed letters.", "Android provides font scaling based on user preferences.", "Larger text may be easier to read.")),
    EducationEntry(Icons.Filled.Settings, listOf("Setări avansate", "Opțiuni pentru dezvoltatori", "Aceste opțiuni sunt setări Android destinate în principal dezvoltatorilor.", "Ele includ instrumente de depanare, monitorizare și configurare a comportamentului sistemului.", "Depanarea USB permite comunicarea cu instrumente de dezvoltare."), listOf("Advanced settings", "Developer options", "These Android settings are mainly intended for developers.", "They include debugging, monitoring and system behavior controls.", "USB debugging enables communication with development tools.")),
    EducationEntry(Icons.Filled.MenuBook, listOf("Dicționar de termeni", "Cache", "Cache-ul păstrează temporar date pentru acces mai rapid.", "Cache-ul reutilizează rezultate sau resurse pentru a reduce operațiile repetate.", "Un browser poate păstra imagini deja încărcate."), listOf("Glossary", "Cache", "A cache temporarily stores data for faster access.", "A cache reuses results or resources to reduce repeated operations.", "A browser may retain previously loaded images."))
)

private data class LibraryLesson(val category: Int, val ro: List<String>, val en: List<String>)
private val libraryLessons = educationEntries.mapIndexed { index, entry -> LibraryLesson(index, entry.ro.drop(1), entry.en.drop(1)) } + listOf(
    LibraryLesson(0, listOf("Procesor", "Procesorul execută instrucțiunile aplicațiilor.", "Nucleele procesorului prelucrează operații, iar frecvența lor poate varia cu sarcina.", "Când deschizi o fotografie, procesorul ajută la decodarea ei."), listOf("Processor", "The processor executes app instructions.", "Processor cores execute operations and their frequency can vary with workload.", "When you open a photo, the processor helps decode it.")),
    LibraryLesson(0, listOf("Senzori", "Senzorii măsoară lucruri din jurul telefonului.", "Accelerometrul măsoară accelerația, iar giroscopul măsoară rotația.", "Rotirea telefonului poate schimba orientarea ecranului."), listOf("Sensors", "Sensors measure things around the phone.", "An accelerometer measures acceleration and a gyroscope measures rotation.", "Rotating the phone can change screen orientation.")),
    LibraryLesson(1, listOf("Criptare", "Criptarea transformă datele într-o formă greu de citit fără cheia potrivită.", "Android folosește criptarea bazată pe fișiere pe dispozitivele moderne compatibile.", "Fotografiile stocate pot fi protejate prin criptarea dispozitivului."), listOf("Encryption", "Encryption transforms data into a form difficult to read without the correct key.", "Android uses file-based encryption on compatible modern devices.", "Stored photos can be protected by device encryption.")),
    LibraryLesson(1, listOf("Autentificare în doi pași", "Identitatea este verificată prin două etape.", "Factorii independenți pot reduce riscul asociat compromiterii unei parole.", "După parolă, un serviciu poate solicita confirmarea pe telefon."), listOf("Two-step verification", "Identity is checked in two steps.", "Independent factors can reduce the risk of a compromised password.", "After a password, a service may request confirmation on your phone.")),
    LibraryLesson(2, listOf("Smishing", "Smishingul este phishing prin mesaje text.", "Mesajele pot imita expeditori legitimi și pot conține linkuri înșelătoare.", "Un SMS fals poate pretinde că un colet așteaptă plata unei taxe."), listOf("Smishing", "Smishing is phishing through text messages.", "Messages may impersonate legitimate senders and contain deceptive links.", "A fake SMS may claim a parcel requires a fee.")),
    LibraryLesson(2, listOf("Vishing", "Vishingul este o înșelătorie prin apeluri vocale.", "Identitatea afișată a apelantului poate fi falsificată.", "Un apelant poate pretinde că reprezintă banca."), listOf("Vishing", "Vishing is a scam using voice calls.", "Caller ID can be spoofed.", "A caller may pretend to represent a bank.")),
    LibraryLesson(3, listOf("Capacitate (mAh)", "mAh este o unitate pentru cantitatea de sarcină electrică a bateriei.", "Energia în Wh depinde atât de capacitatea în Ah, cât și de tensiune.", "Două baterii cu aceeași capacitate pot avea autonomii diferite în telefoane diferite."), listOf("Capacity (mAh)", "mAh is a unit of battery electric charge capacity.", "Energy in Wh depends on both capacity in Ah and voltage.", "Two batteries with equal capacity can yield different runtimes in different phones.")),
    LibraryLesson(3, listOf("Cicluri de încărcare", "Un ciclu echivalent înseamnă utilizarea cumulată a unei capacități complete.", "Uzura celulelor depinde și de temperatură, timp și condițiile de încărcare.", "Două utilizări de câte 50% însumează aproximativ un ciclu."), listOf("Charge cycles", "An equivalent cycle means cumulative use of one full capacity.", "Cell aging also depends on temperature, time and charging conditions.", "Two uses of 50% add up to approximately one cycle.")),
    LibraryLesson(4, listOf("Memorie internă", "Memoria internă păstrează aplicații și fișiere chiar după oprirea telefonului.", "Memoria flash nevolatilă stochează date fără alimentare continuă.", "O fotografie rămâne salvată după repornire."), listOf("Internal storage", "Internal storage retains apps and files even when the phone is off.", "Nonvolatile flash memory stores data without continuous power.", "A photo remains saved after restarting.")),
    LibraryLesson(4, listOf("Memorie cache", "Cache-ul păstrează copii temporare ale datelor folosite frecvent.", "Reutilizarea datelor reduce accesările repetate ale sursei originale.", "O aplicație poate reutiliza miniaturile fotografiilor."), listOf("Cache memory", "A cache keeps temporary copies of frequently used data.", "Reusing data reduces repeated access to the original source.", "An app may reuse photo thumbnails.")),
    LibraryLesson(5, listOf("APK", "Un APK este un pachet de instalare pentru aplicații Android.", "Pachetul conține cod, resurse și un manifest; Android verifică semnătura la instalare.", "O aplicație distribuită direct poate veni ca fișier APK."), listOf("APK", "An APK is an Android app installation package.", "The package contains code, resources and a manifest; Android verifies its signature during installation.", "A directly distributed app may come as an APK file.")),
    LibraryLesson(5, listOf("Procese în fundal", "Unele aplicații pot efectua activități fără a fi afișate pe ecran.", "Android limitează execuția în fundal în funcție de versiune și tipul activității.", "O aplicație de muzică poate continua redarea când ecranul este stins."), listOf("Background processes", "Some apps can perform activities without being displayed.", "Android restricts background execution depending on version and activity type.", "A music app may continue playback when the screen is off.")),
    LibraryLesson(6, listOf("Bluetooth", "Bluetooth conectează dispozitive apropiate fără cablu.", "Bluetooth folosește unde radio în banda de 2,4 GHz și include moduri cu consum redus.", "Căștile wireless primesc sunet de la telefon."), listOf("Bluetooth", "Bluetooth connects nearby devices wirelessly.", "Bluetooth uses radio waves in the 2.4 GHz band and includes low-energy modes.", "Wireless headphones receive audio from the phone.")),
    LibraryLesson(6, listOf("NFC", "NFC permite comunicarea fără fir la distanțe foarte mici.", "Tehnologia utilizează cuplaj electromagnetic la 13,56 MHz.", "Un telefon compatibil poate comunica cu un terminal de plată apropiat."), listOf("NFC", "NFC enables wireless communication over very short distances.", "The technology uses electromagnetic coupling at 13.56 MHz.", "A compatible phone can communicate with a nearby payment terminal.")),
    LibraryLesson(7, listOf("Canale de notificări", "Canalele grupează notificările unei aplicații după tip.", "Android permite configurarea importanței canalelor începând cu Android 8.", "O aplicație poate separa mesajele de promoții."), listOf("Notification channels", "Channels group an app's notifications by type.", "Android supports configurable channel importance starting with Android 8.", "An app may separate messages from promotions.")),
    LibraryLesson(7, listOf("Notificări silențioase", "Aceste notificări pot apărea fără sunet.", "Comportamentul depinde de importanța canalului și setările sistemului.", "O informare meteo poate apărea fără alertă sonoră."), listOf("Silent notifications", "These notifications may appear without sound.", "Behavior depends on channel importance and system settings.", "A weather update may appear without an audible alert.")),
    LibraryLesson(8, listOf("Actualizări de securitate", "Aceste actualizări corectează probleme de securitate ale software-ului.", "Remedierile pot viza componente Android, nucleul sistemului sau componente ale producătorului.", "O actualizare poate remedia o vulnerabilitate dintr-o componentă multimedia."), listOf("Security updates", "These updates fix software security issues.", "Fixes may target Android components, the kernel or manufacturer components.", "An update may fix a vulnerability in a media component.")),
    LibraryLesson(8, listOf("Firmware", "Firmware-ul este software care controlează componentele dispozitivului.", "Poate include cod pentru modem, pornire și alte subsisteme hardware.", "O actualizare poate modifica software-ul modemului."), listOf("Firmware", "Firmware is software that controls device components.", "It may include code for the modem, boot process and other hardware subsystems.", "An update may change modem software.")),
    LibraryLesson(9, listOf("TalkBack", "TalkBack citește și descrie elementele interfeței.", "Cititorul de ecran utilizează informații de accesibilitate oferite de aplicații.", "Un buton poate fi anunțat vocal când primește focalizarea."), listOf("TalkBack", "TalkBack reads and describes interface elements.", "The screen reader uses accessibility information provided by apps.", "A button may be spoken aloud when it receives focus.")),
    LibraryLesson(9, listOf("Mărirea ecranului", "Mărirea ecranului afișează o zonă a interfeței la scară mai mare.", "Sistemul poate mări imaginea fără a modifica dimensiunile originale ale elementelor.", "Un detaliu mic dintr-o imagine poate deveni mai vizibil."), listOf("Screen magnification", "Magnification displays an area of the interface at a larger scale.", "The system can enlarge the image without changing original element dimensions.", "A small detail in an image may become more visible.")),
    LibraryLesson(10, listOf("Depanare USB", "Depanarea USB permite comunicarea cu instrumente de dezvoltare.", "ADB comunică cu dispozitivul printr-un serviciu de depanare și mecanisme de autorizare.", "Un dezvoltator poate consulta jurnalele propriei aplicații."), listOf("USB debugging", "USB debugging enables communication with development tools.", "ADB communicates with the device through a debugging service and authorization mechanisms.", "A developer can inspect logs from their own app.")),
    LibraryLesson(10, listOf("Animații", "Animațiile creează tranziții vizuale între elemente.", "Scările de animație pot modifica durata unor tranziții Android.", "Deschiderea unei ferestre poate fi însoțită de o tranziție."), listOf("Animations", "Animations create visual transitions between elements.", "Animation scales can change the duration of some Android transitions.", "Opening a window may include a transition.")),
    LibraryLesson(0, listOf("Ecran tactil", "Ecranul tactil afișează imagini și detectează atingerea.", "Majoritatea telefoanelor folosesc senzori capacitivi care detectează modificări ale câmpului electric.", "Când mărești o fotografie cu două degete, ecranul detectează ambele atingeri."), listOf("Touchscreen", "A touchscreen displays images and detects touch.", "Most phones use capacitive sensors that detect changes in an electric field.", "When you zoom into a photo with two fingers, the screen detects both touches.")),
    LibraryLesson(0, listOf("Camera foto", "Camera transformă lumina în imagini digitale.", "Senzorul de imagine măsoară lumina, iar procesorul prelucrează semnalul.", "Într-o cameră întunecată, fotografia poate avea mai mult zgomot vizual."), listOf("Camera", "A camera converts light into digital images.", "The image sensor measures light and the processor processes the signal.", "In a dark room, a photo may have more visual noise.")),
    LibraryLesson(0, listOf("Microfon", "Microfonul transformă sunetele în semnale electrice.", "Microfoanele MEMS folosesc structuri microscopice sensibile la presiunea acustică.", "În timpul unui apel, vocea ta este captată de microfon."), listOf("Microphone", "A microphone converts sound into electrical signals.", "MEMS microphones use microscopic structures sensitive to acoustic pressure.", "During a call, your voice is captured by the microphone.")),
    LibraryLesson(1, listOf("Biometrie", "Biometria folosește caracteristici fizice pentru verificarea identității.", "Sistemul compară caracteristici măsurate cu un model biometric înregistrat; precizia diferă între tehnologii.", "Un senzor de amprentă poate permite deblocarea telefonului."), listOf("Biometrics", "Biometrics uses physical characteristics to verify identity.", "The system compares measured features with an enrolled biometric template; accuracy varies by technology.", "A fingerprint sensor may unlock the phone.")),
    LibraryLesson(1, listOf("Parolă și PIN", "Parola și PIN-ul sunt secrete folosite pentru verificarea accesului.", "Un PIN folosește cifre, iar o parolă poate include mai multe tipuri de caractere; sistemele pot limita încercările.", "După repornire, telefonul poate solicita PIN-ul în locul amprentei."), listOf("Password and PIN", "Passwords and PINs are secrets used to verify access.", "A PIN uses digits, while a password may include more character types; systems can limit attempts.", "After restarting, a phone may request a PIN instead of a fingerprint.")),
    LibraryLesson(2, listOf("Coduri QR frauduloase", "Un cod QR poate ascunde o adresă către o pagină înșelătoare.", "Codul reprezintă date, nu o garanție a autenticității destinației.", "Un autocolant fals poate înlocui codul de plată de pe un afiș."), listOf("Fraudulent QR codes", "A QR code can conceal an address leading to a deceptive page.", "The code represents data, not a guarantee of the destination's authenticity.", "A fake sticker may replace a payment code on a sign.")),
    LibraryLesson(2, listOf("Deepfake", "Un deepfake este un material audio sau vizual generat ori modificat pentru a imita o persoană.", "Modelele generative pot sintetiza vocea și aspectul unei persoane din exemple.", "O înregistrare falsă poate imita vocea unei persoane cunoscute."), listOf("Deepfake", "A deepfake is audio or visual material generated or altered to imitate a person.", "Generative models can synthesize a person's voice and appearance from examples.", "A fake recording may imitate a familiar person's voice.")),
    LibraryLesson(3, listOf("Încărcare rapidă", "Încărcarea rapidă permite transferul energiei la o putere mai mare în condiții compatibile.", "Telefonul și încărcătorul pot negocia tensiunea și curentul; puterea variază cu temperatura și nivelul bateriei.", "Încărcarea poate încetini când bateria se apropie de 100%."), listOf("Fast charging", "Fast charging transfers energy at higher power under compatible conditions.", "The phone and charger may negotiate voltage and current; power varies with temperature and battery level.", "Charging may slow as the battery approaches 100%.")),
    LibraryLesson(3, listOf("Degradarea bateriei", "În timp, bateria poate stoca mai puțină energie decât atunci când era nouă.", "Reacțiile chimice secundare reduc capacitatea disponibilă și pot crește rezistența internă.", "Un telefon vechi poate avea autonomie mai mică la aceeași utilizare."), listOf("Battery aging", "Over time, a battery may store less energy than when new.", "Secondary chemical reactions reduce available capacity and may increase internal resistance.", "An older phone may have shorter runtime under the same use.")),
    LibraryLesson(4, listOf("Card microSD", "Un card microSD este un suport de stocare detașabil pentru telefoanele compatibile.", "Cardul folosește memorie flash, iar viteza depinde de card și de interfața dispozitivului.", "Fotografiile pot fi salvate pe card dacă telefonul și aplicația permit."), listOf("microSD card", "A microSD card is removable storage for compatible phones.", "The card uses flash memory, and speed depends on the card and device interface.", "Photos may be saved to the card if the phone and app support it.")),
    LibraryLesson(4, listOf("Memorie virtuală", "Memoria virtuală permite sistemului să gestioneze memoria disponibilă aplicațiilor.", "Android poate utiliza compresie în RAM prin zRAM; unele dispozitive folosesc și stocarea pentru extensii de memorie.", "O funcție numită RAM Plus poate utiliza mecanisme diferite în funcție de model."), listOf("Virtual memory", "Virtual memory lets the system manage memory available to apps.", "Android can use compressed RAM through zRAM; some devices also use storage for memory extensions.", "A feature called RAM Plus may use different mechanisms depending on the model.")),
    LibraryLesson(5, listOf("Aplicații de sistem", "Aplicațiile de sistem sunt furnizate împreună cu software-ul telefonului.", "Unele sunt instalate în partiții de sistem și pot avea privilegii speciale, în funcție de configurație.", "Aplicația Setări este de obicei o aplicație de sistem."), listOf("System apps", "System apps are supplied with the phone software.", "Some are installed in system partitions and may have special privileges depending on configuration.", "The Settings app is usually a system app.")),
    LibraryLesson(5, listOf("Dezinstalare", "Dezinstalarea elimină o aplicație de pe dispozitiv pentru utilizatorul respectiv.", "Android gestionează pachetul și datele private; fișierele partajate sau datele online pot rămâne.", "Ștergerea unei aplicații de mesagerie nu înseamnă automat ștergerea contului online."), listOf("Uninstallation", "Uninstallation removes an app from the device for the relevant user.", "Android manages the package and private data; shared files or online data may remain.", "Removing a messaging app does not automatically delete its online account.")),
    LibraryLesson(6, listOf("GPS", "GPS ajută telefonul să determine poziția folosind semnale de la sateliți.", "Receptorul estimează poziția din timpii de propagare ai semnalelor; telefoanele pot combina mai multe sisteme GNSS.", "O hartă poate afișa poziția telefonului în aer liber."), listOf("GPS", "GPS helps a phone determine its location using satellite signals.", "The receiver estimates position from signal travel times; phones may combine multiple GNSS systems.", "A map may display the phone's position outdoors.")),
    LibraryLesson(6, listOf("Date mobile", "Datele mobile conectează telefonul la internet prin rețeaua operatorului.", "Modemul comunică cu stații de bază prin tehnologii precum LTE sau 5G.", "Un videoclip poate fi redat în autobuz fără Wi-Fi."), listOf("Mobile data", "Mobile data connects a phone to the internet through a carrier network.", "The modem communicates with base stations through technologies such as LTE or 5G.", "A video may play on a bus without Wi-Fi.")),
    LibraryLesson(6, listOf("Hotspot", "Un hotspot permite altor dispozitive să folosească o conexiune a telefonului.", "Telefonul poate funcționa ca punct de acces Wi-Fi și poate partaja conexiunea mobilă.", "Un laptop poate accesa internetul prin hotspotul telefonului."), listOf("Hotspot", "A hotspot lets other devices use a phone's connection.", "The phone can act as a Wi-Fi access point and share its mobile connection.", "A laptop may access the internet through the phone's hotspot.")),
    LibraryLesson(6, listOf("DNS", "DNS traduce numele site-urilor în adrese utilizabile de rețea.", "Rezolvarea DNS consultă servere și poate utiliza rezultate memorate temporar.", "Când introduci numele unui site, DNS ajută la găsirea adresei sale."), listOf("DNS", "DNS translates website names into addresses usable by the network.", "DNS resolution queries servers and may use cached results.", "When you enter a website name, DNS helps find its address.")),
    LibraryLesson(7, listOf("Notificări push", "Notificările push pot informa telefonul despre evenimente de la un serviciu online.", "Un serviciu de mesagerie push poate livra date către aplicații fără interogări permanente din partea lor.", "O aplicație poate anunța sosirea unui mesaj nou."), listOf("Push notifications", "Push notifications can inform a phone about events from an online service.", "A push messaging service can deliver data to apps without constant polling by them.", "An app may announce a new message.")),
    LibraryLesson(8, listOf("Google Play Services", "Google Play Services oferă funcții comune aplicațiilor pe dispozitivele compatibile.", "Componenta oferă interfețe pentru servicii precum localizarea și autentificarea.", "O aplicație poate folosi serviciile Google pentru autentificare."), listOf("Google Play services", "Google Play services provides shared features to apps on compatible devices.", "The component provides APIs for services such as location and authentication.", "An app may use Google services for sign-in.")),
    LibraryLesson(9, listOf("Subtitrări", "Subtitrările afișează text asociat conținutului audio.", "Unele funcții folosesc recunoașterea automată a vorbirii, în funcție de dispozitiv și limbă.", "Un videoclip poate fi urmărit cu textul dialogului pe ecran."), listOf("Captions", "Captions display text associated with audio content.", "Some features use automatic speech recognition, depending on device and language.", "A video can be watched with dialogue text on screen.")),
    LibraryLesson(10, listOf("Bootloader", "Bootloaderul este software-ul care pregătește pornirea sistemului.", "Pe dispozitivele compatibile, pornirea verificată controlează integritatea componentelor încărcate.", "La pornire, telefonul verifică și încarcă software-ul necesar sistemului."), listOf("Bootloader", "The bootloader is software that prepares the system to start.", "On compatible devices, verified boot checks the integrity of loaded components.", "At startup, the phone checks and loads software needed by the system."))
)

private data class DetailedLesson(val ro: List<String>, val en: List<String>)
private val detailedLessons = mapOf(
    "Wi-Fi" to DetailedLesson(listOf("Wi-Fi este o tehnologie de comunicație radio între dispozitive și o rețea locală. Nu este sinonim cu internetul. Routerul de acasă poate conecta telefonul la imprimantă sau la alte dispozitive chiar dacă legătura furnizorului de internet este întreruptă.", "Telefonul și punctul de acces schimbă pachete de date prin unde radio. Pereții, distanța și alte rețele pot influența calitatea semnalului. Benzile de 2,4 GHz, 5 GHz și, pe dispozitive compatibile, 6 GHz au proprietăți diferite. Viteza afișată a legăturii radio nu este identică cu viteza de descărcare de pe internet. Imaginează-ți două drumuri succesive: unul între telefon și router, altul între router și serviciul online.", "Telefonul poate afișa semnal Wi-Fi puternic și totuși să nu deschidă o pagină dacă legătura routerului cu furnizorul este întreruptă. O imprimantă locală poate rămâne accesibilă. Exemplul arată că semnalul radio și accesul la internet sunt proprietăți distincte.", "Wi-Fi folosește familia de standarde IEEE 802.11. Stația se asociază cu un punct de acces, iar traficul este transportat în cadre radio. Configurarea IP poate fi obținută separat prin DHCP.", "Canalul radio este o resursă partajată. Interferențele, retransmisiile, lățimea canalului, modulația și numărul fluxurilor spațiale influențează debitul. WPA2 și WPA3 pot proteja legătura radio, fără să înlocuiască protecția aplicației până la server. Debitul util este inferior ratei fizice din cauza antetelor, confirmărilor și timpului de acces la mediu.", "Într-un model simplificat, legătura radio poate transporta util 100 Mb/s, iar conexiunea furnizorului 40 Mb/s. Transferul online nu poate depăși limita de 40 Mb/s în aceste condiții. Un fișier de 100 MB necesită ideal 20 secunde la această rată, fără întârzieri suplimentare."), listOf("Wi-Fi is radio communication between devices and a local network. It is not synonymous with the internet. A home router may connect a phone to a printer or other devices even when its internet uplink fails.", "The phone and access point exchange data packets over radio. Walls, distance and other networks affect signal quality. The 2.4 GHz, 5 GHz and, on compatible devices, 6 GHz bands have different properties. Displayed radio link speed differs from internet download speed. Imagine two successive roads: phone to router, then router to online service.", "A phone may show strong Wi-Fi while failing to open a website because the router's internet uplink is down. A local printer may remain reachable. Radio signal and internet access are distinct properties.", "Wi-Fi uses the IEEE 802.11 family of standards. A station associates with an access point and traffic travels in radio frames. IP configuration may be obtained separately through DHCP.", "The radio channel is shared. Interference, retransmissions, channel width, modulation and spatial streams influence throughput. WPA2 and WPA3 can protect the radio link without replacing application-to-server protection. Useful throughput is below physical link rate due to headers, acknowledgments and medium access time.", "In a simplified model the radio link delivers 100 Mb/s while the provider connection delivers 40 Mb/s. Online transfer cannot exceed 40 Mb/s under these conditions. A 100 MB file ideally takes 20 seconds at that rate, excluding additional delays.")),
    "Bluetooth" to DetailedLesson(listOf("Bluetooth permite schimbul de date între dispozitive apropiate fără cablu. Este folosit pentru căști, ceasuri, tastaturi și senzori. O conexiune Bluetooth nu necesită automat internet.", "Dispozitivele pot descoperi servicii și stabili o conexiune. Asocierea și memorarea unei relații de încredere sunt mecanisme distincte de transferul efectiv. Bluetooth Classic și Bluetooth Low Energy sunt familii de tehnologii cu caracteristici diferite. Consumul depinde de activitatea radio și de implementare, nu doar de faptul că Bluetooth este activ.", "Căștile pot reda o melodie stocată local pe telefon chiar fără internet. Telefonul transmite sunetul prin Bluetooth, iar căștile îl transformă în unde sonore. Dacă melodia provine dintr-un serviciu online, internetul și Bluetooth îndeplinesc roluri separate.", "Bluetooth operează în banda de 2,4 GHz. Classic utilizează profiluri precum A2DP, iar BLE utilizează frecvent servicii și caracteristici GATT; LE Audio are mecanisme proprii.", "Salturile de frecvență și adaptarea canalelor contribuie la coexistența cu alte sisteme radio. BLE poate alterna perioade de activitate cu perioade de repaus. Latența audio depinde de codec, buffere și procesare, nu doar de viteza radio. Asocierea poate negocia chei, iar proprietățile de autentificare depind de metoda folosită.", "Un senzor ipotetic transmite 20 octeți o dată pe secundă: 1200 octeți utili pe minut, fără antete. O transmisie audio de 128 kb/s transportă 960.000 octeți utili pe minut. Aceste sarcini diferite explică de ce profilul și ritmul comunicației contează."), listOf("Bluetooth exchanges data between nearby devices without cables. It serves headphones, watches, keyboards and sensors. A Bluetooth connection does not automatically require internet.", "Devices can discover services and establish connections. Pairing and retaining a trusted relationship differ from actual data transfer. Bluetooth Classic and Bluetooth Low Energy have different characteristics. Power use depends on radio activity and implementation, not simply whether Bluetooth is enabled.", "Headphones can play a locally stored song without internet. The phone sends audio through Bluetooth and headphones turn it into sound. For online music, internet and Bluetooth perform separate roles.", "Bluetooth operates in the 2.4 GHz band. Classic uses profiles such as A2DP, while BLE commonly uses GATT services and characteristics; LE Audio has its own mechanisms.", "Frequency hopping and channel adaptation support coexistence with other radio systems. BLE may alternate activity and sleep. Audio latency depends on codecs, buffers and processing, not just radio speed. Pairing may negotiate keys, with authentication properties depending on the method.", "A hypothetical sensor sends 20 bytes once per second: 1200 payload bytes per minute, excluding headers. Audio at 128 kb/s carries 960,000 payload bytes per minute. Different workloads explain why communication profiles and timing matter.")),
    "NFC" to DetailedLesson(listOf("NFC este o tehnologie de comunicație fără fir la distanțe foarte mici, de obicei câțiva centimetri. Poate fi folosită pentru citirea etichetelor, bilete și plăți. Apropierea fizică nu este echivalentă cu autorizarea unei tranzacții.", "Un dispozitiv poate genera un câmp electromagnetic care permite comunicarea cu un alt dispozitiv sau cu o etichetă pasivă. Eticheta pasivă poate primi energia necesară din câmp, fără baterie proprie. Datele citite pot reprezenta o adresă sau un identificator. Aplicația interpretează datele în funcție de context.", "O etichetă NFC de pe un obiect poate conține un text. Telefonul o citește când este suficient de aproape. Dacă eticheta conține o adresă web, citirea adresei și accesarea paginii sunt două operații distincte. Internetul poate fi necesar pentru a doua, nu pentru prima.", "NFC utilizează cuplaj inductiv la 13,56 MHz. Modurile includ citirea etichetelor și emularea cardurilor, în funcție de hardware și software.", "În modul cititor, telefonul poate comunica cu etichete care folosesc formate precum NDEF. Emularea cardului poate implica un element securizat sau Host Card Emulation. Plățile folosesc protocoale și mecanisme de autorizare suplimentare; NFC este canalul de comunicație, nu întregul sistem de plată. Distanța mică nu garantează singură confidențialitatea.", "La o plată contactless, terminalul și telefonul schimbă date prin NFC. Aplicația de plată și infrastructura financiară pot verifica separat acreditările și tranzacția. O etichetă simplă cu text utilizează același principiu radio, dar nu același protocol de autorizare."), listOf("NFC is wireless communication over very short distances, usually a few centimeters. It can read tags, tickets and payments. Physical proximity is not equivalent to transaction authorization.", "A device can generate an electromagnetic field enabling communication with another device or passive tag. A passive tag may draw energy from the field without its own battery. Read data may represent an address or identifier. An app interprets it in context.", "An NFC tag on an object may contain text. The phone reads it when close enough. If it contains a web address, reading the address and visiting the page are separate operations. Internet may be needed for the latter, not the former.", "NFC uses inductive coupling at 13.56 MHz. Modes include tag reading and card emulation depending on hardware and software.", "In reader mode the phone may communicate with tags using formats such as NDEF. Card emulation may involve a secure element or Host Card Emulation. Payments use additional protocols and authorization; NFC is the communication channel, not the entire payment system. Short range alone does not guarantee confidentiality.", "During contactless payment the terminal and phone exchange data through NFC. The payment app and financial infrastructure may separately verify credentials and transaction. A simple text tag uses the same radio principle but not the same authorization protocol.")),
    "GPS" to DetailedLesson(listOf("GPS este un sistem de navigație prin satelit. Telefonul recepționează semnale și estimează poziția. GPS este unul dintre sistemele GNSS, alături de Galileo și altele. Recepția semnalelor satelitare nu necesită transmiterea poziției către sateliți.", "Semnalele conțin informații despre timp și poziția sateliților. Receptorul compară momentele de recepție pentru a estima distanțe. Clădirile, reflexiile și vizibilitatea cerului pot afecta precizia. Harta, poziția și calculul traseului sunt funcții diferite. O hartă online poate necesita internet chiar dacă poziția este calculată local.", "Un telefon cu o hartă salvată local poate afișa poziția în aer liber fără date mobile. Într-o clădire, poziția poate fi mai puțin precisă. Exemplul arată diferența dintre recepția semnalelor de navigație și descărcarea informațiilor cartografice.", "Receptorul GNSS estimează pseudodistanțe din timpul de propagare al semnalelor. În modelul obișnuit sunt necesare cel puțin patru măsurători independente pentru trei coordonate și eroarea ceasului receptorului.", "Geometria sateliților, erorile atmosferice și propagarea pe căi multiple influențează soluția. A-GNSS poate furniza date auxiliare prin rețea pentru accelerarea obținerii poziției. Android poate combina GNSS cu Wi-Fi, rețele celulare și senzori. Estimarea de precizie nu reprezintă o garanție că poziția reală se află într-un anumit cerc.", "Lumina parcurge aproximativ 300 metri într-o microsecundă. O eroare temporală de 10 nanosecunde corespunde aproximativ la 3 metri de pseudodistanță. Aceasta nu este direct eroarea poziției finale, care depinde de toate măsurătorile și geometria lor."), listOf("GPS is a satellite navigation system. A phone receives signals and estimates position. GPS is one GNSS constellation alongside Galileo and others. Receiving satellite signals does not require sending position to satellites.", "Signals contain timing and satellite position information. The receiver compares arrival times to estimate distances. Buildings, reflections and sky visibility affect accuracy. Maps, position and route calculation are distinct functions. Online maps may need internet even when position is calculated locally.", "A phone with an offline map may display position outdoors without mobile data. Inside a building position may be less accurate. This distinguishes navigation signal reception from map downloads.", "A GNSS receiver estimates pseudoranges from signal propagation time. The usual model needs at least four independent measurements for three coordinates and receiver clock error.", "Satellite geometry, atmospheric errors and multipath influence the solution. A-GNSS may provide assistance data over networks to accelerate fixes. Android may combine GNSS with Wi-Fi, cellular networks and sensors. An accuracy estimate does not guarantee the actual position lies inside a particular circle.", "Light travels about 300 meters in one microsecond. A timing error of 10 nanoseconds corresponds to about 3 meters of pseudorange. This is not directly final position error, which depends on all measurements and geometry.")),
    "Date mobile" to DetailedLesson(listOf("Datele mobile permit telefonului să comunice prin rețeaua celulară a operatorului. Telefonul schimbă semnale cu o stație de bază, iar rețeaua operatorului poate transporta informațiile către internet. Barele de semnal nu măsoară direct viteza internetului.", "Rețeaua împarte resursele radio între utilizatori. Distanța, obstacolele, frecvența și numărul dispozitivelor active influențează performanța. 4G și 5G reprezintă generații de tehnologii, nu viteze garantate. Volumul datelor, exprimat în MB sau GB, este diferit de debitul exprimat în Mb/s.", "Un videoclip poate consuma 300 MB indiferent dacă este descărcat în două minute sau zece minute, dacă fișierul este identic. Viteza schimbă durata, nu automat volumul. Streamingul adaptiv poate însă modifica rezoluția și cantitatea de date.", "Rețelele LTE și 5G NR utilizează resurse radio programate și infrastructură de transport și nucleu. Identitatea abonamentului este asociată cu SIM sau eSIM, iar conectivitatea IP este furnizată prin sesiuni de date.", "Calitatea radio depinde de puterea semnalului și raportul semnal-interferență-zgomot. Debitul depinde și de lățimea de bandă, modulație, programare și capacitatea infrastructurii. Latența include procesarea, transportul și așteptarea. Schimbarea celulei poate permite continuitatea conexiunii, dar nu elimină toate întreruperile.", "Un flux constant de 5 Mb/s timp de 10 minute transferă ideal 375 MB de date utile, deoarece opt biți formează un octet. Antetele și variațiile fluxului pot schimba volumul măsurat. Exemplul separă debitul de cantitatea cumulată."), listOf("Mobile data lets a phone communicate through a carrier's cellular network. The phone exchanges signals with a base station and the carrier network can transport information to the internet. Signal bars do not directly measure internet speed.", "Radio resources are shared among users. Distance, obstacles, frequency and active devices influence performance. 4G and 5G are technology generations, not guaranteed speeds. Data volume in MB or GB differs from throughput in Mb/s.", "A video may consume 300 MB whether downloaded in two or ten minutes if the file is identical. Speed changes duration, not automatically volume. Adaptive streaming may change resolution and data volume.", "LTE and 5G NR use scheduled radio resources and transport and core infrastructure. Subscription identity is associated with SIM or eSIM, and IP connectivity is provided through data sessions.", "Radio quality depends on signal power and signal-to-interference-plus-noise ratio. Throughput also depends on bandwidth, modulation, scheduling and infrastructure capacity. Latency includes processing, transport and waiting. Cell handover can support continuity but does not eliminate every interruption.", "A constant 5 Mb/s stream for ten minutes ideally transfers 375 MB of payload, since eight bits form a byte. Headers and stream variation may change measured volume. This separates throughput from cumulative quantity.")),
    "Hotspot" to DetailedLesson(listOf("Un hotspot permite telefonului să ofere altor dispozitive acces la o conexiune de rețea. De obicei telefonul creează o rețea Wi-Fi locală și partajează datele mobile. Unele dispozitive acceptă și alte forme de partajare.", "Telefonul îndeplinește două roluri: comunică cu dispozitivele conectate și transportă traficul către rețeaua din amonte. O conexiune la hotspot nu garantează internet dacă legătura din amonte nu funcționează. Mai multe dispozitive pot împărți aceeași capacitate. Activitatea radio și procesarea pot crește consumul bateriei.", "Un laptop conectat la hotspot poate descărca un document prin conexiunea mobilă a telefonului. Laptopul comunică local prin Wi-Fi, iar telefonul transmite traficul către operator. Dacă laptopul descarcă 100 MB, traficul mobil poate include acea cantitate și date suplimentare de protocol.", "Tetheringul poate utiliza Wi-Fi, USB sau Bluetooth. Android poate configura interfețe locale, alocarea adreselor și rutarea către interfața din amonte.", "În IPv4, partajarea folosește frecvent traducerea adreselor NAT; IPv6 poate utiliza mecanisme diferite. Debitul depinde de ambele legături, iar clienții concurează pentru resurse. Politicile operatorului și implementarea dispozitivului pot limita funcția. Criptarea Wi-Fi protejează segmentul local, nu automat întregul traseu.", "Dacă legătura mobilă oferă 30 Mb/s utili și trei clienți transferă simultan, o împărțire ideală egală ar fi 10 Mb/s fiecare. Distribuția reală poate fi inegală din cauza protocoalelor, semnalului și cererii fiecărui client."), listOf("A hotspot lets a phone share network connectivity with other devices. Usually it creates a local Wi-Fi network and shares mobile data. Some devices support other sharing methods.", "The phone has two roles: communicating with connected devices and forwarding traffic upstream. Joining a hotspot does not guarantee internet if the upstream link fails. Multiple devices may share capacity. Radio activity and processing can increase battery use.", "A laptop connected to a hotspot can download a document through the phone's mobile connection. The laptop communicates locally over Wi-Fi and the phone forwards traffic to the carrier. A 100 MB download may contribute that amount plus protocol traffic to mobile usage.", "Tethering may use Wi-Fi, USB or Bluetooth. Android can configure local interfaces, address allocation and routing to an upstream interface.", "IPv4 sharing often uses NAT; IPv6 may use different mechanisms. Throughput depends on both links and clients compete for resources. Carrier policies and device implementation may limit the feature. Wi-Fi encryption protects the local segment, not automatically the entire path.", "If mobile connectivity delivers 30 Mb/s of useful throughput and three clients transfer simultaneously, ideal equal sharing would be 10 Mb/s each. Actual distribution may be unequal due to protocols, signal and client demand.")),
    "DNS" to DetailedLesson(listOf("DNS este sistemul care asociază nume de domenii cu informații necesare comunicației, inclusiv adrese IP. Este asemănător unui catalog de adrese, nu unei hărți a întregului traseu. Browserul poate folosi rezultatul pentru a contacta un serviciu.", "Un nume este mai ușor de reținut decât o adresă numerică. Un domeniu poate avea mai multe adrese, iar o adresă poate deservi mai multe domenii. Rezultatele pot fi păstrate temporar în cache. DNS nu descarcă pagina și nu garantează că serviciul este disponibil sau legitim.", "Când telefonul accesează un nume fictiv precum serviciu.example, poate solicita informații DNS înainte de a iniția conexiunea. Dacă rezolvarea eșuează, pagina poate să nu se deschidă chiar dacă legătura Wi-Fi funcționează. Numele din exemplu este rezervat pentru documentație, nu un serviciu real.", "DNS este un sistem distribuit și ierarhic. Un resolver recursiv poate consulta servere rădăcină, servere ale domeniilor de nivel superior și servere autoritative. Înregistrările A și AAAA reprezintă adrese IPv4 și IPv6.", "TTL indică durata pentru care un rezultat poate fi păstrat în cache. DNSSEC poate valida autenticitatea datelor semnate, fără a le cripta. DNS over TLS și DNS over HTTPS protejează transportul către resolver, dar nu garantează legitimitatea site-ului. Android Private DNS poate utiliza DNS over TLS; unele aplicații folosesc propriile mecanisme.", "Într-un exemplu ipotetic, un rezultat are TTL de 300 secunde. După 120 secunde, un cache care respectă TTL poate reutiliza rezultatul pentru încă 180 secunde. O modificare la serverul autoritativ nu devine neapărat vizibilă imediat tuturor clienților."), listOf("DNS associates domain names with information needed for communication, including IP addresses. It resembles an address directory, not a map of the entire route. A browser can use the result to contact a service.", "Names are easier to remember than numerical addresses. A domain may have multiple addresses and an address may serve multiple domains. Results may be cached temporarily. DNS does not download a page or guarantee service availability or legitimacy.", "When a phone accesses a fictional name such as serviciu.example, it may request DNS information before connecting. If resolution fails, the page may not open even when Wi-Fi works. The example name is reserved for documentation, not a real service.", "DNS is distributed and hierarchical. A recursive resolver may consult root, top-level domain and authoritative servers. A and AAAA records represent IPv4 and IPv6 addresses.", "TTL indicates how long a result may be cached. DNSSEC can validate authenticity of signed data without encrypting it. DNS over TLS and DNS over HTTPS protect transport to the resolver but do not guarantee website legitimacy. Android Private DNS can use DNS over TLS; some apps use their own mechanisms.", "In a hypothetical example a result has a TTL of 300 seconds. After 120 seconds, a cache respecting TTL may reuse it for another 180 seconds. An authoritative server change need not become visible to all clients immediately.")),
    "Phishing" to DetailedLesson(listOf("Phishingul este o formă de înșelăciune în care o comunicare pretinde că provine dintr-o sursă de încredere. Scopul poate fi obținerea unor informații, a unor bani sau a accesului la un cont. Nu presupune obligatoriu exploatarea unei defecțiuni a telefonului: deseori mecanismul principal este manipularea încrederii.", "Aspectul unui mesaj și identitatea reală a expeditorului sunt lucruri diferite. O siglă, un nume cunoscut sau o formulare oficială pot fi imitate. Presiunea timpului și promisiunile pot influența evaluarea situației. Un mesaj poate conduce către o pagină care seamănă cu un serviciu real, fără să aparțină acelui serviciu. Această diferență este asemănătoare cu cea dintre un document autentic și o copie care îi imită aspectul.", "O persoană primește un e-mail despre o presupusă problemă a contului. Pagina indicată seamănă cu pagina serviciului cunoscut, dar aparține altei entități. Simpla recepționare a mesajului nu înseamnă că parola a fost divulgată. Exemplul separă comunicarea înșelătoare de eventualele consecințe ale interacțiunii.", "Phishingul aparține ingineriei sociale, adică influențării deciziilor umane pentru a obține un rezultat neautorizat. Poate combina imitația identității, redirecționarea și colectarea datelor.", "În comunicațiile web, numele domeniului, operatorul serviciului și aspectul paginii reprezintă niveluri distincte. HTTPS poate proteja transportul către un server fără să garanteze legitimitatea activității acelui server. Autentificarea domeniului de e-mail poate reduce anumite falsificări, dar nu dovedește adevărul mesajului. Phishingul direcționat către persoane sau grupuri specifice este numit spear phishing.", "Două pagini pot afișa același logo și pot avea conexiuni HTTPS valide, dar pot aparține unor domenii diferite. Certificatul conexiunii nu demonstrează că cele două organizații sunt identice. Exemplul ilustrează diferența dintre protecția transportului, identitatea serviciului și încrederea în conținut."), listOf("Phishing is deception in which a communication claims to come from a trusted source. Its goal may be information, money or account access. It does not necessarily exploit a phone defect: manipulation of trust is often central.", "Message appearance and actual sender identity are distinct. Logos, familiar names and official language may be imitated. Time pressure and promises can influence judgment. A message may lead to a page resembling a real service without belonging to it, like a document that imitates an authentic original.", "Someone receives an email about an alleged account problem. The indicated page resembles the known service but belongs to another entity. Merely receiving a message does not mean a password has been disclosed. This separates deceptive communication from possible consequences of interaction.", "Phishing is a form of social engineering, influencing human decisions to achieve unauthorized outcomes. It may combine impersonation, redirection and data collection.", "In web communication, the domain, service operator and page appearance are distinct layers. HTTPS may protect transport to a server without guaranteeing the legitimacy of its activity. Email domain authentication can reduce some forms of spoofing but cannot prove a message is truthful. Phishing targeting specific individuals or groups is called spear phishing.", "Two pages can display the same logo and have valid HTTPS connections while belonging to different domains. Connection certificates do not establish that the organizations are identical. This illustrates the difference between transport protection, service identity and trust in content.")),
    "Smishing" to DetailedLesson(listOf("Smishingul este phishing realizat prin SMS sau mesaje text. Denumirea combină SMS și phishing. Mesajul poate pretinde că vine de la o instituție, un curier sau un serviciu cunoscut, iar scopul poate fi obținerea unor date ori inițierea unei plăți.", "Un SMS este un mesaj transportat printr-un sistem de comunicații. Textul său reprezintă afirmațiile expeditorului, nu o dovadă independentă a identității. Pe unele sisteme, identificatorul afișat poate fi imitat. Mesajele scurte au puțin spațiu pentru context, iar ecranul telefonului poate afișa doar o parte dintr-o adresă. Un link este o referință către o destinație, nu destinația însăși.", "Un mesaj pretinde că un colet așteaptă o taxă suplimentară. Existența unui colet real nu demonstrează că mesajul este autentic. Mesajul, firma de transport și pagina de plată sunt trei elemente distincte. O solicitare falsă poate exista chiar atunci când persoana așteaptă într-adevăr o livrare.", "Smishingul exploatează canalul de mesagerie pentru inginerie socială. SMS, RCS și mesageria prin internet au infrastructuri și mecanisme de identificare diferite.", "Identificatorul expeditorului nu este echivalent cu o semnătură criptografică verificată a conținutului. Aplicațiile pot grupa mesajele după identificator, iar filtrarea spamului produce estimări, nu verdicte infailibile. Deschiderea unei adrese poate implica rezoluție DNS, conexiune și redirecționări. Riscul diferă între simpla recepție, accesarea destinației și divulgarea datelor.", "Într-un exemplu ipotetic, două mesaje apar sub același nume comercial. Unul aparține unui flux legitim, celălalt nu. Asemănarea afișării nu stabilește aceeași origine. Un filtru poate clasifica diferit cele două mesaje, dar clasificarea nu reprezintă dovadă absolută."), listOf("Smishing is phishing through SMS or text messages. The term combines SMS and phishing. Messages may impersonate institutions, couriers or familiar services to obtain data or initiate payments.", "An SMS travels through a communication system. Its text contains sender claims, not independent proof of identity. On some systems displayed sender identifiers may be imitated. Short messages offer little context and small screens can truncate addresses. A link is a reference to a destination, not the destination itself.", "A message claims a parcel requires an extra fee. A real pending parcel does not establish the authenticity of that message. The message, delivery company and payment page are distinct. A false request can coincide with a genuine delivery.", "Smishing uses messaging as a social engineering channel. SMS, RCS and internet messaging have different infrastructure and identity mechanisms.", "A sender identifier is not equivalent to a verified cryptographic signature of content. Apps may group messages by identifier, while spam filters provide estimates rather than infallible judgments. Opening an address may involve DNS resolution, connections and redirects. Risk differs between receipt, visiting a destination and disclosing information.", "In a hypothetical example two messages appear under one commercial name. One belongs to a legitimate flow and the other does not. Similar display does not establish common origin. A filter may classify them differently, but classification is not absolute proof.")),
    "Vishing" to DetailedLesson(listOf("Vishingul este inginerie socială prin comunicare vocală. Apelantul poate pretinde că reprezintă o organizație sau o persoană cunoscută. Vocea, numărul afișat și identitatea reală nu sunt același lucru.", "Conversația permite adaptarea mesajului la răspunsurile interlocutorului. Tonul, autoritatea pretinsă și urgența pot influența deciziile. Un apel poate fi înșelător fără ca telefonul să fie infectat. Identificarea apelantului este o funcție de semnalizare și nu reprezintă întotdeauna o autentificare sigură a persoanei care vorbește.", "Un apelant pretinde că există o problemă bancară. Ecranul poate afișa un număr familiar, dar acest fapt nu confirmă identitatea interlocutorului. Cunoașterea numelui persoanei apelate nu demonstrează nici ea că apelantul reprezintă banca. Exemplul evidențiază diferența dintre informații cunoscute și autoritate reală.", "Vishingul combină comunicația vocală cu manipularea socială. Spoofingul identității apelantului reprezintă afișarea unei identități de apel care nu reflectă fidel originea.", "Rețelele telefonice și serviciile VoIP transportă semnalizare și conținut vocal. Mecanismele de validare a identității apelurilor depind de infrastructură și regiune. Chiar și validarea unui număr nu demonstrează că afirmațiile persoanei sunt adevărate. Sinteza vocală poate adăuga o imitație a vocii; autentificarea transportului și autentificarea vorbitorului rămân probleme separate.", "Două apeluri pot afișa același identificator, dar pot proveni din contexte diferite. Un apel autentic poate transmite informații greșite, iar unul cu identitate falsificată poate include detalii adevărate. Aceste situații demonstrează că identitatea, conținutul și autorizarea sunt dimensiuni distincte."), listOf("Vishing is social engineering using voice communication. A caller may claim to represent an organization or familiar person. Voice, displayed number and actual identity are not identical.", "Conversation allows messages to adapt to responses. Tone, claimed authority and urgency can influence decisions. A call may be deceptive without infecting the phone. Caller identification is a signaling function and does not always securely authenticate the speaker.", "A caller claims there is a banking problem. The screen may show a familiar number, but this does not confirm the speaker's identity. Knowing the recipient's name does not establish that the caller represents the bank. Known information differs from actual authority.", "Vishing combines voice communication with social manipulation. Caller ID spoofing displays an identity that does not faithfully reflect the origin.", "Telephone networks and VoIP services carry signaling and voice content. Caller identity validation depends on infrastructure and region. Even validating a number does not prove a speaker's claims are true. Voice synthesis may add impersonation; transport authentication and speaker authentication remain separate.", "Two calls may display the same identifier but arise from different contexts. An authentic call may contain incorrect information, and a spoofed call may contain true details. Identity, content and authorization are distinct dimensions.")),
    "Coduri QR frauduloase" to DetailedLesson(listOf("Un cod QR este o reprezentare vizuală a unor date. Poate conține text, o adresă web sau alte informații. Un cod fraudulos folosește această reprezentare pentru a conduce utilizatorul către un context înșelător.", "Codul seamănă cu o etichetă care poate fi citită automat. Forma sa nu dovedește cine l-a creat. Camera și aplicația de scanare interpretează modelul de pătrate, apoi pot afișa datele sau propune o acțiune. Scanarea, deschiderea unei pagini și efectuarea unei plăți sunt etape distincte. Nu orice cod necunoscut este fraudulos.", "Pe un afiș de plată poate apărea un autocolant care acoperă codul original. Codul nou poate fi perfect lizibil, dar poate indica o altă destinație. Aspectul afișului rămâne familiar, în timp ce informația codificată s-a schimbat. Exemplul separă validitatea tehnică a codului de autenticitatea destinației.", "Codurile QR folosesc module vizuale și mecanisme de corectare a erorilor pentru reprezentarea robustă a datelor. Corectarea erorilor nu este autentificare criptografică.", "Scannerul decodează datele și poate delega interpretarea unui browser sau unei aplicații. O adresă codificată poate avea redirecționări, iar comportamentul ulterior depinde de aplicație și sistem. Quishingul este phishingul care utilizează coduri QR. Un cod lizibil, o conexiune criptată și o destinație legitimă sunt proprietăți independente.", "Două coduri pot fi decodificate fără erori și pot reprezenta două adrese diferite. Unul indică serviciul legitim, celălalt o pagină care îl imită. Corectarea erorilor poate ajuta citirea ambelor coduri, dar nu decide care destinație aparține organizației reale."), listOf("A QR code visually represents data. It may contain text, a web address or other information. A fraudulent code uses that representation to direct a user into a deceptive context.", "A code resembles a machine-readable label. Its shape does not prove who created it. A camera and scanner interpret its square pattern, then may display data or propose an action. Scanning, opening a page and making a payment are distinct stages. Not every unfamiliar code is fraudulent.", "A payment sign may have a sticker covering the original code. The replacement may be perfectly readable but point elsewhere. The sign remains familiar while encoded information changes. Technical code validity differs from destination authenticity.", "QR codes use visual modules and error correction to represent data robustly. Error correction is not cryptographic authentication.", "A scanner decodes data and may delegate interpretation to a browser or app. Encoded addresses may redirect, and subsequent behavior depends on the app and system. Quishing is phishing using QR codes. Readability, encrypted connections and legitimate destinations are independent properties.", "Two codes may decode without errors and represent different addresses. One points to a legitimate service, the other to an imitation. Error correction helps read both but does not establish which destination belongs to the real organization.")),
    "Deepfake" to DetailedLesson(listOf("Un deepfake este un material audio sau vizual sintetizat ori modificat cu tehnici de inteligență artificială pentru a crea o reprezentare aparent autentică. Poate imita vocea, fața sau mișcările unei persoane. Materialele sintetice pot avea utilizări legitime, iar existența lor nu implică automat fraudă.", "O înregistrare nu este întotdeauna dovada că persoana reprezentată a spus sau a făcut ceea ce apare în ea. Modelele pot reproduce caracteristici ale vocii și aspectului fără participarea persoanei imitate. Calitatea variază, iar defectele vizibile sau sonore nu sunt criterii infailibile. Un material autentic poate avea defecte din cauza compresiei sau iluminării.", "Un mesaj vocal pare să provină de la o persoană cunoscută. Vocea seamănă cu a acesteia, dar mesajul poate fi sintetizat. Asemănarea sunetului nu demonstrează originea înregistrării. Exemplul explică diferența dintre recunoașterea unei caracteristici și verificarea autenticității unui mesaj.", "Modelele generative pot sintetiza audio și imagini condiționate de reprezentări ale caracteristicilor unei persoane. Manipularea poate afecta întregul material sau numai anumite regiuni și segmente.", "Detectarea poate utiliza artefacte statistice, informații de proveniență și context. Compresia și transformările ulterioare pot modifica indiciile, iar detectoarele pot produce rezultate fals pozitive și fals negative. Semnăturile de proveniență pot documenta anumite etape, dar absența lor nu dovedește falsificarea. Autenticitatea unui fișier și adevărul afirmațiilor sale sunt aspecte distincte.", "Într-un test pur ipotetic cu 100 materiale autentice și 100 sintetice, un detector marchează 5 autentice și 90 sintetice. Rezultă 5% alarme false și 90% sensibilitate în acel test. Valorile nu sunt performanțe reale și nu garantează rezultate similare pe alte materiale."), listOf("A deepfake is audio or visual material synthesized or altered using AI techniques to create an apparently authentic representation. It may imitate someone's voice, face or movements. Synthetic media can have legitimate uses and is not automatically fraudulent.", "A recording is not always proof that the represented person said or did what appears in it. Models can reproduce voice and appearance characteristics without the person's participation. Quality varies, and visible or audible defects are not infallible criteria. Authentic media can have defects from compression or lighting.", "A voice message seems to come from someone familiar. It sounds like them, but may be synthesized. Similar sound does not prove recording origin. This distinguishes recognizing a characteristic from verifying message authenticity.", "Generative models can synthesize audio and images conditioned on representations of a person's characteristics. Manipulation may affect the whole material or selected regions and segments.", "Detection may use statistical artifacts, provenance information and context. Compression and later transformations may alter evidence, and detectors can return false positives and false negatives. Provenance signatures can document some stages, but their absence does not prove fabrication. File authenticity and the truth of its claims are distinct.", "In a purely hypothetical test of 100 authentic and 100 synthetic items, a detector flags 5 authentic and 90 synthetic items. This gives 5% false alarms and 90% sensitivity in that test. These are not real performance results and do not guarantee similar results elsewhere.")),
    "Blocarea ecranului" to DetailedLesson(listOf("Blocarea ecranului este mecanismul care separă accesul obișnuit la telefon de accesul autorizat. Telefonul poate continua să primească apeluri sau notificări chiar când este blocat. Blocarea nu înseamnă oprirea sistemului și nici întreruperea tuturor aplicațiilor.", "Imaginează-ți o locuință cu ușa încuiată, în care instalațiile continuă să funcționeze. Android verifică identitatea printr-un PIN, o parolă, un model sau o metodă biometrică acceptată. Informațiile vizibile înainte de deblocare depind de configurarea telefonului. Blocarea ecranului și criptarea datelor sunt mecanisme complementare, dar distincte. După prima deblocare de la pornire, unele date pot rămâne disponibile serviciilor autorizate chiar dacă ecranul se blochează din nou.", "Un telefon blocat primește un mesaj. Sistemul poate afișa doar numele aplicației, conținutul mesajului sau nicio notificare, în funcție de configurare. Persoana care vede ecranul nu primește automat acces la conversație. După autentificare, utilizatorul poate deschide aplicația. Exemplul arată diferența dintre informațiile afișate pe ecranul blocat și accesul la sesiunea utilizatorului.", "Android utilizează componente de autentificare și interfața Keyguard pentru controlul accesului la sesiune. Verificarea acreditărilor poate implica componente hardware securizate, în funcție de dispozitiv.", "Gatekeeper sau mecanisme echivalente pot verifica acreditările și limita încercările. Android poate utiliza tokenuri de autentificare pentru autorizarea operațiilor cu chei criptografice. În criptarea bazată pe fișiere, datele disponibile înainte de prima deblocare diferă de datele protejate prin acreditările utilizatorului. Blocarea ulterioară a ecranului nu implică automat eliminarea tuturor cheilor din memorie. Politicile de autentificare și acces depind de versiunea Android și de implementarea hardware.", "După repornire, o aplicație compatibilă cu Direct Boot poate primi evenimente și utiliza date din spațiul protejat la nivel de dispozitiv. Datele din spațiul protejat prin acreditări devin disponibile după prima deblocare. Dacă utilizatorul blochează apoi ecranul, unele servicii pot continua să lucreze. Exemplul diferențiază starea de pornire, autentificarea inițială și blocarea interfeței."), listOf("Screen lock separates ordinary interaction with a phone from authorized access. Calls and notifications can still arrive while locked. Locking does not shut down the operating system or stop every app.", "Imagine a locked house whose utilities continue operating. Android verifies identity through a PIN, password, pattern or accepted biometric method. Information visible before unlocking depends on configuration. Screen locking and encryption are complementary but distinct. After the first unlock following startup, some data may remain available to authorized services even when the screen locks again.", "A locked phone receives a message. Depending on settings, it may display the app name, message content or no notification. Someone viewing the screen does not automatically gain access to the conversation. After authentication the user can open the app. This distinguishes lock-screen information from access to the user session.", "Android uses authentication components and Keyguard to control session access. Credential verification may involve secure hardware depending on the device.", "Gatekeeper or equivalent mechanisms can verify credentials and throttle attempts. Android may use authentication tokens to authorize cryptographic key operations. With file-based encryption, data available before first unlock differs from credential-protected data. Relocking does not automatically evict all keys from memory. Authentication and access policies depend on Android version and hardware implementation.", "After restart, a Direct Boot-aware app may receive events and use device-protected storage. Credential-protected storage becomes available after first unlock. If the user then locks the screen, some services can continue working. This distinguishes startup state, initial authentication and interface locking.")),
    "Criptare" to DetailedLesson(listOf("Criptarea transformă informațiile într-o reprezentare care nu poate fi interpretată în mod normal fără cheia potrivită. Datele nu dispar, ci sunt protejate printr-o transformare matematică reversibilă cu cheia necesară. Este asemănătoare păstrării unui document într-un seif.", "Criptarea poate proteja fișierele stocate sau informațiile transmise prin internet. În primul caz, protecția vizează datele de pe dispozitiv. În al doilea, protecția vizează comunicația dintre punctele care folosesc protocolul. Criptarea de la un capăt la altul urmărește ca mesajul să poată fi citit la capetele autorizate, nu de intermediarii serviciului. Aceste mecanisme nu sunt identice și nu împiedică toate formele de acces la informații.", "Imaginează-ți o fotografie stocată pe telefon și trimisă printr-un serviciu de mesagerie. Copia locală poate fi protejată prin criptarea dispozitivului, iar transmisia printr-un protocol criptat. Dacă serviciul folosește criptare de la un capăt la altul, conținutul mesajului este protejat și față de intermediari, în limitele implementării. Fotografia poate fi totuși vizibilă pe telefonul destinatarului după decriptare.", "Criptarea utilizează algoritmi și chei pentru transformarea textului clar în text cifrat. Criptarea simetrică utilizează un secret comun, iar criptografia asimetrică utilizează perechi de chei cu roluri distincte.", "Android modern utilizează criptarea bazată pe fișiere pe dispozitivele compatibile, cu clase de stocare protejate diferit. Android Keystore poate proteja cheile și poate utiliza hardware securizat. Protocoalele TLS protejează comunicația între capetele conexiunii, dar serverul poate avea acces la conținut. Criptarea autentificată poate proteja și integritatea datelor. Protecția depinde de gestionarea cheilor, implementare și starea dispozitivului; criptarea nu ascunde automat toate metadatele.", "Într-un serviciu obișnuit cu TLS, mesajul poate fi decriptat la server și criptat din nou către destinatar. Într-un sistem cu criptare de la un capăt la altul, serverul poate retransmite textul cifrat fără cheia de decriptare a mesajului. În ambele cazuri, dispozitivele finale pot afișa textul clar. Exemplul evidențiază locul unde se termină protecția criptografică."), listOf("Encryption transforms information into a representation that cannot normally be interpreted without the appropriate key. Data does not disappear; it is protected through a mathematical transformation reversible with the necessary key, like a document kept in a safe.", "Encryption can protect stored files or information transmitted over the internet. The first protects device data, while the second protects communication between protocol endpoints. End-to-end encryption aims to keep messages readable at authorized endpoints rather than by service intermediaries. These mechanisms differ and do not prevent every form of information access.", "Imagine a photo stored on a phone and sent through messaging. The local copy may be protected by device encryption and transmission by an encrypted protocol. With end-to-end encryption, message content is also protected from intermediaries within implementation limits. The recipient can still view the decrypted photo.", "Encryption uses algorithms and keys to transform plaintext into ciphertext. Symmetric encryption uses a shared secret, while asymmetric cryptography uses key pairs with distinct roles.", "Modern Android uses file-based encryption on compatible devices, with differently protected storage classes. Android Keystore can protect keys and use secure hardware. TLS protects communication between connection endpoints, but servers may access content. Authenticated encryption can also protect integrity. Protection depends on key management, implementation and device state; encryption does not automatically hide all metadata.", "In an ordinary TLS-based service, a message may be decrypted at the server and encrypted again toward the recipient. In an end-to-end encrypted system, the server may relay ciphertext without the message decryption key. Both systems can display plaintext on final devices. This illustrates where cryptographic protection ends.")),
    "Autentificare în doi pași" to DetailedLesson(listOf("Autentificarea în doi pași presupune două etape de verificare înainte de acordarea accesului. Etapele pot utiliza o parolă și o confirmare pe alt dispozitiv. Scopul este reducerea dependenței de un singur secret.", "Factorii de autentificare pot reprezenta ceva cunoscut, ceva deținut sau o caracteristică biometrică. Două etape nu înseamnă neapărat doi factori independenți: două parole aparțin aceleiași categorii. Codurile temporare, confirmările și cheile de securitate funcționează diferit. Protecția depinde și de modul în care serviciul verifică identitatea și permite recuperarea contului.", "O persoană introduce parola unui cont, iar serviciul solicită și confirmarea pe telefon. Cunoașterea parolei nu este suficientă pentru finalizarea autentificării dacă a doua verificare nu este îndeplinită. Dacă sesiunea este deja autorizată, serviciul poate să nu repete verificarea la fiecare accesare. Exemplul separă autentificarea inițială de utilizarea unei sesiuni existente.", "Autentificarea multifactor combină factori din categorii distincte. Verificarea în doi pași descrie succesiunea etapelor, fără a garanta independența factorilor.", "TOTP generează coduri pe baza unui secret comun și a timpului. Confirmările push pot asocia o solicitare cu un dispozitiv înregistrat. WebAuthn utilizează criptografie cu chei publice și poate lega autentificarea de originea serviciului, reducând anumite riscuri de phishing. Codurile introduse manual nu oferă automat aceeași protecție. Recuperarea contului, tokenurile de sesiune și înregistrarea dispozitivelor fac parte din modelul de securitate.", "Un serviciu verifică o parolă și un cod temporar, iar altul verifică o semnătură criptografică legată de originea site-ului. Ambele pot implica verificări suplimentare, dar proprietățile lor diferă. Un cod poate fi transmis manual către altă pagină, în timp ce verificarea originii limitează utilizarea unei semnături pentru un domeniu diferit. Exemplul arată de ce metodele nu sunt echivalente."), listOf("Two-step verification requires two verification stages before access is granted. They may use a password and confirmation on another device. The aim is to reduce dependence on a single secret.", "Authentication factors can represent something known, something possessed or a biometric characteristic. Two steps do not necessarily mean two independent factors: two passwords belong to the same category. Temporary codes, confirmations and security keys work differently. Protection also depends on identity verification and account recovery.", "A person enters an account password and the service requests phone confirmation. Knowing the password is insufficient if the second check is not completed. An already authorized session may not require repeated verification on every visit. This separates initial authentication from existing session use.", "Multifactor authentication combines factors from distinct categories. Two-step verification describes a sequence without guaranteeing factor independence.", "TOTP generates codes from a shared secret and time. Push confirmations can associate requests with enrolled devices. WebAuthn uses public-key cryptography and can bind authentication to the service origin, reducing certain phishing risks. Manually entered codes do not automatically offer equivalent protection. Account recovery, session tokens and device enrollment are part of the security model.", "One service verifies a password and temporary code, while another verifies a cryptographic signature bound to the website origin. Both may involve additional checks but have different properties. A code can be manually relayed to another page, while origin verification limits signature use for a different domain. The methods are not equivalent.")),
    "Biometrie" to DetailedLesson(listOf("Biometria utilizează caracteristici fizice pentru verificarea identității, precum amprenta sau trăsăturile feței. Telefonul compară măsurătorile cu un model înregistrat anterior. Rezultatul este o estimare a potrivirii, nu o certitudine absolută.", "Senzorii de amprentă pot utiliza tehnologii capacitive, optice sau ultrasonice. Recunoașterea facială poate utiliza imagini sau informații de profunzime, în funcție de dispozitiv. Sistemul extrage caracteristici și le compară cu modelul biometric. Lumina, poziția, umezeala sau alte condiții pot influența rezultatul. Metodele biometrice diferă ca precizie și nivel de securitate.", "Un utilizator își deblochează telefonul cu amprenta în condiții obișnuite, dar senzorul poate respinge aceeași amprentă când degetul este umed. Respingerea nu înseamnă automat că modelul înregistrat a dispărut. După repornire, sistemul poate solicita PIN-ul înainte de a permite utilizarea biometriei. Aceste situații reflectă atât limitele măsurării, cât și regulile de autentificare.", "Sistemele biometrice extrag caracteristici și calculează un scor de similitudine față de un șablon. Un prag de acceptare separă rezultatele acceptate de cele respinse.", "Ratele de acceptare falsă și respingere falsă descriu erori diferite. Modificarea pragului poate schimba compromisul dintre ele. Detectarea prezentărilor frauduloase și protejarea șabloanelor sunt aspecte distincte. Android clasifică metodele biometrice în funcție de cerințe de securitate; BiometricPrompt permite aplicațiilor să solicite autentificare fără acces direct la șablonul biometric. Implementarea depinde de hardware și de versiunea sistemului.", "Într-un test ipotetic cu 1.000 de încercări legitime, 20 sunt respinse, rezultând o rată de respingere de 2%. Într-un test separat cu 10.000 de încercări nelegitime, una este acceptată, rezultând 0,01%. Aceste valori sunt ilustrative, nu specificații ale telefonului. Ele arată de ce confortul utilizării și rezistența la acceptări eronate trebuie evaluate separat."), listOf("Biometrics uses physical characteristics such as fingerprints or facial features to verify identity. The phone compares measurements with a previously enrolled template. The result is an estimate of similarity, not absolute certainty.", "Fingerprint sensors may use capacitive, optical or ultrasonic technology. Facial recognition may use images or depth information depending on hardware. The system extracts features and compares them with a template. Lighting, position, moisture and other conditions can affect results. Biometric methods differ in accuracy and security.", "A user unlocks a phone with a fingerprint under ordinary conditions, but the sensor may reject the same finger when wet. Rejection does not automatically mean the template disappeared. After restart, the system may require a PIN before allowing biometrics. These situations reflect measurement limits and authentication rules.", "Biometric systems extract features and calculate a similarity score against a template. An acceptance threshold separates accepted and rejected results.", "False acceptance and false rejection rates describe different errors. Changing the threshold can alter their tradeoff. Presentation attack detection and template protection are separate concerns. Android classifies biometrics according to security requirements; BiometricPrompt lets apps request authentication without direct access to biometric templates. Implementation depends on hardware and system version.", "In a hypothetical test of 1,000 legitimate attempts, 20 are rejected, giving a 2% rejection rate. In a separate test of 10,000 illegitimate attempts, one is accepted, giving 0.01%. These are illustrative figures, not phone specifications. They show why convenience and resistance to erroneous acceptance are evaluated separately.")),
    "Parolă și PIN" to DetailedLesson(listOf("Parola și PIN-ul sunt informații secrete utilizate pentru verificarea accesului. PIN-ul este format din cifre, iar parola poate include litere, cifre și alte caractere. Un cod de deblocare local și parola unui cont online au roluri diferite.", "Numărul combinațiilor posibile crește odată cu lungimea și diversitatea caracterelor. Totuși, oamenii nu aleg întotdeauna combinații cu aceeași probabilitate. Sistemele pot limita încercările și pot introduce întârzieri. De aceea, numărul matematic de combinații nu indică singur rezistența reală a unui telefon sau cont. Un PIN poate participa și la protejarea accesului la chei criptografice, în funcție de implementare.", "Un PIN de patru cifre are 10.000 de combinații dacă sunt permise toate secvențele, inclusiv cele care încep cu zero. Un PIN de șase cifre are un milion. Diferența este de o sută de ori în spațiul combinațiilor, dar nu înseamnă automat că un telefon poate fi compromis într-un timp calculat direct din aceste valori. Regulile de verificare și alegerea codului contează.", "Pentru un secret ales uniform dintr-un alfabet de N simboluri, cu lungimea L și repetare permisă, spațiul posibil este N la puterea L. Entropia ideală este logaritmul în baza doi al acestui număr.", "Distribuția reală a secretelor poate fi neuniformă. Verificarea locală poate implica hardware securizat și limitarea încercărilor. Serviciile online pot utiliza funcții de derivare a cheilor, valori salt și politici de autentificare. Aceste mecanisme diferă de simpla stocare a parolei în clar. Rezistența depinde de modelul de amenințare, implementare și posibilitatea verificării încercărilor online sau offline.", "Un PIN uniform de șase cifre are aproximativ 19,93 biți de entropie ideală, iar unul de opt cifre aproximativ 26,58 biți. Diferența este de aproximativ 6,64 biți, corespunzând unui spațiu de o sută de ori mai mare. Dacă alegerea nu este uniformă, entropia efectivă poate fi mai mică. Exemplul descrie proprietăți matematice, nu o estimare a timpului de compromitere."), listOf("Passwords and PINs are secrets used to verify access. A PIN consists of digits, while a password may contain letters, digits and other characters. A local unlock code and an online account password serve different roles.", "The number of possible combinations grows with length and character diversity. People do not always choose combinations with equal probability. Systems may throttle attempts and introduce delays. Mathematical combination counts alone therefore do not establish actual phone or account resistance. A PIN may also help protect access to cryptographic keys depending on implementation.", "A four-digit PIN has 10,000 combinations if all sequences are allowed, including leading zeros. A six-digit PIN has one million. The combination space is one hundred times larger, but this does not automatically establish a time to compromise a phone. Verification rules and code selection matter.", "For a uniformly chosen secret of length L from an alphabet of N symbols with repetition allowed, the possibility space is N raised to L. Ideal entropy is the base-two logarithm of that count.", "Real secret distributions can be nonuniform. Local verification may involve secure hardware and throttling. Online services may use key derivation functions, salts and authentication policies. These differ from storing plaintext passwords. Resistance depends on the threat model, implementation and whether attempts can be verified online or offline.", "A uniform six-digit PIN has about 19.93 bits of ideal entropy, while an eight-digit PIN has about 26.58 bits. The difference is about 6.64 bits, corresponding to a hundredfold larger space. Nonuniform choices may have lower effective entropy. This describes mathematical properties, not time to compromise.")),
    "Permisiunile aplicațiilor" to DetailedLesson(listOf("Permisiunile sunt reguli prin care Android controlează accesul aplicațiilor la informații și componente ale telefonului. De exemplu, accesul la microfon permite captarea sunetului în condițiile stabilite de sistem. O permisiune acordată nu dovedește că funcția este utilizată permanent și nici că datele sunt transmise altcuiva.", "Imaginează-ți o clădire în care fiecare încăpere are propria cheie. O aplicație primește acces la anumite încăperi, nu automat la întreaga clădire. Unele drepturi sunt acordate la instalare, altele necesită o confirmare, iar accesările speciale au reguli separate. În funcție de versiunea Android, locația poate fi aproximativă sau precisă, iar accesul poate fi limitat la perioada utilizării aplicației. Accesul la o fotografie aleasă prin selectorul sistemului poate fi diferit de accesul la întreaga colecție. Permisiunile descriu posibilități de acces, nu intențiile dezvoltatorului.", "O aplicație de navigație poate utiliza locația pentru a afișa poziția pe hartă. O aplicație de mesagerie poate utiliza microfonul când înregistrezi un mesaj vocal. În ambele cazuri, existența permisiunii nu spune cât de des este folosită. Pe versiunile compatibile, indicatorii de confidențialitate și istoricul accesărilor oferă informații despre anumite utilizări efective. Transmiterea datelor prin rețea este o operație distinctă de captarea lor.", "Android izolează aplicațiile prin identități Linux, sandbox și politici SELinux. Manifestul declară permisiunile solicitate, iar sistemul aplică reguli în funcție de nivelul de protecție, versiunea platformei și versiunea Android vizată de aplicație.", "Permisiunile normale sunt în general acordate automat, cele periculoase pot necesita acord în timpul execuției, iar permisiunile de semnătură depind de identitatea criptografică sau alte condiții definite de platformă. Accesările speciale, precum afișarea peste alte aplicații, urmează mecanisme distincte. AppOps poate controla sau înregistra anumite operații suplimentar verificărilor de permisiuni. Dreptul de acces, operația efectivă și transferul ulterior al datelor sunt trei niveluri distincte de analiză. O listă de permisiuni nu constituie un jurnal complet al comportamentului.", "Două aplicații declară acces la cameră. Prima are permisiunea acordată și deschide camera în timpul unei fotografii; a doua nu a primit acordul și solicitarea ei poate fi respinsă. Chiar dacă ambele primesc ulterior acordul, lista permisiunilor nu indică numărul fotografiilor realizate. Un selector foto poate furniza unei a treia aplicații acces la un fișier ales fără acces general la cameră. Exemplul separă declarația, autorizarea și operația efectivă."), listOf("Permissions are rules through which Android controls app access to phone information and components. Microphone access, for example, enables audio capture under system-defined conditions. A granted permission does not prove continuous use or transmission of data.", "Imagine a building where each room has its own key. An app receives access to particular rooms, not automatically the entire building. Some rights are granted at installation, others require confirmation, and special access follows separate rules. Depending on Android version, location can be approximate or precise and access may be limited to app use. Access to a selected photo through the system picker differs from access to an entire collection. Permissions describe access capabilities, not developer intentions.", "A navigation app may use location to display a position on a map. A messaging app may use the microphone when recording a voice message. Neither permission establishes how often it is used. On compatible versions, privacy indicators and access history provide information about certain actual uses. Network transmission is distinct from capturing data.", "Android isolates apps using Linux identities, sandboxing and SELinux policies. The manifest declares requested permissions, while enforcement depends on protection level, platform version and the Android version targeted by the app.", "Normal permissions are generally granted automatically, dangerous permissions may require runtime consent, and signature permissions depend on signing identity or other platform-defined conditions. Special access such as drawing over apps follows distinct mechanisms. AppOps can control or record certain operations in addition to permission checks. Access rights, actual operations and subsequent data transfers are separate analytical layers. A permission list is not a complete behavior log.", "Two apps declare camera access. The first has permission and opens the camera for a photograph; the second has not received consent and its request may be rejected. Even if both later receive permission, their permission lists do not reveal how many photos were taken. A photo picker can provide a third app access to a selected file without general camera access. This separates declaration, authorization and actual operation.")),
    "APK" to DetailedLesson(listOf("Un APK este un pachet de instalare Android, asemănător unei cutii care conține componentele unei aplicații. Include cod, imagini, texte și informații despre cerințele aplicației. Fișierul APK nu este același lucru cu aplicația deja instalată.", "Android citește informațiile pachetului, verifică semnătura digitală și compatibilitatea, apoi pregătește aplicația pentru utilizare. Unele aplicații sunt distribuite prin mai multe pachete complementare, adaptate dispozitivului. Semnătura permite verificarea integrității și a continuității identității între versiuni, dar nu certifică singură că aplicația este sigură. Magazinul de aplicații și sistemul de instalare pot aplica verificări suplimentare.", "Un APK de 30 MB poate ocupa după instalare mai mult spațiu, deoarece aplicația poate genera fișiere și date suplimentare. O actualizare poate avea aceeași identitate de pachet, dar cod diferit. Dacă semnătura nu este compatibilă cu cea a aplicației instalate, Android poate refuza actualizarea. Dimensiunea și numele fișierului nu demonstrează identitatea dezvoltatorului.", "APK este un container bazat pe ZIP care poate include AndroidManifest.xml, fișiere DEX, resurse compilate și biblioteci native. Android App Bundle este un format de publicare din care pot fi generate APK-uri adaptate dispozitivului.", "Package Manager verifică structura, semnătura și cerințele de instalare. Schemele moderne de semnare protejează integritatea pachetului; actualizările necesită o identitate de semnare compatibilă, inclusiv mecanisme acceptate de rotație a cheilor. Pachetele split pot separa resursele după limbă, arhitectură sau densitate. Semnătura nu reprezintă o analiză a comportamentului codului. Compatibilitatea depinde și de versiunea sistemului și de componentele native.", "Un pachet de bază de 20 MB și două pachete complementare de 5 MB și 8 MB însumează 33 MB de fișiere distribuite. Spațiul instalat poate diferi din cauza compresiei, optimizării și datelor generate. Un alt dispozitiv poate primi alte pachete complementare pentru arhitectura sa. Exemplul explică diferența dintre dimensiunea descărcării și amprenta locală."), listOf("An APK is an Android installation package, like a box containing app components. It includes code, images, text and app requirements. An APK file is not the same as an installed app.", "Android reads package information, verifies digital signatures and compatibility, and prepares the app. Some apps use multiple complementary packages tailored to the device. Signing verifies integrity and identity continuity across versions but does not alone certify safety. App stores and installation systems may perform additional checks.", "A 30 MB APK may occupy more space after installation because the app can generate additional files and data. An update may retain the same package identity while changing code. Android may reject an update with an incompatible signature. File size and filename do not establish developer identity.", "An APK is a ZIP-based container that can include AndroidManifest.xml, DEX files, compiled resources and native libraries. An Android App Bundle is a publishing format from which device-specific APKs can be generated.", "Package Manager checks structure, signatures and installation requirements. Modern signing schemes protect package integrity; updates require compatible signing identity, including supported key rotation. Split packages can separate resources by language, architecture or density. Signing does not analyze code behavior. Compatibility also depends on system version and native components.", "A 20 MB base package and two splits of 5 MB and 8 MB total 33 MB of distributed files. Installed size can differ because of compression, optimization and generated data. Another device may receive different architecture-specific splits. This distinguishes download size from local footprint.")),
    "Procese în fundal" to DetailedLesson(listOf("O aplicație poate continua anumite activități chiar dacă nu este vizibilă. Un proces păstrat în memorie nu înseamnă însă că execută permanent operații. Android diferențiază aplicațiile active de cele inactive și gestionează resursele în funcție de necesități.", "Redarea muzicii, sincronizarea și descărcările sunt exemple de activități care pot continua în fundal. Sistemul limitează unele activități pentru a reduce consumul de energie. O aplicație poate rămâne în RAM fără să utilizeze semnificativ procesorul. În alte situații, sistemul poate programa o sarcină pentru mai târziu, când sunt îndeplinite anumite condiții. Comportamentul diferă în funcție de versiunea Android și de producător.", "Asculți muzică și stingi ecranul. Redarea poate continua, în timp ce o aplicație de știri rămasă în memorie nu efectuează neapărat activități. O sincronizare poate porni ulterior pentru câteva secunde. Toate aceste aplicații pot apărea în statistici diferite, dar prezența lor în memorie nu indică același consum de energie.", "Android clasifică importanța proceselor în funcție de componentele active și relația cu utilizatorul. Serviciile foreground, sarcinile programate și procesele cached au comportamente și restricții diferite.", "JobScheduler și WorkManager permit programarea unor activități amânabile. Doze și App Standby limitează anumite operații în condiții specifice. Serviciile foreground sunt supuse cerințelor de notificare și restricțiilor dependente de versiune. Un proces cached poate fi eliminat sub presiune de memorie. Timpul CPU, activitatea rețelei și menținerea dispozitivului treaz sunt indicatori diferiți de memoria ocupată.", "Într-un interval ipotetic de 60 minute, o sarcină execută șase sincronizări de câte 5 secunde, însumând 30 secunde de activitate. Un player audio poate funcționa pe tot intervalul. Durata nu permite singură calcularea energiei consumate, deoarece puterea procesorului, rețeaua și componentele active diferă. Exemplul separă timpul de existență al procesului de activitatea sa."), listOf("An app can continue certain activities while not visible. A process retained in memory does not necessarily execute continuously. Android distinguishes active and inactive apps and manages resources according to demand.", "Music playback, synchronization and downloads can continue in the background. The system limits some activities to reduce energy use. An app can remain in RAM without significant CPU activity. Other tasks may be scheduled for later when conditions are met. Behavior varies by Android version and manufacturer.", "You listen to music and turn off the screen. Playback may continue while a news app retained in memory does not necessarily perform work. Synchronization may start later for a few seconds. These apps can appear in different statistics, but memory presence does not imply equal energy use.", "Android classifies process importance according to active components and their relationship to the user. Foreground services, scheduled tasks and cached processes have different behaviors and restrictions.", "JobScheduler and WorkManager support deferrable tasks. Doze and App Standby restrict certain operations under specific conditions. Foreground services have notification requirements and version-dependent restrictions. Cached processes can be removed under memory pressure. CPU time, network activity and keeping the device awake differ from occupied memory.", "In a hypothetical 60-minute interval a task performs six five-second synchronizations, totaling 30 seconds of activity. An audio player may operate throughout. Duration alone cannot establish energy consumption because processor power, network and active components differ. This separates process lifetime from actual activity.")),
    "Aplicații de sistem" to DetailedLesson(listOf("Aplicațiile de sistem sunt componente software furnizate împreună cu telefonul. Ele pot oferi funcții precum setările, apelurile sau administrarea interfeței. Nu toate aplicațiile preinstalate au aceleași drepturi și nu toate sunt indispensabile.", "Unele aplicații fac parte din imaginea sistemului, iar altele sunt instalate de producător sau operator prin mecanisme diferite. O aplicație de sistem poate primi actualizări fără înlocuirea întregului Android. Posibilitatea de dezactivare sau eliminare diferă între componente. Preinstalarea nu demonstrează automat nici siguranța, nici existența unor privilegii speciale.", "Telefonul poate avea o aplicație Setări și o aplicație de muzică preinstalată. Prima coordonează interfețe pentru configurarea sistemului, iar a doua poate avea un rol asemănător unei aplicații instalate ulterior. Ambele pot apărea ca preinstalate, dar funcțiile și drepturile lor diferă. O actualizare a aplicației de muzică nu implică automat o actualizare a întregului sistem.", "Android distinge aplicațiile incluse în partiții de sistem de aplicațiile instalate în partiția de date. Aplicațiile privilegiate formează o categorie distinctă, supusă regulilor platformei.", "Permisiunile privilegiate pot necesita includerea în locații și liste de autorizare specifice. Permisiunile de semnătură depind de identitatea de semnare sau condiții speciale. O actualizare a unei aplicații de sistem poate fi instalată în partiția de date, în timp ce versiunea originală rămâne în imaginea sistemului. Identitatea Linux, SELinux și verificările serviciilor continuă să limiteze accesul.", "Două pachete preinstalate declară aceeași permisiune privilegiată. Unul îndeplinește condițiile de autorizare ale platformei, celălalt nu. Simplul fapt că ambele sunt preinstalate nu le acordă drepturi identice. Dacă primul primește o actualizare compatibilă, codul utilizat poate proveni din partiția de date fără ca pachetul să își piardă automat statutul de aplicație de sistem."), listOf("System apps are software components supplied with the phone. They may provide settings, calling or interface management. Not all preinstalled apps have the same rights or are indispensable.", "Some apps are part of the system image, while others are installed by manufacturers or carriers through different mechanisms. A system app can receive updates without replacing all of Android. Disabling or removal options vary. Preinstallation alone proves neither safety nor special privileges.", "A phone may include Settings and a preinstalled music app. Settings coordinates system configuration interfaces, while the music app may have a role similar to a later-installed app. Both are preinstalled but their functions and rights differ. Updating the music app does not automatically update the whole system.", "Android distinguishes apps included in system partitions from apps installed in the data partition. Privileged apps are a distinct category subject to platform rules.", "Privileged permissions may require specific locations and allowlists. Signature permissions depend on signing identity or special conditions. A system app update may reside in the data partition while its original version remains in the system image. Linux identity, SELinux and service checks continue to constrain access.", "Two preinstalled packages declare the same privileged permission. One meets platform authorization conditions while the other does not. Preinstallation alone does not give them identical rights. A compatible update to the first may run from the data partition without automatically losing system-app status.")),
    "Dezinstalare" to DetailedLesson(listOf("Dezinstalarea elimină o aplicație pentru utilizatorul respectiv și, în mod obișnuit, datele sale private locale. Nu este identică cu ștergerea contului online, a tuturor fișierelor partajate sau a copiilor de rezervă.", "Imaginează-ți o bibliotecă în care returnezi un catalog, dar documentele păstrate în altă clădire rămân acolo. Aplicația poate stoca date pe telefon și pe servere externe. Eliminarea aplicației afectează în principal instalarea locală. Fișierele exportate în spații partajate pot rămâne, iar datele online sunt administrate separat. Dezactivarea unei aplicații de sistem și arhivarea unei aplicații sunt operații diferite de dezinstalare.", "O aplicație de mesagerie ocupă 200 MB prin cod și date private, iar utilizatorul a exportat fotografii într-un album partajat. După dezinstalare, spațiul privat poate fi eliberat, dar albumul poate rămâne. Contul și mesajele păstrate pe server pot exista în continuare. La reinstalare, unele date pot fi restaurate prin mecanisme de sincronizare sau backup, în funcție de serviciu.", "Package Manager gestionează instalarea și eliminarea pachetelor în contextul utilizatorilor Android. Datele private ale aplicației sunt în mod obișnuit eliminate la dezinstalare, cu excepții dependente de mecanismul utilizat și de platformă.", "Android poate avea mai mulți utilizatori sau profiluri, iar eliminarea pentru unul nu implică automat eliminarea pentru ceilalți. Fișierele din colecții partajate pot avea un ciclu de viață independent. Backupul și sincronizarea pot păstra date în alte locații. Pentru aplicațiile de sistem, eliminarea actualizărilor poate readuce versiunea inclusă în imaginea sistemului. Dezinstalarea nu garantează ștergerea fizică imediată a tuturor urmelor din memoria flash.", "Un pachet ocupă ipotetic 80 MB de cod, 120 MB de date private și are 300 MB de fotografii exportate în spațiul partajat. Eliminarea obișnuită poate elibera aproximativ 200 MB, nu neapărat 500 MB. Dacă pachetul este utilizat și într-un alt profil, codul poate rămâne instalat. Cifrele reale depind de sistem și de organizarea datelor."), listOf("Uninstallation removes an app for the relevant user and ordinarily its private local data. It is not identical to deleting an online account, all shared files or backups.", "Imagine returning a library catalog while documents stored in another building remain there. Apps may store data on the phone and external servers. Removal primarily affects local installation. Exported shared files may remain and online data is managed separately. Disabling a system app and archiving an app differ from uninstallation.", "A messaging app occupies 200 MB in code and private data, and the user exported photos to a shared album. Uninstallation may release private space while the album remains. The account and server-stored messages may still exist. Reinstallation may restore some data through synchronization or backup depending on the service.", "Package Manager manages package installation and removal in the context of Android users. Private app data is ordinarily removed during uninstallation, with exceptions depending on the mechanism and platform.", "Android can have multiple users or profiles, and removal for one does not automatically remove the app for others. Shared collection files can have independent lifecycles. Backup and synchronization may retain data elsewhere. Removing system-app updates may restore the system-image version. Uninstallation does not guarantee immediate physical erasure of every trace in flash memory.", "A hypothetical package occupies 80 MB of code, 120 MB of private data and has 300 MB of photos exported to shared storage. Ordinary removal may release about 200 MB, not necessarily 500 MB. If another profile uses the package, code may remain installed. Actual figures depend on the system and data organization.")),
    "Ecran tactil" to DetailedLesson(listOf("Ecranul tactil combină două funcții diferite: afișarea imaginilor și detectarea poziției degetelor. Panoul de afișare produce imaginea, iar stratul sensibil la atingere transmite sistemului informații despre contact. De aceea, imaginea poate rămâne vizibilă chiar dacă atingerea nu mai funcționează corect.", "Majoritatea telefoanelor folosesc atingerea capacitivă. O rețea de electrozi creează un câmp electric, iar apropierea degetului modifică măsurătorile locale. Controlerul estimează poziția contactului și transmite coordonatele către Android. Sistemul direcționează evenimentele către aplicația potrivită, care interpretează apăsarea, deplasarea sau gesturile cu mai multe degete. Apa, mănușile și anumite folii pot influența detectarea, în funcție de tehnologia dispozitivului. Rata de actualizare a imaginii și frecvența detectării atingerilor sunt proprietăți distincte.", "Imaginează-ți că deschizi o hartă și apropii două degete pentru a micșora imaginea. Controlerul urmărește două puncte de contact, iar aplicația calculează modificarea distanței dintre ele. Dacă deplasezi ambele degete în aceeași direcție, harta se poate deplasa în loc să își schimbe scara. Aceeași suprafață fizică permite astfel operații diferite, în funcție de interpretarea software. Este asemănător unui dirijor ale cărui gesturi sunt interpretate după reguli cunoscute.", "Un digitizor capacitiv proiectat utilizează electrozi și măsurători ale capacității proprii sau mutuale. Controlerul identifică variațiile spațiale și estimează coordonatele contactelor. Android reprezintă interacțiunile prin evenimente de intrare care includ poziții, identificatori și momente de timp.", "Controlerul filtrează zgomotul și poate aplica algoritmi de respingere a contactelor accidentale. Sistemul transformă coordonatele în funcție de orientare și distribuie evenimentele către ferestre. Aplicația interpretează succesiunea evenimentelor pentru a recunoaște gesturi. Latența totală include eșantionarea, procesarea, randarea și afișarea. Frecvența de actualizare a panoului nu este identică cu frecvența de eșantionare tactilă. O rată ridicată nu garantează singură o latență mică.", "Presupunem un digitizor de 120 Hz și un panou de 60 Hz. Intervalele nominale sunt aproximativ 8,3 ms și 16,7 ms. O atingere poate fi detectată între două actualizări ale imaginii, iar rezultatul devine vizibil după procesarea și randarea cadrului. Aceste intervale nu se adună automat într-o latență fixă, deoarece etapele pot fi suprapuse. Dacă aplicația întârzie randarea, răspunsul poate fi mai lent chiar cu un digitizor rapid."), listOf("A touchscreen combines two distinct functions: displaying images and detecting finger positions. The display panel produces the image while the touch-sensitive layer reports contact. An image may therefore remain visible even when touch detection malfunctions.", "Most phones use capacitive touch. A network of electrodes creates an electric field, and a nearby finger changes local measurements. The controller estimates contact positions and sends coordinates to Android. The system routes events to the appropriate app, which interprets taps, movement and multi-finger gestures. Water, gloves and some screen protectors may affect detection depending on device technology. Display refresh rate and touch sampling frequency are distinct properties.", "Imagine opening a map and bringing two fingers together to zoom out. The controller tracks two contacts and the app calculates their changing distance. Moving both fingers in the same direction may pan the map instead. The same physical surface supports different operations through software interpretation, much like a conductor's gestures interpreted according to familiar rules.", "A projected capacitive digitizer uses electrodes and self-capacitance or mutual-capacitance measurements. Its controller identifies spatial variations and estimates contact coordinates. Android represents interactions through input events containing positions, identifiers and timestamps.", "The controller filters noise and may reject accidental contacts. The system transforms coordinates according to orientation and distributes events to windows. Apps interpret event sequences to recognize gestures. Total latency includes sampling, processing, rendering and display. Panel refresh rate differs from touch sampling frequency. A high rate alone does not guarantee low latency.", "Assume a 120 Hz digitizer and a 60 Hz panel. Nominal intervals are about 8.3 ms and 16.7 ms. Touch may be detected between display updates, with results visible after processing and rendering. These intervals do not automatically add to a fixed latency because stages may overlap. Delayed app rendering can slow responses even with a fast digitizer.")),
    "Camera foto" to DetailedLesson(listOf("Camera telefonului transformă lumina în imagini digitale. Obiectivul direcționează lumina către un senzor, care o transformă în semnale electrice. Software-ul prelucrează aceste semnale pentru a obține fotografia. Calitatea nu depinde numai de numărul de megapixeli, ci și de lumină, obiectiv, senzor și procesare.", "În timpul fotografierii, camera reglează focalizarea și expunerea. Expunerea depinde de cantitatea de lumină captată, inclusiv durata captării. În lumină slabă, telefonul poate amplifica semnalul sau combina mai multe cadre. Amplificarea poate face zgomotul mai vizibil, iar expunerile mai lungi pot produce neclaritate dacă subiectul se mișcă. Culorile și contrastul sunt ajustate prin procesare. Zoomul digital decupează și poate interpola imaginea, spre deosebire de schimbarea optică a câmpului vizual.", "Imaginează-ți că fotografiezi aceeași persoană afară, ziua, și apoi într-o cameră slab iluminată. Afară, camera poate folosi o expunere scurtă și surprinde mișcarea clar. În interior, poate avea nevoie de mai mult timp pentru captare sau de amplificare. Dacă persoana își mișcă mâna, fotografia poate deveni neclară, chiar dacă rezoluția este aceeași. Telefonul poate combina cadre pentru a reduce zgomotul, dar mișcarea complică această operație. Megapixelii descriu numărul de puncte ale imaginii, nu cantitatea de lumină captată.", "Un senzor CMOS convertește fotonii în sarcină electrică prin fotodiode. Semnalul este citit și convertit în valori digitale. Multe camere utilizează filtre de culoare și reconstrucția componentelor cromatice prin demosaicizare. Procesorul de semnal de imagine, ISP, aplică transformări înainte de codificare.", "Zgomotul fotonic este legat de natura statistică a luminii, iar zgomotul de citire provine din electronica senzorului. Raportul semnal-zgomot depinde de numărul de fotoni și de alte surse de zgomot. Procesarea poate include reducerea zgomotului, balansul de alb, corecții optice și combinarea cadrelor HDR. Stabilizarea optică sau electronică poate compensa anumite mișcări ale camerei, dar nu elimină automat mișcarea subiectului. Rezoluția finală poate diferi de numărul fotositurilor, inclusiv prin gruparea pixelilor.", "Într-un model ideal dominat de zgomot fotonic, 100 de fotoni detectați au o fluctuație statistică de aproximativ 10, iar 400 au aproximativ 20. Raportul semnal-zgomot crește de la 10 la 20, nu de patru ori. Dacă durata expunerii crește de la 1/100 la 1/25 secundă, lumina captată poate crește de patru ori în condiții identice, dar și deplasarea unui subiect în timpul expunerii crește. Exemplul ilustrează compromisul dintre zgomot și neclaritate."), listOf("A phone camera converts light into digital images. The lens directs light to a sensor that converts it into electrical signals. Software processes these signals into a photograph. Quality depends not only on megapixels but also on light, optics, sensor and processing.", "During photography the camera adjusts focus and exposure. Exposure depends on captured light, including capture duration. In dim light the phone may amplify signals or combine frames. Amplification can make noise more visible, and longer exposures may blur moving subjects. Processing adjusts colors and contrast. Digital zoom crops and may interpolate an image, unlike an optical change in field of view.", "Imagine photographing the same person outdoors in daylight and then in a dim room. Outdoors the camera may use a short exposure and capture motion clearly. Indoors it may need longer capture or amplification. A moving hand may blur despite identical resolution. Combining frames can reduce noise but motion complicates the process. Megapixels describe image sample count, not captured light.", "A CMOS sensor converts photons into electrical charge through photodiodes. Signals are read and digitized. Many cameras use color filters and reconstruct color components through demosaicing. An image signal processor, ISP, applies transformations before encoding.", "Photon noise reflects the statistical nature of light, while read noise originates in sensor electronics. Signal-to-noise ratio depends on photon count and other noise sources. Processing may include noise reduction, white balance, optical corrections and HDR frame fusion. Optical or electronic stabilization can compensate for some camera movement but does not automatically eliminate subject motion. Output resolution may differ from photosite count, including through pixel binning.", "In an ideal photon-noise-dominated model, 100 detected photons have a statistical fluctuation of about 10, while 400 have about 20. Signal-to-noise ratio rises from 10 to 20, not fourfold. Increasing exposure from 1/100 to 1/25 second can capture four times as much light under identical conditions, but also increases subject displacement during exposure. This illustrates the noise-versus-blur tradeoff.")),
    "Microfon" to DetailedLesson(listOf("Microfonul transformă variațiile de presiune produse de sunet în semnale electrice. Telefonul transformă apoi semnalul în date digitale pentru apeluri, înregistrări sau filmări. Unele telefoane au mai multe microfoane, amplasate în zone diferite, pentru a capta sunetul în condiții variate.", "Sunetul este o vibrație care se propagă prin aer. O membrană sensibilă din microfon reacționează la aceste variații. Circuitele și software-ul pot ajusta nivelul semnalului, reduce anumite zgomote și limita ecoul. Rezultatul depinde de distanța față de sursă, orientare, vânt și acustica încăperii. Microfonul nu separă perfect vocea de toate celelalte sunete. Android controlează accesul aplicațiilor la captarea audio prin permisiuni și reguli care depind de versiunea sistemului.", "Imaginează-ți un apel într-o cameră liniștită și apoi pe o stradă aglomerată. În cameră, vocea este mai ușor de separat de fundal. Pe stradă, microfonul captează și mașini, vânt sau alte voci. Telefonul poate utiliza mai multe microfoane și algoritmi pentru a reduce zgomotul, dar unele sunete pot rămâne sau vocea poate părea modificată. Într-o filmare, aplicația poate utiliza alt profil audio decât în apel. Același microfon poate produce rezultate diferite fără să fie defect.", "Microfoanele MEMS folosesc structuri micromecanice sensibile la presiune, adesea cu detecție capacitivă. Semnalul analogic este convertit în eșantioane digitale. Frecvența de eșantionare, rezoluția și răspunsul în frecvență influențează reprezentarea sunetului.", "Conform teoremei de eșantionare, un semnal limitat în bandă poate fi reconstruit ideal dacă frecvența de eșantionare depășește de două ori frecvența maximă. Filtrele antialiasing limitează componentele nedorite. Procesarea poate include control automat al câștigului, anularea ecoului și reducerea zgomotului. Mai multe microfoane pot permite estimarea direcției sunetului prin diferențe de timp și fază. Compresia audio și profilul aplicației pot modifica semnalul transmis sau salvat.", "O înregistrare PCM mono la 48.000 eșantioane pe secundă și 16 biți pe eșantion produce 768.000 biți pe secundă, adică 96.000 octeți pe secundă. Un minut ocupă aproximativ 5,76 MB înainte de antet sau compresie. Un fișier comprimat poate ocupa mai puțin, fără ca microfonul să fi captat mai puține eșantioane inițial. Exemplul separă captarea, reprezentarea digitală și codificarea fișierului."), listOf("A microphone converts sound pressure variations into electrical signals. The phone converts these into digital data for calls, recordings and videos. Some phones have multiple microphones in different locations to capture sound under varying conditions.", "Sound is a vibration traveling through air. A sensitive microphone membrane responds to pressure changes. Circuits and software may adjust signal level, reduce some noise and limit echo. Results depend on source distance, orientation, wind and room acoustics. A microphone cannot perfectly separate speech from every other sound. Android controls app access to audio capture through permissions and rules that vary by system version.", "Imagine a call in a quiet room and then on a busy street. Speech is easier to distinguish from background sound indoors. Outside the microphone also captures traffic, wind and other voices. Multiple microphones and algorithms may reduce noise, but some sounds may remain or speech may sound altered. A video app may use a different audio profile from a call. The same microphone can produce different results without being faulty.", "MEMS microphones use micromechanical pressure-sensitive structures, often with capacitive detection. Analog signals are converted into digital samples. Sampling frequency, resolution and frequency response influence sound representation.", "Under the sampling theorem, a band-limited signal can ideally be reconstructed when sampling frequency exceeds twice its highest frequency. Antialiasing filters limit unwanted components. Processing may include automatic gain control, echo cancellation and noise reduction. Multiple microphones can enable direction estimation using timing and phase differences. Audio compression and app profiles can modify transmitted or stored signals.", "Mono PCM at 48,000 samples per second and 16 bits per sample produces 768,000 bits per second, or 96,000 bytes per second. One minute occupies about 5.76 MB before headers or compression. A compressed file may occupy less without the microphone initially capturing fewer samples. This separates capture, digital representation and file encoding.")),
    "Android" to DetailedLesson(listOf("Android este sistemul de operare care face legătura dintre componentele fizice ale telefonului și aplicațiile utilizate. El gestionează ecranul, memoria, conexiunile și accesul la date. Interfața poate arăta diferit în funcție de producător, chiar dacă telefoanele folosesc aceeași versiune Android.", "Când deschizi o aplicație, aceasta nu controlează direct toate componentele telefonului. Solicită servicii sistemului, care verifică drepturile de acces și distribuie resursele. Android gestionează și trecerea aplicațiilor în fundal, notificările și salvarea stării interfeței. Este asemenea administratorului unei clădiri care coordonează accesul la încăperi și utilități. Interfața producătorului, precum One UI, adaugă propriile funcții peste sistemul Android.", "Imaginează-ți că primești un apel în timp ce urmărești un film. Sistemul poate întrerupe sunetul filmului, afișa interfața apelului și direcționa vocea către microfon și difuzor. După apel, aplicația video poate relua redarea. Dacă între timp procesul a fost închis, aplicația poate restaura poziția salvată. Aceste operații implică mai multe servicii coordonate, nu doar aplicația de telefonie.", "Android este o platformă software construită peste nucleul Linux, cu servicii native, mediul de execuție ART și un cadru de programare pentru aplicații. Componentele comunică prin interfețe definite, inclusiv mecanismul Binder.", "Nucleul gestionează procesele, memoria și driverele. Straturile de abstractizare hardware oferă interfețe către componente specifice dispozitivului. Serviciile de sistem coordonează activitățile, ferestrele, permisiunile și energia. Aplicațiile rulează de regulă cu identități Linux distincte și acces limitat la datele altor aplicații. ART execută codul aplicațiilor prin mecanisme de interpretare și compilare. Serviciile Google nu sunt identice cu sistemul Android și nu există pe toate dispozitivele.", "Când o aplicație solicită o fotografie, poate folosi interfața camerei sau un selector de imagini. În primul caz, serviciul camerei verifică accesul și comunică prin straturile hardware; în al doilea, sistemul poate acorda acces doar la fișierul ales. Aplicația primește datele prin interfețe controlate. Exemplul arată diferența dintre accesul la o componentă și accesul la un document selectat."), listOf("Android is the operating system connecting phone hardware and applications. It manages the display, memory, connections and data access. Manufacturer interfaces can differ even on the same Android version.", "Apps request system services rather than controlling every component directly. Android checks access rights, allocates resources, manages background activity, notifications and interface state. Like a building manager coordinating rooms and utilities, it organizes shared resources. Manufacturer interfaces such as One UI add features on top of Android.", "Imagine receiving a call while watching a film. The system may pause audio, display the call interface and route voice through the microphone and speaker. Afterwards the video app may resume playback or restore its saved position if its process was ended. Multiple coordinated services are involved.", "Android is a software platform built on the Linux kernel, native services, ART runtime and an application framework. Components communicate through defined interfaces including Binder.", "The kernel manages processes, memory and drivers. Hardware abstraction layers expose device-specific components. System services coordinate activities, windows, permissions and power. Apps generally run under distinct Linux identities with restricted access to other apps' data. ART executes app code using interpretation and compilation. Google services are not identical to Android and are not present on every device.", "When an app requests a photo it may use a camera interface or an image picker. In the first case camera services check access and communicate with hardware layers; in the second the system may grant access only to the chosen file. The app receives data through controlled interfaces, illustrating component access versus access to a selected document.")),
    "Procesor" to DetailedLesson(listOf("Procesorul execută instrucțiunile necesare funcționării telefonului. El efectuează calcule, interpretează date și coordonează operații. Telefoanele includ de obicei mai multe nuclee și componente specializate pentru grafică, imagini și comunicații.", "Nucleele pot lucra simultan la sarcini diferite. Unele sunt optimizate pentru consum redus, altele pentru performanță. Frecvența se poate modifica în funcție de sarcină și temperatură. Un telefon rapid nu depinde numai de procesor: memoria, stocarea și software-ul influențează răspunsul. Este ca o echipă în care fiecare membru are un rol, iar rezultatul depinde și de accesul la materiale.", "Când deschizi o fotografie mare, stocarea furnizează fișierul, procesorul sau un accelerator îl decodează, memoria RAM păstrează datele temporare, iar componenta grafică ajută la afișare. Dacă apoi editezi fotografia, solicitarea crește. Într-o sesiune lungă, temperatura poate determina reducerea performanței. O întârziere nu dovedește singură că procesorul este defect.", "Procesorul central, CPU, execută instrucțiuni ale unei arhitecturi precum Arm. Un sistem pe cip, SoC, poate integra CPU, GPU, procesor de imagine, acceleratoare și modem. Performanța depinde de arhitectură, frecvență, paralelism și accesul la memorie.", "Nucleele folosesc cache-uri pentru reducerea latenței și pot executa mai multe operații în paralel. Planificatorul sistemului distribuie firele de execuție. Mecanismele DVFS ajustează tensiunea și frecvența în funcție de sarcină și limite termice. GPU este eficient pentru anumite calcule paralele, dar nu înlocuiește CPU în toate sarcinile. Frecvența în GHz nu măsoară direct numărul de instrucțiuni utile executate pe secundă.", "Într-un model simplificat, o sarcină durează 10 secunde, dintre care 6 pot fi paralelizate perfect și 4 sunt secvențiale. Cu două nuclee identice, durata ideală devine 4 + 6/2 = 7 secunde, nu 5. Cu patru nuclee devine 5,5 secunde. Costurile de sincronizare și acces la memorie pot reduce câștigul real. Exemplul explică de ce numărul nucleelor nu garantează o accelerare proporțională."), listOf("The processor executes instructions needed for phone operation. It calculates, interprets data and coordinates operations. Phones usually include multiple cores and specialized graphics, imaging and communication components.", "Cores can work on different tasks simultaneously. Some prioritize efficiency and others performance. Frequency can change with workload and temperature. Memory, storage and software also influence responsiveness. Like a team with specialized members, results depend on access to materials as well as workers.", "Opening a large photo involves storage supplying the file, a processor or accelerator decoding it, RAM retaining temporary data and graphics hardware helping display it. Editing increases demand. During a long session temperature may reduce performance. A delay alone does not establish a faulty processor.", "The CPU executes instructions of an architecture such as Arm. A system on chip, SoC, can integrate CPU, GPU, image processor, accelerators and modem. Performance depends on architecture, frequency, parallelism and memory access.", "Cores use caches to reduce latency and may execute operations in parallel. The scheduler distributes threads. DVFS adjusts voltage and frequency according to workload and thermal limits. GPUs are efficient for some parallel computations but do not replace CPUs for all tasks. GHz does not directly measure useful instructions per second.", "In a simplified model a task takes 10 seconds, with 6 perfectly parallelizable and 4 sequential. Two identical cores ideally take 4 + 6/2 = 7 seconds, not 5. Four take 5.5 seconds. Synchronization and memory costs may reduce actual gains. Core count does not guarantee proportional acceleration.")),
    "Senzori" to DetailedLesson(listOf("Senzorii sunt componente care măsoară proprietăți fizice precum mișcarea, lumina sau apropierea unui obiect. Telefonul transformă măsurătorile în informații utile pentru sistem și aplicații. Nu toate telefoanele au aceiași senzori.", "Accelerometrul măsoară accelerația specifică, inclusiv efectul gravitației asupra măsurătorii; giroscopul măsoară viteza de rotație. Senzorul de lumină poate ajuta la reglarea luminozității, iar cel de proximitate poate detecta apropierea telefonului de față. Sistemul poate combina măsurători pentru a estima orientarea. Senzorii nu înțeleg singuri contextul: software-ul interpretează valorile, care pot conține erori.", "În timpul unui apel, apropii telefonul de ureche. Pe un dispozitiv cu funcție de proximitate, sistemul poate stinge ecranul pentru a evita atingerile accidentale. Când îndepărtezi telefonul, ecranul poate reveni. Dacă rotești telefonul în timpul vizionării unui film, sistemul poate schimba orientarea imaginii. Aceste reacții provin din măsurători și reguli software, nu din recunoașterea intenției tale.", "Senzorii MEMS folosesc structuri micromecanice pentru măsurarea accelerației și rotației. Android expune senzori fizici și virtuali prin cadrul său de senzori. Disponibilitatea, rezoluția și frecvența de eșantionare depind de dispozitiv.", "Accelerometrul raportează valori pe trei axe, iar giroscopul viteze unghiulare. Integrarea vitezei unghiulare estimează rotația, dar erorile se pot acumula. Fuziunea senzorilor poate combina accelerometrul, giroscopul și magnetometrul pentru estimări mai stabile. Câmpurile magnetice locale pot perturba busola. Filtrarea reduce zgomotul, cu posibile întârzieri. Frecvența de eșantionare influențează rezoluția temporală și consumul.", "La 50 de măsurători pe secundă, intervalul nominal este 20 ms. O rotație constantă de 90 grade pe secundă timp de două secunde ar produce ideal 180 grade. O eroare constantă de 1 grad pe secundă ar adăuga 2 grade estimării. În practică, mișcarea și erorile variază, iar fuziunea senzorilor poate corecta parțial deriva."), listOf("Sensors measure physical properties such as motion, light and proximity. The phone converts measurements into useful information. Not all phones have the same sensors.", "An accelerometer measures specific acceleration, including gravity's effect on readings; a gyroscope measures rotation rate. A light sensor can help adjust brightness and proximity sensing can detect a phone near the face. Software combines measurements to estimate orientation. Sensors do not understand context themselves and readings can contain errors.", "During a call you bring the phone to your ear. A device with proximity sensing may turn off the display to avoid accidental touches, then restore it when moved away. Rotating the phone during a film may change display orientation. These reactions arise from measurements and software rules, not recognition of your intentions.", "MEMS sensors use micromechanical structures to measure acceleration and rotation. Android exposes physical and virtual sensors through its sensor framework. Availability, resolution and sampling frequency depend on hardware.", "Accelerometers report three-axis values and gyroscopes angular rates. Integrating angular rate estimates rotation but errors can accumulate. Sensor fusion may combine accelerometer, gyroscope and magnetometer for more stable estimates. Local magnetic fields can disturb compass readings. Filtering reduces noise with possible delay. Sampling frequency affects temporal resolution and power use.", "At 50 samples per second the nominal interval is 20 ms. A constant rotation of 90 degrees per second for two seconds ideally gives 180 degrees. A constant error of one degree per second adds two degrees. Actual motion and errors vary, and sensor fusion may partly correct drift.")),
    "Memoria RAM" to DetailedLesson(listOf("RAM este memoria de lucru a telefonului. Aici sunt păstrate temporar instrucțiunile și datele de care sistemul și aplicațiile au nevoie în timpul funcționării. Spre deosebire de spațiul de stocare, memoria RAM obișnuită pierde informațiile atunci când alimentarea este întreruptă. Capacitatea RAM influențează câte activități pot rămâne pregătite pentru utilizare, dar nu determină singură viteza telefonului.", "Imaginează-ți un birou și un dulap cu dosare. Biroul reprezintă RAM, iar dulapul reprezintă stocarea internă. Când lucrezi cu un document, îl aduci pe birou; când nu mai ai loc, unele dosare trebuie puse deoparte. Android gestionează automat această memorie. O aplicație pe care ai părăsit-o poate rămâne în RAM pentru a se redeschide mai repede. Memoria ocupată de asemenea aplicații nu înseamnă neapărat că telefonul are o problemă. Dacă sistemul are nevoie de spațiu, poate elibera memoria unor procese mai puțin importante. Datele nesalvate pot dispărea dacă un proces este închis, dar fișierele salvate rămân în stocare.", "Deschizi o hartă, apoi camera foto și o pagină web. Când revii la hartă, aceasta poate fi afișată imediat dacă procesul a rămas în RAM. Dacă între timp camera și browserul au necesitat multă memorie, Android poate închide procesul hărții. La revenire, aplicația se încarcă din nou și poate restaura poziția anterioară dacă a salvat-o. Nu înseamnă că harta a fost dezinstalată sau că fotografiile au fost șterse. Este diferența dintre memoria de lucru și informațiile păstrate permanent.", "RAM este memoria fizică volatilă utilizată pentru paginile de cod și date ale proceselor, structurile nucleului și anumite resurse grafice. Android, bazat pe Linux, oferă fiecărui proces un spațiu virtual de adrese, mapat la pagini fizice prin mecanismele hardware și software de administrare a memoriei.", "Unele pagini pot fi partajate între procese, de exemplu codul bibliotecilor, iar altele sunt private. Paginile asociate fișierelor pot fi eliminate și reîncărcate ulterior. Datele anonime pot fi comprimate în zRAM pe dispozitivele configurate astfel. Sub presiune de memorie, Android poate termina procese mai puțin importante prin mecanisme precum lmkd. Importanța unui proces depinde de relația sa cu activitatea utilizatorului și serviciile active. Indicatorii RSS și PSS descriu diferit memoria unui proces: PSS repartizează proporțional paginile partajate. Prin urmare, însumarea simplă a RSS poate număra aceeași memorie de mai multe ori.", "Presupunem două procese care folosesc fiecare 100 MiB de pagini private și partajează 60 MiB de cod. RSS poate indica 160 MiB pentru fiecare, deși totalul fizic al acestor pagini este 260 MiB, nu 320 MiB. PSS ar atribui fiecăruia 130 MiB în acest exemplu simplificat. Într-un telefon real există și memorie a nucleului, resurse grafice și alte procese. Exemplul explică de ce cifrele afișate de instrumente diferite nu coincid întotdeauna."), listOf("RAM is the phone's working memory. It temporarily holds instructions and data needed by the system and apps while they run. Unlike storage, ordinary RAM loses information when power is removed. RAM capacity affects how many activities can remain ready to use, but does not alone determine phone speed.", "Imagine a desk and a filing cabinet. The desk represents RAM and the cabinet represents internal storage. Documents are brought onto the desk when needed; when space runs out, some must be put away. Android manages this memory automatically. An app you leave may remain in RAM so it opens faster. Memory occupied by such apps does not necessarily indicate a problem. When space is needed, the system can reclaim memory from less important processes. Unsaved data may disappear when a process ends, while saved files remain in storage.", "You open a map, then the camera and a web page. Returning to the map may be immediate if its process remains in RAM. If the camera and browser required substantial memory, Android may have ended the map process. It then loads again and may restore its previous position if saved. The map has not been uninstalled and photos have not been deleted. This illustrates working memory versus persistent information.", "RAM is volatile physical memory used for process code and data pages, kernel structures and some graphics resources. Android, based on Linux, gives each process a virtual address space mapped to physical pages through hardware and software memory management.", "Some pages can be shared between processes, such as library code, while others are private. File-backed pages can be discarded and reloaded. Anonymous data can be compressed in zRAM on suitably configured devices. Under memory pressure Android may terminate less important processes through mechanisms such as lmkd. Process importance depends on user activity and active services. RSS and PSS describe process memory differently: PSS allocates shared pages proportionally. Simply adding RSS may count the same memory more than once.", "Assume two processes each use 100 MiB of private pages and share 60 MiB of code. RSS may report 160 MiB each, although these pages occupy 260 MiB physically, not 320 MiB. PSS would attribute 130 MiB to each in this simplified example. Real phones also have kernel memory, graphics resources and other processes. This explains why different tools may report different figures.")),
    "Memorie internă" to DetailedLesson(listOf("Memoria internă este spațiul în care telefonul păstrează sistemul Android, aplicațiile, fotografiile, videoclipurile și alte fișiere. Este nevolatilă: informațiile salvate rămân disponibile după oprirea telefonului. Capacitatea ei este exprimată de obicei în gigaocteți, GB, și nu trebuie confundată cu memoria RAM.", "O parte din capacitatea totală este ocupată de sistem și componentele preinstalate. Aplicațiile ocupă spațiu atât prin fișierele de instalare, cât și prin datele create ulterior. Un album foto sau un videoclip descărcat poate ocupa spațiu fără să fie deschis. Spre deosebire de RAM, stocarea nu trebuie alimentată continuu pentru a păstra datele. Totuși, nu este indestructibilă și nu înlocuiește o copie de rezervă. Spațiul liber afișat poate varia după actualizări, descărcări și activități ale aplicațiilor. Unitățile de măsură și modul de raportare pot explica unele diferențe între capacitatea de pe ambalaj și cea afișată.", "Un telefon de 128 GB are, într-un exemplu ipotetic, 24 GB ocupați de sistem, 30 GB de aplicații și datele lor, 40 GB de fotografii și videoclipuri și 10 GB de alte fișiere. Dacă aceste categorii nu se suprapun, rămân 24 GB. O filmare de 4 GB ar reduce spațiul liber la 20 GB. Repornirea telefonului nu eliberează automat acei 4 GB, deoarece filmarea este un fișier persistent. În schimb, memoria RAM folosită pentru realizarea filmării poate fi reutilizată după închiderea procesului.", "Stocarea internă modernă utilizează de regulă memorie flash NAND, accesată prin controlere și interfețe precum eMMC sau UFS. Celulele păstrează informația prin stări electrice distincte. Sistemul de fișiere organizează datele, iar controlerul gestionează maparea adreselor logice la locații fizice.", "Memoria NAND se programează și se șterge în unități diferite, iar controlerul utilizează mecanisme de distribuire a uzurii și colectare a blocurilor reutilizabile. Performanța depinde de accesul secvențial sau aleatoriu, dimensiunea operațiilor, gradul de ocupare și caracteristicile controlerului. Android separă datele private ale aplicațiilor de spațiile partajate, cu reguli care depind de versiunea sistemului. Criptarea bazată pe fișiere poate proteja datele pe dispozitivele compatibile. Capacitatea comercială este de obicei exprimată zecimal: 1 GB înseamnă un miliard de octeți, în timp ce 1 GiB înseamnă 1.073.741.824 octeți.", "128 GB zecimali reprezintă aproximativ 119,2 GiB înainte de a scădea spațiul rezervat sistemului. Dacă un instrument raportează GiB și altul GB, cifrele pot fi diferite fără să lipsească fizic memorie. Pentru un fișier de 2 GB, o viteză secvențială ipotetică de 100 MB/s ar da aproximativ 20 de secunde de transfer, ignorând alte întârzieri. Copierea multor fișiere mici cu același volum total poate dura diferit, deoarece implică operații suplimentare asupra metadatelor și acces aleatoriu."), listOf("Internal storage holds Android, apps, photos, videos and other files. It is nonvolatile: saved information remains after the phone is turned off. Its capacity is usually expressed in gigabytes, GB, and should not be confused with RAM.", "Part of total capacity is occupied by the system and preinstalled components. Apps use space for installation files and data created later. A photo album or downloaded video occupies space even when not open. Unlike RAM, storage does not require continuous power to retain data. It is not indestructible and does not replace a backup. Reported free space can vary after updates, downloads and app activities. Measurement units and reporting methods can explain differences between advertised and displayed capacity.", "In a hypothetical 128 GB phone, the system occupies 24 GB, apps and their data 30 GB, photos and videos 40 GB, and other files 10 GB. If categories do not overlap, 24 GB remains. Recording a 4 GB video reduces free space to 20 GB. Restarting does not automatically release those 4 GB because the video is persistent. RAM used while recording can instead be reused after the process ends.", "Modern internal storage generally uses NAND flash accessed through controllers and interfaces such as eMMC or UFS. Cells retain information through distinct electrical states. The file system organizes data and the controller maps logical addresses to physical locations.", "NAND is programmed and erased in different units; controllers use wear leveling and garbage collection. Performance depends on sequential or random access, operation size, occupancy and controller characteristics. Android separates private app data from shared spaces under rules that depend on system version. File-based encryption can protect data on compatible devices. Commercial capacity is usually decimal: 1 GB is one billion bytes, whereas 1 GiB is 1,073,741,824 bytes.", "128 decimal GB is approximately 119.2 GiB before subtracting system reservations. Tools reporting GiB and GB can therefore show different numbers without physical memory being missing. A 2 GB file transferred at a hypothetical sequential speed of 100 MB/s takes about 20 seconds, ignoring other delays. Copying many small files of the same total size may take a different time because of metadata operations and random access.")),
    "Memorie cache" to DetailedLesson(listOf("Memoria cache păstrează copii ale unor informații sau rezultate care ar putea fi necesare din nou. Scopul este reducerea timpului și a resurselor necesare pentru acces. Cache-ul nu este un singur loc din telefon: există cache în procesor, în memoria de lucru și în fișierele temporare ale aplicațiilor. Când Android afișează dimensiunea cache-ului unei aplicații, se referă în general la datele temporare de stocare ale acesteia.", "O aplicație poate salva local imagini, miniaturi sau rezultate deja calculate pentru a nu repeta aceeași operație la fiecare deschidere. Este asemănător păstrării unor obiecte folosite frecvent într-un sertar apropiat. Copiile pot deveni vechi, iar aplicația decide când trebuie actualizate. Cache-ul poate crește sau scădea pe parcursul utilizării. Eliminarea copiilor temporare poate determina reconstruirea lor la următoarea deschidere, cu timp sau trafic suplimentar. Datele permanente ale aplicației, precum preferințele și documentele, sunt o categorie diferită, deși comportamentul exact depinde de aplicație.", "Deschizi o galerie cu sute de fotografii. La prima afișare, aplicația poate genera miniaturi pentru fiecare fotografie. Dacă acestea sunt păstrate în cache, la următoarea vizită lista se poate afișa mai repede. Presupunem 200 de miniaturi de câte 50 KB: aproximativ 10 MB de copii temporare. Dacă ele dispar, fotografiile originale nu dispar din acest motiv; galeria poate genera miniaturile din nou. Într-o aplicație online, reconstruirea cache-ului poate necesita și o conexiune la internet. Exemplul arată de ce spațiul temporar poate avea un rol util.", "Un cache este un mecanism de reutilizare a datelor, organizat în jurul unor chei, reguli de validitate și politici de înlocuire. Un acces satisfăcut din cache este un hit; un acces care necesită sursa originală este un miss. Cache-urile pot exista în hardware, în memoria procesului, în sistemul de fișiere sau în stocarea privată a aplicației.", "Politicile de expirare, invalidare și eliminare limitează utilizarea datelor învechite și consumul de resurse. Cache-urile de procesor reduc latența accesului la memorie, în timp ce cache-urile de fișiere pot evita decodarea sau transferul repetat. Un cache HTTP poate utiliza informații de validare furnizate de server. Android poate recupera anumite fișiere din directoarele de cache când spațiul este insuficient. Aplicațiile nu ar trebui să trateze aceste directoare ca singura locație pentru date indispensabile. Cache-ul este un compromis între spațiu, latență, consum energetic și actualitatea informației.", "Într-un model ipotetic, 80 din 100 de cereri sunt satisfăcute local în 10 ms, iar 20 necesită 200 ms fiecare, incluzând costul verificării. Latența medie este 0,8 × 10 + 0,2 × 200 = 48 ms. Dacă toate ar necesita 200 ms, media ar fi 200 ms. Avantajul depinde de rata de hit și de costurile reale. Dacă datele se schimbă frecvent, invalidarea poate reduce rata de hit; păstrarea necontrolată a copiilor ar putea furniza informații depășite."), listOf("A cache retains copies of information or results that may be needed again. Its purpose is to reduce access time and resource use. A phone has multiple caches: processor caches, working-memory caches and temporary app files. When Android reports an app's cache size, it generally refers to its temporary storage data.", "An app may retain images, thumbnails or calculated results locally rather than repeating an operation every time it opens. This resembles keeping frequently used objects in a nearby drawer. Copies can become outdated, and the app determines when to refresh them. Cache size can grow or shrink during use. Removing temporary copies may require rebuilding them later, with additional time or network traffic. Permanent app data such as preferences and documents is a different category, although exact behavior depends on the app.", "You open a gallery with hundreds of photos. On the first visit it may generate thumbnails. If cached, the list may appear faster next time. Assume 200 thumbnails of 50 KB each, about 10 MB of temporary copies. If they disappear, the original photos do not disappear for that reason; the gallery can generate thumbnails again. In an online app, rebuilding a cache may also require internet access. This shows why temporary storage can be useful.", "A cache is a data-reuse mechanism organized around keys, validity rules and replacement policies. An access satisfied by the cache is a hit; one requiring the original source is a miss. Caches can exist in hardware, process memory, the file system or private app storage.", "Expiration, invalidation and eviction policies limit stale data and resource use. Processor caches reduce memory access latency, while file caches can avoid repeated decoding or transfer. An HTTP cache may use validation information supplied by the server. Android can reclaim some files in cache directories when storage is scarce. Apps should not treat these directories as the only location for indispensable data. Caching trades space against latency, energy consumption and freshness.", "In a hypothetical model, 80 of 100 requests are served locally in 10 ms, while 20 require 200 ms each including lookup overhead. Average latency is 0.8 × 10 + 0.2 × 200 = 48 ms. If all required 200 ms, the average would be 200 ms. Benefits depend on hit rate and actual costs. Frequently changing data may reduce hit rate through invalidation; retaining copies without validation could return outdated information.")),
    "Card microSD" to DetailedLesson(listOf("Un card microSD este un suport de stocare mic și detașabil, disponibil pentru telefoanele care au un slot compatibil. Poate păstra fotografii, videoclipuri, muzică și alte fișiere. Nu este memorie RAM și nu mărește automat capacitatea disponibilă pentru instalarea tuturor aplicațiilor. Compatibilitatea depinde de modelul telefonului și de software.", "Cardul păstrează informații în memorie flash, la fel ca alte dispozitive de stocare. Telefonul îl poate utiliza ca spațiu portabil sau, pe anumite dispozitive, ca stocare adoptată și integrată în sistem. Aceste moduri au proprietăți diferite. Un card portabil poate fi citit de alte dispozitive compatibile, în timp ce unul adoptat poate fi criptat și legat de telefonul care l-a configurat. Viteza cardului influențează cât durează citirea și scrierea fișierelor. Capacitatea mare nu garantează viteza mare. Cardurile pot avea erori sau se pot uza, iar existența unei singure copii pe card nu reprezintă o copie de rezervă.", "Un telefon compatibil are 10 GB liberi intern și un card cu 50 GB liberi. O aplicație foto care permite salvarea pe card poate înregistra acolo un videoclip de 3 GB. În acest caz, cardul rămâne cu aproximativ 47 GB, iar spațiul intern poate rămâne aproape neschimbat, exceptând datele auxiliare. O altă aplicație poate continua să folosească exclusiv stocarea internă. Astfel, cei 50 GB de pe card nu devin automat spațiu disponibil pentru orice operație. Dacă un fișier există numai pe card, scoaterea cardului îl face indisponibil telefonului până la reconectare.", "microSD este un format de card de memorie flash cu controler integrat. Familiile SDHC și SDXC definesc intervale de capacitate și cerințe de compatibilitate. Performanța depinde de magistrală, controler, tipul memoriei și profilul operațiilor. Clasele de viteză pentru video și clasele de performanță pentru aplicații descriu proprietăți diferite.", "Clasele video precum V30 indică o viteză minimă de scriere secvențială de 30 MB/s în condițiile standardului aplicabil. Clasele A1 și A2 includ cerințe pentru operații aleatorii, relevante pentru anumite sarcini de aplicație; performanța A2 depinde și de suportul gazdei. Sistemul de fișiere și modul de montare determină accesul. Android poate aplica reguli de acces la stocarea partajată, iar suportul pentru stocare adoptată este dependent de producător. Memoria flash are limite de anduranță, gestionate parțial de controler.", "Un flux video ipotetic de 80 Mb/s înseamnă 10 MB/s, deoarece un octet are opt biți. În zece minute, fluxul ar produce aproximativ 6 GB, fără a include variațiile și datele auxiliare. Un card V30 are o clasă de scriere secvențială peste această rată, dar rezultatul real depinde de telefon, compatibilitate, card și condițiile de utilizare. Această comparație nu prezice viteza la deschiderea multor fișiere mici, care implică alte caracteristici."), listOf("A microSD card is a small removable storage medium for phones with a compatible slot. It can hold photos, videos, music and other files. It is not RAM and does not automatically increase space available for installing every app. Compatibility depends on the phone and its software.", "The card retains information in flash memory. A phone may use it as portable storage or, on some devices, adopted storage integrated with the system. These modes differ. Portable cards can be read by other compatible devices, while adopted cards may be encrypted and tied to the configuring phone. Card speed affects reading and writing time. Large capacity does not guarantee high speed. Cards can develop errors or wear out, and a single copy on a card is not a backup.", "A compatible phone has 10 GB free internally and 50 GB free on a card. A camera app supporting card storage may record a 3 GB video there. The card then has about 47 GB free while internal space may remain nearly unchanged apart from auxiliary data. Another app may still use internal storage exclusively. The card's 50 GB is therefore not automatically available for every operation. A file existing only on the card becomes unavailable to the phone when the card is removed until it is reconnected.", "microSD is a flash memory card format with an integrated controller. SDHC and SDXC families define capacity ranges and compatibility requirements. Performance depends on the bus, controller, memory type and workload. Video speed classes and application performance classes describe different properties.", "Video classes such as V30 indicate minimum sequential write speed of 30 MB/s under applicable standard conditions. A1 and A2 include random-operation requirements relevant to some app workloads; A2 performance also depends on host support. The file system and mounting mode determine access. Android can enforce shared-storage access rules, and adopted-storage support depends on the manufacturer. Flash memory has endurance limits partly managed by its controller.", "A hypothetical 80 Mb/s video stream equals 10 MB/s because a byte contains eight bits. In ten minutes it would produce about 6 GB, excluding variations and auxiliary data. A V30 card has a sequential write class above this rate, but actual results depend on the phone, compatibility, card and conditions. This comparison does not predict performance when opening many small files, which involves other characteristics.")),
    "Memorie virtuală" to DetailedLesson(listOf("Memoria virtuală este un mecanism prin care sistemul de operare organizează memoria utilizată de programe. Nu este pur și simplu o cantitate suplimentară de RAM. Fiecare aplicație lucrează cu propriile adrese de memorie, iar sistemul stabilește unde se află datele corespunzătoare. Unele telefoane oferă și funcții comerciale numite extensie RAM sau RAM Plus, care pot folosi mecanisme diferite în funcție de model.", "Imaginează-ți o bibliotecă în care fiecare cititor are un catalog propriu. Numărul din catalog nu trebuie să fie identic cu poziția fizică a cărții. Sistemul gestionează această corespondență și separă datele aplicațiilor. Când RAM devine insuficientă, Android poate comprima anumite date în RAM, poate recupera pagini reutilizabile sau poate închide procese mai puțin importante. Unele configurații folosesc și stocarea ca spațiu de schimb. Stocarea este în general mai lentă decât RAM, iar compresia necesită procesare. O extensie de memorie nu transformă fizic un telefon de 4 GB RAM într-unul de 8 GB RAM.", "Presupunem un telefon cu 4 GB RAM și o opțiune de extensie afișată ca 4 GB. Deschizi un browser, o hartă și mai multe aplicații. Sistemul poate păstra unele date inactive comprimate sau într-un spațiu de schimb, în funcție de implementare. La revenirea într-o aplicație, datele pot necesita decomprimare ori citire din stocare. Această operație poate fi mai lentă decât accesul direct la RAM. Dacă presiunea rămâne ridicată, Android poate totuși închide aplicații. Cifra 4 + 4 nu garantează aceeași performanță ca 8 GB RAM fizică.", "Memoria virtuală oferă proceselor spații de adrese mapate la pagini fizice prin tabele de pagini și unitatea de management al memoriei, MMU. Protecțiile de acces izolează regiunile de memorie. Spațiul virtual rezervat nu este echivalent cu memoria fizică ocupată; paginile pot fi alocate la cerere.", "Un page fault poate declanșa alocarea unei pagini, încărcarea datelor dintr-un fișier sau recuperarea unei pagini din swap, în funcție de cauză. zRAM oferă un dispozitiv bloc comprimat în RAM, utilizat frecvent ca swap pe Android. Compresia poate reduce memoria fizică necesară unor date, cu cost de procesare și metadate. Datele deja comprimate pot avea beneficii reduse. Swap pe stocare, dacă este configurat, introduce latențe și operații de scriere suplimentare. Android combină aceste mecanisme cu recuperarea memoriei și terminarea proceselor sub presiune. Denumirile comerciale nu permit deducerea implementării exacte fără documentația dispozitivului.", "Presupunem 600 MiB de pagini anonime care se comprimă într-un raport de 2:1. Ele ar ocupa aproximativ 300 MiB de date comprimate, plus metadate, eliberând mai puțin de 300 MiB net. La acces, paginile trebuie decomprimate. Dacă aceleași date se comprimă slab, economia este mult mai mică. Exemplul arată că o capacitate logică de swap de 600 MiB nu reprezintă 600 MiB de RAM fizică suplimentară și că performanța depinde de tipul datelor și de frecvența accesului."), listOf("Virtual memory is a mechanism by which the operating system organizes memory used by programs. It is not simply additional RAM. Each app works with its own memory addresses, and the system determines where corresponding data resides. Some phones also offer commercial features called RAM extension or RAM Plus, which may use different mechanisms depending on the model.", "Imagine a library where each reader has a separate catalog. A catalog number need not equal a book's physical location. The system manages this mapping and separates app data. When RAM is scarce, Android may compress some data in RAM, reclaim reusable pages or terminate less important processes. Some configurations also use storage as swap. Storage is generally slower than RAM, and compression requires processing. Memory extension does not physically turn a 4 GB RAM phone into an 8 GB RAM phone.", "Assume a phone with 4 GB RAM and an extension option labeled 4 GB. You open a browser, a map and several apps. Depending on implementation, the system may retain inactive data compressed or in swap. Returning to an app may require decompression or reading from storage, which can be slower than direct RAM access. If pressure remains high, Android may still terminate apps. The figure 4 + 4 does not guarantee the same performance as 8 GB of physical RAM.", "Virtual memory provides processes with address spaces mapped to physical pages through page tables and the memory management unit, MMU. Access protections isolate memory regions. Reserved virtual address space is not equivalent to occupied physical memory; pages may be allocated on demand.", "A page fault may trigger allocation, loading data from a file or retrieving a page from swap, depending on its cause. zRAM provides a compressed RAM block device frequently used as swap on Android. Compression can reduce physical memory needed for some data, at a cost in processing and metadata. Already compressed data may benefit little. Storage-backed swap, if configured, introduces latency and additional writes. Android combines these mechanisms with memory reclamation and process termination under pressure. Commercial names alone do not establish the exact implementation without device documentation.", "Assume 600 MiB of anonymous pages compress at a 2:1 ratio. They would occupy about 300 MiB of compressed data plus metadata, releasing less than 300 MiB net. On access, pages must be decompressed. If the same data compresses poorly, savings are much smaller. This illustrates why 600 MiB of logical swap is not 600 MiB of extra physical RAM and why performance depends on data type and access frequency.")),
    "Bateria" to DetailedLesson(listOf("Bateria este componenta care permite telefonului să funcționeze fără a fi conectat permanent la o priză. Ea păstrează energie sub formă chimică și o transformă în energie electrică atunci când telefonul are nevoie de ea. Majoritatea telefoanelor moderne utilizează baterii reîncărcabile din familia litiu-ion.", "În timpul încărcării, energia furnizată de încărcător este folosită pentru a modifica starea chimică a materialelor din baterie. În timpul utilizării, procesul se desfășoară în sens invers, iar energia electrică alimentează ecranul, procesorul, memoria și celelalte componente. Telefonul conține circuite care monitorizează tensiunea, curentul și temperatura. Indicatorul procentual este o estimare a nivelului de încărcare, nu o măsurare directă a numărului de ore rămase. Autonomia depinde atât de energia disponibilă, cât și de ritmul consumului.\n\nChiar și când ecranul este stins, anumite componente pot continua să funcționeze: modemul poate comunica cu rețeaua, iar sistemul poate executa activități programate.", "Imaginează-ți două zile în care pleci de acasă cu telefonul încărcat la 100%. În prima zi citești câteva mesaje și lași telefonul în buzunar cea mai mare parte a timpului. În a doua zi folosești navigația GPS, filmezi și urmărești videoclipuri. La aceeași oră, nivelul bateriei poate fi mult mai mic în a doua zi, fără ca bateria să se fi deteriorat peste noapte. Diferența provine din consumul componentelor. Este asemănător unui rezervor care se golește mai repede atunci când debitul este mai mare.", "O celulă litiu-ion este un sistem electrochimic cu doi electrozi, electrolit și separator. Energia electrică rezultă din diferența de potențial dintre electrozi și reacțiile electrochimice reversibile. Capacitatea electrică se exprimă în Ah, iar energia în Wh.", "La descărcare, ionii de litiu se deplasează prin electrolit între electrozi, iar electronii circulă prin circuitul exterior. La încărcare, o sursă externă impune deplasarea în sens invers. Separatorul permite transportul ionic, limitând contactul electric direct dintre electrozi. Circuitele de protecție monitorizează limitele de funcționare, iar circuitele de management energetic convertesc tensiunea bateriei la valorile necesare componentelor.\n\nEstimarea stării de încărcare poate combina integrarea curentului în timp, tensiunea și modele ale celulei. Tensiunea depinde și de temperatură, sarcină și îmbătrânire, astfel încât nu există o corespondență universală simplă între tensiune și procent. Puterea instantanee este produsul tensiunii și curentului, iar energia consumată este integrala puterii în timp.", "Considerăm o baterie ipotetică de 5 Ah cu tensiune nominală de 3,85 V. Energia nominală este aproximativ 19,25 Wh. Dacă un telefon consumă în medie 2 W măsurați la baterie, modelul ideal oferă 9,6 ore. Dacă solicitarea crește la 5 W, estimarea devine 3,85 ore. În realitate, tensiunea variază, iar energia utilizabilă depinde de pragurile de protecție și de condițiile de funcționare. Exemplul arată de ce capacitatea nominală singură nu poate prezice autonomia și de ce măsurarea consumului energetic este importantă."), listOf("The battery allows a phone to operate without being permanently connected to a power outlet. It stores energy chemically and converts it into electrical energy when the phone needs it. Most modern phones use rechargeable lithium-ion batteries.", "During charging, energy supplied by the charger changes the chemical state of materials inside the battery. During use, the process reverses and electrical energy powers the display, processor, memory and other components. Circuits monitor voltage, current and temperature. The percentage indicator estimates charge level rather than directly measuring remaining hours. Runtime depends on both available energy and the rate of consumption.\n\nEven when the display is off, some components may continue working: the modem may communicate with the network and the system may execute scheduled activities.", "Imagine two days when you leave home with a fully charged phone. On the first day you read a few messages and keep the phone in your pocket most of the time. On the second day you use GPS navigation, record video and watch films. At the same time of day, the battery level may be much lower on the second day without the battery having deteriorated overnight. The difference comes from component consumption, like a reservoir emptying faster when the flow rate increases.", "A lithium-ion cell is an electrochemical system with two electrodes, an electrolyte and a separator. Electrical energy results from the potential difference between electrodes and reversible electrochemical reactions. Charge capacity is expressed in Ah and energy in Wh.", "During discharge, lithium ions travel through the electrolyte between electrodes while electrons flow through the external circuit. Charging reverses this movement using an external source. The separator permits ionic transport while limiting direct electrical contact between electrodes. Protection circuits monitor operating limits and power management circuits convert battery voltage to levels needed by components.\n\nState-of-charge estimation may combine current integration over time, voltage and cell models. Voltage also depends on temperature, load and aging, so there is no universal simple voltage-to-percentage relationship. Instantaneous power is voltage multiplied by current, and consumed energy is the integral of power over time.", "Consider a hypothetical 5 Ah battery with nominal voltage of 3.85 V. Nominal energy is about 19.25 Wh. If a phone draws an average of 2 W measured at the battery, an ideal model gives 9.6 hours. At 5 W the estimate becomes 3.85 hours. Actual voltage varies and usable energy depends on protection thresholds and operating conditions. This shows why nominal capacity alone cannot predict runtime and why energy consumption matters.")),
    "Cicluri de încărcare" to DetailedLesson(listOf("Un ciclu echivalent de încărcare și descărcare descrie utilizarea cumulată a unei cantități de energie sau sarcină corespunzătoare aproximativ unei capacități complete a bateriei. Nu este același lucru cu numărul de conectări la încărcător. Mai multe încărcări parțiale pot însuma un singur ciclu echivalent.", "Telefonul poate fi încărcat în mai multe reprize. Dacă folosești jumătate din capacitate, reîncarci bateria și apoi folosești încă jumătate, ai consumat în total aproximativ o capacitate completă. Procentele sunt estimări, iar metoda exactă de contabilizare depinde de producător.\n\nCiclurile sunt utile pentru descrierea utilizării acumulate, dar nu sunt un ceas care prezice exact durata de viață. Bateria îmbătrânește și când telefonul nu este folosit. Temperatura, vechimea și condițiile de încărcare influențează degradarea. Două baterii cu același număr de cicluri pot avea capacități actuale diferite.", "Imaginează-ți că luni folosești telefonul de la 100% la 60%, apoi îl încarci. Marți consumi 35%, iar miercuri încă 25%. Ai conectat telefonul de trei ori la încărcător, dar consumul cumulat este 40 + 35 + 25 = 100%: aproximativ un ciclu echivalent. Dacă în alte trei zile consumi doar câte 10%, vei acumula aproximativ 0,3 cicluri. Este ca un contor care adună cantitățile utilizate, nu numărul vizitelor la priză. Acest exemplu simplifică estimările reale ale telefonului.", "Un ciclu complet echivalent (EFC) normalizează utilizarea cumulată la o capacitate de referință. O convenție posibilă folosește sarcina totală descărcată împărțită la capacitatea de referință. Alte convenții folosesc energia sau contabilizează atât încărcarea, cât și descărcarea cu normalizarea corespunzătoare.", "Prin integrarea curentului de descărcare în timp se poate estima sarcina transferată. Raportarea la capacitatea nominală sau la o altă referință produce un indicator comparabil în cadrul aceleiași metode. Adâncimea descărcării, rata de încărcare, temperatura și starea de încărcare influențează mecanismele electrochimice de uzură. De aceea, același EFC nu implică automat aceeași degradare.\n\nÎmbătrânirea calendaristică și cea ciclică se suprapun. Indicatorii de sănătate pot include atât capacitatea rămasă, cât și rezistența internă. Numărul de cicluri nu permite singur calcularea acestor valori.", "Pentru o capacitate de referință de 5 Ah, presupunem descărcări succesive de 1,5 Ah, 2 Ah și 1,5 Ah. Sarcina cumulată este 5 Ah, deci EFC = 5 / 5 = 1. Dacă două celule acumulează fiecare 100 EFC, una la temperaturi moderate și alta în condiții termice mai solicitante, capacitățile lor măsurate pot fi diferite. Numărul de cicluri descrie cantitatea utilizată, nu toate condițiile electrochimice ale utilizării."), listOf("An equivalent charge-discharge cycle describes cumulative use of an amount of charge or energy approximately equal to one full battery capacity. It is not the number of times a charger is connected. Several partial charges can add up to one equivalent cycle.", "A phone can be charged in several sessions. If you use half its capacity, recharge it and then use another half, you have consumed approximately one full capacity in total. Percentages are estimates and exact accounting depends on the manufacturer.\n\nCycles describe accumulated use but do not precisely predict lifetime. Batteries age even when unused. Temperature, age and charging conditions influence degradation. Two batteries with the same cycle count can have different current capacities.", "Imagine using your phone from 100% to 60% on Monday, then charging it. On Tuesday you consume 35% and on Wednesday another 25%. You connected the charger three times, but cumulative consumption is 40 + 35 + 25 = 100%, approximately one equivalent cycle. If you consume only 10% on each of three other days, you accumulate about 0.3 cycles. It is like a meter adding amounts used rather than counting visits to an outlet. This example simplifies actual phone estimates.", "An equivalent full cycle (EFC) normalizes cumulative use to a reference capacity. One convention divides total discharged charge by reference capacity. Other conventions use energy or count both charge and discharge with appropriate normalization.", "Integrating discharge current over time estimates transferred charge. Dividing by nominal capacity or another reference produces a metric comparable within the same method. Depth of discharge, charging rate, temperature and state of charge influence electrochemical aging mechanisms. Equal EFC therefore does not automatically imply equal degradation.\n\nCalendar and cycle aging overlap. Health indicators may include remaining capacity and internal resistance. Cycle count alone cannot calculate these values.", "For a reference capacity of 5 Ah, assume successive discharges of 1.5 Ah, 2 Ah and 1.5 Ah. Cumulative charge is 5 Ah, so EFC = 5 / 5 = 1. If two cells each accumulate 100 EFC, one at moderate temperatures and the other under more demanding thermal conditions, their measured capacities may differ. Cycle count describes quantity used, not all electrochemical conditions of use.")),
    "Încărcare rapidă" to DetailedLesson(listOf("Încărcarea rapidă este o tehnologie care permite unui telefon compatibil să primească energie la o putere mai mare decât în anumite moduri obișnuite de încărcare. Puterea este exprimată în wați. Valoarea înscrisă pe încărcător reprezintă o capacitate maximă în anumite condiții, nu puterea primită permanent de baterie.", "Telefonul, încărcătorul și uneori cablul comunică pentru a stabili un mod de alimentare compatibil. Circuitele telefonului transformă energia primită și controlează încărcarea bateriei. Pe măsură ce bateria se umple, puterea poate scădea. Temperatura și utilizarea telefonului în timpul încărcării influențează și ele viteza.\n\nUn încărcător de putere mai mare nu obligă automat un telefon compatibil cu standardele respective să accepte întreaga putere. Telefonul are propriile limite. În plus, o parte din energia primită poate alimenta componentele active, iar o parte se pierde prin conversie sub formă de căldură.", "Imaginează-ți că două telefoane identice pornesc de la 20%. Unul folosește un încărcător compatibil de putere mai mare, iar celălalt un mod de încărcare mai lent. Primul poate ajunge mai repede la 60%, dar diferența de viteză se poate reduce aproape de 100%. Este asemănător umplerii unui recipient printr-un robinet al cărui debit este redus treptat aproape de final. Dacă telefonul rulează și un joc, o parte din energie alimentează jocul, iar temperatura poate modifica viteza. Puterea maximă nu permite singură calcularea timpului total.", "Încărcarea rapidă mărește puterea de intrare disponibilă prin combinații de tensiune și curent compatibile cu dispozitivul. Protocoale precum USB Power Delivery permit negocierea parametrilor sursei, iar PPS permite ajustări în limitele convenite pe echipamente compatibile.", "Puterea electrică este P = U × I. Puterea la portul USB diferă de puterea absorbită de celulă din cauza conversiei, pierderilor și consumului sistemului. Controlul încărcării celulelor litiu-ion include frecvent o etapă cu curent controlat urmată de o etapă cu tensiune controlată, în care curentul scade. Telefoanele moderne pot utiliza profiluri mai complexe și limitări termice.\n\nPuterea maximă negociată nu este o medie pe întreaga sesiune. Temperatura, impedanța celulei, starea de încărcare și limitele componentelor determină profilul real. Încărcarea rapidă trebuie înțeleasă ca un sistem de control dinamic, nu ca un transfer constant la valoarea de pe ambalaj.", "Presupunem că portul primește 9 V și 2 A, adică 18 W. Dacă randamentul conversiei este ipotetic 90%, rămân 16,2 W după conversie. Dacă sistemul consumă 2 W din această putere, bateria primește aproximativ 14,2 W. Pentru a adăuga 10 Wh la putere constantă, modelul ideal estimează aproximativ 42 de minute. Dacă puterea scade în apropierea limitei de tensiune, durata reală crește. Acest calcul ilustrează diferența dintre puterea la port și energia stocată, nu performanța garantată a unui anumit telefon."), listOf("Fast charging allows a compatible phone to receive energy at higher power than certain ordinary charging modes. Power is expressed in watts. The number printed on a charger represents a maximum capability under specified conditions, not the power constantly received by the battery.", "The phone, charger and sometimes cable communicate to establish a compatible power mode. Phone circuits convert incoming energy and control battery charging. Power may decrease as the battery fills. Temperature and phone use during charging also influence speed.\n\nA higher-power charger does not automatically force a phone compatible with the relevant standards to accept its full power. The phone has its own limits. Some incoming energy may power active components, and some is lost as heat during conversion.", "Imagine two identical phones starting at 20%. One uses a compatible higher-power charger and the other a slower charging mode. The first may reach 60% sooner, but the speed difference may shrink near 100%. It resembles filling a container with a tap whose flow gradually decreases near the end. If the phone is also running a game, some energy powers the game and temperature may change charging speed. Maximum power alone cannot determine total charging time.", "Fast charging increases available input power through voltage and current combinations compatible with the device. Protocols such as USB Power Delivery negotiate source parameters, while PPS allows adjustments within agreed limits on compatible equipment.", "Electrical power is P = U × I. USB port power differs from cell input power because of conversion, losses and system consumption. Lithium-ion charging control frequently includes a controlled-current stage followed by a controlled-voltage stage in which current decreases. Modern phones may use more complex profiles and thermal limits.\n\nMaximum negotiated power is not the average over an entire session. Temperature, cell impedance, state of charge and component limits determine the actual profile. Fast charging is a dynamic control system rather than a constant transfer at the value printed on packaging.", "Assume the port receives 9 V and 2 A, or 18 W. With hypothetical conversion efficiency of 90%, 16.2 W remains after conversion. If the system consumes 2 W of that power, the battery receives approximately 14.2 W. Adding 10 Wh at constant power would ideally take about 42 minutes. If power falls near the voltage limit, actual time increases. This calculation illustrates the difference between port power and stored energy, not guaranteed performance of a particular phone.")),
    "Capacitate (mAh)" to DetailedLesson(listOf("Capacitatea bateriei exprimată în miliamperi-oră descrie cantitatea de sarcină electrică pe care bateria o poate furniza în anumite condiții de măsurare. Nu reprezintă direct numărul de ore în care telefonul va funcționa. Două telefoane cu aceeași capacitate pot avea autonomii diferite.", "Imaginează-ți bateria ca pe un rezervor. Capacitatea descrie rezerva, iar consumul componentelor determină cât de repede se golește. Ecranul, procesorul și modemul consumă diferit în funcție de activitate. Luminozitatea, semnalul mobil și temperatura pot schimba consumul chiar dacă folosești aceeași aplicație. Capacitatea nominală nu este identică întotdeauna cu cea disponibilă: îmbătrânirea și condițiile de măsurare influențează rezultatul.", "Două telefoane au baterii de 5.000 mAh. Primul consumă în medie 500 mA într-un scenariu ipotetic, iar al doilea 1.000 mA. Într-un model simplificat, primul funcționează aproximativ zece ore, iar al doilea cinci ore. În realitate consumul variază continuu: vizionarea unui film, un apel și repausul nu solicită aceleași componente. Identitatea capacității nu garantează identitatea autonomiei.", "Capacitatea electrică Q reprezintă integrala curentului în timp. Un amper-oră corespunde la 3.600 coulombi. Energia, exprimată în Wh, depinde și de tensiunea bateriei.", "Pentru a estima energia nominală se înmulțește capacitatea în Ah cu tensiunea nominală. Energia efectiv livrată depinde de variația tensiunii în timpul descărcării, temperatură, curent și pragurile circuitului de protecție. Consumul telefonului este mai bine descris energetic atunci când se compară dispozitive cu tensiuni diferite. Circuitele de conversie introduc și pierderi.", "O baterie de 5 Ah la 3,85 V are o energie nominală de aproximativ 19,25 Wh. Dacă telefonul solicită în medie 2 W de la baterie, modelul ideal estimează 9,6 ore. La 4 W, estimarea scade la 4,8 ore. Aceste valori nu sunt măsurători reale: tensiunea, randamentul și energia utilizabilă pot modifica rezultatul."), listOf("Battery capacity in milliamp-hours describes the electric charge a battery can deliver under specified measurement conditions. It does not directly represent hours of phone use. Two phones with equal capacity can have different runtimes.", "Imagine the battery as a reservoir. Capacity describes the reserve, while component consumption determines how quickly it empties. The display, processor and modem consume different amounts depending on activity. Brightness, mobile signal and temperature can change consumption even within one app. Nominal capacity is not always identical to available capacity: aging and measurement conditions affect it.", "Two phones have 5,000 mAh batteries. In a hypothetical scenario the first draws an average of 500 mA and the second 1,000 mA. A simplified model gives about ten hours and five hours respectively. Actual consumption varies continuously: watching a film, calling and standby demand different components. Equal capacity does not guarantee equal runtime.", "Electric charge capacity Q is the integral of current over time. One ampere-hour equals 3,600 coulombs. Energy in Wh also depends on battery voltage.", "Nominal energy can be estimated by multiplying capacity in Ah by nominal voltage. Delivered energy depends on voltage variation during discharge, temperature, current and protection thresholds. Energy is a better basis for comparing devices with different battery voltages. Conversion circuits also introduce losses.", "A 5 Ah battery at 3.85 V has nominal energy of about 19.25 Wh. If the phone draws an average of 2 W from the battery, the ideal estimate is 9.6 hours. At 4 W it falls to 4.8 hours. These are not actual measurements: voltage, efficiency and usable energy can change the result.")),
    "Degradarea bateriei" to DetailedLesson(listOf("Degradarea bateriei este procesul natural prin care bateria pierde treptat capacitatea de a stoca energie și de a furniza putere. Nu înseamnă neapărat că telefonul este defect. Chiar și o baterie utilizată normal îmbătrânește în timp.", "În interiorul bateriei au loc reacții chimice la fiecare încărcare și descărcare. O parte dintre aceste reacții modifică lent materialele interne. Temperatura, timpul și ciclurile de încărcare influențează ritmul schimbărilor.\n\nNivelul de încărcare și sănătatea bateriei sunt lucruri diferite. Nivelul arată cât din capacitatea actuală este disponibil, iar sănătatea poate descrie capacitatea actuală comparată cu cea inițială. De aceea, o baterie veche poate afișa 100% fără să mai stocheze la fel de mult ca atunci când era nouă.", "Imaginează-ți un telefon cu o baterie nouă de 5.000 mAh care oferă zece ore de utilizare într-un anumit scenariu. După câțiva ani, presupunem că mai păstrează 4.000 mAh, adică 80% din capacitatea inițială. Ambele baterii pot afișa 100%, dar cea veche are o rezervă mai mică. Dacă toate celelalte condiții rămân identice, autonomia ar putea fi aproximativ opt ore. Este ca diferența dintre două recipiente pline, unul de cinci litri și altul de patru litri. Valorile sunt ipotetice, nu o măsurătoare a telefonului.", "Degradarea litiu-ion reprezintă modificarea progresivă a proprietăților electrochimice, cu reducerea capacității disponibile și adesea creșterea rezistenței interne.", "Ionii de litiu se deplasează între electrozi. Reacțiile secundare pot consuma litiu disponibil. Stratul SEI, o peliculă de la suprafața electrodului negativ, este necesar funcționării, însă creșterea sa poate contribui la pierderea capacității. Îmbătrânirea calendaristică apare odată cu timpul, iar cea ciclică este asociată încărcărilor și descărcărilor. Rezistența internă mai mare poate provoca încălzire și căderi de tensiune sub sarcină.", "Pentru o capacitate inițială de 5.000 mAh și una măsurată de 4.000 mAh, indicatorul sănătății bazat pe capacitate este 4.000 / 5.000 × 100 = 80%. În timpul filmării, curentul solicitat poate fi mai mare decât în repaus. Rezistența internă crescută poate determina o cădere de tensiune mai mare și, în anumite condiții, oprirea telefonului înainte ca indicatorul să ajungă la zero. Capacitatea și puterea disponibilă sunt deci aspecte diferite ale degradării."), listOf("Battery aging is the natural process by which a battery gradually loses its ability to store energy and deliver power. It does not necessarily mean the phone is faulty. Even a normally used battery ages over time.", "Chemical reactions occur inside the battery whenever it charges or discharges. Some reactions slowly alter its internal materials. Temperature, time and charge cycles influence the rate.\n\nCharge level and battery health are different. Charge level describes the fraction of current capacity available, whereas health can describe current capacity relative to original capacity. An older battery can therefore show 100% without storing as much as it did when new.", "Imagine a new 5,000 mAh phone battery providing ten hours of use in a particular scenario. Years later, suppose it retains 4,000 mAh, or 80% of its original capacity. Both batteries can display 100%, but the older one has a smaller reserve. With all other conditions identical, runtime might be about eight hours. It is like two full containers holding five and four litres respectively. These are hypothetical figures, not a measurement of your phone.", "Lithium-ion aging is a progressive change in electrochemical properties, involving reduced available capacity and often increased internal resistance.", "Lithium ions move between electrodes. Side reactions can consume available lithium. The SEI layer, a film on the negative electrode, is necessary for operation, but its growth can contribute to capacity loss. Calendar aging occurs with time; cycle aging is associated with charging and discharging. Increased internal resistance can cause heating and voltage drops under load.", "For an original capacity of 5,000 mAh and a measured capacity of 4,000 mAh, capacity-based health is 4,000 / 5,000 × 100 = 80%. Recording video can demand more current than idle operation. Increased internal resistance can cause a larger voltage drop and, under some conditions, shutdown before the indicator reaches zero. Capacity and available power are therefore distinct aspects of aging."))
)

@Composable
private fun EducationalLibrary(english: Boolean, category: Int, topic: Int, onCategory: (Int) -> Unit, onTopic: (Int) -> Unit) {
    var query by rememberSaveable { mutableStateOf("") }
    var advanced by rememberSaveable(category, topic) { mutableStateOf(false) }
    val entry = educationEntries.getOrNull(category)
    fun words(lesson: LibraryLesson) = if (english) lesson.en else lesson.ro
    fun matches(value: String): Boolean {
        fun normalize(text: String) = Normalizer.normalize(text, Normalizer.Form.NFD).replace(Regex("\\p{M}+"), "").lowercase(Locale.ROOT)
        return normalize(value).contains(normalize(query.trim()))
    }
    val lessons = if (category == 11) libraryLessons.sortedBy { words(it)[0].lowercase(Locale.ROOT) } else libraryLessons.filter { it.category == category }
    val lesson = lessons.getOrNull(topic)
    if (entry == null) {
        Text(if (english) "Learn about your phone" else "Învață despre telefon", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = navy)
        Text(if (english) "Educational library" else "Bibliotecă educativă")
        OutlinedTextField(value = query, onValueChange = { query = it }, label = { Text(if (english) "Search topics" else "Caută un subiect") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
        if (query.isBlank()) {
            educationEntries.forEachIndexed { index, item ->
                val title = if (english) item.en[0] else item.ro[0]
                EducationTile(item.icon, title, "") { onCategory(index) }
            }
        } else {
            val results = libraryLessons.filter { words(it).any(::matches) || matches(if (english) educationEntries[it.category].en[0] else educationEntries[it.category].ro[0]) }
            if (results.isEmpty()) Text(if (english) "No results" else "Niciun rezultat")
            results.forEach { result ->
                val index = libraryLessons.filter { it.category == result.category }.indexOf(result)
                val target = if (result.category == 11) libraryLessons.sortedBy { words(it)[0].lowercase(Locale.ROOT) }.indexOf(result) else index
                EducationTile(educationEntries[result.category].icon, words(result)[0], if (english) educationEntries[result.category].en[0] else educationEntries[result.category].ro[0]) { onCategory(result.category); onTopic(target) }
            }
        }
    } else {
        TextButton(onClick = { if (topic >= 0) onTopic(-1) else onCategory(-1) }) {
            Icon(Icons.Filled.ArrowBack, contentDescription = null)
            Text(if (english) "Back" else "Înapoi")
        }
        Text(if (english) entry.en[0] else entry.ro[0], style = MaterialTheme.typography.titleLarge, color = navy)
        if (lesson == null) {
            lessons.forEachIndexed { index, item -> EducationTile(educationEntries[item.category].icon, words(item)[0], "") { onTopic(index) } }
        } else {
            val content = words(lesson)
            Icon(educationEntries[lesson.category].icon, contentDescription = null, tint = green, modifier = Modifier.size(48.dp))
            Text(content[0], style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold, color = navy)
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(selected = !advanced, onClick = { advanced = false }, label = { Text(if (english) "In simple terms" else "Pe înțelesul tuturor") }, modifier = Modifier.weight(1f))
                FilterChip(selected = advanced, onClick = { advanced = true }, label = { Text(if (english) "Learn more" else "Află mai multe") }, modifier = Modifier.weight(1f))
            }
            Card(colors = CardDefaults.cardColors(containerColor = paleGreen), modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (english) "Definition" else "Definiție", fontWeight = FontWeight.Bold, color = navy)
                    val detail = detailedLessons[lesson.ro[0]]
                    val sections = if (english) detail?.en else detail?.ro
                    val offset = if (advanced) 3 else 0
                    if (sections != null) {
                        Text(sections[offset])
                        Text(if (english) "How it works" else "Cum funcționează?", fontWeight = FontWeight.Bold, color = navy)
                        Text(sections[offset + 1])
                        Text(if (english) "Practical example" else "Exemplu practic", fontWeight = FontWeight.Bold, color = navy)
                        Text(sections[offset + 2])
                    } else {
                        Text(content[1])
                        if (advanced) {
                            Text(if (english) "How it works" else "Cum funcționează?", fontWeight = FontWeight.Bold, color = navy)
                            Text(content[2])
                        }
                        Text(if (english) "Practical example" else "Exemplu practic", fontWeight = FontWeight.Bold, color = navy)
                        Text(content[3])
                    }
                }
            }
            Text(if (english) "Related topics" else "Subiecte conexe", fontWeight = FontWeight.Bold, color = navy)
            lessons.withIndex().filter { it.index != topic && it.value.category == lesson.category }.take(4).forEach { related ->
                EducationTile(educationEntries[related.value.category].icon, words(related.value)[0], "") { onTopic(related.index) }
            }
        }
    }
}

@Composable
private fun EducationTile(icon: ImageVector, title: String, subtitle: String, onClick: () -> Unit) {
    Surface(onClick = onClick, modifier = Modifier.fillMaxWidth().heightIn(min = 78.dp), shape = RoundedCornerShape(16.dp), color = paleGreen, border = BorderStroke(1.dp, Color(0xFFC5E9B6))) {
        Row(Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(Modifier.size(48.dp).background(green, CircleShape), contentAlignment = Alignment.Center) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
            }
            Column(Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.Bold, color = navy)
                if (subtitle.isNotEmpty()) Text(subtitle, style = MaterialTheme.typography.bodySmall)
            }
            Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = navy)
        }
    }
}
