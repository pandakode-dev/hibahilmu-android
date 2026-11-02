package id.my.hibahilmu.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.*
import kotlin.random.Random

@Composable
fun LayarSimulatorWokwiDanSensorLengkap(
    user: UserAccount,
    tokenAktif: String,
    onTambahXp: (Int) -> Unit
) {
    var subMenuWokwi by remember { mutableIntStateOf(0) } // 0: Wokwi Wiring & Sensor, 1: Editor Program / Wokwi Code

    // State Sensor Lingkungan & Kualitas Air (pH, TDS, Turbidity, BOD, COD, Detak Jantung)
    var phAir by remember { mutableFloatStateOf(7.0f) } // 0 - 14 pH
    var tdsPpm by remember { mutableFloatStateOf(150f) } // 0 - 1000 PPM
    var turbidityNtu by remember { mutableFloatStateOf(2.5f) } // 0 - 50 NTU
    var bodMgL by remember { mutableFloatStateOf(15f) } // mg/L
    var codMgL by remember { mutableFloatStateOf(30f) } // mg/L
    var bpmHeartRate by remember { mutableFloatStateOf(78f) } // Detak Jantung BPM

    // State Editor Wokwi / Firmware Code
    var kodeWokwiArduino by remember {
        mutableStateOf(
            "// Simulasi Wokwi ESP32 & Sensor Kualitas Air IoT\n" +
                    "#include <Wire.h>\n\n" +
                    "void setup() {\n" +
                    "  Serial.begin(115200);\n" +
                    "  pinMode(23, OUTPUT); // Relay Pompa / Pengolah Air\n" +
                    "}\n\n" +
                    "void loop() {\n" +
                    "  float ph = 7.0;\n" +
                    "  int turbidity = 3;\n" +
                    "  Serial.println(\"Sistem Filtrasi Air IoT Aktif...\");\n" +
                    "}"
        )
    }
    var terminalOutput by remember { mutableStateOf("Wokwi Simulator Siap. Rangkaian virtual ESP32 terhubung.") }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Navigasi Atas (Mirip Tampilan Wokwi Web)
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(
                    0 to "🎛️ Rangkaian Wokwi & Sensor",
                    1 to "💻 Wokwi Code Editor"
                ).forEach { (idx, judul) ->
                    val aktif = subMenuWokwi == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (aktif) Color(0xFF10B981) else Color.Transparent)
                            .clickable { subMenuWokwi = idx }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = judul,
                            color = if (aktif) Color(0xFF022C22) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        if (subMenuWokwi == 0) {
            // ================================================================
            // 1. WOKWI VIRTUAL WIRING & SENSOR KUALITAS AIR, MEDIS & LINGKUNGAN
            // ================================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(22.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "WOKWI VIRTUAL LAB • ESP32 & SENSOR",
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                "Simulasi Kualitas Air & Medis Akurat",
                                color = Color.White,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Surface(
                            color = Color(0xFF10B981),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "🟢 RUNNING",
                                color = Color(0xFF022C22),
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    // KANVAS VISUALISASI GRAFIK EKG / DETAK JANTUNG (MAX30100)
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(130.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF020617))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                            .padding(8.dp)
                    ) {
                        Column(modifier = Modifier.fillMaxSize()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("❤️ Sensor Detak Jantung (MAX30100)", color = Color(0xFFFB7185), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                Text("${bpmHeartRate.roundToInt()} BPM", color = Color(0xFFFB7185), fontSize = 12.sp, fontWeight = FontWeight.Black)
                            }
                            Canvas(modifier = Modifier.fillMaxWidth().weight(1f)) {
                                val w = size.width
                                val h = size.height
                                val path = Path()
                                val step = w / 100f
                                for (i in 0..100) {
                                    val x = i * step
                                    val normalWave = sin(i * 0.15f + System.currentTimeMillis() * 0.015f) * 15f
                                    // Beri denyut EKG tajam
                                    val spike = if (i % 25 == 0) 45f else 0f
                                    val y = (h / 2f) - normalWave - spike
                                    if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
                                }
                                drawPath(path, color = Color(0xFFFB7185), style = Stroke(width = 3f))
                            }
                        }
                    }

                    Text("Atur Slider Detak Jantung (BPM):", color = Color(0xFF94A3B8), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Slider(value = bpmHeartRate, onValueChange = { bpmHeartRate = it }, valueRange = 50f..160f)
                }
            }

            // GRID SENSOR KUALITAS AIR (pH, TDS, Turbidity, BOD, COD)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // SENSOR pH AIR
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, if (phAir in 6.5f..8.5f) Color(0xFF10B981) else Color(0xFFEF4444), RoundedCornerShape(20.dp))
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("SENSING pH AIR", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text(String.format("%.1f pH", phAir), color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text(
                            if (phAir in 6.5f..8.5f) "Normal / Layak" else "Berbahaya!",
                            color = if (phAir in 6.5f..8.5f) Color(0xFF34D399) else Color(0xFFF87171),
                            fontSize = 9.5.sp
                        )
                        Slider(value = phAir, onValueChange = { phAir = it }, valueRange = 0f..14f)
                    }

                    // SENSOR TDS AIR
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF38BDF8), RoundedCornerShape(20.dp))
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("TDS KEKERASAN", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text("${tdsPpm.roundToInt()} PPM", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text(if (tdsPpm < 300f) "Air Minum Baik" else "TDS Tinggi", color = Color(0xFF94A3B8), fontSize = 9.5.sp)
                        Slider(value = tdsPpm, onValueChange = { tdsPpm = it }, valueRange = 0f..1000f)
                    }
                }
            }

            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("🌊 Turbidity (Kekeruhan Air): ${String.format("%.1f", turbidityNtu)} NTU", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Slider(value = turbidityNtu, onValueChange = { turbidityNtu = it }, valueRange = 0f..50f)

                    Text("🧪 BOD (Biochemical Oxygen Demand): ${bodMgL.roundToInt()} mg/L", color = Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Slider(value = bodMgL, onValueChange = { bodMgL = it }, valueRange = 0f..100f)

                    Text("⚗️ COD (Chemical Oxygen Demand): ${codMgL.roundToInt()} mg/L", color = Color(0xFFA5B4FC), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Slider(value = codMgL, onValueChange = { codMgL = it }, valueRange = 0f..200f)
                }
            }
        } else {
            // ================================================================
            // 2. WOKWI CODE EDITOR & SERIAL MONITOR DI ANDROID
            // ================================================================
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(22.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("💻 Wokwi Arduino C++ Firmware Editor", color = Color(0xFF34D399), fontSize = 13.sp, fontWeight = FontWeight.Black)

                    OutlinedTextField(
                        value = kodeWokwiArduino,
                        onValueChange = { kodeWokwiArduino = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )

                    Button(
                        onClick = {
                            onTambahXp(30)
                            terminalOutput = "[WOKWI VIRTUAL SIMULATOR ESP32]\n" +
                                    "Compiling sketch for ESP32 Dev Kit...\n" +
                                    "Upload complete (100%).\n" +
                                    "-> pH Air Terdeteksi: ${String.format("%.1f", phAir)}\n" +
                                    "-> TDS Mineral: ${tdsPpm.roundToInt()} PPM\n" +
                                    "-> Kekeruhan Turbidity: ${String.format("%.1f", turbidityNtu)} NTU\n" +
                                    "-> Detak Jantung Pasien: ${bpmHeartRate.roundToInt()} BPM\n" +
                                    "Simulasi Berhasil! (+30 XP ditambahkan)."
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF022C22))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Compile & Run Wokwi (+30 XP)", color = Color(0xFF022C22), fontWeight = FontWeight.Black)
                    }

                    Surface(
                        color = Color(0xFF020617),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = terminalOutput,
                            color = Color(0xFFFBBF24),
                            fontSize = 11.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(14.dp)
                        )
                    }
                }
            }
        }
    }
}