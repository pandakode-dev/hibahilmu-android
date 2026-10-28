package id.my.hibahilmu.app

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.WebChromeClient
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(
                colorScheme = darkColorScheme(
                    primary = Color(0xFF10B981),
                    secondary = Color(0xFF6366F1),
                    background = Color(0xFF090D16),
                    surface = Color(0xFF0F172A)
                )
            ) {
                AplikasiUtamaHibahIlmuAndroid()
            }
        }
    }
}

@Composable
fun AplikasiUtamaHibahIlmuAndroid() {
    val context = LocalContext.current
    var tampilSplash by remember { mutableStateOf(true) }
    var userAktif by remember { mutableStateOf<UserAccount?>(null) }

    LaunchedEffect(Unit) {
        userAktif = AuthAndTokenManager.ambilSesiLoginAktif(context)
        delay(1200)
        tampilSplash = false
    }

    if (tampilSplash) {
        SplashScreenHibahIlmu()
    } else if (userAktif == null) {
        // GERBANG WAJIB LOGIN & REGISTER SEBELUM BISA AKSES FITUR APAPUN
        LayarWajibLoginDanRegister(
            onBerhasilMasuk = { akun ->
                userAktif = akun
            }
        )
    } else {
        // DASHBOARD UTAMA SETELAH LOGIN
        LayarUtamaSetelahLogin(
            user = userAktif!!,
            onLogout = {
                AuthAndTokenManager.simpanSesiLogin(context, null)
                userAktif = null
            }
        )
    }
}

@Composable
fun SplashScreenHibahIlmu() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF022C22))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = RoundedCornerShape(28.dp),
                color = Color(0xFF10B981).copy(alpha = 0.2f),
                modifier = Modifier.size(92.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Memory,
                        contentDescription = "Logo",
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(52.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "HibahIlmu.my.id",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "WAKAF ILMU, SIMULATOR CODING & BLYNK IOT CLOUD",
                color = Color(0xFF34D399),
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(28.dp))
            CircularProgressIndicator(color = Color(0xFF10B981), strokeWidth = 3.dp)
        }
    }
}

// ============================================================================
// GERBANG WAJIB REGISTER & LOGIN (SISWA / PENGAJAR)
// ============================================================================
@Composable
fun LayarWajibLoginDanRegister(onBerhasilMasuk: (UserAccount) -> Unit) {
    val context = LocalContext.current
    var modeDaftarBaru by remember { mutableStateOf(false) }
    var pilihRole by remember { mutableStateOf("siswa") }

    var inputNama by remember { mutableStateOf("") }
    var inputEmail by remember { mutableStateOf("aisyah@siswa.id") }
    var inputPassword by remember { mutableStateOf("123456") }
    var inputInstansi by remember { mutableStateOf("SMAN 1 Pekanbaru") }
    var pesanError by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .padding(20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF0F172A))
                .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(28.dp))
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color(0xFF10B981).copy(alpha = 0.15f),
                shape = RoundedCornerShape(50),
                modifier = Modifier.padding(bottom = 10.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "AKSES TERPROTEKSI • WAJIB LOGIN AKUN",
                        color = Color(0xFF34D399),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Text(
                text = if (modeDaftarBaru) "Daftar Akun Baru HibahIlmu" else "Masuk ke HibahIlmu.my.id",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Sinkronisasi Project IoT, Token Blynk & Kelas Belajar",
                color = Color(0xFF94A3B8),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Pilihan Role: Siswa / Mahasiswa vs Guru / Pengajar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF020617))
                    .padding(4.dp)
            ) {
                listOf("siswa" to "🎓 Siswa / Mahasiswa", "guru" to "👨‍🏫 Guru / Pengajar").forEach { (kode, label) ->
                    val aktif = pilihRole == kode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (aktif) Color(0xFF10B981) else Color.Transparent)
                            .clickable {
                                pilihRole = kode
                                if (!modeDaftarBaru) {
                                    inputEmail = if (kode == "siswa") "aisyah@siswa.id" else "budi@guru.id"
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (aktif) Color(0xFF022C22) else Color(0xFF94A3B8),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (modeDaftarBaru) {
                OutlinedTextField(
                    value = inputNama,
                    onValueChange = { inputNama = it },
                    label = { Text("Nama Lengkap") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = inputInstansi,
                    onValueChange = { inputInstansi = it },
                    label = { Text("Asal Sekolah / Kampus") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = inputEmail,
                onValueChange = { inputEmail = it },
                label = { Text("Alamat Email") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = inputPassword,
                onValueChange = { inputPassword = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (pesanError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFF7F1D1D),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "⚠️ $pesanError",
                        color = Color(0xFFFECACA),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = {
                    pesanError = null
                    if (modeDaftarBaru) {
                        if (inputNama.isBlank() || inputEmail.isBlank() || inputPassword.length < 4) {
                            pesanError = "Mohon lengkapi nama, email, dan password minimal 4 karakter!"
                            return@Button
                        }
                        val akunBaru = UserAccount(
                            id = "usr-${System.currentTimeMillis()}",
                            nama = inputNama.trim(),
                            email = inputEmail.trim(),
                            password = inputPassword,
                            role = pilihRole,
                            instansi = inputInstansi.ifBlank { "HibahIlmu Indonesia" },
                            xp = 500
                        )
                        val res = AuthAndTokenManager.registrasiAkunBaru(context, akunBaru)
                        res.onSuccess {
                            Toast.makeText(context, "Registrasi Berhasil! Selamat datang ${it.nama}", Toast.LENGTH_SHORT).show()
                            onBerhasilMasuk(it)
                        }.onFailure {
                            pesanError = it.message
                        }
                    } else {
                        val res = AuthAndTokenManager.loginAkun(context, inputEmail, inputPassword, pilihRole)
                        res.onSuccess {
                            Toast.makeText(context, "Login Berhasil sebagai ${it.nama}!", Toast.LENGTH_SHORT).show()
                            onBerhasilMasuk(it)
                        }.onFailure {
                            pesanError = it.message
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    text = if (modeDaftarBaru) "Daftar & Masuk Sekarang" else "Masuk ke Aplikasi (Login)",
                    color = Color(0xFF022C22),
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            TextButton(onClick = {
                modeDaftarBaru = !modeDaftarBaru
                pesanError = null
            }) {
                Text(
                    text = if (modeDaftarBaru) "Sudah punya akun? Klik untuk Login" else "Belum punya akun? Daftar Akun Baru Gratis",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ============================================================================
// LAYAR UTAMA SETELAH LOGIN (3 TAB: REMOTE IOT BLYNK | WEB SIMULATOR | PROFIL)
// ============================================================================
@Composable
fun LayarUtamaSetelahLogin(user: UserAccount, onLogout: () -> Unit) {
    val context = LocalContext.current
    var tabAktif by remember { mutableStateOf(0) }

    val daftarProjectPreset = listOf(
        "proj-1" to "Proyek 1: Lampu Jalan LDR + Relay",
        "proj-2" to "Proyek 2: Smart Door Lock RFID + Solenoid",
        "proj-3" to "Proyek 3: Smart Greenhouse Pompa & Kipas",
        "proj-4" to "Proyek 4: Detektor Api & Gas MQ-2",
        "proj-5" to "Proyek 5: Dual-ESP32 Telemetry Bridge",
        "proj-6" to "Proyek 6: Otomasi Pabrik Relay 4-Ch + Servo 360"
    )
    var projectPilih by remember { mutableStateOf(daftarProjectPreset[4]) }

    val tokenOtomatis = remember(user.id, projectPilih.first) {
        AuthAndTokenManager.buatTokenBlynkUnik(user, projectPilih.first)
    }
    var tokenAktif by remember {
        mutableStateOf(AuthAndTokenManager.ambilTokenKustom(context) ?: tokenOtomatis)
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF10B981)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = Color(0xFF022C22)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "HibahIlmu.my.id",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 16.sp
                        )
                        Text(
                            text = "${user.nama} (${user.role.uppercase()})",
                            color = Color(0xFF34D399),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                IconButton(onClick = onLogout) {
                    Icon(
                        imageVector = Icons.Default.Logout,
                        contentDescription = "Keluar",
                        tint = Color(0xFFF87171)
                    )
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0F172A)) {
                NavigationBarItem(
                    selected = tabAktif == 0,
                    onClick = { tabAktif = 0 },
                    icon = { Icon(Icons.Default.SettingsRemote, contentDescription = null) },
                    label = { Text("Remote IoT Blynk", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = tabAktif == 1,
                    onClick = { tabAktif = 1 },
                    icon = { Icon(Icons.Default.School, contentDescription = null) },
                    label = { Text("Web & Simulator", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = tabAktif == 2,
                    onClick = { tabAktif = 2 },
                    icon = { Icon(Icons.Default.VpnKey, contentDescription = null) },
                    label = { Text("Token & Akun", fontSize = 11.sp, fontWeight = FontWeight.Bold) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0xFF090D16))
                .padding(innerPadding)
        ) {
            when (tabAktif) {
                0 -> LayarRemoteBlynkIotNative(
                    user = user,
                    tokenAktif = tokenAktif,
                    onUbahToken = { baru ->
                        tokenAktif = baru
                        AuthAndTokenManager.simpanTokenKustom(context, baru)
                    },
                    daftarProjectPreset = daftarProjectPreset,
                    projectPilih = projectPilih,
                    onPilihProject = { prj ->
                        projectPilih = prj
                        val tBaru = AuthAndTokenManager.buatTokenBlynkUnik(user, prj.first)
                        tokenAktif = tBaru
                        AuthAndTokenManager.simpanTokenKustom(context, tBaru)
                    }
                )
                1 -> LayarWebViewHibahIlmu()
                2 -> LayarProfilDanTokenAkun(
                    user = user,
                    tokenAktif = tokenAktif,
                    onLogout = onLogout
                )
            }
        }
    }
}

// ============================================================================
// TAB 1: REMOTE DASHBOARD BLYNK IOT NATIVE (SINKRON REAL-TIME KE HIBAHILMU.MY.ID)
// ============================================================================
@Composable
fun LayarRemoteBlynkIotNative(
    user: UserAccount,
    tokenAktif: String,
    onUbahToken: (String) -> Unit,
    daftarProjectPreset: List<Pair<String, String>>,
    projectPilih: Pair<String, String>,
    onPilihProject: (Pair<String, String>) -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var brokerRunning by remember { mutableStateOf(true) }
    var statusKoneksi by remember { mutableStateOf("Menghubungkan ke hibahilmu.my.id...") }
    var suhuV0 by remember { mutableStateOf(31.0) }
    var jarakV1 by remember { mutableStateOf(40.0) }
    var sakelarSolenoidV2 by remember { mutableStateOf(true) }
    var sakelarPompaV3 by remember { mutableStateOf(true) }
    var sudutServoV4 by remember { mutableStateOf(90f) }
    var logServer by remember { mutableStateOf<List<LogPesanIot>>(emptyList()) }

    // Polling Real-Time setiap 2 detik ke https://www.hibahilmu.my.id/api/iot/cloud
    LaunchedEffect(tokenAktif, brokerRunning) {
        while (brokerRunning) {
            val res = IotCloudApiService.ambilStatePinDariServer(tokenAktif)
            res.onSuccess { st ->
                statusKoneksi = "ONLINE • Terhubung ke hibahilmu.my.id (${st.terakhirUpdate})"
                st.virtualPins["V0"]?.let { suhuV0 = it }
                st.virtualPins["V1"]?.let { jarakV1 = it }
                st.statusAktuator["V1"]?.let { sakelarSolenoidV2 = it }
                st.statusAktuator["V2"]?.let { sakelarPompaV3 = it }
                st.virtualPins["V3"]?.let { sudutServoV4 = it.toFloat() }
                logServer = st.logPesan
            }.onFailure {
                statusKoneksi = "Mode Lokal Siaga (Pastikan /api/iot/cloud sudah di-deploy)"
            }
            delay(2200)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Kartu Status Broker & Token Project
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.5.dp, Color(0xFF10B981).copy(alpha = 0.4f), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "SERVER BROKER HIBAHILMU.MY.ID",
                            color = Color(0xFF34D399),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            text = statusKoneksi,
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Button(
                        onClick = { brokerRunning = !brokerRunning },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (brokerRunning) Color(0xFF10B981) else Color(0xFFEF4444)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = if (brokerRunning) "● RUNNING" else "⏹ STOPPED",
                            color = if (brokerRunning) Color(0xFF022C22) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Pilihan Project untuk Ganti Token Otomatis
                Text(
                    text = "Pilih Project Aktif (Token Berbeda Tiap Project):",
                    color = Color(0xFF94A3B8),
                    fontSize = 11.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    daftarProjectPreset.take(3).forEach { prj ->
                        val dipilih = projectPilih.first == prj.first
                        Surface(
                            color = if (dipilih) Color(0xFF4F46E5) else Color(0xFF1E293B),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onPilihProject(prj) }
                        ) {
                            Text(
                                text = prj.first.uppercase(),
                                color = Color.White,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(vertical = 8.dp, horizontal = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tokenAktif,
                    onValueChange = { onUbahToken(it) },
                    label = { Text("BLYNK AUTH TOKEN (Tempel Token dari Web / ESP32)") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = {
                            clipboard.setText(AnnotatedString(tokenAktif))
                            Toast.makeText(context, "Token berhasil disalin!", Toast.LENGTH_SHORT).show()
                        }) {
                            Icon(Icons.Default.ContentCopy, contentDescription = "Salin", tint = Color(0xFF34D399))
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // Baris Gauge Sensor V0 (Suhu DHT22) & V1 (Sonar Ultrasonik)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // V0: Suhu DHT22
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("VIRTUAL PIN V0", color = Color(0xFFFBBF24), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("${suhuV0.roundToInt()} °C", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                    Text("Suhu DHT22", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Slider(
                        value = suhuV0.toFloat(),
                        onValueChange = { suhuV0 = it.toDouble() },
                        onValueChangeFinished = {
                            scope.launch {
                                IotCloudApiService.kirimPerubahanPinKeServer(
                                    tokenAktif, user.nama, projectPilih.second, "V0", suhuV0.roundToInt()
                                )
                            }
                        },
                        valueRange = -10f..60f
                    )
                }

                // V1: Ultrasonik
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("VIRTUAL PIN V1", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("${jarakV1.roundToInt()} cm", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                    Text("Sonar HC-SR04", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Slider(
                        value = jarakV1.toFloat(),
                        onValueChange = { jarakV1 = it.toDouble() },
                        onValueChangeFinished = {
                            scope.launch {
                                IotCloudApiService.kirimPerubahanPinKeServer(
                                    tokenAktif, user.nama, projectPilih.second, "V1", jarakV1.roundToInt()
                                )
                            }
                        },
                        valueRange = 2f..400f
                    )
                }
            }
        }

        // Baris Sakelar Aktuator V2 (Solenoid Lock 12V) & V3 (Pompa Air Mini / Relay)
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Sakelar Solenoid Door Lock
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("PIN V1 • RELAY CH1", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Solenoid Lock 12V", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            sakelarSolenoidV2 = !sakelarSolenoidV2
                            scope.launch {
                                IotCloudApiService.kirimPerubahanPinKeServer(
                                    tokenAktif, user.nama, projectPilih.second, "V1", if (sakelarSolenoidV2) 1 else 0
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sakelarSolenoidV2) Color(0xFF10B981) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (sakelarSolenoidV2) "🔓 UNLOCK (ON)" else "🔒 LOCKED (OFF)",
                            color = if (sakelarSolenoidV2) Color(0xFF022C22) else Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }

                // Sakelar Pompa Air Mini
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                        .padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("PIN V2 • RELAY CH2", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Pompa Air Mini", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            sakelarPompaV3 = !sakelarPompaV3
                            scope.launch {
                                IotCloudApiService.kirimPerubahanPinKeServer(
                                    tokenAktif, user.nama, projectPilih.second, "V2", if (sakelarPompaV3) 1 else 0
                                )
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sakelarPompaV3) Color(0xFF0284C7) else Color(0xFF1E293B)
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(
                            text = if (sakelarPompaV3) "💦 POMPA ON" else "⏹ POMPA OFF",
                            color = Color.White,
                            fontWeight = FontWeight.Black,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        // Slider Motor Servo PWM (180 / 360)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("VIRTUAL PIN V3 • MOTOR SERVO PWM", color = Color(0xFFA5B4FC), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Text("${sudutServoV4.roundToInt()}°", color = Color(0xFFFBBF24), fontSize = 16.sp, fontWeight = FontWeight.Black)
                }
                Slider(
                    value = sudutServoV4,
                    onValueChange = { sudutServoV4 = it },
                    onValueChangeFinished = {
                        scope.launch {
                            IotCloudApiService.kirimPerubahanPinKeServer(
                                tokenAktif, user.nama, projectPilih.second, "V3", sudutServoV4.roundToInt()
                            )
                        }
                    },
                    valueRange = 0f..180f
                )
            }
        }

        // Log Lalu Lintas Paket Broker hibahilmu.my.id
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF020617))
                    .border(1.dp, Color(0xFF1E293B), RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Text(
                    text = "📡 LIVE TRAFFIC BROKER (https://www.hibahilmu.my.id/api/iot/cloud):",
                    color = Color(0xFFFBBF24),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(8.dp))
                if (logServer.isEmpty()) {
                    Text(
                        text = "Tekan sakelar di atas untuk mengirim paket ke server...",
                        color = Color(0xFF64748B),
                        fontSize = 11.sp,
                        fontFamily = FontFamily.Monospace
                    )
                } else {
                    logServer.take(6).forEach { lg ->
                        Text(
                            text = "[${lg.waktu}] [${lg.sumber}] ${lg.topik}: ${lg.payload}",
                            color = Color(0xFFCBD5E1),
                            fontSize = 10.sp,
                            fontFamily = FontFamily.Monospace,
                            modifier = Modifier.padding(vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}

// ============================================================================
// TAB 2: WEBVIEW PENUH KE HTTPS://WWW.HIBAHILMU.MY.ID (KELAS & SIMULATOR)
// ============================================================================
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun LayarWebViewHibahIlmu() {
    AndroidView(
        modifier = Modifier.fillMaxSize(),
        factory = { ctx ->
            WebView(ctx).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                settings.databaseEnabled = true
                settings.cacheMode = WebSettings.LOAD_DEFAULT
                webViewClient = WebViewClient()
                webChromeClient = WebChromeClient()
                loadUrl(IotCloudApiService.BASE_WEB_URL)
            }
        }
    )
}

// ============================================================================
// TAB 3: PROFIL AKUN & ENDPOINT REST API UNTUK ESP32 FISIK
// ============================================================================
@Composable
fun LayarProfilDanTokenAkun(
    user: UserAccount,
    tokenAktif: String,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Text("PROFIL AKUN TERVERIFIKASI", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(6.dp))
            Text(user.nama, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text("${user.email} • Role: ${user.role.uppercase()}", color = Color(0xFF94A3B8), fontSize = 12.sp)
            Text("Instansi: ${user.instansi}", color = Color(0xFFCBD5E1), fontSize = 12.sp)
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, Color(0xFF334155), RoundedCornerShape(24.dp))
                .padding(20.dp)
        ) {
            Text("ENDPOINT API UNTUK ESP32 FISIK & ANDROID", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${IotCloudApiService.IOT_CLOUD_ENDPOINT}?token=$tokenAktif",
                color = Color(0xFF38BDF8),
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = {
                    clipboard.setText(AnnotatedString("${IotCloudApiService.IOT_CLOUD_ENDPOINT}?token=$tokenAktif"))
                    Toast.makeText(context, "URL Endpoint berhasil disalin!", Toast.LENGTH_SHORT).show()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Salin URL Endpoint ESP32", color = Color.White, fontWeight = FontWeight.Black)
            }
        }

        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Text("Keluar dari Akun (Logout)", color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}