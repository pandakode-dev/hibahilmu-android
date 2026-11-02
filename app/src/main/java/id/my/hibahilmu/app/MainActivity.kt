package id.my.hibahilmu.app

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
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
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.security.MessageDigest
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

@Suppress("DEPRECATION")
fun ambilSha1Aplikasi(context: Context): String {
    return try {
        val pkgInfo = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNING_CERTIFICATES
            )
        } else {
            context.packageManager.getPackageInfo(
                context.packageName,
                PackageManager.GET_SIGNATURES
            )
        }
        val signatures = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            pkgInfo.signingInfo?.apkContentsSigners
        } else {
            pkgInfo.signatures
        }
        val cert = signatures?.firstOrNull()?.toByteArray() ?: return "Tidak terdeteksi"
        val md = MessageDigest.getInstance("SHA-1")
        md.digest(cert).joinToString(":") { "%02X".format(it) }
    } catch (e: Exception) {
        "Gagal: ${e.localizedMessage}"
    }
}

@Composable
fun AplikasiNativeHibahIlmu() {
    var tampilSplash by remember { mutableStateOf(true) }
    var userAktif by remember { mutableStateOf<UserAccount?>(null) }

    LaunchedEffect(Unit) {
        delay(1000)
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
            Text("HibahIlmu Native App", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Black)
            Text("MULTI-ROLE (SISWA • GURU • ADMIN) & IOT CLOUD", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = Color(0xFF10B981), strokeWidth = 3.dp)
        }
    }
}

// ============================================================================
// GERBANG LOGIN & REGISTER WAJIB ONLINE + GOOGLE SSO (PILIH PERAN SISWA / GURU / ADMIN)
// ============================================================================
@Suppress("DEPRECATION")
@Composable
fun LayarLoginOnlineWajibServer(onLoginSuksesDariServer: (UserAccount) -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var modeRegister by remember { mutableStateOf(false) }
    var pilihRole by remember { mutableStateOf("siswa") } // "siswa", "guru", "admin"

    var inputNama by remember { mutableStateOf("") }
    var inputEmail by remember { mutableStateOf("siswa@hibahilmu.id") }
    var inputPassword by remember { mutableStateOf("123") }
    var inputInstansi by remember { mutableStateOf("SMA Negeri 1") }

    var sedangMemeriksaServer by remember { mutableStateOf(false) }
    var pesanErrorServer by remember { mutableStateOf<String?>(null) }
    var tampilkanInfoSha1 by remember { mutableStateOf(false) }
    val sha1Terdeteksi = remember { ambilSha1Aplikasi(context) }

    fun verifikasiIdTokenGoogleKeBackend(idTokenResmi: String) {
        scope.launch {
            sedangMemeriksaServer = true
            val resServer = IotCloudApiService.loginDenganGoogleSsoKeServer(
                idTokenGoogle = idTokenResmi,
                rolePilihan = if (pilihRole == "admin") "guru" else pilihRole,
                instansi = inputInstansi.ifBlank { "Google Verified Android" }
            )
            sedangMemeriksaServer = false
            resServer.onSuccess { akunTerverifikasi ->
                Toast.makeText(
                    context,
                    "Login Google Berhasil sebagai ${akunTerverifikasi.role.uppercase()}: ${akunTerverifikasi.nama}!",
                    Toast.LENGTH_SHORT
                ).show()
                onLoginSuksesDariServer(akunTerverifikasi)
            }.onFailure { err ->
                pesanErrorServer = err.message
            }
        }
    }

    val legacyGoogleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
            val account = task.getResult(ApiException::class.java)
            val idToken = account?.idToken
            if (!idToken.isNullOrBlank()) {
                verifikasiIdTokenGoogleKeBackend(idToken)
            } else {
                sedangMemeriksaServer = false
                pesanErrorServer = "ID Token kosong. Pastikan SHA-1 ($sha1Terdeteksi) terdaftar di Google Cloud Console!"
                tampilkanInfoSha1 = true
            }
        } catch (e: ApiException) {
            sedangMemeriksaServer = false
            if (e.statusCode == 10) {
                tampilkanInfoSha1 = true
                pesanErrorServer = "Kode Error 10: Pastikan SHA-1 di bawah sudah terdaftar di OAuth Android Google Cloud!"
            } else {
                pesanErrorServer = "Google Sign-In dibatalkan / gagal (Kode ${e.statusCode})"
            }
        } catch (e: Exception) {
            sedangMemeriksaServer = false
            pesanErrorServer = "Gagal membaca akun Google: ${e.localizedMessage}"
        }
    }

    fun jalankanFallbackLegacyGoogleSignIn() {
        try {
            val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(IotCloudApiService.GOOGLE_WEB_CLIENT_ID)
                .requestEmail()
                .requestProfile()
                .build()
            val googleSignInClient = GoogleSignIn.getClient(context, gso)
            googleSignInClient.signOut().addOnCompleteListener {
                legacyGoogleSignInLauncher.launch(googleSignInClient.signInIntent)
            }
        } catch (e: Exception) {
            sedangMemeriksaServer = false
            pesanErrorServer = "Gagal membuka dialog Google: ${e.localizedMessage}"
        }
    }

    fun jalankanLoginGoogleNativeAndroid() {
        pesanErrorServer = null
        sedangMemeriksaServer = true
        scope.launch {
            try {
                val credentialManager = CredentialManager.create(context)
                val signInWithGoogleOption = GetSignInWithGoogleOption.Builder(
                    serverClientId = IotCloudApiService.GOOGLE_WEB_CLIENT_ID
                ).build()
                val request = GetCredentialRequest.Builder()
                    .addCredentialOption(signInWithGoogleOption)
                    .build()
                val result = credentialManager.getCredential(request = request, context = context)
                val credential = result.credential
                if (credential is CustomCredential &&
                    credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
                ) {
                    val googleIdTokenCred = GoogleIdTokenCredential.createFrom(credential.data)
                    verifikasiIdTokenGoogleKeBackend(googleIdTokenCred.idToken)
                } else {
                    jalankanFallbackLegacyGoogleSignIn()
                }
            } catch (_: Exception) {
                jalankanFallbackLegacyGoogleSignIn()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF090D16))
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0xFF0F172A))
                .border(2.dp, Color(0xFF1E293B), RoundedCornerShape(28.dp))
                .padding(22.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color(0xFF10B981).copy(alpha = 0.15f),
                shape = RoundedCornerShape(50),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "☁️ DATABASE TERPUSAT HIBAHILMU.MY.ID",
                    color = Color(0xFF34D399),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Text(
                text = if (modeRegister) "Registrasi Akun Baru" else "Masuk ke HibahIlmu",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Pilih peran untuk membuka fitur Siswa, Studio Guru, atau Admin:",
                color = Color(0xFF94A3B8),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 3 Pilihan Role: Siswa | Guru / Dosen | Admin
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF020617))
                    .padding(4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                listOf(
                    "siswa" to "🎓 Siswa",
                    "guru" to "👨‍🏫 Guru",
                    "admin" to "🛡️ Admin"
                ).forEach { (kode, label) ->
                    val aktif = pilihRole == kode
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (!aktif) Color.Transparent
                                else if (kode == "guru") Color(0xFF6366F1)
                                else if (kode == "admin") Color(0xFFF59E0B)
                                else Color(0xFF10B981)
                            )
                            .clickable {
                                pilihRole = kode
                                if (!modeRegister) {
                                    when (kode) {
                                        "siswa" -> {
                                            inputEmail = "siswa@hibahilmu.id"
                                            inputPassword = "123"
                                        }
                                        "guru" -> {
                                            inputEmail = "guru@hibahilmu.id"
                                            inputPassword = "123"
                                        }
                                        "admin" -> {
                                            inputEmail = "admin@hibahilmu.id"
                                            inputPassword = "123"
                                        }
                                    }
                                }
                            }
                            .padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = label,
                            color = if (aktif && kode != "guru") Color(0xFF022C22) else Color.White,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Button(
                enabled = !sedangMemeriksaServer,
                onClick = { jalankanLoginGoogleNativeAndroid() },
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Google",
                    tint = Color(0xFFEA4335),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Login / Daftar Google (${pilihRole.uppercase()})",
                    color = Color(0xFF0F172A),
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }

            TextButton(onClick = { tampilkanInfoSha1 = !tampilkanInfoSha1 }) {
                Text(
                    text = if (tampilkanInfoSha1) "▲ Sembunyikan SHA-1" else "🔍 Cek SHA-1 Aplikasi",
                    color = Color(0xFFFBBF24),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (tampilkanInfoSha1) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF020617))
                        .padding(10.dp)
                ) {
                    Text("SHA-1: $sha1Terdeteksi", color = Color(0xFF34D399), fontSize = 10.sp, fontFamily = FontFamily.Monospace)
                    Button(
                        onClick = {
                            clipboard.setText(AnnotatedString(sha1Terdeteksi))
                            Toast.makeText(context, "SHA-1 disalin!", Toast.LENGTH_SHORT).show()
                        },
                        modifier = Modifier.fillMaxWidth().padding(top = 6.dp)
                    ) {
                        Text("Salin SHA-1", fontSize = 11.sp)
                    }
                }
            }

            if (modeRegister) {
                OutlinedTextField(
                    value = inputNama,
                    onValueChange = { inputNama = it },
                    label = { Text("Nama Lengkap") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = inputInstansi,
                    onValueChange = { inputInstansi = it },
                    label = { Text("Sekolah / Kampus / Instansi") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
            }

            OutlinedTextField(
                value = inputEmail,
                onValueChange = { inputEmail = it },
                label = { Text("Email Server") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = inputPassword,
                onValueChange = { inputPassword = it },
                label = { Text("Password Server") },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            if (pesanErrorServer != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Surface(color = Color(0xFF7F1D1D), shape = RoundedCornerShape(12.dp), modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "❌ $pesanErrorServer",
                        color = Color(0xFFFECACA),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(12.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                            onLoginSuksesDariServer(akun)
                        }.onFailure { err ->
                            pesanErrorServer = err.message
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(
                    text = if (sedangMemeriksaServer) "Menghubungi Server..." else if (modeRegister) "Daftarkan Akun Baru" else "Masuk (${pilihRole.uppercase()})",
                    color = Color(0xFF022C22),
                    fontWeight = FontWeight.Black
                )
            }

            TextButton(onClick = { modeRegister = !modeRegister }) {
                Text(
                    text = if (modeRegister) "Sudah punya akun? Login" else "Belum punya akun? Daftar Manual",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ============================================================================
// DASHBOARD NATIVE UTAMA — MENYESUAIKAN FITUR BERDASARKAN ROLE (SISWA / GURU / ADMIN)
// ============================================================================
@Composable
fun DashboardNativeUtama(user: UserAccount, onLogout: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isGuru = user.role.lowercase() == "guru"
    val isAdmin = user.role.lowercase() == "admin"

    var tabAktif by remember { mutableStateOf(0) }
    var xpPengguna by remember { mutableStateOf(user.xp) }

    var sedangMemuatKonten by remember { mutableStateOf(true) }
    var pesanErrorKonten by remember { mutableStateOf<String?>(null) }
    var daftarJenjang by remember { mutableStateOf<List<JenjangNativeItem>>(emptyList()) }
    var daftarTryout by remember { mutableStateOf<List<PaketTryoutNative>>(emptyList()) }
    var daftarWebinar by remember { mutableStateOf<List<WebinarNativeItem>>(emptyList()) }
    var daftarForum by remember { mutableStateOf<List<DiskusiPrNativeItem>>(emptyList()) }
    var daftarBeasiswa by remember { mutableStateOf<List<PengajuanBeasiswaNative>>(emptyList()) }
    var daftarSemuaAkun by remember { mutableStateOf<List<UserAccount>>(emptyList()) }

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
                daftarBeasiswa = bundle.beasiswaList
                daftarSemuaAkun = bundle.daftarAkunServer
            }.onFailure { e ->
                pesanErrorKonten = "Gagal memuat database: ${e.localizedMessage}"
            }
        }
    }

    LaunchedEffect(Unit) {
        muatUlangDataServer()
    }

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().background(Color(0xFF0F172A))) {
                // Banner Status Role Berbeda Warna antara Siswa, Guru, dan Admin
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            if (isAdmin) Color(0xFFD97706)
                            else if (isGuru) Color(0xFF4F46E5)
                            else Color(0xFF059669)
                        )
                        .padding(horizontal = 16.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = if (isAdmin) "🛡️ MODE SUPER ADMIN SERVER (${daftarSemuaAkun.size} Akun Terdaftar)"
                        else if (isGuru) "👨‍🏫 MODE STUDIO PENGAJAR / GURU — Kelola Modul, CBT, Webinar & PR Siswa"
                        else "🎓 MODE PELAJAR / MAHASISWA — Kurikulum, CBT, IoT Blynk & Beasiswa",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(user.nama, color = Color.White, fontWeight = FontWeight.Black, fontSize = 15.sp)
                        Text("${user.email} • ${user.instansi}", color = Color(0xFF94A3B8), fontSize = 11.sp)
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
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                            )
                        }
                        IconButton(onClick = onLogout) {
                            Icon(Icons.Default.Logout, contentDescription = "Keluar", tint = Color(0xFFF87171))
                        }
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
                    label = { Text("Kelas", fontSize = 9.5.sp, fontWeight = FontWeight.Bold) }
                )
                // TAB KE-2 BERBEDA SESUAI ROLE: STUDIO GURU vs PANEL ADMIN vs BEASISWA SISWA!
                NavigationBarItem(
                    selected = tabAktif == 1,
                    onClick = { tabAktif = 1 },
                    icon = {
                        Icon(
                            if (isAdmin) Icons.Default.AdminPanelSettings
                            else if (isGuru) Icons.Default.EditNote
                            else Icons.Default.EmojiEvents,
                            contentDescription = null
                        )
                    },
                    label = {
                        Text(
                            if (isAdmin) "Data Akun"
                            else if (isGuru) "Studio Guru"
                            else "Beasiswa",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                )
                NavigationBarItem(
                    selected = tabAktif == 2,
                    onClick = { tabAktif = 2 },
                    icon = { Icon(Icons.Default.SettingsRemote, contentDescription = null) },
                    label = { Text("IoT Blynk", fontSize = 9.5.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = tabAktif == 3,
                    onClick = { tabAktif = 3 },
                    icon = { Icon(Icons.Default.Quiz, contentDescription = null) },
                    label = { Text("CBT Ujian", fontSize = 9.5.sp, fontWeight = FontWeight.Bold) }
                )
                NavigationBarItem(
                    selected = tabAktif == 4,
                    onClick = { tabAktif = 4 },
                    icon = { Icon(Icons.Default.Forum, contentDescription = null) },
                    label = { Text("Forum & Live", fontSize = 9.5.sp, fontWeight = FontWeight.Bold) }
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
                1 -> {
                    if (isAdmin) {
                        MenuPanelAdminServerNative(
                            daftarAkun = daftarSemuaAkun,
                            daftarBeasiswa = daftarBeasiswa,
                            onRefresh = { muatUlangDataServer() }
                        )
                    } else if (isGuru) {
                        MenuStudioPengajarGuruNative(
                            guru = user,
                            daftarJenjang = daftarJenjang,
                            daftarAkun = daftarSemuaAkun,
                            onSuksesSimpanKeServer = {
                                xpPengguna += 100
                                muatUlangDataServer()
                            }
                        )
                    } else {
                        MenuBeasiswaLeaderboardDanLaporanSiswa(
                            siswa = user,
                            daftarBeasiswa = daftarBeasiswa,
                            daftarAkun = daftarSemuaAkun,
                            tokenAktif = tokenAktif,
                            onRefresh = { muatUlangDataServer() },
                            onLogout = onLogout
                        )
                    }
                }
                2 -> LayarRemoteBlynkIotNative(
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
                3 -> MenuTryoutCbtNative(
                    daftarTryout = daftarTryout,
                    onTambahXp = { xpPengguna += it }
                )
                4 -> MenuForumDanWebinarNative(
                    user = user,
                    daftarWebinar = daftarWebinar,
                    daftarForum = daftarForum,
                    onRefreshServer = { muatUlangDataServer() }
                )
            }
        }
    }
}

// ============================================================================
// FITUR KHUSUS ROLE GURU / DOSEN: STUDIO PENGAJAR NATIVE
// (Buat Modul & Bab, Buat Soal CBT, Buat Live Webinar, & Pantau Siswa)
// ============================================================================
@Composable
fun MenuStudioPengajarGuruNative(
    guru: UserAccount,
    daftarJenjang: List<JenjangNativeItem>,
    daftarAkun: List<UserAccount>,
    onSuksesSimpanKeServer: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var subMenuGuru by remember { mutableStateOf(0) } // 0: Buat Modul, 1: Buat CBT, 2: Buat Webinar, 3: Data Siswa

    // State Form Modul Baru
    var jenjangTarget by remember { mutableStateOf("sma") }
    var judulModul by remember { mutableStateOf("") }
    var rangkumanModul by remember { mutableStateOf("") }
    var judulBab by remember { mutableStateOf("Bab 1: Konsep Dasar & Implementasi") }
    var isiBab by remember { mutableStateOf("") }
    var rumusBab by remember { mutableStateOf("") }
    var tanyaKuis by remember { mutableStateOf("") }
    var opsiA by remember { mutableStateOf("") }
    var opsiB by remember { mutableStateOf("") }
    var opsiC by remember { mutableStateOf("") }
    var opsiD by remember { mutableStateOf("") }
    var pembahasanKuis by remember { mutableStateOf("") }

    // State Form CBT Baru
    var judulCbt by remember { mutableStateOf("") }
    var kategoriCbt by remember { mutableStateOf("UTBK") }
    var soalCbt by remember { mutableStateOf("") }
    var cbtOpsiA by remember { mutableStateOf("") }
    var cbtOpsiB by remember { mutableStateOf("") }
    var cbtOpsiC by remember { mutableStateOf("") }
    var cbtOpsiD by remember { mutableStateOf("") }
    var bahasCbt by remember { mutableStateOf("") }

    // State Form Live Webinar Baru
    var judulWebinar by remember { mutableStateOf("") }
    var jadwalWebinar by remember { mutableStateOf("Minggu, 19:30 WIB") }
    var linkMeetWebinar by remember { mutableStateOf("https://meet.google.com/hibah-ilmu") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Brush.horizontalGradient(listOf(Color(0xFF312E81), Color(0xFF4F46E5))))
                    .padding(18.dp)
            ) {
                Text("👨‍🏫 STUDIO PENGAJAR & DOSEN NATIVE", color = Color(0xFFFBBF24), fontSize = 10.sp, fontWeight = FontWeight.Black)
                Text("Kelola Kurikulum Server hibahilmu.my.id", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Black)
                Text("Modul, CBT, dan Webinar yang Anda buat langsung tampil di HP seluruh Siswa & Website!", color = Color(0xFFE0E7FF), fontSize = 11.sp)
            }
        }

        // Sub-Tab Navigasi Studio Guru
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                listOf(
                    0 to "+ Modul & Bab",
                    1 to "+ Ujian CBT",
                    2 to "+ Live Kelas",
                    3 to "Siswa (${daftarAkun.count { it.role == "siswa" }})"
                ).forEach { (idx, label) ->
                    val dipilih = subMenuGuru == idx
                    Surface(
                        color = if (dipilih) Color(0xFF10B981) else Color(0xFF1E293B),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f).clickable { subMenuGuru = idx }
                    ) {
                        Text(
                            text = label,
                            color = if (dipilih) Color(0xFF022C22) else Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp)
                        )
                    }
                }
            }
        }

        if (subMenuGuru == 0) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF4F46E5), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("📚 TERBITKAN MODUL KELAS, BAB & KUIS BARU", color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.Black)

                    Text("Pilih Jenjang Target:", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("sd" to "SD", "smp" to "SMP", "sma" to "SMA/SMK", "kuliah" to "Kuliah").forEach { (idJ, namaJ) ->
                            Surface(
                                color = if (jenjangTarget == idJ) Color(0xFF4F46E5) else Color(0xFF1E293B),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.clickable { jenjangTarget = idJ }
                            ) {
                                Text(namaJ, color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp))
                            }
                        }
                    }

                    OutlinedTextField(value = judulModul, onValueChange = { judulModul = it }, label = { Text("Judul Modul Kelas Baru") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = rangkumanModul, onValueChange = { rangkumanModul = it }, label = { Text("Rangkuman Singkat Modul") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = judulBab, onValueChange = { judulBab = it }, label = { Text("Judul Bab 1") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = isiBab, onValueChange = { isiBab = it }, label = { Text("Uraian Materi Lengkap Bab 1") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = rumusBab, onValueChange = { rumusBab = it }, label = { Text("Rumus Fisika/Matematika/Kode (Opsional)") }, modifier = Modifier.fillMaxWidth())

                    HorizontalDivider(color = Color(0xFF1E293B))
                    Text("📝 Tambahkan Soal Kuis Evaluasi Modul:", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    OutlinedTextField(value = tanyaKuis, onValueChange = { tanyaKuis = it }, label = { Text("Pertanyaan Kuis") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = opsiA, onValueChange = { opsiA = it }, label = { Text("Opsi A (Jawaban Benar)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = opsiB, onValueChange = { opsiB = it }, label = { Text("Opsi B") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = opsiC, onValueChange = { opsiC = it }, label = { Text("Opsi C") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = opsiD, onValueChange = { opsiD = it }, label = { Text("Opsi D") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = pembahasanKuis, onValueChange = { pembahasanKuis = it }, label = { Text("Pembahasan Kuis") }, modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = {
                            if (judulModul.isBlank() || isiBab.isBlank()) {
                                Toast.makeText(context, "Judul Modul dan Isi Bab wajib diisi!", Toast.LENGTH_SHORT).show()
                                return@Button
                            }
                            scope.launch {
                                val payload = JSONObject().apply {
                                    put("action", "guru_tambah_modul")
                                    put("idJenjang", jenjangTarget)
                                    put("judul", judulModul)
                                    put("pembuatId", guru.id)
                                    put("pembuatNama", guru.nama)
                                    put("rangkuman", rangkumanModul)
                                    put("judulBab", judulBab)
                                    put("isiBab", isiBab)
                                    put("rumus", rumusBab)
                                    put("pertanyaanKuis", tanyaKuis)
                                    put("opsiA", opsiA)
                                    put("opsiB", opsiB)
                                    put("opsiC", opsiC)
                                    put("opsiD", opsiD)
                                    put("jawabanBenar", 0)
                                    put("pembahasanKuis", pembahasanKuis)
                                }
                                val res = IotCloudApiService.kirimAksiKontenKeServer(payload)
                                res.onSuccess { msg ->
                                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show()
                                    judulModul = ""
                                    rangkumanModul = ""
                                    isiBab = ""
                                    tanyaKuis = ""
                                    onSuksesSimpanKeServer()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Terbitkan Modul ke Server (+100 XP Guru)", color = Color(0xFF022C22), fontWeight = FontWeight.Black)
                    }
                }
            }
        } else if (subMenuGuru == 1) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF10B981), RoundedCornerShape(20.dp))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🎯 BUAT PAKET SOAL TRYOUT CBT BARU", color = Color(0xFFFBBF24), fontSize = 12.sp, fontWeight = FontWeight.Black)
                    OutlinedTextField(value = judulCbt, onValueChange = { judulCbt = it }, label = { Text("Judul Paket Tryout CBT") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = kategoriCbt, onValueChange = { kategoriCbt = it }, label = { Text("Kategori (UTBK / TPA / TOEFL)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = soalCbt, onValueChange = { soalCbt = it }, label = { Text("Pertanyaan Soal CBT") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cbtOpsiA, onValueChange = { cbtOpsiA = it }, label = { Text("Pilihan A (Kunci Benar)") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cbtOpsiB, onValueChange = { cbtOpsiB = it }, label = { Text("Pilihan B") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cbtOpsiC, onValueChange = { cbtOpsiC = it }, label = { Text("Pilihan C") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = cbtOpsiD, onValueChange = { cbtOpsiD = it }, label = { Text("Pilihan D") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = bahasCbt, onValueChange = { bahasCbt = it }, label = { Text("Pembahasan Soal CBT") }, modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = {
                            if (judulCbt.isBlank() || soalCbt.isBlank()) return@Button
                            scope.launch {
                                val payload = JSONObject().apply {
                                    put("action", "guru_tambah_cbt")
                                    put("judul", judulCbt)
                                    put("kategoriTes", kategoriCbt)
                                    put("pertanyaan", soalCbt)
                                    put("opsiA", cbtOpsiA)
                                    put("opsiB", cbtOpsiB)
                                    put("opsiC", cbtOpsiC)
                                    put("opsiD", cbtOpsiD)
                                    put("jawabanBenar", 0)
                                    put("pembahasan", bahasCbt)
                                }
                                IotCloudApiService.kirimAksiKontenKeServer(payload).onSuccess {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                    judulCbt = ""
                                    soalCbt = ""
                                    onSuksesSimpanKeServer()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Simpan Paket CBT ke Server", color = Color(0xFF022C22), fontWeight = FontWeight.Black)
                    }
                }
            }
        } else if (subMenuGuru == 2) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF0F172A))
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("🔴 JADWALKAN LIVE TEACHING / WEBINAR BARU", color = Color(0xFFF87171), fontSize = 12.sp, fontWeight = FontWeight.Black)
                    OutlinedTextField(value = judulWebinar, onValueChange = { judulWebinar = it }, label = { Text("Judul Sesi Live Kelas / Webinar") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = jadwalWebinar, onValueChange = { jadwalWebinar = it }, label = { Text("Hari & Jam Pelaksanaan") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = linkMeetWebinar, onValueChange = { linkMeetWebinar = it }, label = { Text("Link Zoom / Google Meet / YouTube") }, modifier = Modifier.fillMaxWidth())

                    Button(
                        onClick = {
                            if (judulWebinar.isBlank()) return@Button
                            scope.launch {
                                val payload = JSONObject().apply {
                                    put("action", "guru_tambah_webinar")
                                    put("judulAcara", judulWebinar)
                                    put("narasumber", guru.nama)
                                    put("tanggalWaktu", jadwalWebinar)
                                    put("url", linkMeetWebinar)
                                }
                                IotCloudApiService.kirimAksiKontenKeServer(payload).onSuccess {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                    judulWebinar = ""
                                    onSuksesSimpanKeServer()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Text("Terbitkan Jadwal Live Kelas", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        } else {
            item {
                Text("👨‍🎓 DAFTAR SISWA & PENGGUNA TERDAFTAR DI SERVER (${daftarAkun.size} AKUN):", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
            items(daftarAkun) { acc ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F172A))
                        .border(1.dp, Color(0xFF334155), RoundedCornerShape(16.dp))
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(acc.nama, color = Color.White, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Text("${acc.email} • ${acc.role.uppercase()}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                        Text(acc.instansi, color = Color(0xFF34D399), fontSize = 10.sp)
                    }
                    Text("🔥 ${acc.xp} XP", color = Color(0xFFFBBF24), fontWeight = FontWeight.Black, fontSize = 12.sp)
                }
            }
        }
    }
}

// ============================================================================
// FITUR KHUSUS ROLE ADMIN: KELOLA SELURUH AKUN SERVER (TERMASUK GOOGLE SSO)
// ============================================================================
@Composable
fun MenuPanelAdminServerNative(
    daftarAkun: List<UserAccount>,
    daftarBeasiswa: List<PengajuanBeasiswaNative>,
    onRefresh: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("PANEL SUPER ADMIN NATIVE", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Black)
                    Text("Data Akun Terdaftar (${daftarAkun.size})", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                }
                Button(onClick = onRefresh) {
                    Text("🔄 Refresh")
                }
            }
        }

        items(daftarAkun) { acc ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF334155), RoundedCornerShape(18.dp))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(acc.nama, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text(acc.email, color = Color(0xFF38BDF8), fontSize = 12.sp)
                        Text("Role: ${acc.role.uppercase()} • Status: ${acc.status.uppercase()}", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Text("Info: ${acc.instansi}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    }
                    if (acc.role != "admin") {
                        IconButton(onClick = {
                            scope.launch {
                                IotCloudApiService.adminKelolaAkunServer("admin_delete_user", acc.id)
                                Toast.makeText(context, "Akun dihapus dari server", Toast.LENGTH_SHORT).show()
                                onRefresh()
                            }
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "Hapus", tint = Color(0xFFF87171))
                        }
                    }
                }
            }
        }

        if (daftarBeasiswa.isNotEmpty()) {
            item {
                Spacer(modifier = Modifier.height(8.dp))
                Text("🎓 PENGAJUAN BEASISWA MASUK (${daftarBeasiswa.size}):", color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.Black)
            }
            items(daftarBeasiswa) { bsw ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF0F172A))
                        .padding(14.dp)
                ) {
                    Text("${bsw.namaPemohon} (${bsw.asalSekolahKampus})", color = Color.White, fontWeight = FontWeight.Black)
                    Text("Alasan: ${bsw.alasanPengajuan}", color = Color(0xFFCBD5E1), fontSize = 12.sp)
                    Text("Status: ${bsw.statusVerifikasi.uppercase()}", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// ============================================================================
// FITUR KHUSUS ROLE SISWA: BEASISWA, LEADERBOARD XP, LAPORAN BUG & TOKEN IOT
// ============================================================================
@Composable
fun MenuBeasiswaLeaderboardDanLaporanSiswa(
    siswa: UserAccount,
    daftarBeasiswa: List<PengajuanBeasiswaNative>,
    daftarAkun: List<UserAccount>,
    tokenAktif: String,
    onRefresh: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var alasanBeasiswa by remember { mutableStateOf("") }
    var linkBerkasBeasiswa by remember { mutableStateOf("") }
    var judulLaporanBug by remember { mutableStateOf("") }
    var isiLaporanBug by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Papan Peringkat (Leaderboard XP)
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFFF59E0B), RoundedCornerShape(22.dp))
                    .padding(16.dp)
            ) {
                Text("🏆 LEADERBOARD XP PELAJAR NASIONAL", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Black)
                Spacer(modifier = Modifier.height(8.dp))
                daftarAkun.sortedByDescending { it.xp }.take(5).forEachIndexed { idx, acc ->
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("#${idx + 1} ${acc.nama} (${acc.role})", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("🔥 ${acc.xp} XP", color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        // 2. Form Pengajuan Beasiswa Pendidikan Siswa
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0F172A))
                    .border(1.dp, Color(0xFF10B981), RoundedCornerShape(22.dp))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🎓 AJUKAN BEASISWA WAKAF PENDIDIKAN", color = Color(0xFF34D399), fontSize = 12.sp, fontWeight = FontWeight.Black)
                Text("Program bantuan pendidikan untuk pelajar berprestasi & kurang mampu.", color = Color(0xFF94A3B8), fontSize = 11.sp)
                OutlinedTextField(
                    value = alasanBeasiswa,
                    onValueChange = { alasanBeasiswa = it },
                    label = { Text("Alasan Pengajuan & Prestasi Anda") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = linkBerkasBeasiswa,
                    onValueChange = { linkBerkasBeasiswa = it },
                    label = { Text("Link Google Drive Rapor / Sertifikat") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Button(
                    onClick = {
                        if (alasanBeasiswa.isBlank()) return@Button
                        scope.launch {
                            val payload = JSONObject().apply {
                                put("action", "ajukan_beasiswa")
                                put("pemohonId", siswa.id)
                                put("namaPemohon", siswa.nama)
                                put("emailPemohon", siswa.email)
                                put("instansi", siswa.instansi)
                                put("alasan", alasanBeasiswa)
                                put("linkBerkas", linkBerkasBeasiswa)
                            }
                            IotCloudApiService.kirimAksiKontenKeServer(payload).onSuccess {
                                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
                                alasanBeasiswa = ""
                                linkBerkasBeasiswa = ""
                                onRefresh()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kirim Pengajuan Beasiswa ke Admin", color = Color(0xFF022C22), fontWeight = FontWeight.Black)
                }
            }
        }

        // 3. Form Kirim Laporan Bug / Usulan Fitur
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(22.dp))
                    .background(Color(0xFF0F172A))
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text("🐞 LAPORKAN BUG / USULAN MATERI KE ADMIN", color = Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.Black)
                OutlinedTextField(value = judulLaporanBug, onValueChange = { judulLaporanBug = it }, label = { Text("Judul Laporan / Usulan") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(value = isiLaporanBug, onValueChange = { isiLaporanBug = it }, label = { Text("Detail Masukan") }, modifier = Modifier.fillMaxWidth())
                Button(
                    onClick = {
                        if (judulLaporanBug.isBlank()) return@Button
                        scope.launch {
                            val payload = JSONObject().apply {
                                put("action", "kirim_laporan")
                                put("pelaporNama", siswa.nama)
                                put("pelaporRole", siswa.role)
                                put("judul", judulLaporanBug)
                                put("deskripsi", isiLaporanBug)
                            }
                            IotCloudApiService.kirimAksiKontenKeServer(payload).onSuccess {
                                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                judulLaporanBug = ""
                                isiLaporanBug = ""
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4F46E5)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Kirim Laporan ke Server", color = Color.White, fontWeight = FontWeight.Black)
                }
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
            CircularProgressIndicator(color = Color(0xFF10B981))
        }
        return
    }

    if (pesanError != null) {
        Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(pesanError, color = Color(0xFFF87171), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onRefresh) { Text("Muat Ulang dari Server") }
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
                    Text(mdl.judul, color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Text("Pengajar: ${mdl.pembuatNama}", color = Color(0xFF94A3B8), fontSize = 12.sp)
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(mdl.rangkumanTeks, color = Color(0xFFCBD5E1), fontSize = 13.sp)
                }
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
                            Text("📐 Rumus: ${bab.rumus}", color = Color(0xFF38BDF8), fontSize = 11.sp, fontFamily = FontFamily.Monospace, modifier = Modifier.padding(10.dp))
                        }
                    }
                }
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
                            Text("${('A' + idx)}. $teksOpsi", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    if (dipilih != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("💡 Pembahasan: ${kuis.pembahasan}", color = Color(0xFFFBBF24), fontSize = 11.sp)
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
                    Text(kls.judul, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                    Text("Oleh: ${kls.pembuatNama}", color = Color(0xFF94A3B8), fontSize = 11.sp)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("${kls.daftarBab.size} Bab • ${kls.daftarKuis.size} Kuis → Buka Modul", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
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
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(item.nama, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Black)
                    Text("${item.kelas.size} Modul", color = Color(0xFF34D399), fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
                Text(item.tagline, color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text(item.deskripsi, color = Color(0xFF94A3B8), fontSize = 12.sp)
            }
        }
    }
}

// ============================================================================
// MENU CBT NATIVE (DENGAN TIMER HITUNG MUNDUR)
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
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(Color(0xFF0F172A)).padding(16.dp)
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
                    modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFF0F172A)).border(1.dp, Color(0xFF334155), RoundedCornerShape(18.dp)).padding(16.dp)
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
                Text(pkt.judul, color = Color.White, fontSize = 16.sp, fontWeight = FontWeight.Black)
                Text("${pkt.daftarSoal.size} Butir Soal → Mulai Kerjakan CBT", color = Color(0xFF38BDF8), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ============================================================================
// MENU FORUM DISKUSI PR (GURU BISA JAWAB & TANDAI SOLVED) & LIVE WEBINAR
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
    var inputBalasanMap by remember { mutableStateOf<Map<String, String>>(emptyMap()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("🔴 AGENDA WEBINAR NASIONAL (SERVER):", color = Color(0xFFF87171), fontSize = 11.sp, fontWeight = FontWeight.Black)
        }
        items(daftarWebinar) { wb ->
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFF0F172A)).border(1.dp, Color(0xFF334155), RoundedCornerShape(18.dp)).padding(14.dp)
            ) {
                Text("${wb.kategoriAcara} • ${wb.platform}", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Black)
                Text(wb.judulAcara, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Black)
                Text("Narasumber: ${wb.narasumber} | ${wb.tanggalWaktu}", color = Color(0xFF94A3B8), fontSize = 11.sp)
            }
        }

        item {
            Text("💬 TANYA PR / DISKUSI BARU:", color = Color(0xFFFBBF24), fontSize = 11.sp, fontWeight = FontWeight.Black)
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFF0F172A)).padding(14.dp)
            ) {
                OutlinedTextField(value = judulTanya, onValueChange = { judulTanya = it }, label = { Text("Judul Pertanyaan PR / Koding") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(value = isiTanya, onValueChange = { isiTanya = it }, label = { Text("Detail soal...") }, modifier = Modifier.fillMaxWidth())
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = {
                        if (judulTanya.isBlank() || isiTanya.isBlank()) return@Button
                        scope.launch {
                            val payload = JSONObject().apply {
                                put("action", "tambah_diskusi")
                                put("penanya", user.nama)
                                put("mapel", "Sains & IoT")
                                put("judul", judulTanya)
                                put("pertanyaan", isiTanya)
                            }
                            IotCloudApiService.kirimAksiKontenKeServer(payload).onSuccess {
                                judulTanya = ""
                                isiTanya = ""
                                onRefreshServer()
                            }
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
            val teksBalas = inputBalasanMap[d.id] ?: ""
            Column(
                modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color(0xFF0F172A)).border(1.dp, if (d.solved) Color(0xFF10B981) else Color(0xFF334155), RoundedCornerShape(18.dp)).padding(14.dp)
            ) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    Text("${d.mapel} • Oleh ${d.penanya}", color = Color(0xFF38BDF8), fontSize = 10.sp, fontWeight = FontWeight.Bold)
                    if (d.solved) {
                        Text("✓ TERJAWAB GURU", color = Color(0xFF34D399), fontSize = 10.sp, fontWeight = FontWeight.Black)
                    }
                }
                Text(d.judul, color = Color.White, fontSize = 14.sp, fontWeight = FontWeight.Black)
                Text(d.pertanyaan, color = Color(0xFFCBD5E1), fontSize = 12.sp)

                if (d.jawaban.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    d.jawaban.forEach { jwb ->
                        Surface(
                            color = Color(0xFF020617),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text("${jwb.penjawab} (${jwb.role})", color = Color(0xFFFBBF24), fontSize = 10.sp, fontWeight = FontWeight.Black)
                                Text(jwb.isi, color = Color.White, fontSize = 11.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = teksBalas,
                        onValueChange = { inputBalasanMap = inputBalasanMap + (d.id to it) },
                        label = { Text(if (user.role == "guru") "Jawab PR ini sebagai Guru..." else "Tulis balasan...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = {
                            if (teksBalas.isBlank()) return@Button
                            scope.launch {
                                val payload = JSONObject().apply {
                                    put("action", "balas_diskusi")
                                    put("diskusiId", d.id)
                                    put("penjawab", user.nama)
                                    put("role", user.role)
                                    put("isiJawaban", teksBalas)
                                }
                                IotCloudApiService.kirimAksiKontenKeServer(payload).onSuccess {
                                    inputBalasanMap = inputBalasanMap - d.id
                                    Toast.makeText(context, "Balasan terkirim!", Toast.LENGTH_SHORT).show()
                                    onRefreshServer()
                                }
                            }
                        }
                    ) {
                        Text("Kirim")
                    }
                }
            }
        }
    }
}

// ============================================================================
// MENU REMOTE DASHBOARD BLYNK IOT NATIVE
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