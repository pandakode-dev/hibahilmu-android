package id.my.hibahilmu.app

import androidx.compose.animation.core.*
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

@Composable
fun LayarSimulatorMultiLabAndroid(
    user: UserAccount,
    tokenAktif: String,
    onTambahXp: (Int) -> Unit
) {
    val scope = rememberCoroutineScope()
    var subTabSimulator by remember { mutableStateOf(0) } // 0: IoT Sensor Animasi, 1: MATLAB, 2: C++ Arduino, 3: Python

    // State Seluruh Sensor IoT
    var jarakOrangCm by remember { mutableStateOf(90f) } // 5..400 cm
    var pirGerakanAktif by remember { mutableStateOf(true) }
    var suhuCelcius by remember { mutableStateOf(33f) }
    var kelembapanTanah by remember { mutableStateOf(35f) }
    var cahayaLux by remember { mutableStateOf(220f) }
    var gasPpm by remember { mutableStateOf(300f) }
    var sudutServo by remember { mutableStateOf(90f) }

    // Animasi Berkelanjutan untuk Kaki Orang Berjalan, Gelombang Radar & Kipas
    val infiniteTransition = rememberInfiniteTransition(label = "iot_anim")
    val faseLangkahKaki by infiniteTransition.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(550, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "langkah_orang"
    )
    val rotasiKipas by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "kipas_spin"
    )

    // State Editor Multi-Bahasa (MATLAB, C++, Python)
    var kodeMatlab by remember {
        mutableStateOf(
            "% Simulasi Matriks & Filter Sinyal Sensor di MATLAB\n" +
                    "A = [4 2; 1 3];\n" +
                    "det_A = det(A);\n" +
                    "eig_A = eig(A);\n" +
                    "disp('Determinan Matriks A = 10');"
        )
    }
    var kodeCpp by remember {
        mutableStateOf(
            "#include <iostream>\nusing namespace std;\n\nint main() {\n" +
                    "    int jarak = 90;\n" +
                    "    cout << \"Radar Ultrasonik: \" << jarak << \" cm\" << endl;\n" +
                    "    return 0;\n}"
        )
    }
    var kodePython by remember {
        mutableStateOf(
            "# Analisis Sensor & Machine Learning Python\n" +
                    "suhu = [30.5, 31.2, 33.0, 34.5]\n" +
                    "rata_rata = sum(suhu) / len(suhu)\n" +
                    "print(f'Rata-rata Suhu DHT22: {rata_rata:.2f} C')"
        )
    }
    var outputTerminalLab by remember {
        mutableStateOf("Terminal Siap. Pilih tab bahasa & tekan tombol 'Jalankan Simulasi'.")
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Navigasi Sub-Tab Lab Simulator
        item {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF0F172A))
                    .padding(6.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    0 to "📡 IoT Sensor",
                    1 to "📊 MATLAB",
                    2 to "⚡ C++ / Ino",
                    3 to "🐍 Python"
                ).forEach { (idx, judul) ->
                    val aktif = subTabSimulator == idx
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (aktif) Color(0xFF10B981) else Color.Transparent)
                            .clickable { subTabSimulator = idx }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = judul,
                            color = if (aktif) Color(0xFF022C22) else Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
        }

        // ====================================================================
        // SUB-TAB 0: ANIMASI LENGKAP SENSOR IOT (ORANG BERJALAN DI PIR & SONAR)
        // ====================================================================
        if (subTabSimulator == 0) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.5.dp, Color(0xFF38BDF8), RoundedCornerShape(22.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                "ANIMASI ORANG BERJALAN (PIR & ULTRASONIK)",
                                color = Color(0xFF38BDF8),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black
                            )
                            Text(
                                "Jarak Objek: ${jarakOrangCm.roundToInt()} cm",
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black
                            )
                        }
                        Surface(
                            color = if (pirGerakanAktif || jarakOrangCm < 150f) Color(0xFFE11D48) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.clickable { pirGerakanAktif = !pirGerakanAktif }
                        ) {
                            Text(
                                text = if (pirGerakanAktif || jarakOrangCm < 150f) "🚶 PIR: ADA GERAKAN!" else "PIR: STANDBY",
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }

                    // KANVAS GAMBAR ANIMASI MODUL SENSOR, RADAR & ORANG BERJALAN
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(175.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(Color(0xFF020617))
                            .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(16.dp))
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val w = size.width
                            val h = size.height
                            val sensorX = 55f
                            val sensorY = h / 2f

                            // 1. Pancaran Kerucut Inframerah PIR
                            val pirPath = Path().apply {
                                moveTo(sensorX, sensorY)
                                lineTo(w, 15f)
                                lineTo(w, h - 15f)
                                close()
                            }
                            drawPath(
                                path = pirPath,
                                color = if (pirGerakanAktif || jarakOrangCm < 150f)
                                    Color(0x33F43F5E) else Color(0x1810B981)
                            )

                            // 2. Gambar Modul Sensor di Kiri
                            drawRoundRect(
                                color = Color(0xFF1E1B4B),
                                topLeft = Offset(12f, sensorY - 45f),
                                size = androidx.compose.ui.geometry.Size(42f, 90f),
                                cornerRadius = androidx.compose.ui.geometry.CornerRadius(12f, 12f)
                            )
                            drawCircle(
                                color = if (pirGerakanAktif) Color(0xFFF43F5E) else Color(0xFF10B981),
                                radius = 11f,
                                center = Offset(33f, sensorY)
                            )

                            // 3. Hitung Posisi X Karakter Orang Berjalan berdasarkan jarakOrangCm (5..400cm)
                            val rasioJarak = (jarakOrangCm / 400f).coerceIn(0.12f, 0.90f)
                            val orangX = w * rasioJarak
                            val orangY = sensorY + 10f

                            // 4. Gelombang Ultrasonik antara Sensor dan Orang
                            for (i in 1..3) {
                                val waveX = sensorX + ((orangX - sensorX) * (i / 4f))
                                drawArc(
                                    color = Color(0xFF38BDF8),
                                    startAngle = -45f,
                                    sweepAngle = 90f,
                                    useCenter = false,
                                    topLeft = Offset(waveX - 20f, sensorY - 35f),
                                    size = androidx.compose.ui.geometry.Size(40f, 70f),
                                    style = Stroke(width = 3.5f)
                                )
                            }

                            // 5. Gambar Siluet Orang Berjalan (Kepala, Badan, Tangan & Kaki Mengayun)
                            val warnaOrang = if (pirGerakanAktif || jarakOrangCm < 150f)
                                Color(0xFFFB7185) else Color(0xFF38BDF8)

                            // Kepala
                            drawCircle(
                                color = warnaOrang,
                                radius = 14f,
                                center = Offset(orangX, orangY - 48f)
                            )
                            // Badan
                            drawLine(
                                color = warnaOrang,
                                start = Offset(orangX, orangY - 34f),
                                end = Offset(orangX, orangY + 8f),
                                strokeWidth = 8f,
                                cap = StrokeCap.Round
                            )
                            // Ayunan Tangan Kiri & Kanan
                            val ayun = faseLangkahKaki * 18f
                            drawLine(
                                color = Color.White,
                                start = Offset(orangX, orangY - 24f),
                                end = Offset(orangX - ayun, orangY - 2f),
                                strokeWidth = 5.5f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = Color(0xFF94A3B8),
                                start = Offset(orangX, orangY - 24f),
                                end = Offset(orangX + ayun, orangY - 2f),
                                strokeWidth = 5.5f,
                                cap = StrokeCap.Round
                            )
                            // Langkah Kaki Kiri & Kanan Berjalan
                            drawLine(
                                color = warnaOrang,
                                start = Offset(orangX, orangY + 8f),
                                end = Offset(orangX + ayun, orangY + 44f),
                                strokeWidth = 6.5f,
                                cap = StrokeCap.Round
                            )
                            drawLine(
                                color = warnaOrang,
                                start = Offset(orangX, orangY + 8f),
                                end = Offset(orangX - ayun, orangY + 44f),
                                strokeWidth = 6.5f,
                                cap = StrokeCap.Round
                            )
                        }
                    }

                    Text(
                        "Geser Slider untuk Menggerakkan Orang Mendekati / Menjauhi Sensor:",
                        color = Color(0xFF94A3B8),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Slider(
                        value = jarakOrangCm,
                        onValueChange = {
                            jarakOrangCm = it
                            if (it < 150f) pirGerakanAktif = true
                        },
                        onValueChangeFinished = {
                            scope.launch {
                                IotCloudApiService.kirimPerubahanPinKeServer(
                                    tokenAktif,
                                    user.nama,
                                    "Android Sensor Simulator",
                                    "V1",
                                    jarakOrangCm.roundToInt()
                                )
                            }
                        },
                        valueRange = 5f..400f
                    )
                }
            }

            // KARTU SENSOR SUHU DHT22 + KIPAS BERPUTAR & KELEMBAPAN TANAH
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(20.dp))
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("SUHU DHT22 & KIPAS", color = Color(0xFFFBBF24), fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text("${suhuCelcius.roundToInt()} °C", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        Icon(
                            imageVector = Icons.Default.Toys,
                            contentDescription = "Kipas",
                            tint = if (suhuCelcius >= 32f) Color(0xFF38BDF8) else Color(0xFF475569),
                            modifier = Modifier
                                .size(42.dp)
                                .rotate(if (suhuCelcius >= 32f) rotasiKipas else 0f)
                        )
                        Text(
                            if (suhuCelcius >= 32f) "Kipas ON (Berputar)" else "Kipas Standby",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                        Slider(
                            value = suhuCelcius,
                            onValueChange = { suhuCelcius = it },
                            valueRange = -5f..55f
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF10B981), RoundedCornerShape(20.dp))
                            .padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("TANAH & POMPA AIR", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text("${kelembapanTanah.roundToInt()} %", color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                        Spacer(modifier = Modifier.height(6.dp))
                        Icon(
                            imageVector = Icons.Default.WaterDrop,
                            contentDescription = "Pompa Air",
                            tint = if (kelembapanTanah < 40f) Color(0xFF38BDF8) else Color(0xFF10B981),
                            modifier = Modifier.size(42.dp)
                        )
                        Text(
                            if (kelembapanTanah < 40f) "💦 Pompa Menyiram!" else "Tanah Lembap",
                            color = Color(0xFF94A3B8),
                            fontSize = 10.sp
                        )
                        Slider(
                            value = kelembapanTanah,
                            onValueChange = { kelembapanTanah = it },
                            valueRange = 0f..100f
                        )
                    }
                }
            }

            // KARTU SENSOR CAHAYA LDR, GAS MQ-2 & MOTOR SERVO SG90
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF6366F1), RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        "☀️ Cahaya LDR (${cahayaLux.roundToInt()} Lux) • ${if (cahayaLux < 300f) "Lampu Jalan MENYALA" else "Lampu Jalan MATI"}",
                        color = Color(0xFFFDE047),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Slider(value = cahayaLux, onValueChange = { cahayaLux = it }, valueRange = 0f..1000f)

                    Text(
                        "🔥 Sensor Gas & Asap MQ-2 (${gasPpm.roundToInt()} PPM) • ${if (gasPpm > 450f) "BAHAYA ASAP TERDETEKSI!" else "Udara Aman"}",
                        color = if (gasPpm > 450f) Color(0xFFF87171) else Color(0xFF34D399),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Slider(value = gasPpm, onValueChange = { gasPpm = it }, valueRange = 100f..1000f)

                    Text(
                        "⚙️ Sudut Lengan Motor Servo SG90: ${sudutServo.roundToInt()}°",
                        color = Color(0xFFA5B4FC),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Black
                    )
                    Slider(value = sudutServo, onValueChange = { sudutServo = it }, valueRange = 0f..180f)
                }
            }
        } else {
            // ====================================================================
            // SUB-TAB 1, 2, 3: SIMULATOR MATLAB, C++ ARDUINO, & PYTHON DI ANDROID
            // ====================================================================
            item {
                val judulLab = when (subTabSimulator) {
                    1 -> "📊 MATLAB / Octave Numerical & Matrix Simulator"
                    2 -> "⚡ C++20 & Arduino Firmware Compiler"
                    else -> "🐍 Python 3 Data Science & IoT Interpreter"
                }
                val nilaiKode = when (subTabSimulator) {
                    1 -> kodeMatlab
                    2 -> kodeCpp
                    else -> kodePython
                }

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(22.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(judulLab, color = Color(0xFF34D399), fontSize = 13.sp, fontWeight = FontWeight.Black)

                    OutlinedTextField(
                        value = nilaiKode,
                        onValueChange = { baru ->
                            when (subTabSimulator) {
                                1 -> kodeMatlab = baru
                                2 -> kodeCpp = baru
                                else -> kodePython = baru
                            }
                        },
                        label = { Text("Source Code Editor") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )

                    Button(
                        onClick = {
                            onTambahXp(25)
                            outputTerminalLab = when (subTabSimulator) {
                                1 -> "[MATLAB ENGINE ANDROID]\n>> menjalankan script matriks...\nA = [4 2; 1 3]\nDeterminan Matriks A = 10\nEigenvalues = [5.000, 2.000]\nData Sensor Saat Ini: Jarak=${jarakOrangCm.roundToInt()}cm, Suhu=${suhuCelcius.roundToInt()}C"
                                2 -> "[G++ / ARDUINO COMPILER]\nCompiling sketch... Done (0 errors).\nRadar Ultrasonik: ${jarakOrangCm.roundToInt()} cm\nPIR Status: ${if (pirGerakanAktif) "HIGH (MOTION)" else "LOW"}\nServo Angle: ${sudutServo.roundToInt()} deg"
                                else -> "[PYTHON 3.12 ANDROID RUNTIME]\nRata-rata Suhu DHT22: ${suhuCelcius.roundToInt()}.00 C\nKelembapan Tanah: ${kelembapanTanah.roundToInt()}%\nEksekusi selesai (+25 XP)."
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color(0xFF022C22))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Jalankan Kode & Simulasi (+25 XP)", color = Color(0xFF022C22), fontWeight = FontWeight.Black)
                    }

                    Surface(
                        color = Color(0xFF020617),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = outputTerminalLab,
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