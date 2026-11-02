package id.my.hibahilmu.app

import android.os.Bundle
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
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
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
                AplikasiNativeHibahIlmu()
            }
        }
    }
}

@Composable
fun AplikasiNativeHibahIlmu() {
    var tampilSplash by remember { mutableStateOf(true) }
    var userAktif by remember { mutableStateOf<UserAccount?>(null) }

    LaunchedEffect(Unit) {
        delay(1100)
        tampilSplash = false
    }

    if (tampilSplash) {
        SplashScreenNative()
    } else if (userAktif == null) {
        LayarLoginOnlineWajibServer(
            onLoginSuksesDariServer = { akunServer ->
                userAktif = akunServer
            }
        )
    } else {
        DashboardNativeUtama(
            user = userAktif!!,
            onLogout = { userAktif = null }
        )
    }
}

@Composable
fun SplashScreenNative() {
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
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(52.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "HibahIlmu Native App",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "100% NATIVE ANDROID • GOOGLE SSO & CLOUD DB",
                color = Color(0xFF34D399),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = Color(0xFF10B981), strokeWidth = 3.dp)
        }
    }
}

// ============================================================================
// GERBANG LOGIN & REGISTER WAJIB ONLINE + GOOGLE SSO NATIVE ANDROID
// ============================================================================
@Composable
fun LayarLoginOnlineWajibServer(onLoginSuksesDariServer: (UserAccount) -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var modeRegister by remember { mutableStateOf(false) }
    var pilihRole by remember { mutableStateOf("siswa") }

    var inputNama by remember { mutableStateOf("") }
    var inputEmail by remember { mutableStateOf("siswa@hibahilmu.id") }
    var inputPassword by remember { mutableStateOf("123") }
    var inputInstansi by remember { mutableStateOf("SMA Negeri 1") }

    var sedangMemeriksaServer by remember { mutableStateOf(false) }
    var pesanErrorServer by remember { mutableStateOf<String?>(null) }

    // Fungsi Memanggil Bottom Sheet Native Google Sign-In Android (Credential Manager)
    fun jalankanLoginGoogleNativeAndroid() {
        pesanErrorServer = null
        sedangMemeriksaServer = true

        scope.launch {
            try {
                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetGoogleIdOption.Builder()
                    .setFilterByAuthorizedAccounts(false)
                    .setServerClientId(IotCloudApiService.GOOGLE_WEB_CLIENT_ID)
                    .setAutoSelectEnabled(false)
                    .build()

                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(googleIdOption)
                    .build()

                val result = credentialManager.getCredential(
                    request = request,
                    context = context
                )

                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCred = GoogleIdTokenCredential.createFrom(credential.data)
                    val idTokenResmi = googleIdTokenCred.idToken

                    val resServer = IotCloudApiService.loginDenganGoogleSsoKeServer(
                        idTokenGoogle = idTokenResmi,
                        rolePilihan = pilihRole,
                        instansi = inputInstansi.ifBlank { "Google Verified Android" }
                    )

                    sedangMemeriksaServer = false
                    resServer.onSuccess { akunTerverifikasi ->
                        Toast.makeText(
                            context,
                            "Login Google Berhasil: ${akunTerverifikasi.nama}!",
                            Toast.LENGTH_SHORT
                        ).show()
                        onLoginSuksesDariServer(akunTerverifikasi)
                    }.onFailure { err ->
                        pesanErrorServer = err.message
                    }
                } else {
                    sedangMemeriksaServer = false
                    pesanErrorServer = "Jenis kredensial Google tidak dikenali."
                }
            } catch (e: Exception) {
                sedangMemeriksaServer = false
                pesanErrorServer =
                    "Kendala Google Sign-In: Pastikan akun Google sudah login di HP/Emulator dan SHA-1 Android sudah didaftarkan di Google Cloud Console. (${e.localizedMessage})"
            }
        }
    }

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
                        Icons.Default.CloudDone,
                        contentDescription = null,
                        tint = Color(0xFF34D399),
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "WAJIB LOGIN SERVER • GOOGLE SSO AKTIF",
                        color = Color(0xFF34D399),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }

            Text(
                text = if (modeRegister) "Registrasi ke Server" else "Masuk ke HibahIlmu",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Pilih peran Anda lalu masuk dengan 1-Klik Google SSO atau akun Email.",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF020617))
                    .padding(4.dp)
            ) {
                listOf("siswa" to "🎓 Siswa", "guru" to "👨‍🏫 Guru / Dosen").forEach { (kode, label) ->
                    val aktif = pilihRole == kode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (aktif) Color(0xFF10B981) else Color.Transparent)
                            .clickable {
                                pilihRole = kode
                                if (!modeRegister) {
                                    if (kode == "siswa") {
                                        inputEmail = "siswa@hibahilmu.id"
                                        inputPassword = "123"
                                    } else {
                                        inputEmail = "guru@hibahilmu.id"
                                        inputPassword = "123"
                                    }
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

            // TOMBOL UTAMA: LOGIN / REGISTER 1-KLIK VIA GOOGLE SSO NATIVE
            Button(
                enabled = !sedangMemeriksaServer,
                onClick = { jalankanLoginGoogleNativeAndroid() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Google",
                    tint = Color(0xFFEA4335),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Lanjutkan dengan Google (${pilihRole.uppercase()})",
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF1E293B))
                Text(
                    text = "  ATAU EMAIL SERVER  ",
                    color = Color(0xFF64748B),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
                HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFF1E293B))
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (modeRegister) {
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
                    label = { Text("Asal Sekolah / Universitas") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
            }

            OutlinedTextField(
                value = inputEmail,
                onValueChange = { inputEmail = it },
                label = { Text("Email Terdaftar di Server") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(10.dp))

            OutlinedTextField(
                value = inputPassword,
                onValueChange = { inputPassword = it },
                label = { Text("Password Server") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (pesanErrorServer != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    color = Color(0xFF7F1D1D),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "❌ $pesanErrorServer",
                        color = Color(0xFFFECACA),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                enabled = !sedangMemeriksaServer,
                onClick = {
                    pesanErrorServer = null
                    sedangMemeriksaServer = true
                    scope.launch {
                        val aksi = if (modeRegister) "register" else "login"
                        val res = IotCloudApiService.autentikasiKeServer(
                            action = aksi,
                            email = inputEmail,
                            password = inputPassword,
                            nama = inputNama,
                            role = pilihRole,
                            instansi = inputInstansi
                        )
                        sedangMemeriksaServer = false
                        res.onSuccess { akun ->
                            Toast.makeText(
                                context,
                                "Terverifikasi oleh Server hibahilmu.my.id!",
                                Toast.LENGTH_SHORT
                            ).show()
                            onLoginSuksesDariServer(akun)
                        }.onFailure { err ->
                            pesanErrorServer = err.message
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (sedangMemeriksaServer) {
                    CircularProgressIndicator(
                        color = Color(0xFF022C22),
                        strokeWidth = 2.5.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Memverifikasi...", color = Color(0xFF022C22), fontWeight = FontWeight.Black)
                } else {
                    Text(
                        text = if (modeRegister) "Daftarkan Email ke Server" else "Login dengan Email Server",
                        color = Color(0xFF022C22),
                        fontWeight = FontWeight.Black,
                        fontSize = 13.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            TextButton(onClick = {
                modeRegister = !modeRegister
                pesanErrorServer = null
            }) {
                Text(
                    text = if (modeRegister) "Sudah punya akun? Klik untuk Login" else "Ingin daftar manual pakai Email? Klik di sini",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ============================================================================
// DASHBOARD NATIVE UTAMA (5 MENU NATIVE TANPA WEBVIEW)
// ============================================================================
@Composable
fun DashboardNativeUtama(user: UserAccount, onLogout: () -> Unit) {
    val context = LocalContext.current
    var tabAktif by remember { mutableStateOf(0) }
    var xpPengguna by remember { mutableStateOf(user.xp) }

    var sedangMemuatKonten by remember { mutableStateOf(true) }
    var pesanErrorKonten by remember { mutableStateOf<String?>(null) }
    var daftarJenjang by remember { mutableStateOf<List<JenjangNativeItem>>(emptyList()) }
    var daftarTryout by remember { mutableStateOf<List<PaketTryoutNative>>(emptyList()) }
    var daftarWebinar by remember { mutableStateOf<List<WebinarNativeItem>>(emptyList()) }
    var daftarForum by remember { mutableStateOf<List<DiskusiPrNativeItem>>(emptyList()) }

    val daftarProjectPreset = listOf(
        "proj-1" to "Proyek 1: Lampu Jalan LDR + Relay",
        "proj-2" to "Proyek 2: Smart Door Lock RFID + Solenoid",
        "proj-3" to "Proyek 3: Smart Greenhouse Pompa & Kipas",
        "proj-5" to "Proyek 5: Dual-ESP32 Telemetry Bridge"
    )
    var projectPilih by remember { mutableStateOf(daftarProjectPreset[3]) }
    val tokenDefault = remember(user.id, projectPilih.first) {
        SessionTokenStore.buatTokenBlynkUnik(user, projectPilih.first)
    }
    var tokenAktif by remember {
        mutableStateOf(SessionTokenStore.ambilTokenKustom(context) ?: tokenDefault)
    }

    val scope = rememberCoroutineScope()

    fun muatUlangDataServer() {
        sedangMemuatKonten = true
        pesanErrorKonten = null
        scope.launch {
            val res = IotCloudApiService.ambilKontenUtamaDariServer()
            sedangMemuatKonten = false
            res.onSuccess { bundle ->
                daftarJenjang = bundle.jenjangList
                daftarTryout = bundle.tryoutList
                daftarWebinar = bundle.webinarList
                daftarForum = bundle.forumList
            }.onFailure { e ->
                pesanErrorKonten = "Gagal memuat database dari hibahilmu.my.id (${e.localizedMessage})"
            }
        }
    }

    LaunchedEffect(Unit) {
        muatUlangDataServer()
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
                        Icon(Icons.Default.Memory, contentDescription = null, tint = Color(0xFF022C22))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text("HibahIlmu Native", color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                        Text("${user.nama} • ${user.role.uppercase()}", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        color = Color(0xFFF59E0B).copy(alpha = 0.2f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "🔥 $xpPengguna XP",
                            color = Color(0xFFFBBF24),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                    IconButton(onClick = onLogout) {
                        Icon(Icons.Default.Logout, contentDescription = "Keluar", tint = Color(0xFFF87171))
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(containerColor = Color(0xFF0F172A)) {
                NavigationBarItem(
                    selected = tabAktif == 0,
                    onClick = { tabAktif = 0 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    label = { Text("Kelas", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = tabAktif == 1,
                    onClick = { tabAktif = 1 },
                    icon = { Icon(Icons.Default.SettingsRemote, contentDescription = null) },
                    label = { Text("IoT Blynk", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = tabAktif == 2,
                    onClick = { tabAktif = 2 },
                    icon = { Icon(Icons.Default.Quiz, contentDescription = null) },
                    label = { Text("CBT Ujian", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = tabAktif == 3,
                    onClick = { tabAktif = 3 },
                    icon = { Icon(Icons.Default.Forum, contentDescription = null) },
                    label = { Text("Forum & Live", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = tabAktif == 4,
                    onClick = { tabAktif = 4 },
                    icon = { Icon(Icons.Default.Person, contentDescription = null) },
                    label = { Text("Akun", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
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
                0 -> MenuRuangBelajarNative(
                    sedangMemuat = sedangMemuatKonten,
                    pesanError = pesanErrorKonten,
                    daftarJenjang = daftarJenjang,
                    onRefresh = { muatUlangDataServer() },
                    onTambahXp = { xpPengguna += it }
                )
                1 -> LayarRemoteBlynkIotNative(
                    user = user,
                    tokenAktif = tokenAktif,
                    onUbahToken = { baru ->
                        tokenAktif = baru
                        SessionTokenStore.simpanTokenKustom(context, baru)
                    },
                    daftarProjectPreset = daftarProjectPreset,
                    projectPilih = projectPilih,
                    onPilihProject = { prj ->
                        projectPilih = prj
                        val tBaru = SessionTokenStore.buatTokenBlynkUnik(user, prj.first)
                        tokenAktif = tBaru
                        SessionTokenStore.simpanTokenKustom(context, tBaru)
                    }
                )
                2 -> MenuTryoutCbtNative(
                    daftarTryout = daftarTryout,
                    onTambahXp = { xpPengguna += it }
                )
                3 -> MenuForumDanWebinarNative(
                    user = user,
                    daftarWebinar = daftarWebinar,
                    daftarForum = daftarForum,
                    onRefreshServer = { muatUlangDataServer() }
                )
                4 -> LayarProfilDanTokenAkun(
                    user = user,
                    tokenAktif = tokenAktif,
                    onLogout = onLogout
                )
            }
        }
    }
}

// ============================================================================
// MENU 1 NATIVE: RUANG BELAJAR, JENJANG, BAB MATERI & KUIS INTERAKTIF
// ============================================================================
@Composable
fun MenuRuangBelajarNative(
    sedangMemuat: Boolean,
    pesanError: String?,
    daftarJenjang: List<JenjangNativeItem>,
    onRefresh: () -> Unit,
    onTambahXp: (Int) -> Unit
) {
    var jenjangDipilih by remember { mutableStateOf<JenjangNativeItem?>(null) }
    var modulDipilih by remember { mutableStateOf<KelasModulNative?>(null) }
    var jawabanKuisMap by remember { mutableStateOf<Map<String, Int>>(emptyMap()) }

    if (sedangMemuat) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFF10B981))
                Spacer(modifier = Modifier.height(10.dp))
                Text("Mengambil data kurikulum dari server hibahilmu.my.id...", color = Color.White, fontSize = 12.sp)
            }
        }
        return
    }

    if (pesanError != null) {
        Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(pesanError, color = Color(0xFFF87171), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onRefresh) { Text("Coba Muat Ulang dari Server") }
            }
        }
        return
    }

    if (modulDipilih != null) {
        val mdl = modulDipilih!!
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Button(
                    onClick = { modulDipilih = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                ) {
                    Text("← Kembali ke Daftar Modul", color = Color.White, fontSize = 12.sp)
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(22.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(22.dp))
                        .padding(18.dp)
                ) {
                    Text("${mdl.level} • ${mdl.durasi} • KKM ${mdl.syaratSkorMinimal}", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(mdl.judul, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("Pengajar: ${mdl.pembuatNama}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(mdl.rangkumanTeks, color = Color(0xFFCBD5E1), fontSize = 13.sp)
                }
            }

            if (mdl.daftarBab.isNotEmpty()) {
                item {
                    Text("📖 DAFTAR BAB MATERI (${mdl.daftarBab.size} BAB):", color = Color(0xFFFBBF24), fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
                items(mdl.daftarBab) { bab ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Text("${bab.judulBab} (${bab.durasiBab})", color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(bab.isiTeks, color = Color(0xFFCBD5E1), fontSize = 12.sp)
                        if (bab.rumus.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(color = Color(0xFF020617), shape = RoundedCornerShape(10.dp)) {
                                Text(
                                    text = "📐 Rumus: ${bab.rumus}",
                                    color = Color(0xFF38BDF8),
                                    fontSize = 11.sp,
                                    fontFamily = FontFamily.Monospace,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }
                    }
                }
            }

            if (mdl.daftarKuis.isNotEmpty()) {
                item {
                    Text("📝 LATIHAN KUIS MODUL (${mdl.daftarKuis.size} SOAL):", color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.Black)
                }
                items(mdl.daftarKuis) { kuis ->
                    val dipilih = jawabanKuisMap[kuis.id]
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0F172A))
                            .border(1.dp, Color(0xFF334155), RoundedCornerShape(18.dp))
                            .padding(16.dp)
                    ) {
                        Text(kuis.tanya, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        kuis.opsi.forEachIndexed { idx, teksOpsi ->
                            val isSelected = dipilih == idx
                            val isCorrect = idx == kuis.jawabanBenar
                            val bgWarna = if (!isSelected) Color(0xFF1E293B) else if (isCorrect) Color(0xFF065F46) else Color(0xFF7F1D1D)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgWarna)
                                    .clickable {
                                        jawabanKuisMap = jawabanKuisMap + (kuis.id to idx)
                                        if (isCorrect) onTambahXp(25)
                                    }
                                    .padding(12.dp)
                            ) {
                                Text(
                                    text = "${('A' + idx)}. $teksOpsi",
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        if (dipilih != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "💡 Pembahasan: ${kuis.pembahasan}",
                                color = Color(0xFFFBBF24),
                                fontSize = 11.sp
                            )
                        }
                    }
                }
            }
        }
        return
    }

    if (jenjangDipilih != null) {
        val jnj = jenjangDipilih!!
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Button(
                    onClick = { jenjangDipilih = null },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                ) {
                    Text("← Kembali ke Semua Jenjang", color = Color.White, fontSize = 12.sp)
                }
            }
            item {
                Text(jnj.nama, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
                Text(jnj.deskripsi, color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
            items(jnj.kelas) { kls ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                        .clickable { modulDipilih = kls }
                        .padding(16.dp)
                ) {
                    Text("${kls.level} • ${kls.durasi}", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(kls.judul, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text("Oleh: ${kls.pembuatNama}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "${kls.daftarBab.size} Bab Materi • ${kls.daftarKuis.size} Soal Kuis → Buka Modul",
                        color = Color(0xFFFBBF24),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF065F46), Color(0xFF1E1B4B))))
                    .padding(20.dp)
            ) {
                Text("KURIKULUM SERVER HIBAHILMU.MY.ID", color = Color(0xFFFBBF24), fontSize = 10.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text("Pilih Jenjang & Modul Kelas Native", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                Text("Seluruh materi dan kuis ditarik langsung dari database server.", color = Color(0xFFCBD5E1), fontSize = 12.sp)
            }
        }

        items(daftarJenjang) { item ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                    .clickable { jenjangDipilih = item }
                    .padding(18.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(item.nama, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
                    Surface(color = Color(0xFF10B981).copy(alpha = 0.2f), shape = RoundedCornerShape(8.dp)) {
                        Text(
                            text = "${item.kelas.size} Modul",
                            color = Color(0xFF34D399),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(item.tagline, color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(4.dp))
                Text(item.deskripsi, color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
        }
    }
}

// ============================================================================
// MENU 3 NATIVE: TRYOUT CBT INTERAKTIF
// ============================================================================
@Composable
fun MenuTryoutCbtNative(
    daftarTryout: List<PaketTryoutNative>,
    onTambahXp: (Int) -> Unit
) {
    var paketAktif by remember { mutableStateOf<PaketTryoutNative?>(null) }
    var jawabanUser by remember { mutableStateOf<Map<Int, Int>>(emptyMap()) }
    var sudahSubmit by remember { mutableStateOf(false) }

    if (paketAktif != null) {
        val pkt = paketAktif!!
        val benarCount = pkt.daftarSoal.indices.count { idx ->
            jawabanUser[idx] == pkt.daftarSoal[idx].jawabanBenar
        }
        val skorAkhir = if (pkt.daftarSoal.isNotEmpty()) (benarCount * 100) / pkt.daftarSoal.size else 0

        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Button(
                    onClick = {
                        paketAktif = null
                        jawabanUser = emptyMap()
                        sudahSubmit = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1E293B))
                ) {
                    Text("← Keluar dari Ujian CBT", color = Color.White)
                }
            }
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A))
                        .padding(16.dp)
                ) {
                    Text("${pkt.kategoriTes} • ${pkt.durasiMenit} Menit", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Text(pkt.judul, color = Color.White, fontSize = 18.sp, fontWeight = FontWeight.Black)
                    if (sudahSubmit) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("🏆 Skor Akhir Anda: $skorAkhir / 100", color = Color(0xFF34D399), fontSize = 18.sp, fontWeight = FontWeight.Black)
                    }
                }
            }

            items(pkt.daftarSoal.size) { sIdx ->
                val soal = pkt.daftarSoal[sIdx]
                val dipilih = jawabanUser[sIdx]
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(18.dp))
                        .padding(16.dp)
                ) {
                    Text("Soal #${sIdx + 1}: ${soal.tanya}", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    soal.opsi.forEachIndexed { oIdx, teks ->
                        val dipilihIni = dipilih == oIdx
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (dipilihIni) Color(0xFF4F46E5) else Color(0xFF1E293B))
                                .clickable(enabled = !sudahSubmit) {
                                    jawabanUser = jawabanUser + (sIdx to oIdx)
                                }
                                .padding(12.dp)
                        ) {
                            Text("${('A' + oIdx)}. $teks", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (sudahSubmit) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("✓ Pembahasan: ${soal.pembahasan}", color = Color(0xFF34D399), fontSize = 11.sp)
                    }
                }
            }

            if (!sudahSubmit) {
                item {
                    Button(
                        onClick = {
                            sudahSubmit = true
                            onTambahXp(50)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Kumpulkan Ujian CBT (+50 XP)", color = Color(0xFF022C22), fontWeight = FontWeight.Black)
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text("SIMULASI UJIAN CBT NATIVE", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Black)
            Text("Paket Tryout Nasional (Database Server)", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
        }
        items(daftarTryout) { pkt ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
                    .clickable { paketAktif = pkt }
                    .padding(18.dp)
            ) {
                Text("${pkt.kategoriTes} • ${pkt.jenjang} • ${pkt.durasiMenit} Menit", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(pkt.judul, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Text("${pkt.daftarSoal.size} Butir Soal → Mulai Kerjakan CBT", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ============================================================================
// MENU 4 NATIVE: FORUM DISKUSI PR & JADWAL WEBINAR LIVE SERVER
// ============================================================================
@Composable
fun MenuForumDanWebinarNative(
    user: UserAccount,
    daftarWebinar: List<WebinarNativeItem>,
    daftarForum: List<DiskusiPrNativeItem>,
    onRefreshServer: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var judulTanya by remember { mutableStateOf("") }
    var isiTanya by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("🔴 AGENDA WEBINAR NASIONAL (SERVER):", color = Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        items(daftarWebinar) { wb ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Text("${wb.kategoriAcara} • ${wb.platform}", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Black)
                Text(wb.judulAcara, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text("Narasumber: ${wb.narasumber} | ${wb.tanggalWaktu}", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
        }

        item {
            Spacer(modifier = Modifier.height(6.dp))
            Text("💬 TANYA PR BARU KE SERVER HIBAHILMU.MY.ID:", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Black)
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F172A))
                    .padding(14.dp)
            ) {
                OutlinedTextField(
                    value = judulTanya,
                    onValueChange = { judulTanya = it },
                    label = { Text("Judul Pertanyaan PR / Koding") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = isiTanya,
                    onValueChange = { isiTanya = it },
                    label = { Text("Detail soal yang ingin didiskusikan...") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (judulTanya.isBlank() || isiTanya.isBlank()) return@Button
                        scope.launch {
                            IotCloudApiService.kirimPertanyaanForumKeServer(
                                penanya = user.nama,
                                mapel = "Sains & IoT",
                                judul = judulTanya,
                                pertanyaan = isiTanya
                            )
                            judulTanya = ""
                            isiTanya = ""
                            Toast.makeText(context, "Pertanyaan terkirim ke server!", Toast.LENGTH_SHORT).show()
                            onRefreshServer()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kirim Pertanyaan ke Server", color = Color(0xFF022C22), fontWeight = FontWeight.Black)
                }
            }
        }

        items(daftarForum) { d ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(18.dp))
                    .padding(14.dp)
            ) {
                Text("${d.mapel} • Oleh ${d.penanya} (${d.waktu})", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                Text(d.judul, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(d.pertanyaan, color = Color(0xFFCBD5E1), fontSize = 12.sp)
            }
        }
    }
}

// ============================================================================
// MENU 2 NATIVE: REMOTE DASHBOARD BLYNK IOT NATIVE
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

    LaunchedEffect(tokenAktif, brokerRunning) {
        while (brokerRunning) {
            val res = IotCloudApiService.ambilStatePinDariServer(tokenAktif)
            res.onSuccess { st ->
                statusKoneksi = "ONLINE • hibahilmu.my.id (${st.terakhirUpdate})"
                st.virtualPins["V0"]?.let { suhuV0 = it }
                st.virtualPins["V1"]?.let { jarakV1 = it }
                st.statusAktuator["V1"]?.let { sakelarSolenoidV2 = it }
                st.statusAktuator["V2"]?.let { sakelarPompaV3 = it }
                st.virtualPins["V3"]?.let { sudutServoV4 = it.toFloat() }
                logServer = st.logPesan
            }.onFailure {
                statusKoneksi = "Menunggu respon /api/iot/cloud..."
            }
            delay(2200)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
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
                        Text("SERVER BROKER HIBAHILMU.MY.ID", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text(statusKoneksi, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = tokenAktif,
                    onValueChange = { onUbahToken(it) },
                    label = { Text("BLYNK AUTH TOKEN PROJECT") },
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

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(Color(0xFF0F172A)).border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp)).padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("VIRTUAL PIN V0", color = Color(0xFFFBBF24), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text("${suhuV0.roundToInt()} °C", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                    Text("Suhu DHT22", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Slider(
                        value = suhuV0.toFloat(),
                        onValueChange = { suhuV0 = it.toDouble() },
                        onValueChangeFinished = {
                            scope.launch {
                                IotCloudApiService.kirimPerubahanPinKeServer(tokenAktif, user.nama, projectPilih.second, "V0", suhuV0.roundToInt())
                            }
                        },
                        valueRange = -10f..60f
                    )
                }

                Column(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(Color(0xFF0F172A)).border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp)).padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("VIRTUAL PIN V1", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text("${jarakV1.roundToInt()} cm", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
                    Text("Sonar HC-SR04", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Slider(
                        value = jarakV1.toFloat(),
                        onValueChange = { jarakV1 = it.toDouble() },
                        onValueChangeFinished = {
                            scope.launch {
                                IotCloudApiService.kirimPerubahanPinKeServer(tokenAktif, user.nama, projectPilih.second, "V1", jarakV1.roundToInt())
                            }
                        },
                        valueRange = 2f..400f
                    )
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Column(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(Color(0xFF0F172A)).border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp)).padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("PIN V1 • RELAY CH1", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text("Solenoid Lock 12V", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            sakelarSolenoidV2 = !sakelarSolenoidV2
                            scope.launch {
                                IotCloudApiService.kirimPerubahanPinKeServer(tokenAktif, user.nama, projectPilih.second, "V1", if (sakelarSolenoidV2) 1 else 0)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (sakelarSolenoidV2) Color(0xFF10B981) else Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (sakelarSolenoidV2) "🔓 UNLOCK" else "🔒 LOCKED", color = if (sakelarSolenoidV2) Color(0xFF022C22) else Color.White, fontWeight = FontWeight.Black)
                    }
                }

                Column(
                    modifier = Modifier.weight(1f).clip(RoundedCornerShape(20.dp)).background(Color(0xFF0F172A)).border(1.dp, Color(0xFF334155), RoundedCornerShape(20.dp)).padding(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("PIN V2 • RELAY CH2", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    Text("Pompa Air Mini", color = Color.White, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            sakelarPompaV3 = !sakelarPompaV3
                            scope.launch {
                                IotCloudApiService.kirimPerubahanPinKeServer(tokenAktif, user.nama, projectPilih.second, "V2", if (sakelarPompaV3) 1 else 0)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = if (sakelarPompaV3) Color(0xFF0284C7) else Color(0xFF1E293B)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(if (sakelarPompaV3) "💦 POMPA ON" else "⏹ POMPA OFF", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

// ============================================================================
// MENU 5 NATIVE: PROFIL AKUN & ENDPOINT REST API ESP32
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
        modifier = Modifier.fillMaxSize().padding(20.dp).verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFF0F172A)).border(1.dp, Color(0xFF334155), RoundedCornerShape(24.dp)).padding(20.dp)
        ) {
            Text("AKUN TERVERIFIKASI DATABASE SERVER", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(6.dp))
            Text(user.nama, color = Color.White, fontSize = 22.sp, fontWeight = FontWeight.Black)
            Text("${user.email} • Role: ${user.role.uppercase()}", color = Color(0xFF94A3B8), fontSize = 12.sp)
            Text("Instansi: ${user.instansi}", color = Color(0xFFCBD5E1), fontSize = 12.sp)
        }

        Column(
            modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(24.dp)).background(Color(0xFF0F172A)).border(1.dp, Color(0xFF334155), RoundedCornerShape(24.dp)).padding(20.dp)
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
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Keluar dari Akun Server (Logout)", color = Color.White, fontWeight = FontWeight.Black)
        }
    }
}