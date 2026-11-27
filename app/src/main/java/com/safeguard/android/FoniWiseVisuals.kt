package com.safeguard.android

import android.graphics.Canvas as AndroidCanvas
import android.graphics.Color as AndroidColor
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import androidx.core.graphics.PathParser
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.w3c.dom.Element
import javax.xml.parsers.DocumentBuilderFactory

private val visualDark = Color(0xFF064D2B)
private val visualGreen = Color(0xFF087C26)
private val visualPale = Color(0xFFE7F8DB)
private val visualBackground = Color(0xFFF3FBF2)
private val visualGraphic = Color(0xFF72B94C)

private data class VisualSpec(val id: Int, val title: String, val svg: String, val description: String,
    val choices: List<String>, val initial: String, val details: List<String>, val note: String)

private val v24RO = VisualSpec(24, "Animații", """<svg viewBox="0 0 300 130" role="img" aria-label="Trei cadre ale unei tranziții"><rect x="12" y="8" width="76" height="100" rx="8" fill="#064D2B"/><rect x="18" y="17" width="64" height="81" rx="3" fill="#E7F8DB"/><rect x="40" y="45" width="20" height="24" rx="4" fill="#72B94C" stroke="#087C26"/><text x="50" y="124" text-anchor="middle" font-size="11" fill="#064D2B">Început</text><rect x="112" y="8" width="76" height="100" rx="8" fill="#064D2B"/><rect x="118" y="17" width="64" height="81" rx="3" fill="#E7F8DB"/><rect x="128" y="30" width="44" height="54" rx="4" fill="#72B94C" stroke="#087C26"/><text x="150" y="124" text-anchor="middle" font-size="11" fill="#064D2B">Tranziție</text><rect x="212" y="8" width="76" height="100" rx="8" fill="#064D2B"/><rect x="218" y="17" width="64" height="81" rx="3" fill="#E7F8DB"/><rect x="220" y="20" width="60" height="74" rx="4" fill="#72B94C" stroke="#087C26"/><text x="250" y="124" text-anchor="middle" font-size="11" fill="#064D2B">Final</text></svg>""", "Trei cadre ale unei tranziții", listOf("0,5×", "1×", "2×"), "Animația durează 400 ms; încărcarea rămâne 600 ms.", listOf("Animația durează 200 ms; încărcarea rămâne 600 ms.", "Animația durează 400 ms; încărcarea rămâne 600 ms.", "Animația durează 800 ms; încărcarea rămâne 600 ms."), "Model ipotetic. Duratele se pot suprapune; nu sunt un benchmark.")

private val v25RO = VisualSpec(25, "Bootloader", """<svg viewBox="0 0 320 275" role="img" aria-label="Etape conceptuale de pornire a telefonului"><rect x="30" y="8" width="260" height="49" rx="9" fill="#E7F8DB" stroke="#72B94C"/><text x="45" y="28" font-size="13" font-weight="bold" fill="#064D2B">1. Buton de pornire</text><text x="45" y="44" font-size="11" fill="#064D2B">Începe alimentarea</text><path d="M160 58 v16 l-5 -5 m5 5 l5 -5" stroke="#087C26" stroke-width="2" fill="none"/><rect x="30" y="76" width="260" height="49" rx="9" fill="#E7F8DB" stroke="#72B94C"/><text x="45" y="96" font-size="13" font-weight="bold" fill="#064D2B">2. Componente de pornire</text><text x="45" y="112" font-size="11" fill="#064D2B">Pregătire și verificări</text><path d="M160 126 v16 l-5 -5 m5 5 l5 -5" stroke="#087C26" stroke-width="2" fill="none"/><rect x="30" y="144" width="260" height="49" rx="9" fill="#064D2B" stroke="#72B94C"/><text x="45" y="164" font-size="13" font-weight="bold" fill="white">3. Bootloader</text><text x="45" y="180" font-size="11" fill="white">Pregătește încărcarea</text><path d="M160 194 v16 l-5 -5 m5 5 l5 -5" stroke="#087C26" stroke-width="2" fill="none"/><rect x="30" y="212" width="260" height="49" rx="9" fill="#E7F8DB" stroke="#72B94C"/><text x="45" y="232" font-size="13" font-weight="bold" fill="#064D2B">4. Android</text><text x="45" y="248" font-size="11" fill="#064D2B">Sistemul devine disponibil</text><g id="v25highlight0" visibility="hidden"><rect x="26" y="4" width="268" height="57" rx="7" stroke="#064D2B" stroke-width="2" fill="none"/><rect x="29" y="7" width="262" height="51" rx="5" stroke="#B5F52C" stroke-width="3" fill="none"/></g><g id="v25highlight1" visibility="hidden"><rect x="26" y="72" width="268" height="57" rx="7" stroke="#064D2B" stroke-width="2" fill="none"/><rect x="29" y="75" width="262" height="51" rx="5" stroke="#B5F52C" stroke-width="3" fill="none"/></g><g id="v25highlight2" visibility="hidden"><rect x="26" y="140" width="268" height="57" rx="7" stroke="#064D2B" stroke-width="2" fill="none"/><rect x="29" y="143" width="262" height="51" rx="5" stroke="#B5F52C" stroke-width="3" fill="none"/></g><g id="v25highlight3" visibility="hidden"><rect x="26" y="208" width="268" height="57" rx="7" stroke="#064D2B" stroke-width="2" fill="none"/><rect x="29" y="211" width="262" height="51" rx="5" stroke="#B5F52C" stroke-width="3" fill="none"/></g></svg>""", "Etape conceptuale de pornire a telefonului", listOf("Buton de pornire", "Componente de pornire", "Bootloader", "Android"), "Alege o etapă pentru a explora rolul ei.", listOf("Apăsarea butonului inițiază secvența de pornire.", "Componentele timpurii pregătesc hardware-ul și pot verifica următoarea etapă.", "Bootloaderul pregătește încărcarea sistemului conform regulilor dispozitivului.", "După inițializare, Android devine disponibil."), "Reprezentare simplificată; etapele și verificările diferă între dispozitive.")

private val v26RO = VisualSpec(26, "RAM, stocare internă și cache", """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 340 275" role="img" aria-label="RAM, stocare și cache: masă, dulap și sertar" font-family="Nunito Sans, sans-serif"><rect x="224" y="12" width="105" height="185" rx="6" fill="#064D2B"/><rect x="232" y="20" width="89" height="51" rx="3" fill="#E7F8DB"/><rect x="239" y="28" width="15" height="35" rx="2" fill="#72B94C"/><path d="M242 53 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="258" y="28" width="15" height="35" rx="2" fill="#087C26"/><path d="M261 53 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="277" y="28" width="15" height="35" rx="2" fill="#A4C98C"/><path d="M280 53 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="296" y="28" width="15" height="35" rx="2" fill="#D1E6C5"/><path d="M299 53 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="232" y="78" width="89" height="51" rx="3" fill="#E7F8DB"/><rect x="239" y="86" width="15" height="35" rx="2" fill="#72B94C"/><path d="M242 111 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="258" y="86" width="15" height="35" rx="2" fill="#087C26"/><path d="M261 111 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="277" y="86" width="15" height="35" rx="2" fill="#A4C98C"/><path d="M280 111 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="296" y="86" width="15" height="35" rx="2" fill="#D1E6C5"/><path d="M299 111 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="232" y="136" width="89" height="51" rx="3" fill="#E7F8DB"/><rect x="239" y="144" width="15" height="35" rx="2" fill="#72B94C"/><path d="M242 169 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="258" y="144" width="15" height="35" rx="2" fill="#087C26"/><path d="M261 169 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="277" y="144" width="15" height="35" rx="2" fill="#A4C98C"/><path d="M280 169 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="296" y="144" width="15" height="35" rx="2" fill="#D1E6C5"/><path d="M299 169 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="12" y="112" width="192" height="15" rx="3" fill="#064D2B"/><rect x="23" y="127" width="12" height="70" fill="#087C26"/><rect x="183" y="127" width="12" height="70" fill="#087C26"/><g transform="rotate(-8 70 95)"><rect x="38" y="64" width="65" height="45" rx="2" fill="white" stroke="#72B94C"/><path d="M46 73 H95" stroke="#72B94C" stroke-width="2"/><path d="M46 81 H95" stroke="#72B94C" stroke-width="2"/><path d="M46 89 H95" stroke="#72B94C" stroke-width="2"/><path d="M46 97 H95" stroke="#72B94C" stroke-width="2"/></g><rect x="120" y="82" width="62" height="29" rx="3" fill="#72B94C"/><rect x="125" y="77" width="52" height="25" rx="3" fill="#E7F8DB" stroke="#087C26"/><rect x="121" y="128" width="60" height="35" rx="3" fill="#B5F52C" stroke="#087C26"/><rect x="141" y="139" width="20" height="5" rx="2" fill="#064D2B"/><path d="M216 64 Q168 27 101 61 l8 -2 m-8 2 l3 -8" fill="none" stroke="#087C26" stroke-width="2" stroke-dasharray="5 3"/><g fill="#064D2B" text-anchor="middle"><text x="79" y="39" font-size="13" font-weight="bold">RAM</text><text x="79" y="53" font-size="10">Documente în lucru</text><path d="M150 166 V213" stroke="#087C26"/><text x="150" y="228" font-size="13" font-weight="bold">Cache</text><text x="150" y="243" font-size="10">Obiecte la îndemână</text><text x="276" y="214" font-size="13" font-weight="bold">Stocare internă</text><text x="276" y="229" font-size="10">Fișiere păstrate</text></g><g id="v26highlight0" visibility="hidden"><rect x="8" y="22" width="199" height="106" rx="7" stroke="#064D2B" stroke-width="2" fill="none"/><rect x="11" y="25" width="193" height="100" rx="5" stroke="#B5F52C" stroke-width="3" fill="none"/></g><g id="v26highlight1" visibility="hidden"><rect x="220" y="8" width="113" height="192" rx="7" stroke="#064D2B" stroke-width="2" fill="none"/><rect x="223" y="11" width="107" height="186" rx="5" stroke="#B5F52C" stroke-width="3" fill="none"/></g><g id="v26highlight2" visibility="hidden"><rect x="117" y="124" width="68" height="43" rx="7" stroke="#064D2B" stroke-width="2" fill="none"/><rect x="120" y="127" width="62" height="37" rx="5" stroke="#B5F52C" stroke-width="3" fill="none"/></g></svg>""", "RAM, stocare și cache: masă, dulap și sertar", listOf("RAM", "Stocare internă", "Cache"), "RAM este memoria de lucru; stocarea internă păstrează fișierele; cache-ul păstrează copii sau rezultate pentru reutilizare.", listOf("Masa reprezintă memoria RAM: documentele necesare lucrului curent sunt la îndemână.", "Dulapul reprezintă stocarea internă: fișierele sunt păstrate și când telefonul nu este alimentat.", "Sertarul reprezintă cache-ul: copii sau rezultate pot fi reutilizate pentru a reduce munca repetată."), "Analogie: masa de lucru, dulapul și sertarul. Cache-ul poate exista în RAM, stocare sau procesor; nu sunt obligatoriu trei memorii fizice separate.")

private val v24EN = VisualSpec(24, "Animations", """<svg viewBox="0 0 300 130" role="img" aria-label="Three frames of a transition"><rect x="12" y="8" width="76" height="100" rx="8" fill="#064D2B"/><rect x="18" y="17" width="64" height="81" rx="3" fill="#E7F8DB"/><rect x="40" y="45" width="20" height="24" rx="4" fill="#72B94C" stroke="#087C26"/><text x="50" y="124" text-anchor="middle" font-size="11" fill="#064D2B">Start</text><rect x="112" y="8" width="76" height="100" rx="8" fill="#064D2B"/><rect x="118" y="17" width="64" height="81" rx="3" fill="#E7F8DB"/><rect x="128" y="30" width="44" height="54" rx="4" fill="#72B94C" stroke="#087C26"/><text x="150" y="124" text-anchor="middle" font-size="11" fill="#064D2B">Transition</text><rect x="212" y="8" width="76" height="100" rx="8" fill="#064D2B"/><rect x="218" y="17" width="64" height="81" rx="3" fill="#E7F8DB"/><rect x="220" y="20" width="60" height="74" rx="4" fill="#72B94C" stroke="#087C26"/><text x="250" y="124" text-anchor="middle" font-size="11" fill="#064D2B">End</text></svg>""", "Three frames of a transition", listOf("0.5×", "1×", "2×"), "Animation takes 400 ms; loading remains 600 ms.", listOf("Animation takes 200 ms; loading remains 600 ms.", "Animation takes 400 ms; loading remains 600 ms.", "Animation takes 800 ms; loading remains 600 ms."), "Hypothetical model. Durations may overlap; this is not a benchmark.")

private val v25EN = VisualSpec(25, "Bootloader", """<svg viewBox="0 0 320 275" role="img" aria-label="Conceptual phone startup stages"><rect x="30" y="8" width="260" height="49" rx="9" fill="#E7F8DB" stroke="#72B94C"/><text x="45" y="28" font-size="13" font-weight="bold" fill="#064D2B">1. Power button</text><text x="45" y="44" font-size="11" fill="#064D2B">Startup begins</text><path d="M160 58 v16 l-5 -5 m5 5 l5 -5" stroke="#087C26" stroke-width="2" fill="none"/><rect x="30" y="76" width="260" height="49" rx="9" fill="#E7F8DB" stroke="#72B94C"/><text x="45" y="96" font-size="13" font-weight="bold" fill="#064D2B">2. Startup components</text><text x="45" y="112" font-size="11" fill="#064D2B">Preparation and checks</text><path d="M160 126 v16 l-5 -5 m5 5 l5 -5" stroke="#087C26" stroke-width="2" fill="none"/><rect x="30" y="144" width="260" height="49" rx="9" fill="#064D2B" stroke="#72B94C"/><text x="45" y="164" font-size="13" font-weight="bold" fill="white">3. Bootloader</text><text x="45" y="180" font-size="11" fill="white">Prepares loading</text><path d="M160 194 v16 l-5 -5 m5 5 l5 -5" stroke="#087C26" stroke-width="2" fill="none"/><rect x="30" y="212" width="260" height="49" rx="9" fill="#E7F8DB" stroke="#72B94C"/><text x="45" y="232" font-size="13" font-weight="bold" fill="#064D2B">4. Android</text><text x="45" y="248" font-size="11" fill="#064D2B">System becomes available</text><g id="v25highlight0" visibility="hidden"><rect x="26" y="4" width="268" height="57" rx="7" stroke="#064D2B" stroke-width="2" fill="none"/><rect x="29" y="7" width="262" height="51" rx="5" stroke="#B5F52C" stroke-width="3" fill="none"/></g><g id="v25highlight1" visibility="hidden"><rect x="26" y="72" width="268" height="57" rx="7" stroke="#064D2B" stroke-width="2" fill="none"/><rect x="29" y="75" width="262" height="51" rx="5" stroke="#B5F52C" stroke-width="3" fill="none"/></g><g id="v25highlight2" visibility="hidden"><rect x="26" y="140" width="268" height="57" rx="7" stroke="#064D2B" stroke-width="2" fill="none"/><rect x="29" y="143" width="262" height="51" rx="5" stroke="#B5F52C" stroke-width="3" fill="none"/></g><g id="v25highlight3" visibility="hidden"><rect x="26" y="208" width="268" height="57" rx="7" stroke="#064D2B" stroke-width="2" fill="none"/><rect x="29" y="211" width="262" height="51" rx="5" stroke="#B5F52C" stroke-width="3" fill="none"/></g></svg>""", "Conceptual phone startup stages", listOf("Power button", "Startup components", "Bootloader", "Android"), "Choose a stage to explore its role.", listOf("Pressing the button initiates the startup sequence.", "Early components prepare the hardware and may verify the next stage.", "The bootloader prepares system loading according to the device's rules.", "After initialization, Android becomes available."), "Simplified representation; stages and checks differ across devices.")

private val v26EN = VisualSpec(26, "RAM, internal storage and cache", """<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 340 275" role="img" aria-label="RAM, storage and cache: desk, cabinet and drawer" font-family="Nunito Sans, sans-serif"><rect x="224" y="12" width="105" height="185" rx="6" fill="#064D2B"/><rect x="232" y="20" width="89" height="51" rx="3" fill="#E7F8DB"/><rect x="239" y="28" width="15" height="35" rx="2" fill="#72B94C"/><path d="M242 53 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="258" y="28" width="15" height="35" rx="2" fill="#087C26"/><path d="M261 53 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="277" y="28" width="15" height="35" rx="2" fill="#A4C98C"/><path d="M280 53 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="296" y="28" width="15" height="35" rx="2" fill="#D1E6C5"/><path d="M299 53 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="232" y="78" width="89" height="51" rx="3" fill="#E7F8DB"/><rect x="239" y="86" width="15" height="35" rx="2" fill="#72B94C"/><path d="M242 111 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="258" y="86" width="15" height="35" rx="2" fill="#087C26"/><path d="M261 111 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="277" y="86" width="15" height="35" rx="2" fill="#A4C98C"/><path d="M280 111 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="296" y="86" width="15" height="35" rx="2" fill="#D1E6C5"/><path d="M299 111 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="232" y="136" width="89" height="51" rx="3" fill="#E7F8DB"/><rect x="239" y="144" width="15" height="35" rx="2" fill="#72B94C"/><path d="M242 169 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="258" y="144" width="15" height="35" rx="2" fill="#087C26"/><path d="M261 169 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="277" y="144" width="15" height="35" rx="2" fill="#A4C98C"/><path d="M280 169 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="296" y="144" width="15" height="35" rx="2" fill="#D1E6C5"/><path d="M299 169 h9" stroke="#F3FBF2" stroke-width="2"/><rect x="12" y="112" width="192" height="15" rx="3" fill="#064D2B"/><rect x="23" y="127" width="12" height="70" fill="#087C26"/><rect x="183" y="127" width="12" height="70" fill="#087C26"/><g transform="rotate(-8 70 95)"><rect x="38" y="64" width="65" height="45" rx="2" fill="white" stroke="#72B94C"/><path d="M46 73 H95" stroke="#72B94C" stroke-width="2"/><path d="M46 81 H95" stroke="#72B94C" stroke-width="2"/><path d="M46 89 H95" stroke="#72B94C" stroke-width="2"/><path d="M46 97 H95" stroke="#72B94C" stroke-width="2"/></g><rect x="120" y="82" width="62" height="29" rx="3" fill="#72B94C"/><rect x="125" y="77" width="52" height="25" rx="3" fill="#E7F8DB" stroke="#087C26"/><rect x="121" y="128" width="60" height="35" rx="3" fill="#B5F52C" stroke="#087C26"/><rect x="141" y="139" width="20" height="5" rx="2" fill="#064D2B"/><path d="M216 64 Q168 27 101 61 l8 -2 m-8 2 l3 -8" fill="none" stroke="#087C26" stroke-width="2" stroke-dasharray="5 3"/><g fill="#064D2B" text-anchor="middle"><text x="79" y="39" font-size="13" font-weight="bold">RAM</text><text x="79" y="53" font-size="10">Working documents</text><path d="M150 166 V213" stroke="#087C26"/><text x="150" y="228" font-size="13" font-weight="bold">Cache</text><text x="150" y="243" font-size="10">Items within reach</text><text x="276" y="214" font-size="13" font-weight="bold">Internal storage</text><text x="276" y="229" font-size="10">Retained files</text></g><g id="v26highlight0" visibility="hidden"><rect x="8" y="22" width="199" height="106" rx="7" stroke="#064D2B" stroke-width="2" fill="none"/><rect x="11" y="25" width="193" height="100" rx="5" stroke="#B5F52C" stroke-width="3" fill="none"/></g><g id="v26highlight1" visibility="hidden"><rect x="220" y="8" width="113" height="192" rx="7" stroke="#064D2B" stroke-width="2" fill="none"/><rect x="223" y="11" width="107" height="186" rx="5" stroke="#B5F52C" stroke-width="3" fill="none"/></g><g id="v26highlight2" visibility="hidden"><rect x="117" y="124" width="68" height="43" rx="7" stroke="#064D2B" stroke-width="2" fill="none"/><rect x="120" y="127" width="62" height="37" rx="5" stroke="#B5F52C" stroke-width="3" fill="none"/></g></svg>""", "RAM, storage and cache: desk, cabinet and drawer", listOf("RAM", "Internal storage", "Cache"), "RAM is working memory; internal storage retains files; cache holds copies or results for reuse.", listOf("The desk represents RAM: documents needed for current work are within reach.", "The cabinet represents internal storage: files are retained even when the phone is not powered.", "The drawer represents cache: copies or results can be reused to reduce repeated work."), "Analogy: desk, cabinet and drawer. Cache can exist in RAM, storage or the processor; these need not be three physically separate memories.")


private fun visualFor(title: String, english: Boolean): VisualSpec? = when (title) {
    "Animații" -> if (english) v24EN else v24RO
    "Bootloader" -> if (english) v25EN else v25RO
    "Cache", "Memoria RAM", "Memorie internă", "Memorie cache" -> if (english) v26EN else v26RO
    else -> null
}

/** Native, offline lesson illustration. No device settings are read or changed. */
@Composable
internal fun FoniWiseLessonVisual(title: String, english: Boolean, advanced: Boolean) {
    val spec = visualFor(title, english) ?: return
    val initial = if (spec.id == 24) 1 else -1
    var selected by rememberSaveable(title, english, advanced) { mutableIntStateOf(initial) }
    val context = LocalContext.current
    val typeface = remember(context) { context.resources.getFont(R.font.nunito_sans) }
    // Only the fixed, bundled SVG subset below is parsed; never remote or user-provided XML.
    val root = remember(spec.svg) {
        DocumentBuilderFactory.newInstance().apply {
            isExpandEntityReferences = false
        }.newDocumentBuilder().parse(spec.svg.byteInputStream(Charsets.UTF_8)).documentElement
    }
    val view = remember(root) { root.getAttribute("viewBox").trim().split(Regex("[ ,]+")).map { it.toFloat() } }
    val detail = spec.details.getOrNull(selected) ?: spec.initial
    Surface(color = visualBackground, shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
        Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Surface(color = visualDark, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text(spec.title, color = Color.White, fontWeight = FontWeight.Bold, modifier = Modifier.padding(12.dp))
            }
            Canvas(Modifier.fillMaxWidth().aspectRatio(view[2] / view[3]).semantics {
                contentDescription = spec.description + ". " + detail
            }) {
                drawIntoCanvas { c ->
                    val native = c.nativeCanvas
                    val save = native.save()
                    native.scale(size.width / view[2], size.height / view[3])
                    native.translate(-view[0], -view[1])
                    drawSvgElement(native, root, SvgStyle(), selected, typeface)
                    native.restoreToCount(save)
                }
            }
            Text(if (spec.id == 24) (if (english) "Duration factor" else "Factor de durată")
                else if (spec.id == 25) (if (english) "Explore the stages" else "Explorează etapele")
                else (if (english) "Explore the analogy" else "Explorează analogia"), color = visualDark)
            // Native controls wrap naturally at large font sizes by using full-width rows.
            spec.choices.forEachIndexed { index, label ->
                FilterChip(selected = selected == index, onClick = { selected = index },
                    label = { Text(label, color = if (selected == index) Color.White else visualDark) },
                    colors = FilterChipDefaults.filterChipColors(containerColor = visualPale, selectedContainerColor = visualGreen),
                    modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp))
            }
            if (spec.id == 24) {
                val ms = listOf(200, 400, 800)[selected.coerceIn(0, 2)]
                Text((if (english) "Animation duration: " else "Durata animației: ") + ms + " ms", color = visualDark)
                VisualDurationBar(ms / 800f, visualGraphic)
                Text(if (english) "Data loading: 600 ms" else "Încărcarea datelor: 600 ms", color = visualDark)
                VisualDurationBar(0.75f, visualDark)
            }
            Surface(color = visualPale, shape = RoundedCornerShape(8.dp), modifier = Modifier.fillMaxWidth()) {
                Text(detail, color = visualDark, modifier = Modifier.padding(12.dp))
            }
            Text(spec.note, color = visualDark, style = MaterialTheme.typography.bodySmall)
            Text(if (english) "Illustrative example" else "Exemplu informativ", color = visualDark,
                style = MaterialTheme.typography.labelMedium)
            OutlinedButton(onClick = { selected = initial }, modifier = Modifier.fillMaxWidth().heightIn(min = 48.dp)) {
                Text(if (english) "Reset example" else "Revino la exemplul inițial", color = visualDark)
            }
        }
    }
}

@Composable
private fun VisualDurationBar(fraction: Float, color: Color) {
    Box(Modifier.fillMaxWidth().height(16.dp).background(visualPale, RoundedCornerShape(8.dp))) {
        Box(Modifier.fillMaxWidth(fraction).fillMaxHeight().background(color, RoundedCornerShape(8.dp)))
    }
}

private data class SvgStyle(val fill: String = "#000000", val stroke: String = "none", val strokeWidth: Float = 1f,
    val fontSize: Float = 16f, val bold: Boolean = false, val anchor: String = "start",
    val dash: String = "", val cap: String = "butt")

private fun Element.attr(name: String, fallback: String) = if (hasAttribute(name)) getAttribute(name) else fallback
private fun Element.num(name: String, fallback: Float = 0f) = getAttribute(name).toFloatOrNull() ?: fallback

/** Small native renderer for the fixed rect/path/text/g subset used by V24–V26. */
private fun drawSvgElement(canvas: AndroidCanvas, e: Element, inherited: SvgStyle, selected: Int, font: Typeface) {
    val id = e.getAttribute("id")
    if (id.startsWith("v25highlight") || id.startsWith("v26highlight")) {
        if (id.lastOrNull()?.digitToIntOrNull() != selected) return
    } else if (e.getAttribute("visibility") == "hidden") return
    val style = SvgStyle(e.attr("fill", inherited.fill), e.attr("stroke", inherited.stroke),
        e.num("stroke-width", inherited.strokeWidth), e.num("font-size", inherited.fontSize),
        e.attr("font-weight", if (inherited.bold) "bold" else "normal") == "bold",
        e.attr("text-anchor", inherited.anchor), e.attr("stroke-dasharray", inherited.dash),
        e.attr("stroke-linecap", inherited.cap))
    val save = canvas.save()
    val transform = e.getAttribute("transform")
    if (transform.startsWith("rotate(")) {
        val values = transform.substringAfter("(").substringBefore(")").trim().split(Regex("[ ,]+")).mapNotNull { it.toFloatOrNull() }
        if (values.size == 3) canvas.rotate(values[0], values[1], values[2])
    }
    fun paint(color: String, stroke: Boolean): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        this.color = AndroidColor.parseColor(color)
        this.style = if (stroke) Paint.Style.STROKE else Paint.Style.FILL
        strokeWidth = style.strokeWidth
        strokeCap = if (style.cap == "round") Paint.Cap.ROUND else Paint.Cap.BUTT
        if (stroke && style.dash.isNotBlank()) {
            val intervals = style.dash.split(Regex("[ ,]+")).mapNotNull { it.toFloatOrNull() }.toFloatArray()
            if (intervals.size >= 2 && intervals.size % 2 == 0) pathEffect = DashPathEffect(intervals, 0f)
        }
    }
    fun shape(draw: (Paint) -> Unit) {
        if (style.fill != "none") draw(paint(style.fill, false))
        if (style.stroke != "none") draw(paint(style.stroke, true))
    }
    when (e.tagName) {
        "rect" -> {
            val x = e.num("x"); val y = e.num("y")
            val r = RectF(x, y, x + e.num("width"), y + e.num("height"))
            val rx = e.num("rx"); val ry = e.num("ry", rx)
            shape { canvas.drawRoundRect(r, rx, ry, it) }
        }
        "path" -> {
            val path = PathParser.createPathFromPathData(e.getAttribute("d"))
            if (path != null) shape { canvas.drawPath(path, it) }
        }
        "text" -> if (style.fill != "none") {
            val p = paint(style.fill, false).apply {
                textSize = style.fontSize
                typeface = Typeface.create(font, if (style.bold) Typeface.BOLD else Typeface.NORMAL)
                textAlign = when (style.anchor) {
                    "middle" -> Paint.Align.CENTER
                    "end" -> Paint.Align.RIGHT
                    else -> Paint.Align.LEFT
                }
            }
            canvas.drawText(e.textContent, e.num("x"), e.num("y"), p)
        }
        "svg", "g" -> {
            for (i in 0 until e.childNodes.length) {
                val child = e.childNodes.item(i)
                if (child is Element) drawSvgElement(canvas, child, style, selected, font)
            }
        }
    }
    canvas.restoreToCount(save)
}
