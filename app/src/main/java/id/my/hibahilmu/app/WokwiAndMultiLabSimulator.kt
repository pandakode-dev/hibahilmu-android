package id.my.hibahilmu.app

import android.graphics.Paint as AndroidPaint
import android.graphics.Typeface
import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

data class WokwiWebProjectPreset(
    val id: String,
    val nomor: Int,
    val judul: String,
    val board: String,
    val komponenUtama: List<String>,
    val deskripsi: String,
    val kodeSketchIno: String,
    val diagramJson: String
)

val DAFTAR_8_PROYEK_WOKWI_WEB = listOf(
    WokwiWebProjectPreset(
        id = "proj-1",
        nomor = 1,
        judul = "Proyek 1: Lampu Jalan Otomatis LDR + Relay",
        board = "ESP32 DevKit V1 + LDR + Relay",
        komponenUtama = listOf("Sensor LDR (GPIO 34)", "Relay 5V (GPIO 26)", "Lampu Jalan AC", "OLED SSD1306"),
        deskripsi = "Mendeteksi intensitas cahaya matahari (Lux). Saat sore/malam (< 300 Lux), sakelar Relay tertarik otomatis menyalakan lampu jalan.",
        kodeSketchIno = """
            // HibahIlmu Wokwi - Proyek 1: Lampu Jalan Otomatis LDR
            #define PIN_LDR 34
            #define PIN_RELAY 26
            void setup() {
              Serial.begin(115200);
              pinMode(PIN_LDR, INPUT);
              pinMode(PIN_RELAY, OUTPUT);
            }
            void loop() {
              int lux = map(analogRead(PIN_LDR), 0, 4095, 0, 1000);
              bool malam = (lux < 300);
              digitalWrite(PIN_RELAY, malam ? HIGH : LOW);
              Serial.printf("[PROJ-1] Lux: %d | Lampu Jalan: %s\n", lux, malam ? "ON" : "OFF");
              delay(1000);
            }
        """.trimIndent(),
        diagramJson = """{ "version": 1, "author": "HibahIlmu.id", "parts": ["esp32", "wokwi-photoresistor-sensor", "wokwi-relay-module", "wokwi-led"] }"""
    ),
    WokwiWebProjectPreset(
        id = "proj-2",
        nomor = 2,
        judul = "Proyek 2: Smart Door Lock RFID + Solenoid + Servo",
        board = "ESP32 + RFID RC522 + Servo SG90",
        komponenUtama = listOf("RFID RC522 (SPI)", "Solenoid 12V (GPIO 27)", "Servo SG90 (GPIO 13)", "Buzzer Akses"),
        deskripsi = "Keamanan pintu laboratorium pintar. Saat kartu E-KTP mendekat atau tombol UNLOCK ditekan, batang Solenoid tertarik dan lengan Servo berputar membuka palang.",
        kodeSketchIno = """
            // HibahIlmu Wokwi - Proyek 2: Smart Door Lock RFID & Servo
            #include <ESP32Servo.h>
            #define PIN_SOLENOID 27
            #define PIN_SERVO 13
            Servo palang;
            void setup() {
              Serial.begin(115200);
              pinMode(PIN_SOLENOID, OUTPUT);
              palang.attach(PIN_SERVO);
            }
            void loop() {
              digitalWrite(PIN_SOLENOID, HIGH);
              palang.write(90);
              Serial.println("[RFID] Akses Diterima -> Solenoid UNLOCK & Servo 90 deg");
              delay(2000);
            }
        """.trimIndent(),
        diagramJson = """{ "version": 1, "author": "HibahIlmu.id", "parts": ["esp32", "wokwi-servo", "wokwi-RFID-rc522", "wokwi-solenoid"] }"""
    ),
    WokwiWebProjectPreset(
        id = "proj-3",
        nomor = 3,
        judul = "Proyek 3: Smart Greenhouse DHT22 + Pompa + Kipas",
        board = "ESP32 + DHT22 + Soil + Pompa Air",
        komponenUtama = listOf("DHT22 Suhu/Lembab (GPIO 15)", "Probe Tanah (GPIO 35)", "Kipas Pendingin (GPIO 33)", "Pompa Irigasi (GPIO 25)"),
        deskripsi = "Otomasi rumah kaca pertanian. Baling-baling Kipas berputar saat suhu > 32°C dan Pompa Air menyemprotkan air ke tanaman saat kelembaban tanah < 40%.",
        kodeSketchIno = """
            // HibahIlmu Wokwi - Proyek 3: Smart Greenhouse Irigasi
            #include "DHTesp.h"
            #define PIN_DHT 15
            #define PIN_POMPA 25
            #define PIN_KIPAS 33
            DHTesp dht;
            void setup() {
              Serial.begin(115200);
              dht.setup(PIN_DHT, DHTesp::DHT22);
              pinMode(PIN_POMPA, OUTPUT);
              pinMode(PIN_KIPAS, OUTPUT);
            }
            void loop() {
              TempAndHumidity d = dht.getTempAndHumidity();
              digitalWrite(PIN_KIPAS, d.temperature > 32.0 ? HIGH : LOW);
              Serial.printf("[GREENHOUSE] T=%.1fC H=%.0f%%\n", d.temperature, d.humidity);
              delay(1500);
            }
        """.trimIndent(),
        diagramJson = """{ "version": 1, "author": "HibahIlmu.id", "parts": ["esp32", "wokwi-dht22", "wokwi-soil-sensor", "wokwi-pump"] }"""
    ),
    WokwiWebProjectPreset(
        id = "proj-4",
        nomor = 4,
        judul = "Proyek 4: Radar Sonar Ultrasonik HC-SR04 + Buzzer",
        board = "ESP32 + HC-SR04 + LED Bar + Buzzer",
        komponenUtama = listOf("Ultrasonik HC-SR04 (Trig 5, Echo 18)", "Piezo Buzzer (GPIO 19)", "8-LED Bar Graph", "Target Objek"),
        deskripsi = "Memancarkan gelombang akustik ultrasonik ke objek sasaran. Saat jarak objek < 25 cm, Buzzer berbunyi nyaring dan LED Bar menyala merah penuh.",
        kodeSketchIno = """
            // HibahIlmu Wokwi - Proyek 4: Radar Ultrasonik HC-SR04
            #define PIN_TRIG 5
            #define PIN_ECHO 18
            #define PIN_BUZZER 19
            void setup() {
              Serial.begin(115200);
              pinMode(PIN_TRIG, OUTPUT);
              pinMode(PIN_ECHO, INPUT);
              pinMode(PIN_BUZZER, OUTPUT);
            }
            void loop() {
              Serial.println("[SONAR] Memindai gelombang jarak objek...");
              delay(500);
            }
        """.trimIndent(),
        diagramJson = """{ "version": 1, "author": "HibahIlmu.id", "parts": ["esp32", "wokwi-hc-sr04", "wokwi-buzzer", "wokwi-led-bar-graph"] }"""
    ),
    WokwiWebProjectPreset(
        id = "proj-5",
        nomor = 5,
        judul = "Proyek 5: Dual-ESP32 Telemetry Bridge Blynk Cloud",
        board = "Dual ESP32 (Node A TX & Node B RX)",
        komponenUtama = listOf("Node A: Sensor Transmitter", "Node B: Actuator Receiver", "Broker Cloud hibahilmu.my.id", "OLED Status Monitor"),
        deskripsi = "Komunikasi nirkabel dua unit mikrokontroler ESP32 melalui broker Cloud hibahilmu.my.id dengan visualisasi pengiriman paket data JSON secara langsung.",
        kodeSketchIno = """
            // HibahIlmu Wokwi - Proyek 5: Dual-ESP32 Cloud Bridge
            #include <WiFi.h>
            #include <HTTPClient.h>
            void setup() {
              Serial.begin(115200);
              Serial.println("[DUAL-ESP32] Node A & Node B Terhubung ke Broker Cloud!");
            }
            void loop() {
              Serial.println("[BRIDGE] TX Node A -> Cloud V0..V4 -> RX Node B OK!");
              delay(1500);
            }
        """.trimIndent(),
        diagramJson = """{ "version": 1, "author": "HibahIlmu.id", "parts": ["esp32-node-A", "esp32-node-B", "cloud-broker", "ssd1306"] }"""
    ),
    WokwiWebProjectPreset(
        id = "proj-6",
        nomor = 6,
        judul = "Proyek 6: Smart Traffic Light 3-Fase + 7-Segment Timer",
        board = "ESP32 + Traffic Light + 7-Segment",
        komponenUtama = listOf("Lampu Merah-Kuning-Hijau (GPIO 21,22,23)", "7-Segment Countdown Display", "Tombol Penyeberang Pejalan Kaki"),
        deskripsi = "Simulasi lampu lalu lintas jalan raya cerdas dengan layar hitung mundur 7-Segmen dan siklus pergantian otomatis Merah -> Kuning -> Hijau.",
        kodeSketchIno = """
            // HibahIlmu Wokwi - Proyek 6: Smart Traffic Light + Timer
            #define LED_MERAH 23
            #define LED_KUNING 22
            #define LED_HIJAU 21
            void setup() {
              Serial.begin(115200);
              pinMode(LED_MERAH, OUTPUT);
              pinMode(LED_KUNING, OUTPUT);
              pinMode(LED_HIJAU, OUTPUT);
            }
            void loop() {
              Serial.println("[TRAFFIC] Siklus Lampu Lalu Lintas Aktif...");
              delay(1000);
            }
        """.trimIndent(),
        diagramJson = """{ "version": 1, "author": "HibahIlmu.id", "parts": ["esp32", "wokwi-traffic-light", "wokwi-7segment"] }"""
    ),
    WokwiWebProjectPreset(
        id = "proj-7",
        nomor = 7,
        judul = "Proyek 7: Detektor Gas Bocor MQ-2 + Api + Exhaust Fan",
        board = "ESP32 + Gas MQ-2 + Flame + Sirine",
        komponenUtama = listOf("Sensor Gas MQ-2 (GPIO 32)", "Sensor Api Flame IR (GPIO 33)", "Kipas Exhaust Darurat", "Sirine Strobe Merah"),
        deskripsi = "Keamanan dapur dan pabrik pintar. Ketika kadar gas LPG/asap tinggi atau terdeteksi percikan api, Kipas Exhaust berputar kencang dan Sirine menyala.",
        kodeSketchIno = """
            // HibahIlmu Wokwi - Proyek 7: Detektor Kebocoran Gas & Api
            #define PIN_MQ2 32
            #define PIN_EXHAUST 25
            void setup() {
              Serial.begin(115200);
              pinMode(PIN_EXHAUST, OUTPUT);
            }
            void loop() {
              int ppm = analogRead(PIN_MQ2);
              Serial.printf("[SAFETY] Kadar Gas: %d PPM | Exhaust: ON\n", ppm);
              delay(1000);
            }
        """.trimIndent(),
        diagramJson = """{ "version": 1, "author": "HibahIlmu.id", "parts": ["esp32", "wokwi-gas-sensor", "wokwi-Ky-026-flame", "exhaust-fan"] }"""
    ),
    WokwiWebProjectPreset(
        id = "proj-8",
        nomor = 8,
        judul = "Proyek 8: Solar Tracker Panel Surya Otomatis + INA219",
        board = "ESP32 + Solar Panel + Dual LDR + Servo",
        komponenUtama = listOf("Panel Surya Fotovoltaik 12V", "Dual LDR Timur-Barat", "Servo Tracker (GPIO 13)", "Sensor Daya INA219 (Volt/Ampere)"),
        deskripsi = "Panel surya pintar yang memiringkan sudutnya secara otomatis mengikuti arah datangnya sinar matahari untuk menghasilkan daya listrik (Watt) maksimal.",
        kodeSketchIno = """
            // HibahIlmu Wokwi - Proyek 8: Smart Solar Tracker Dual-LDR
            #include <ESP32Servo.h>
            Servo tracker;
            void setup() {
              Serial.begin(115200);
              tracker.attach(13);
            }
            void loop() {
              Serial.println("[SOLAR] Melacak posisi matahari -> Daya Optimal 14.2W");
              delay(1200);
            }
        """.trimIndent(),
        diagramJson = """{ "version": 1, "author": "HibahIlmu.id", "parts": ["esp32", "solar-panel-cell", "wokwi-servo", "ina219-power"] }"""
    )
)

@Composable
fun LayarSimulatorWokwiWebSync(
    user: UserAccount,
    tokenAktif: String,
    onTambahXp: (Int) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var proyekAktif by remember { mutableStateOf(DAFTAR_8_PROYEK_WOKWI_WEB[0]) }
    var simulasiBerjalan by remember { mutableStateOf(true) }
    var tabEditorAktif by remember { mutableStateOf("visual") }

    // Parameter Interaktif yang Langsung Menggerakkan Animasi Komponen
    var nilaiCahayaLux by remember { mutableStateOf(210f) } // Proyek 1 & 8
    var nilaiSuhuDht by remember { mutableStateOf(33.5f) }  // Proyek 3 & 7
    var nilaiLembabTanah by remember { mutableStateOf(32f) } // Proyek 3
    var nilaiJarakSonar by remember { mutableStateOf(18f) }  // Proyek 4
    var sudutServo by remember { mutableStateOf(90f) }       // Proyek 2 & 8
    var kadarGasPpm by remember { mutableStateOf(650f) }     // Proyek 7
    var relaySolenoidOn by remember { mutableStateOf(true) } // Proyek 2
    var relayPompaOn by remember { mutableStateOf(true) }    // Proyek 3

    // Engine Animasi Kontinu 60 FPS untuk Putaran Kipas, Gelombang Sonar, & Aliran Elektron
    val infiniteTransition = rememberInfiniteTransition(label = "wokwi_anim")
    val faseAnimasi by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "fase_derajat"
    )

    var detakDetik by remember { mutableStateOf(0) }
    var logSerialList by remember {
        mutableStateOf(
            listOf(
                "[00:00.1] ESP32 Chip Revision 3 (WiFi + BT Ready)",
                "[00:00.4] Connected to Wokwi-GUEST | Broker: hibahilmu.my.id",
                "[00:00.8] Project Loaded: ${DAFTAR_8_PROYEK_WOKWI_WEB[0].judul}"
            )
        )
    }

    val lampuJalanNyala = nilaiCahayaLux < 300f
    val kipasNyala = nilaiSuhuDht > 32f
    val pompaNyala = nilaiLembabTanah < 40f || relayPompaOn
    val alarmSonarBahaya = nilaiJarakSonar < 25f
    val bahayaGasApi = kadarGasPpm > 500f

    LaunchedEffect(simulasiBerjalan, proyekAktif) {
        while (simulasiBerjalan) {
            delay(1000)
            detakDetik += 1
            val logBaru = when (proyekAktif.nomor) {
                1 -> "[#${detakDetik}s] LDR=${nilaiCahayaLux.roundToInt()}lx -> Relay:${if (lampuJalanNyala) "CLOSED (Lampu ON)" else "OPEN (OFF)"}"
                2 -> "[#${detakDetik}s] RFID Tag OK -> Solenoid:${if (relaySolenoidOn) "UNLOCK" else "LOCK"} | Servo:${sudutServo.roundToInt()}°"
                3 -> "[#${detakDetik}s] DHT=${"%.1f".format(nilaiSuhuDht)}C Soil=${nilaiLembabTanah.roundToInt()}% -> Kipas:${if (kipasNyala) "ON" else "OFF"} Pompa:${if (pompaNyala) "ON" else "OFF"}"
                4 -> "[#${detakDetik}s] Sonar=${nilaiJarakSonar.roundToInt()}cm -> ${if (alarmSonarBahaya) "ALARM BEEP! 🚨" else "Zona Aman"}"
                5 -> "[#${detakDetik}s] TX Node A -> Cloud V0=${nilaiSuhuDht.roundToInt()} V1=${nilaiJarakSonar.roundToInt()} -> RX Node B Ack!"
                6 -> "[#${detakDetik}s] Traffic Phase: ${when (detakDetik % 3) { 0 -> "HIJAU (GO)"; 1 -> "KUNING (WAIT)"; else -> "MERAH (STOP)" }}"
                7 -> "[#${detakDetik}s] MQ-2 Gas=${kadarGasPpm.roundToInt()} PPM -> Exhaust:${if (bahayaGasApi) "TURBO ON" else "STANDBY"}"
                else -> "[#${detakDetik}s] Solar Tracker=${sudutServo.roundToInt()}° | Lux=${nilaiCahayaLux.roundToInt()} -> Output: ${"%.1f".format(nilaiCahayaLux * 0.018f)} Watt"
            }
            logSerialList = (logSerialList + logBaru).takeLast(16)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // HEADER SIMULATOR WOKWI CERAH
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(2.dp, Color(0xFF10B981), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(50),
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Text(
                            text = "⚡ 8 PROYEK WOKWI REAL-TIME SIMULATOR",
                            color = Color(0xFF065F46),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (simulasiBerjalan) Color(0xFF10B981) else Color(0xFFE11D48),
                        modifier = Modifier.clickable {
                            simulasiBerjalan = !simulasiBerjalan
                            if (simulasiBerjalan) onTambahXp(15)
                        }
                    ) {
                        Text(
                            text = if (simulasiBerjalan) "▶ RUNNING" else "⏹ STOPPED",
                            color = Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            softWrap = false,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)
                        )
                    }
                }

                Text(
                    text = proyekAktif.judul,
                    color = Color(0xFF0F172A),
                    fontSize = 16.5.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = proyekAktif.deskripsi,
                    color = Color(0xFF475569),
                    fontSize = 11.5.sp
                )

                // PILIHAN 8 PROYEK WOKWI (SETIAP PROYEK MEMILIKI GAMBAR RANGKAIAN BERBEDA!)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    DAFTAR_8_PROYEK_WOKWI_WEB.forEach { prj ->
                        val dipilih = prj.id == proyekAktif.id
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (dipilih) Color(0xFF047857) else Color(0xFFF1F5F9),
                            modifier = Modifier
                                .border(
                                    width = 1.5.dp,
                                    color = if (dipilih) Color(0xFFFACC15) else Color(0xFFCBD5E1),
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { proyekAktif = prj }
                        ) {
                            Text(
                                text = "🔌 Proyek ${prj.nomor}: ${prj.judul.substringAfter(": ")}",
                                color = if (dipilih) Color.White else Color(0xFF1E293B),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                            )
                        }
                    }
                }
            }
        }

        // SUB-TAB SIMULATOR
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "visual" to "🛠️ 1. Rangkaian & Animasi Live",
                "ino" to "💻 2. Kode C++ (sketch.ino)",
                "json" to "📐 3. Wokwi (diagram.json)",
                "serial" to "📟 4. Serial Monitor (${logSerialList.size})"
            ).forEach { (kode, label) ->
                val aktif = tabEditorAktif == kode
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = if (aktif) Color(0xFF2563EB) else Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier
                        .border(
                            width = 1.5.dp,
                            color = if (aktif) Color(0xFF1D4ED8) else Color(0xFFCBD5E1),
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { tabEditorAktif = kode }
                ) {
                    Text(
                        text = label,
                        color = if (aktif) Color.White else Color(0xFF1E293B),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp)
                    )
                }
            }
        }

        if (tabEditorAktif == "visual") {
            // MEJA RANGKAIAN BREADBOARD INTERAKTIF (GAMBAR RANGKAIAN BERUBAH TOTAL SESUAI PROYEK 1-8!)
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "🔩 ${proyekAktif.board}",
                            color = Color(0xFF0F172A),
                            fontSize = 13.5.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        Surface(
                            color = Color(0xFFFEF3C7),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.border(1.dp, Color(0xFFFDE68A), RoundedCornerShape(8.dp))
                        ) {
                            Text(
                                text = "115200 baud",
                                color = Color(0xFF92400E),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // KANVAS SKEMA RANGKAIAN SPESIFIK PER PROYEK (1 S.D. 8)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(255.dp)
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFFF8FAFC))
                            .border(2.dp, Color(0xFF94A3B8), RoundedCornerShape(18.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            gambarRangkaianWokwiSpesifikPerProyek(
                                nomorProyek = proyekAktif.nomor,
                                running = simulasiBerjalan,
                                faseDerajat = if (simulasiBerjalan) faseAnimasi else 0f,
                                detakDetik = detakDetik,
                                lux = nilaiCahayaLux,
                                suhu = nilaiSuhuDht,
                                tanah = nilaiLembabTanah,
                                jarakCm = nilaiJarakSonar,
                                sudutServo = sudutServo,
                                gasPpm = kadarGasPpm,
                                solenoidUnlock = relaySolenoidOn,
                                pompaOn = pompaNyala
                            )
                        }
                    }

                    // Komponen Aktif pada Proyek Ini
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        proyekAktif.komponenUtama.forEach { komp ->
                            Surface(
                                color = Color(0xFFF0FDF4),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.border(1.dp, Color(0xFF86EFAC), RoundedCornerShape(10.dp))
                            ) {
                                Text(
                                    text = "✓ $komp",
                                    color = Color(0xFF065F46),
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    softWrap = false,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                                )
                            }
                        }
                    }
                }
            }

            // PANEL KONTROL SENSOR DINAMIS SESUAI PROYEK YANG DIPILIH
            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color.White,
                shadowElevation = 4.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🎛️ Ubah Nilai Sensor & Lihat Animasi Rangkaian di Atas:",
                        color = Color(0xFF0F172A),
                        fontSize = 13.5.sp,
                        fontWeight = FontWeight.Black
                    )

                    // Tampilkan kontrol slider yang relevan dengan proyek maupun umum
                    if (proyekAktif.nomor in listOf(1, 5, 8)) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("☀️ Intensitas Cahaya LDR:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                Text(
                                    text = "${nilaiCahayaLux.roundToInt()} Lux (${if (lampuJalanNyala) "Gelap -> Lampu ON 💡" else "Terang -> OFF"})",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (lampuJalanNyala) Color(0xFFD97706) else Color(0xFF059669)
                                )
                            }
                            Slider(value = nilaiCahayaLux, onValueChange = { nilaiCahayaLux = it }, valueRange = 0f..1000f)
                        }
                    }

                    if (proyekAktif.nomor in listOf(2, 8)) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("⚙️ Sudut Putar Motor Servo SG90:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                Text("${sudutServo.roundToInt()}°", fontSize = 11.5.sp, fontWeight = FontWeight.Black, color = Color(0xFF2563EB))
                            }
                            Slider(value = sudutServo, onValueChange = { sudutServo = it }, valueRange = 0f..180f)
                        }
                    }

                    if (proyekAktif.nomor in listOf(3, 5, 7)) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🌡️ Suhu Sensor DHT22 (Pin V0):", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                Text(
                                    text = "${"%.1f".format(nilaiSuhuDht)} °C (${if (kipasNyala) "Kipas Berputar 🌀" else "Normal"})",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (kipasNyala) Color(0xFFE11D48) else Color(0xFF2563EB)
                                )
                            }
                            Slider(value = nilaiSuhuDht, onValueChange = { nilaiSuhuDht = it }, valueRange = 15f..50f)
                        }
                    }

                    if (proyekAktif.nomor == 3) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🌱 Kelembaban Tanah (Soil Moisture):", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                Text(
                                    text = "${nilaiLembabTanah.roundToInt()}% (${if (nilaiLembabTanah < 40f) "Kering -> Pompa ON 💦" else "Lembab"})",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (nilaiLembabTanah < 40f) Color(0xFF0284C7) else Color(0xFF059669)
                                )
                            }
                            Slider(value = nilaiLembabTanah, onValueChange = { nilaiLembabTanah = it }, valueRange = 5f..100f)
                        }
                    }

                    if (proyekAktif.nomor in listOf(4, 5)) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("📡 Jarak Objek Ultrasonik HC-SR04:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                Text(
                                    text = "${nilaiJarakSonar.roundToInt()} cm (${if (alarmSonarBahaya) "ALARM BEEP! 🚨" else "Aman"})",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (alarmSonarBahaya) Color(0xFFE11D48) else Color(0xFF059669)
                                )
                            }
                            Slider(value = nilaiJarakSonar, onValueChange = { nilaiJarakSonar = it }, valueRange = 3f..180f)
                        }
                    }

                    if (proyekAktif.nomor == 7) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("🔥 Kadar Gas LPG / Asap MQ-2:", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF334155))
                                Text(
                                    text = "${kadarGasPpm.roundToInt()} PPM (${if (bahayaGasApi) "BAHAYA GAS! 🚨" else "Udara Bersih"})",
                                    fontSize = 11.5.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (bahayaGasApi) Color(0xFFE11D48) else Color(0xFF059669)
                                )
                            }
                            Slider(value = kadarGasPpm, onValueChange = { kadarGasPpm = it }, valueRange = 100f..1000f)
                        }
                    }

                    // Tombol Interaksi Cepat
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Button(
                            onClick = { relaySolenoidOn = !relaySolenoidOn },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (relaySolenoidOn) Color(0xFF059669) else Color(0xFF64748B)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (relaySolenoidOn) "🔓 Solenoid: UNLOCK" else "🔒 Solenoid: LOCK",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false
                            )
                        }

                        Button(
                            onClick = { relayPompaOn = !relayPompaOn },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (relayPompaOn) Color(0xFF0284C7) else Color(0xFF64748B)
                            ),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = if (relayPompaOn) "💦 Pompa: ON" else "⏹ Pompa: OFF",
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }
            }
        } else if (tabEditorAktif == "ino" || tabEditorAktif == "json") {
            val isiKode = if (tabEditorAktif == "ino") proyekAktif.kodeSketchIno else proyekAktif.diagramJson
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (tabEditorAktif == "ino") "📄 sketch.ino (C++ ESP32)" else "📐 diagram.json (Wokwi)",
                            color = Color(0xFFFACC15),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                clipboard.setText(AnnotatedString(isiKode))
                                Toast.makeText(context, "Kode disalin!", Toast.LENGTH_SHORT).show()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("📋 Salin", color = Color(0xFF022C22), fontSize = 11.sp, fontWeight = FontWeight.Black)
                        }
                    }
                    Text(
                        text = isiKode,
                        color = Color(0xFF34D399),
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = Color(0xFF090D16),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "📟 WOKWI SERIAL MONITOR (115200 BAUD)",
                        color = Color(0xFF34D399),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black
                    )
                    logSerialList.forEach { baris ->
                        Text(
                            text = baris,
                            color = Color(0xFFE2E8F0),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// MESIN PENGGAMBAR 8 SKEMA RANGKAIAN WOKWI SPESIFIK & ANIMASI ELEKTRON LIVE
// ============================================================================
fun DrawScope.gambarRangkaianWokwiSpesifikPerProyek(
    nomorProyek: Int,
    running: Boolean,
    faseDerajat: Float,
    detakDetik: Int,
    lux: Float,
    suhu: Float,
    tanah: Float,
    jarakCm: Float,
    sudutServo: Float,
    gasPpm: Float,
    solenoidUnlock: Boolean,
    pompaOn: Boolean
) {
    val w = size.width
    val h = size.height
    val rasioAnim = (faseDerajat % 360f) / 360f

    // 1. Grid Lubang Breadboard Halus
    for (gx in 18..w.toInt() step 24) {
        for (gy in 18..h.toInt() step 24) {
            drawCircle(
                color = Color(0xFFCBD5E1),
                radius = 1.8f,
                center = Offset(gx.toFloat(), gy.toFloat())
            )
        }
    }

    // Helper: Gambar Teks di dalam Canvas secara Presisi
    fun gambarTeksCanvas(teks: String, x: Float, y: Float, warnaHex: Int, ukuranPx: Float = 22f, rataTengah: Boolean = false) {
        drawContext.canvas.nativeCanvas.apply {
            val paint = AndroidPaint().apply {
                color = warnaHex
                textSize = ukuranPx
                typeface = Typeface.create(Typeface.MONOSPACE, Typeface.BOLD)
                isAntiAlias = true
                textAlign = if (rataTengah) AndroidPaint.Align.CENTER else AndroidPaint.Align.LEFT
            }
            drawText(teks, x, y, paint)
        }
    }

    // Helper: Gambar Kabel Jumper + Animasi Pulsa Elektron Mengalir
    fun gambarKabelJumperAnimasi(p1: Offset, p2: Offset, warnaKabel: Color, aktifMengalir: Boolean = true) {
        val midX = (p1.x + p2.x) / 2f
        val path = Path().apply {
            moveTo(p1.x, p1.y)
            cubicTo(midX, p1.y, midX, p2.y, p2.x, p2.y)
        }
        drawPath(path, color = warnaKabel, style = Stroke(width = 6.5f, cap = StrokeCap.Round))
        if (running && aktifMengalir) {
            val ex = p1.x + (p2.x - p1.x) * rasioAnim
            val ey = p1.y + (p2.y - p1.y) * rasioAnim
            drawCircle(color = Color(0xFFFDE047), radius = 6f, center = Offset(ex, ey))
        }
    }

    // Helper: Gambar Papan Mikrokontroler ESP32 DevKit V1 Lengkap Kaki Pin Emas & USB
    fun gambarBoardEsp32(x: Float, y: Float, bw: Float, bh: Float, labelBoard: String = "ESP32-WROOM") {
        // Kaki-kaki Pin Emas di Kiri & Kanan
        for (py in (y + 18f).toInt()..(y + bh - 18f).toInt() step 14) {
            drawRect(Color(0xFFFACC15), topLeft = Offset(x - 7f, py.toFloat()), size = Size(7f, 6f))
            drawRect(Color(0xFFFACC15), topLeft = Offset(x + bw, py.toFloat()), size = Size(7f, 6f))
        }
        // PCB Hitam ESP32
        drawRoundRect(
            color = Color(0xFF1E293B),
            topLeft = Offset(x, y),
            size = Size(bw, bh),
            cornerRadius = CornerRadius(14f, 14f)
        )
        // Kaleng Shield Wi-Fi Perak
        drawRoundRect(
            color = Color(0xFF94A3B8),
            topLeft = Offset(x + bw * 0.16f, y + 14f),
            size = Size(bw * 0.68f, bh * 0.36f),
            cornerRadius = CornerRadius(8f, 8f)
        )
        // Port Micro-USB Perak di Bawah
        drawRoundRect(
            color = Color(0xFFCBD5E1),
            topLeft = Offset(x + bw * 0.32f, y + bh - 8f),
            size = Size(bw * 0.36f, 12f),
            cornerRadius = CornerRadius(4f, 4f)
        )
        // LED Power Merah & LED TX Biru Berkedip
        drawCircle(color = Color(0xFFEF4444), radius = 5.5f, center = Offset(x + bw * 0.28f, y + bh * 0.80f))
        drawCircle(
            color = if (running && detakDetik % 2 == 0) Color(0xFF38BDF8) else Color(0xFF0369A1),
            radius = 6f,
            center = Offset(x + bw * 0.72f, y + bh * 0.80f)
        )
        gambarTeksCanvas(labelBoard, x + bw / 2f, y + bh * 0.64f, android.graphics.Color.WHITE, 19f, true)
    }

    // Helper: Gambar Layar OLED SSD1306 I2C dengan Teks Presisi di Dalam Layar Hitam!
    fun gambarLayarOledI2c(ox: Float, oy: Float, ow: Float, oh: Float, baris1: String, baris2: String, baris3: String) {
        drawRoundRect(
            color = Color(0xFF0284C7),
            topLeft = Offset(ox, oy),
            size = Size(ow, oh),
            cornerRadius = CornerRadius(12f, 12f)
        )
        // 4 Pin I2C (GND, VCC, SCL, SDA)
        for (i in 0..3) {
            drawCircle(Color(0xFFFACC15), radius = 4f, center = Offset(ox + ow * 0.30f + i * 16f, oy + 7f))
        }
        // Kaca Hitam OLED
        val kacaX = ox + 8f
        val kacaY = oy + 14f
        val kacaW = ow - 16f
        val kacaH = oh - 22f
        drawRoundRect(
            color = Color(0xFF090D16),
            topLeft = Offset(kacaX, kacaY),
            size = Size(kacaW, kacaH),
            cornerRadius = CornerRadius(6f, 6f)
        )
        gambarTeksCanvas(baris1, kacaX + 8f, kacaY + kacaH * 0.32f, android.graphics.Color.parseColor("#38BDF8"), 19f)
        gambarTeksCanvas(baris2, kacaX + 8f, kacaY + kacaH * 0.62f, android.graphics.Color.parseColor("#34D399"), 19f)
        gambarTeksCanvas(baris3, kacaX + 8f, kacaY + kacaH * 0.88f, android.graphics.Color.parseColor("#FACC15"), 19f)
    }

    when (nomorProyek) {
        // ====================================================================
        // PROYEK 1: LAMPU JALAN OTOMATIS LDR + MODUL RELAY + LAMPU AC MENYALA
        // ====================================================================
        1 -> {
            val malamNyala = lux < 300f
            gambarBoardEsp32(w * 0.05f, h * 0.14f, w * 0.25f, h * 0.72f)

            // Kabel Jumper ke LDR & Relay
            gambarKabelJumperAnimasi(Offset(w * 0.30f, h * 0.30f), Offset(w * 0.45f, h * 0.25f), Color(0xFF10B981))
            gambarKabelJumperAnimasi(Offset(w * 0.30f, h * 0.62f), Offset(w * 0.45f, h * 0.68f), Color(0xFFF97316), malamNyala)
            gambarKabelJumperAnimasi(Offset(w * 0.66f, h * 0.68f), Offset(w * 0.80f, h * 0.62f), Color(0xFFEF4444), malamNyala)

            // Modul Sensor LDR (Photoresistor) + Cahaya Matahari
            drawRoundRect(Color(0xFF1E3A8A), Offset(w * 0.44f, h * 0.14f), Size(w * 0.22f, h * 0.24f), CornerRadius(10f, 10f))
            drawCircle(Color(0xFFFCD34D), radius = 20f, center = Offset(w * 0.55f, h * 0.26f))
            drawLine(Color(0xFFB45309), Offset(w * 0.52f, h * 0.23f), Offset(w * 0.58f, h * 0.29f), strokeWidth = 3f)
            gambarTeksCanvas("LDR: ${lux.roundToInt()} lx", w * 0.55f, h * 0.11f, android.graphics.Color.parseColor("#0F172A"), 20f, true)

            // Modul Relay Biru 5V dengan Kontak Sakelar Animasi
            drawRoundRect(Color(0xFF1D4ED8), Offset(w * 0.44f, h * 0.52f), Size(w * 0.22f, h * 0.32f), CornerRadius(10f, 10f))
            drawCircle(if (malamNyala) Color(0xFF22C55E) else Color(0xFF64748B), radius = 6f, center = Offset(w * 0.48f, h * 0.58f))
            gambarTeksCanvas(if (malamNyala) "RELAY: ON" else "RELAY: OFF", w * 0.55f, h * 0.72f, android.graphics.Color.WHITE, 19f, true)

            // Bohlam Lampu Jalan Besar di Kanan (Berpendar Kuning Saat Malam!)
            val cxLampu = w * 0.83f
            val cyLampu = h * 0.42f
            if (malamNyala) {
                drawCircle(Color(0xFFFEE08B).copy(alpha = 0.45f), radius = 58f, center = Offset(cxLampu, cyLampu))
                drawCircle(Color(0xFFFDE047).copy(alpha = 0.75f), radius = 42f, center = Offset(cxLampu, cyLampu))
            }
            drawCircle(if (malamNyala) Color(0xFFFACC15) else Color(0xFFCBD5E1), radius = 30f, center = Offset(cxLampu, cyLampu))
            drawRoundRect(Color(0xFF475569), Offset(cxLampu - 14f, cyLampu + 26f), Size(28f, 18f), CornerRadius(4f, 4f))
            gambarTeksCanvas(if (malamNyala) "💡 LAMPU NYALA" else "LAMPU MATI", cxLampu, cyLampu + 65f, android.graphics.Color.parseColor("#0F172A"), 20f, true)
        }

        // ====================================================================
        // PROYEK 2: SMART DOOR LOCK RFID RC522 + MOTOR SERVO BERPUTAR + SOLENOID
        // ====================================================================
        2 -> {
            gambarBoardEsp32(w * 0.04f, h * 0.14f, w * 0.24f, h * 0.72f)
            gambarKabelJumperAnimasi(Offset(w * 0.28f, h * 0.28f), Offset(w * 0.39f, h * 0.28f), Color(0xFF06B6D4))
            gambarKabelJumperAnimasi(Offset(w * 0.28f, h * 0.55f), Offset(w * 0.68f, h * 0.32f), Color(0xFFF59E0B))
            gambarKabelJumperAnimasi(Offset(w * 0.28f, h * 0.72f), Offset(w * 0.68f, h * 0.74f), Color(0xFF10B981), solenoidUnlock)

            // Modul RFID RC522 Biru Tua + Gelombang Antena + Kartu E-KTP
            drawRoundRect(Color(0xFF0F172A), Offset(w * 0.38f, h * 0.14f), Size(w * 0.22f, h * 0.42f), CornerRadius(10f, 10f))
            drawCircle(Color(0xFF38BDF8), radius = 22f, center = Offset(w * 0.49f, h * 0.32f), style = Stroke(width = 2.5f))
            drawCircle(Color(0xFF38BDF8), radius = 12f, center = Offset(w * 0.49f, h * 0.32f), style = Stroke(width = 2.5f))
            gambarTeksCanvas("RFID RC522", w * 0.49f, h * 0.51f, android.graphics.Color.WHITE, 18f, true)

            // Kartu E-KTP Hijau/Biru di dekat RFID
            val geserKartu = if (running) sin(Math.toRadians(faseDerajat.toDouble())).toFloat() * 8f else 0f
            drawRoundRect(Color(0xFF0284C7), Offset(w * 0.39f + geserKartu, h * 0.62f), Size(w * 0.20f, h * 0.22f), CornerRadius(8f, 8f))
            drawRoundRect(Color(0xFFFACC15), Offset(w * 0.41f + geserKartu, h * 0.68f), Size(14f, 12f), CornerRadius(3f, 3f))
            gambarTeksCanvas("TAG E-KTP", w * 0.49f + geserKartu, h * 0.80f, android.graphics.Color.WHITE, 17f, true)

            // Motor Servo SG90 dengan Lengan (Horn) yang Berputar Sesuai Slider Sudut!
            val servoCx = w * 0.78f
            val servoCy = h * 0.28f
            drawRoundRect(Color(0xFF1D4ED8), Offset(servoCx - 38f, servoCy - 22f), Size(76f, 44f), CornerRadius(8f, 8f))
            rotate(degrees = sudutServo - 90f, pivot = Offset(servoCx, servoCy)) {
                drawRoundRect(
                    color = Color(0xFFF8FAFC),
                    topLeft = Offset(servoCx - 8f, servoCy - 48f),
                    size = Size(16f, 56f),
                    cornerRadius = CornerRadius(8f, 8f)
                )
                drawCircle(Color(0xFFEF4444), radius = 5f, center = Offset(servoCx, servoCy - 38f))
            }
            drawCircle(Color(0xFFFACC15), radius = 9f, center = Offset(servoCx, servoCy))
            gambarTeksCanvas("SERVO: ${sudutServo.roundToInt()}°", servoCx, servoCy + 42f, android.graphics.Color.parseColor("#0F172A"), 19f, true)

            // Solenoid Door Lock 12V dengan Batang Besi (Plunger) Keluar/Masuk
            val solX = w * 0.68f
            val solY = h * 0.64f
            drawRoundRect(Color(0xFF475569), Offset(solX, solY), Size(w * 0.20f, h * 0.22f), CornerRadius(8f, 8f))
            val panjangPlunger = if (solenoidUnlock) 12f else 34f
            drawRoundRect(
                color = if (solenoidUnlock) Color(0xFF10B981) else Color(0xFFEF4444),
                topLeft = Offset(solX + w * 0.20f, solY + 12f),
                size = Size(panjangPlunger, h * 0.10f),
                cornerRadius = CornerRadius(4f, 4f)
            )
            gambarTeksCanvas(if (solenoidUnlock) "🔓 UNLOCKED" else "🔒 LOCKED", solX + w * 0.10f, solY + h * 0.14f, android.graphics.Color.WHITE, 18f, true)
        }

        // ====================================================================
        // PROYEK 3: SMART GREENHOUSE DHT22 + SOIL MOISTURE + KIPAS + POMPA AIR
        // ====================================================================
        3 -> {
            val kipasPutar = suhu > 32f
            gambarBoardEsp32(w * 0.04f, h * 0.14f, w * 0.23f, h * 0.72f)
            gambarKabelJumperAnimasi(Offset(w * 0.27f, h * 0.25f), Offset(w * 0.38f, h * 0.25f), Color(0xFF10B981))
            gambarKabelJumperAnimasi(Offset(w * 0.27f, h * 0.68f), Offset(w * 0.38f, h * 0.72f), Color(0xFFD97706))
            gambarKabelJumperAnimasi(Offset(w * 0.56f, h * 0.28f), Offset(w * 0.72f, h * 0.28f), Color(0xFFEF4444), kipasPutar)
            gambarKabelJumperAnimasi(Offset(w * 0.56f, h * 0.72f), Offset(w * 0.72f, h * 0.72f), Color(0xFF0284C7), pompaOn)

            // Sensor Suhu & Kelembaban DHT22 Putih Berjaring
            drawRoundRect(Color.White, Offset(w * 0.38f, h * 0.12f), Size(w * 0.18f, h * 0.32f), CornerRadius(8f, 8f))
            drawRoundRect(Color(0xFF0284C7), Offset(w * 0.38f, h * 0.12f), Size(w * 0.18f, h * 0.32f), CornerRadius(8f, 8f), style = Stroke(width = 3f))
            gambarTeksCanvas("DHT22", w * 0.47f, h * 0.24f, android.graphics.Color.parseColor("#0F172A"), 19f, true)
            gambarTeksCanvas("${"%.1f".format(suhu)}°C", w * 0.47f, h * 0.36f, android.graphics.Color.parseColor("#E11D48"), 20f, true)

            // Garpu Soil Moisture Sensor di Bawah
            drawRoundRect(Color(0xFFB45309), Offset(w * 0.40f, h * 0.56f), Size(w * 0.04f, h * 0.30f), CornerRadius(4f, 4f))
            drawRoundRect(Color(0xFFB45309), Offset(w * 0.50f, h * 0.56f), Size(w * 0.04f, h * 0.30f), CornerRadius(4f, 4f))
            drawRoundRect(Color(0xFF1E293B), Offset(w * 0.38f, h * 0.52f), Size(w * 0.18f, h * 0.10f), CornerRadius(4f, 4f))
            gambarTeksCanvas("Tanah:${tanah.roundToInt()}%", w * 0.47f, h * 0.92f, android.graphics.Color.parseColor("#065F46"), 19f, true)

            // Kipas Pendingin 4-Baling Berputar di Kanan Atas
            val fanCx = w * 0.81f
            val fanCy = h * 0.28f
            drawCircle(Color(0xFF1E293B), radius = 36f, center = Offset(fanCx, fanCy))
            rotate(degrees = if (kipasPutar && running) faseDerajat * 3f else 0f, pivot = Offset(fanCx, fanCy)) {
                for (b in 0..3) {
                    rotate(degrees = b * 90f, pivot = Offset(fanCx, fanCy)) {
                        drawRoundRect(
                            color = if (kipasPutar) Color(0xFF38BDF8) else Color(0xFF94A3B8),
                            topLeft = Offset(fanCx - 7f, fanCy - 30f),
                            size = Size(14f, 26f),
                            cornerRadius = CornerRadius(7f, 7f)
                        )
                    }
                }
            }
            drawCircle(Color(0xFFFACC15), radius = 8f, center = Offset(fanCx, fanCy))
            gambarTeksCanvas(if (kipasPutar) "KIPAS: ON" else "KIPAS: OFF", fanCx, fanCy + 50f, android.graphics.Color.parseColor("#0F172A"), 18f, true)

            // Pompa Air Mini + Animasi Semprotan Tetesan Air Biru di Kanan Bawah
            val pumpX = w * 0.72f
            val pumpY = h * 0.64f
            drawRoundRect(Color(0xFF0284C7), Offset(pumpX, pumpY), Size(w * 0.16f, h * 0.22f), CornerRadius(10f, 10f))
            gambarTeksCanvas("POMPA", pumpX + w * 0.08f, pumpY + h * 0.13f, android.graphics.Color.WHITE, 18f, true)
            if (pompaOn && running) {
                for (d in 1..3) {
                    val dropX = pumpX + w * 0.17f + d * 12f + (rasioAnim * 14f)
                    val dropY = pumpY + 8f + d * 10f
                    drawCircle(Color(0xFF38BDF8), radius = 5f, center = Offset(dropX, dropY))
                }
            }
        }

        // ====================================================================
        // PROYEK 4: RADAR SONAR ULTRASONIK HC-SR04 + GELOMBANG + BUZZER + LED BAR
        // ====================================================================
        4 -> {
            val bahaya = jarakCm < 25f
            gambarBoardEsp32(w * 0.04f, h * 0.14f, w * 0.23f, h * 0.72f)
            gambarKabelJumperAnimasi(Offset(w * 0.27f, h * 0.32f), Offset(w * 0.36f, h * 0.32f), Color(0xFF0284C7))
            gambarKabelJumperAnimasi(Offset(w * 0.27f, h * 0.72f), Offset(w * 0.42f, h * 0.76f), Color(0xFFEF4444), bahaya)

            // Modul Ultrasonik HC-SR04 Biru dengan 2 Silinder Transduser (T & R)
            drawRoundRect(Color(0xFF1D4ED8), Offset(w * 0.36f, h * 0.14f), Size(w * 0.14f, h * 0.42f), CornerRadius(8f, 8f))
            drawCircle(Color(0xFFE2E8F0), radius = 16f, center = Offset(w * 0.43f, h * 0.25f))
            drawCircle(Color(0xFF475569), radius = 10f, center = Offset(w * 0.43f, h * 0.25f))
            drawCircle(Color(0xFFE2E8F0), radius = 16f, center = Offset(w * 0.43f, h * 0.45f))
            drawCircle(Color(0xFF475569), radius = 10f, center = Offset(w * 0.43f, h * 0.45f))

            // Gelombang Sonar Memancar & Objek Sasaran yang Bergeser Sesuai Jarak Cm!
            val posObjekX = (w * 0.56f + (jarakCm / 180f) * (w * 0.34f)).coerceIn(w * 0.55f, w * 0.92f)
            for (wIdx in 1..3) {
                val rWave = wIdx * 22f + (if (running) rasioAnim * 18f else 0f)
                if (w * 0.50f + rWave < posObjekX) {
                    drawArc(
                        color = if (bahaya) Color(0xFFEF4444) else Color(0xFF06B6D4),
                        startAngle = -45f,
                        sweepAngle = 90f,
                        useCenter = false,
                        topLeft = Offset(w * 0.50f - rWave, h * 0.35f - rWave),
                        size = Size(rWave * 2f, rWave * 2f),
                        style = Stroke(width = 3.5f)
                    )
                }
            }
            // Kotak Objek Sasaran
            drawRoundRect(
                color = if (bahaya) Color(0xFFEF4444) else Color(0xFF059669),
                topLeft = Offset(posObjekX, h * 0.18f),
                size = Size(w * 0.06f, h * 0.34f),
                cornerRadius = CornerRadius(6f, 6f)
            )
            gambarTeksCanvas("${jarakCm.roundToInt()} cm", posObjekX, h * 0.14f, android.graphics.Color.parseColor("#0F172A"), 20f, true)

            // Buzzer Piezo Hitam + LED Bar 8-Segmen di Bawah
            drawCircle(Color(0xFF0F172A), radius = 22f, center = Offset(w * 0.43f, h * 0.76f))
            drawCircle(Color(0xFF94A3B8), radius = 7f, center = Offset(w * 0.43f, h * 0.76f))
            if (bahaya && running) {
                drawCircle(Color(0xFFEF4444), radius = 28f + rasioAnim * 10f, center = Offset(w * 0.43f, h * 0.76f), style = Stroke(width = 3f))
            }

            // 8-Segmen LED Bar Graph
            val jmlLedNyala = ((180f - jarakCm) / 180f * 8f).toInt().coerceIn(1, 8)
            for (i in 0 until 8) {
                val warnaSegmen = if (i >= jmlLedNyala) Color(0xFFCBD5E1)
                else if (i < 3) Color(0xFF22C55E)
                else if (i < 6) Color(0xFFFACC15)
                else Color(0xFFEF4444)
                drawRoundRect(
                    color = warnaSegmen,
                    topLeft = Offset(w * 0.56f + i * 18f, h * 0.68f),
                    size = Size(13f, 32f),
                    cornerRadius = CornerRadius(3f, 3f)
                )
            }
        }

        // ====================================================================
        // PROYEK 5: DUAL-ESP32 TELEMETRY BRIDGE BLYNK CLOUD (2 ESP32 + CLOUD + OLED)
        // ====================================================================
        5 -> {
            // Node A: ESP32 Transmitter di Kiri
            gambarBoardEsp32(w * 0.04f, h * 0.14f, w * 0.23f, h * 0.72f, "NODE-A (TX)")
            // Node B: ESP32 Receiver di Kanan
            gambarBoardEsp32(w * 0.73f, h * 0.14f, w * 0.23f, h * 0.72f, "NODE-B (RX)")

            // Kabel Data Nirkabel menuju Broker Cloud di Tengah Atas
            gambarKabelJumperAnimasi(Offset(w * 0.27f, h * 0.32f), Offset(w * 0.42f, h * 0.26f), Color(0xFF10B981))
            gambarKabelJumperAnimasi(Offset(w * 0.58f, h * 0.26f), Offset(w * 0.73f, h * 0.32f), Color(0xFF38BDF8))

            // Broker Cloud Server di Tengah Atas
            drawRoundRect(Color(0xFF047857), Offset(w * 0.36f, h * 0.10f), Size(w * 0.28f, h * 0.30f), CornerRadius(16f, 16f))
            gambarTeksCanvas("☁️ CLOUD BROKER", w * 0.50f, h * 0.22f, android.graphics.Color.WHITE, 18f, true)
            gambarTeksCanvas("V0:${suhu.roundToInt()}C V1:${jarakCm.roundToInt()}cm", w * 0.50f, h * 0.34f, android.graphics.Color.parseColor("#FDE047"), 17f, true)

            // Layar OLED SSD1306 I2C di Tengah Bawah (Teks Presisi di Dalam Layar Hitam!)
            gambarLayarOledI2c(
                ox = w * 0.33f,
                oy = h * 0.48f,
                ow = w * 0.34f,
                oh = h * 0.42f,
                baris1 = "BRIDGE V0..V4 OK",
                baris2 = "TX:${"%.1f".format(suhu)}C L:${lux.roundToInt()}",
                baris3 = "RX:${jarakCm.roundToInt()}cm S:${sudutServo.roundToInt()}d"
            )
        }

        // ====================================================================
        // PROYEK 6: SMART TRAFFIC LIGHT 3-FASE + 7-SEGMENT TIMER
        // ====================================================================
        6 -> {
            val faseLampu = detakDetik % 3 // 0=Hijau, 1=Kuning, 2=Merah
            gambarBoardEsp32(w * 0.05f, h * 0.14f, w * 0.24f, h * 0.72f)
            gambarKabelJumperAnimasi(Offset(w * 0.29f, h * 0.28f), Offset(w * 0.45f, h * 0.28f), Color(0xFFEF4444), faseLampu == 2)
            gambarKabelJumperAnimasi(Offset(w * 0.29f, h * 0.48f), Offset(w * 0.45f, h * 0.48f), Color(0xFFFACC15), faseLampu == 1)
            gambarKabelJumperAnimasi(Offset(w * 0.29f, h * 0.68f), Offset(w * 0.45f, h * 0.68f), Color(0xFF22C55E), faseLampu == 0)

            // Tiang Kotak Lampu Lalu Lintas Hitam
            drawRoundRect(Color(0xFF0F172A), Offset(w * 0.44f, h * 0.10f), Size(w * 0.16f, h * 0.80f), CornerRadius(18f, 18f))
            drawCircle(if (faseLampu == 2) Color(0xFFEF4444) else Color(0xFF450A0A), radius = 20f, center = Offset(w * 0.52f, h * 0.26f))
            drawCircle(if (faseLampu == 1) Color(0xFFFACC15) else Color(0xFF422006), radius = 20f, center = Offset(w * 0.52f, h * 0.50f))
            drawCircle(if (faseLampu == 0) Color(0xFF22C55E) else Color(0xFF052E16), radius = 20f, center = Offset(w * 0.52f, h * 0.74f))

            // Layar Hitung Mundur 7-Segment di Kanan
            drawRoundRect(Color(0xFF1E293B), Offset(w * 0.68f, h * 0.24f), Size(w * 0.24f, h * 0.52f), CornerRadius(14f, 14f))
            val angkaCountdown = 9 - (detakDetik % 9)
            gambarTeksCanvas("0$angkaCountdown", w * 0.80f, h * 0.56f, android.graphics.Color.parseColor("#34D399"), 48f, true)
            gambarTeksCanvas("TIMER DETIK", w * 0.80f, h * 0.70f, android.graphics.Color.WHITE, 17f, true)
        }

        // ====================================================================
        // PROYEK 7: DETEKTOR GAS BOCOR MQ-2 + FLAME API + EXHAUST FAN + SIRINE
        // ====================================================================
        7 -> {
            val bahayaGas = gasPpm > 500f
            gambarBoardEsp32(w * 0.04f, h * 0.14f, w * 0.24f, h * 0.72f)
            gambarKabelJumperAnimasi(Offset(w * 0.28f, h * 0.35f), Offset(w * 0.42f, h * 0.35f), Color(0xFFF97316))
            gambarKabelJumperAnimasi(Offset(w * 0.58f, h * 0.35f), Offset(w * 0.74f, h * 0.35f), Color(0xFFEF4444), bahayaGas)

            // Sensor Tabung Jaring Logam MQ-2 Gas
            drawRoundRect(Color(0xFF991B1B), Offset(w * 0.40f, h * 0.18f), Size(w * 0.20f, h * 0.40f), CornerRadius(10f, 10f))
            drawCircle(Color(0xFFCBD5E1), radius = 24f, center = Offset(w * 0.50f, h * 0.36f))
            gambarTeksCanvas("MQ-2 GAS", w * 0.50f, h * 0.53f, android.graphics.Color.WHITE, 18f, true)
            gambarTeksCanvas("${gasPpm.roundToInt()} PPM", w * 0.50f, h * 0.74f, android.graphics.Color.parseColor("#DC2626"), 22f, true)

            // Kipas Exhaust Pembuangan & Sirine Merah di Kanan
            val exCx = w * 0.82f
            val exCy = h * 0.45f
            drawCircle(if (bahayaGas) Color(0xFFDC2626) else Color(0xFF334155), radius = 40f, center = Offset(exCx, exCy))
            rotate(degrees = if (bahayaGas && running) faseDerajat * 4f else 0f, pivot = Offset(exCx, exCy)) {
                for (b in 0..3) {
                    rotate(degrees = b * 90f, pivot = Offset(exCx, exCy)) {
                        drawRoundRect(Color.White, Offset(exCx - 6f, exCy - 32f), Size(12f, 28f), CornerRadius(6f, 6f))
                    }
                }
            }
            gambarTeksCanvas(if (bahayaGas) "🚨 SIRINE & EXHAUST" else "STANDBY AMAN", exCx, exCy + 58f, android.graphics.Color.parseColor("#0F172A"), 18f, true)
        }

        // ====================================================================
        // PROYEK 8: SOLAR TRACKER PANEL SURYA OTOMATIS + INA219 POWER METER
        // ====================================================================
        else -> {
            gambarBoardEsp32(w * 0.04f, h * 0.14f, w * 0.23f, h * 0.72f)
            gambarKabelJumperAnimasi(Offset(w * 0.27f, h * 0.35f), Offset(w * 0.46f, h * 0.45f), Color(0xFFFACC15))
            gambarKabelJumperAnimasi(Offset(w * 0.27f, h * 0.65f), Offset(w * 0.68f, h * 0.65f), Color(0xFF10B981))

            // Matahari Bersinar di Atas
            val sunX = w * 0.35f + (lux / 1000f) * (w * 0.30f)
            drawCircle(Color(0xFFFACC15), radius = 22f, center = Offset(sunX, h * 0.16f))

            // Panel Surya Fotovoltaik Biru yang Miring Mengikuti Sudut Servo
            val panelCx = w * 0.48f
            val panelCy = h * 0.54f
            drawRect(Color(0xFF475569), topLeft = Offset(panelCx - 6f, panelCy), size = Size(12f, h * 0.32f))
            rotate(degrees = (sudutServo - 90f) * 0.45f, pivot = Offset(panelCx, panelCy)) {
                drawRoundRect(
                    color = Color(0xFF1D4ED8),
                    topLeft = Offset(panelCx - 58f, panelCy - 14f),
                    size = Size(116f, 28f),
                    cornerRadius = CornerRadius(6f, 6f)
                )
            }

            // Layar OLED Pengukur Daya INA219 di Kanan
            val dayaWatt = (lux * 0.018f).coerceAtLeast(0.5f)
            gambarLayarOledI2c(
                ox = w * 0.64f,
                oy = h * 0.38f,
                ow = w * 0.32f,
                oh = h * 0.44f,
                baris1 = "SOLAR INA219",
                baris2 = "Sudut: ${sudutServo.roundToInt()} deg",
                baris3 = "Daya: ${"%.1f".format(dayaWatt)} W"
            )
        }
    }
}

// ============================================================================
// 2. MULTI-LAB SIMULATOR (PYTHON, MATLAB, C++ & OSILOSKOP SINYAL WEB-SYNC)
// ============================================================================
@Composable
fun LayarSimulatorMultiLabWebSync(
    user: UserAccount,
    tokenAktif: String,
    onTambahXp: (Int) -> Unit
) {
    var bahasaAktif by remember { mutableStateOf("Python") }
    var frekuensiHz by remember { mutableStateOf(3.0f) }
    var amplitudoVolt by remember { mutableStateOf(5.0f) }
    var outputTerminal by remember {
        mutableStateOf(">>> HibahIlmu Multi-Lab Ready!\n>>> Pilih bahasa pemrograman & jalankan simulasi...")
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp,
            modifier = Modifier
                .fillMaxWidth()
                .border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(24.dp))
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "🔬 Multi-Lab: Osiloskop Sinyal, Python, MATLAB & C++",
                    color = Color(0xFF0F172A),
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Black
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Python", "MATLAB", "C++ Arduino", "SQL Database", "Web HTML/JS").forEach { bhs ->
                        val aktif = bahasaAktif == bhs
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (aktif) Color(0xFF059669) else Color(0xFFF1F5F9),
                            modifier = Modifier.clickable { bahasaAktif = bhs }
                        ) {
                            Text(
                                text = bhs,
                                color = if (aktif) Color.White else Color(0xFF1E293B),
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(145.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F172A))
                        .padding(8.dp)
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val w = size.width
                        val h = size.height
                        val midY = h / 2f
                        drawLine(Color(0xFF334155), Offset(0f, midY), Offset(w, midY), strokeWidth = 2f)

                        val pathGelombang = Path().apply {
                            moveTo(0f, midY)
                            val steps = 120
                            for (i in 0..steps) {
                                val x = (i.toFloat() / steps) * w
                                val rad = (i.toFloat() / steps) * frekuensiHz * 2f * Math.PI.toFloat()
                                val y = midY - sin(rad) * (amplitudoVolt / 10f) * (h * 0.42f)
                                lineTo(x, y)
                            }
                        }
                        drawPath(pathGelombang, color = Color(0xFF34D399), style = Stroke(width = 4.5f))
                    }
                    Text(
                        text = "📈 Osiloskop: ${"%.1f".format(frekuensiHz)} Hz | ${"%.1f".format(amplitudoVolt)} Vpp",
                        color = Color(0xFFFACC15),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Text(
                    text = "Frekuensi Gelombang (${"%.1f".format(frekuensiHz)} Hz):",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF334155)
                )
                Slider(value = frekuensiHz, onValueChange = { frekuensiHz = it }, valueRange = 1f..10f)

                Button(
                    onClick = {
                        onTambahXp(20)
                        outputTerminal = ">>> Menjalankan Compiler $bahasaAktif...\n" +
                                ">>> Frekuensi: ${"%.1f".format(frekuensiHz)} Hz | Amplitudo: ${"%.1f".format(amplitudoVolt)} V\n" +
                                ">>> [SUCCESS] Eksekusi berhasil (+20 XP)!"
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "▶ Jalankan Simulasi $bahasaAktif (+20 XP)",
                        color = Color.White,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false
                    )
                }

                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = outputTerminal,
                        color = Color(0xFF34D399),
                        fontSize = 11.5.sp,
                        fontFamily = FontFamily.Monospace,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }
        }
    }
}