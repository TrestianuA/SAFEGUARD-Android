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
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
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

private val green = Color(0xFF139B22)
private val navy = Color(0xFF082B63)
private val paleBlue = Color(0xFFF2F9FF)
private val paleGreen = Color(0xFFE8F9E8)

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
                NavigationBar(
                    containerColor = Color.White,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    listOf("Acasă", "Protecție", "Rapoarte").forEachIndexed { index, title ->
                        NavigationBarItem(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = navy,
                                indicatorColor = green,
                                unselectedIconColor = navy,
                                unselectedTextColor = navy
                            ),
                            icon = { Text(listOf("⌂", "🛡", "▥")[index]) },
                            label = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) },
                            alwaysShowLabel = true
                        )
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier.fillMaxSize().padding(innerPadding).verticalScroll(rememberScrollState())
                    .padding(horizontal = 12.dp, vertical = 10.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (selectedTab == 0) SafeguardHeader(onHelp = { showHelp = true }, onLanguage = { helpEnglish = !helpEnglish }, helpEnglish = helpEnglish)
                if (selectedTab == 0) {
                DashboardOverview(report != null, scanning)
                Text("Acces rapid", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = navy)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DashboardTile("🛡️", "Scanare\nsecuritate", "Locală", Modifier.weight(1f)) { selectedTab = 1 }
                    DashboardTile("🔔", "Inspector\nnotificări", if (notificationAccessEnabled) "Activ" else "Opțional", Modifier.weight(1f)) { selectedTab = 2 }
                    DashboardTile("📱", "Gestionare\naplicații", "Setări Android", Modifier.weight(1f)) {
                        launchSettings(context, Intent(Settings.ACTION_APPLICATION_SETTINGS))
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    DashboardTile("🧹", "Curățare\nmanuală", "Cache Advisor", Modifier.weight(1f)) {
                        launchSettings(context, Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS))
                    }
                    DashboardTile("🔋", "Optimizare\nbaterie", "Sfaturi", Modifier.weight(1f)) {
                        launchSettings(context, Intent("android.intent.action.POWER_USAGE_SUMMARY"))
                    }
                    DashboardTile("📊", "Rapoarte\nși activitate", "Date locale", Modifier.weight(1f)) { selectedTab = 2 }
                }
                Text("Starea dispozitivului", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = navy)
                DeviceHealthCards(context, helpEnglish)
                }
                if (selectedTab == 1) {
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
                if (selectedTab == 2) {
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
                Text("SAFEGUARD 0.7.3-rc1 • analiză locală • fără cont și fără reclame", style = MaterialTheme.typography.labelSmall)
            }
        }
    }

    if (showHelp) {
        AlertDialog(
            onDismissRequest = { showHelp = false },
            title = { Text(if (helpEnglish) "About SAFEGUARD" else "Despre SAFEGUARD") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(if (helpEnglish) "SAFEGUARD performs local checks of visible apps, sensitive permissions and selected Android special accesses. It is not an antivirus and does not provide real-time malware protection." else "SAFEGUARD verifică local aplicațiile vizibile, permisiunile sensibile și anumite accesări speciale Android. Nu este antivirus și nu oferă protecție antivirus în timp real.")
                    Text(if (helpEnglish) "The notification inspector stores only the source package and time, locally, when you grant access. Battery and storage shortcuts open Android settings. No automatic cleaning or app blocking is provided." else "Inspectorul notificărilor păstrează local doar pachetul-sursă și ora, dacă acorzi acces. Scurtăturile pentru baterie și stocare deschid setările Android. Nu există curățare automată sau blocare a aplicațiilor.")
                }
            },
            confirmButton = { TextButton(onClick = { showHelp = false }) { Text(if (helpEnglish) "Close" else "Închide") } }
        )
    }

    if (showConsent) {
        AlertDialog(
            onDismissRequest = { showConsent = false },
            title = { Text("Acord pentru scanarea locală") },
            text = { Text("SAFEGUARD va citi local lista aplicațiilor vizibile, permisiunile acordate și unele setări de acces special. Inventarul aplicațiilor este sensibil. În această versiune nu este transmis sau salvat. Continui?") },
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
            text = { Text("Android avertizează că serviciile de notificări pot vedea conținutul notificărilor. SAFEGUARD folosește doar pachetul-sursă și ora, păstrate local, maximum 100 de evenimente. Accesul se activează numai din Setările Android.") },
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
            text = { Text("Vor fi șterse toate înregistrările păstrate de SAFEGUARD.") },
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
private fun BrandLogo(resourceName: String, size: androidx.compose.ui.unit.Dp) {
    val context = LocalContext.current
    val id = remember(resourceName) {
        @Suppress("DiscouragedApi")
        context.resources.getIdentifier(resourceName, "drawable", context.packageName)
    }
    if (id != 0) {
        Image(
            painter = painterResource(id),
            contentDescription = "Logo SAFEGUARD",
            modifier = Modifier.size(size),
            contentScale = ContentScale.Fit
        )
    } else {
        Text("🛡️", style = MaterialTheme.typography.headlineMedium)
    }
}

@Composable
private fun DashboardOverview(hasReport: Boolean, scanning: Boolean) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFB9EFAF))
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().padding(18.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(5.dp)
        ) {
            BrandLogo("safeguard_logo_simplu", 112.dp)
            Text(
                when {
                    scanning -> "Scanare în desfășurare"
                    hasReport -> "Verificare locală finalizată"
                    else -> "Verifică dispozitivul"
                },
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = navy
            )
            Text(
                if (hasReport) "Rezultatele sunt disponibile în Protecție."
                else "Analiză locală disponibilă • Fără protecție antivirus în timp real.",
                style = MaterialTheme.typography.bodySmall,
                color = navy
            )
        }
    }
}

@Composable
private fun DashboardTile(
    symbol: String,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFA6ECA5))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(7.dp),
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(symbol, style = MaterialTheme.typography.headlineMedium)
            Text(title, fontWeight = FontWeight.Bold, color = navy, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center, lineHeight = androidx.compose.ui.unit.TextUnit.Unspecified)
            Text(subtitle, style = MaterialTheme.typography.labelSmall, color = green, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        }
    }
}

@Composable
private fun SafeguardHeader(onHelp: () -> Unit, onLanguage: () -> Unit, helpEnglish: Boolean) {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = navy),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BrandLogo("safeguard_logo_simplu", 48.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text("SAFEGUARD", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = Color.White)
                Text(if (helpEnglish) "Local analysis • You stay in control" else "Analiză locală • Controlul rămâne la tine", style = MaterialTheme.typography.labelSmall, color = Color(0xFFA9E5C6))
            }
            TextButton(onClick = onHelp, contentPadding = PaddingValues(2.dp)) { Text("⚙", color = Color.White, style = MaterialTheme.typography.headlineMedium) }
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

@Composable
private fun DeviceHealthCards(context: Context, helpEnglish: Boolean) {
    var memory by remember { mutableStateOf(readDeviceMemory(context)) }
    val memorySnapshot = memory
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
            Text("Memorie RAM", fontWeight = FontWeight.Bold, color = green)
            if (memorySnapshot != null && memorySnapshot.totalMem > 0) {
                Text("Disponibil: %.1f GB din %.1f GB".format(memorySnapshot.availMem / 1073741824.0, memorySnapshot.totalMem / 1073741824.0))
                Text(if (memorySnapshot.lowMemory) { if (helpEnglish) "Android reports memory pressure." else "Android raportează presiune asupra memoriei." } else { if (helpEnglish) "Android does not report critically low memory." else "Android nu raportează memorie critic de redusă." })
            } else Text("Date indisponibile")
            Text(if (helpEnglish) "ⓘ RAM is the phone’s working memory. Android uses some for apps and caching; high usage does not automatically mean a problem." else "ⓘ RAM este memoria de lucru a telefonului. Android folosește o parte pentru aplicații și cache, iar utilizarea ridicată nu înseamnă automat o problemă.", style = MaterialTheme.typography.bodySmall)
            if (memorySnapshot != null) {
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
            Text(if (helpEnglish) "SAFEGUARD does not stop processes automatically or force RAM to be freed." else "SAFEGUARD nu oprește automat procese și nu eliberează forțat RAM.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { launchSettings(context, Intent(Settings.ACTION_APPLICATION_SETTINGS)) }) { Text(if (helpEnglish) "Review apps" else "Verifică aplicațiile") }
        }
    }
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
                if (helpEnglish) "ⓘ Android animations" else "ⓘ Animații Android",
                fontWeight = FontWeight.Bold,
                color = green
            )
            Text(
                if (helpEnglish)
                    "Animations are visual transitions between screens and windows. Reducing them may make navigation feel faster, but does not increase physical RAM."
                else
                    "Animațiile sunt tranziții vizuale între ecrane și ferestre. Reducerea lor poate face navigarea să pară mai rapidă, dar nu mărește memoria RAM fizică.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                if (helpEnglish)
                    "Manual option: Android Settings → Accessibility → Color and motion / Visibility enhancements → Remove animations (names vary by device). If unavailable, leave the setting unchanged."
                else
                    "Opțiune manuală: Setări Android → Accesibilitate → Îmbunătățiri pentru vizibilitate / Culori și mișcare → Elimină animațiile (denumirile diferă după telefon). Dacă opțiunea lipsește, lasă setarea neschimbată.",
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                if (helpEnglish)
                    "SAFEGUARD does not change animation scales or enable Developer options."
                else
                    "SAFEGUARD nu modifică scările animațiilor și nu activează Opțiunile pentru dezvoltatori.",
                style = MaterialTheme.typography.bodySmall
            )
            OutlinedButton(onClick = { launchSettings(context, Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)) }) {
                Text(if (helpEnglish) "Open Accessibility settings" else "Deschide Accesibilitate")
            }
        }
    }
    Card {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("Baterie", fontWeight = FontWeight.Bold, color = green)
            Text(if (battery == null) "Date indisponibile" else "Nivel baterie: $battery%")
            Text("SAFEGUARD nu modifică automat setările de energie.", style = MaterialTheme.typography.bodySmall)
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
            Text(if (helpEnglish) "SAFEGUARD does not measure each app’s cache or delete files automatically." else "SAFEGUARD nu măsoară cache-ul fiecărei aplicații și nu șterge automat fișiere.", style = MaterialTheme.typography.bodySmall)
            OutlinedButton(onClick = { launchSettings(context, Intent(Settings.ACTION_APPLICATION_SETTINGS)) }) { Text(if (helpEnglish) "Cache Advisor · App settings" else "Cache Advisor · Setări aplicații") }
            Text(if (helpEnglish) "SAFEGUARD does not read or delete personal files." else "SAFEGUARD nu citește sau șterge fișiere personale.", style = MaterialTheme.typography.bodySmall)
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
