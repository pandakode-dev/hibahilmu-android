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
import androidx.compose.foundation.horizontalScroll
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

// ============================================================================
// SISTEM 4 TEMA WARNA CERAH & MENARIK (DEFAULT: ZAMRUD CERAH / MINT DAYLIGHT)
// ============================================================================
enum class HibahVisualTheme(
    val id: String,
    val label: String,
    val ikon: String,
    val bgUtama: Color,
    val bgKartu: Color,
    val teksUtama: Color,
    val teksSekunder: Color,
    val aksenPrimer: Color,
    val headerGradStart: Color,
    val headerGradEnd: Color,
    val borderKartu: Color
) {
    MINT_CERAH(
        id = "mint",
        label = "Zamrud Cerah",
        ikon = "🌿",
        bgUtama = Color(0xFFF0FDF4),
        bgKartu = Color(0xFFFFFFFF),
        teksUtama = Color(0xFF0F172A),
        teksSekunder = Color(0xFF475569),
        aksenPrimer = Color(0xFF059669),
        headerGradStart = Color(0xFF065F46),
        headerGradEnd = Color(0xFF10B981),
        borderKartu = Color(0xFFBBF7D0)
    ),
    SKY_OCEAN(
        id = "sky",
        label = "Biru Langit",
        ikon = "🌊",
        bgUtama = Color(0xFFF0F9FF),
        bgKartu = Color(0xFFFFFFFF),
        teksUtama = Color(0xFF0F172A),
        teksSekunder = Color(0xFF475569),
        aksenPrimer = Color(0xFF0284C7),
        headerGradStart = Color(0xFF075985),
        headerGradEnd = Color(0xFF0EA5E9),
        borderKartu = Color(0xFFBAE6FD)
    ),
    SUNRISE_GOLD(
        id = "sunrise",
        label = "Krem Mentari",
        ikon = "🌅",
        bgUtama = Color(0xFFFFFBEB),
        bgKartu = Color(0xFFFFFFFF),
        teksUtama = Color(0xFF0F172A),
        teksSekunder = Color(0xFF57534E),
        aksenPrimer = Color(0xFFD97706),
        headerGradStart = Color(0xFF9A3412),
        headerGradEnd = Color(0xFFF59E0B),
        borderKartu = Color(0xFFFDE68A)
    ),
    DARK_CYBER(
        id = "dark",
        label = "Malam Cyber",
        ikon = "🌌",
        bgUtama = Color(0xFF090D16),
        bgKartu = Color(0xFF0F172A),
        teksUtama = Color(0xFFFFFFFF),
        teksSekunder = Color(0xFF94A3B8),
        aksenPrimer = Color(0xFF10B981),
        headerGradStart = Color(0xFF0F172A),
        headerGradEnd = Color(0xFF1E1B4B),
        borderKartu = Color(0xFF334155)
    )
}

val LocalHibahTheme = compositionLocalOf { HibahVisualTheme.MINT_CERAH }

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            var temaPilihan by remember { mutableStateOf(HibahVisualTheme.MINT_CERAH) }
            CompositionLocalProvider(LocalHibahTheme provides temaPilihan) {
                MaterialTheme(
                    colorScheme = if (temaPilihan == HibahVisualTheme.DARK_CYBER) {
                        darkColorScheme(
                            primary = temaPilihan.aksenPrimer,
                            background = temaPilihan.bgUtama,
                            surface = temaPilihan.bgKartu
                        )
                    } else {
                        lightColorScheme(
                            primary = temaPilihan.aksenPrimer,
                            background = temaPilihan.bgUtama,
                            surface = temaPilihan.bgKartu
                        )
                    }
                ) {
                    AplikasiNativeHibahIlmu(
                        temaAktif = temaPilihan,
                        onGantiTema = { temaPilihan = it }
                    )
                }
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
fun AplikasiNativeHibahIlmu(
    temaAktif: HibahVisualTheme,
    onGantiTema: (HibahVisualTheme) -> Unit
) {
    var tampilSplash by remember { mutableStateOf(true) }
    var userAktif by remember { mutableStateOf<UserAccount?>(null) }

    LaunchedEffect(Unit) {
        delay(900)
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
            temaAktif = temaAktif,
            onGantiTema = onGantiTema,
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
                    listOf(Color(0xFF065F46), Color(0xFF059669), Color(0xFF10B981))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Surface(
                shape = RoundedCornerShape(32.dp),
                color = Color.White,
                modifier = Modifier
                    .size(104.dp)
                    .border(3.dp, Color(0xFFFACC15), RoundedCornerShape(32.dp))
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text("🐼", fontSize = 54.sp)
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Panda HibahIlmu Native",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                "TALENT MAPPING • WOKWI IOT • MULTI-ROLE",
                color = Color(0xFFFDE047),
                fontSize = 11.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(24.dp))
            CircularProgressIndicator(color = Color(0xFFFACC15), strokeWidth = 3.dp)
        }
    }
}

@Suppress("DEPRECATION")
@Composable
fun LayarLoginOnlineWajibServer(onLoginSuksesDariServer: (UserAccount) -> Unit) {
    val context = LocalContext.current
    val clipboard = LocalClipboardManager.current
    val scope = rememberCoroutineScope()

    var modeRegister by remember { mutableStateOf(false) }
    var pilihRole by remember { mutableStateOf("siswa") }

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
            .background(
                Brush.verticalGradient(
                    listOf(Color(0xFFECFDF5), Color(0xFFE0F2FE), Color(0xFFFFFBEB))
                )
            )
            .padding(18.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(28.dp))
                .background(Color.White)
                .border(2.dp, Color(0xFF10B981), RoundedCornerShape(28.dp))
                .padding(22.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                color = Color(0xFFDCFCE7),
                shape = RoundedCornerShape(50),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Text(
                    text = "🐼 PANDA HIBAHILMU • TALENT MAPPING & WOKWI IOT",
                    color = Color(0xFF065F46),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            Text(
                text = if (modeRegister) "Registrasi Akun Baru" else "Masuk ke HibahIlmu",
                color = Color(0xFF0F172A),
                fontSize = 22.sp,
                fontWeight = FontWeight.Black
            )
            Text(
                text = "Pilih peran untuk membuka Talent Mapping, Kelas, Studio Guru, atau Admin:",
                color = Color(0xFF475569),
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFFF1F5F9))
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
                                else if (kode == "guru") Color(0xFF4F46E5)
                                else if (kode == "admin") Color(0xFFD97706)
                                else Color(0xFF059669)
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
                            color = if (aktif) Color.White else Color(0xFF334155),
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0F172A)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.AccountCircle,
                    contentDescription = "Google",
                    tint = Color(0xFFFACC15),
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Login / Daftar Google (${pilihRole.uppercase()})",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp
                )
            }

            TextButton(onClick = { tampilkanInfoSha1 = !tampilkanInfoSha1 }) {
                Text(
                    text = if (tampilkanInfoSha1) "▲ Sembunyikan SHA-1" else "🔍 Cek SHA-1 Aplikasi",
                    color = Color(0xFF0284C7),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            if (tampilkanInfoSha1) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFFF8FAFC))
                        .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(12.dp))
                        .padding(10.dp)
                ) {
                    Text(
                        "SHA-1: $sha1Terdeteksi",
                        color = Color(0xFF065F46),
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace
                    )
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
                Surface(
                    color = Color(0xFFFEE2E2),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "❌ $pesanErrorServer",
                        color = Color(0xFF991B1B),
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Text(
                    text = if (sedangMemeriksaServer) "Menghubungi Server..." else if (modeRegister) "Daftarkan Akun Baru" else "Masuk (${pilihRole.uppercase()})",
                    color = Color.White,
                    fontWeight = FontWeight.Black
                )
            }

            TextButton(onClick = { modeRegister = !modeRegister }) {
                Text(
                    text = if (modeRegister) "Sudah punya akun? Login" else "Belum punya akun? Daftar Manual",
                    color = Color(0xFF0284C7),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun DashboardNativeUtama(
    user: UserAccount,
    temaAktif: HibahVisualTheme,
    onGantiTema: (HibahVisualTheme) -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val isGuru = user.role.lowercase() == "guru"
    val isAdmin = user.role.lowercase() == "admin"

    var tabAktif by remember { mutableStateOf(0) }
    var xpPengguna by remember { mutableStateOf(user.xp) }
    var bukaPemilihTema by remember { mutableStateOf(false) }

    var sedangMemuatKonten by remember { mutableStateOf(true) }
    var pesanErrorKonten by remember { mutableStateOf<String?>(null) }
    var daftarJenjang by remember { mutableStateOf(listOf<JenjangNativeItem>()) }
    var daftarTryout by remember { mutableStateOf(listOf<PaketTryoutNative>()) }
    var daftarWebinar by remember { mutableStateOf(listOf<WebinarNativeItem>()) }
    var daftarForum by remember { mutableStateOf(listOf<DiskusiPrNativeItem>()) }
    var daftarBeasiswa by remember { mutableStateOf(listOf<PengajuanBeasiswaNative>()) }
    var daftarSemuaAkun by remember { mutableStateOf(listOf<UserAccount>()) }

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
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.horizontalGradient(
                            listOf(temaAktif.headerGradStart, temaAktif.headerGradEnd)
                        )
                    )
            ) {
                // Baris Status Role + Pemilih 4 Tema Cerah + Shortcut Forum
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color.Black.copy(alpha = 0.18f))
                        .padding(horizontal = 14.dp, vertical = 5.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isAdmin) "🛡️ SUPER ADMIN (${daftarSemuaAkun.size} Akun)"
                        else if (isGuru) "👨‍🏫 STUDIO GURU & DOSEN"
                        else "🎓 PELAJAR & TALENT CENTER",
                        color = Color.White,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Black
                    )

                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // TOMBOL GANTI TEMA CERAH
                        Surface(
                            color = Color.White.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.clickable { bukaPemilihTema = !bukaPemilihTema }
                        ) {
                            Text(
                                text = "${temaAktif.ikon} Tema: ${temaAktif.label}",
                                color = Color.White,
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }

                        Surface(
                            color = Color.Black.copy(alpha = 0.28f),
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.clickable { tabAktif = 5 }
                        ) {
                            Text(
                                text = "💬 Forum & Live",
                                color = Color(0xFFFDE68A),
                                fontSize = 9.5.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
                            )
                        }
                    }
                }

                // Bar Pemilih 4 Tema Warna Cerah (Jika Diklik)
                if (bukaPemilihTema) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(Color.White)
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("🎨 Pilih Tema:", fontSize = 11.sp, fontWeight = FontWeight.Black, color = Color(0xFF0F172A))
                        HibahVisualTheme.entries.forEach { tm ->
                            val dipilih = tm == temaAktif
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = if (dipilih) tm.aksenPrimer else Color(0xFFF1F5F9),
                                modifier = Modifier
                                    .border(1.dp, tm.aksenPrimer, RoundedCornerShape(12.dp))
                                    .clickable {
                                        onGantiTema(tm)
                                        bukaPemilihTema = false
                                    }
                            ) {
                                Text(
                                    text = "${tm.ikon} ${tm.label}",
                                    color = if (dipilih) Color.White else Color(0xFF0F172A),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                                .border(2.dp, Color(0xFFFACC15), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("🐼", fontSize = 20.sp)
                        }
                        Column {
                            Text(
                                user.nama,
                                color = Color.White,
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp,
                                maxLines = 1
                            )
                            Text(
                                "${user.email} • ${user.instansi}",
                                color = Color.White.copy(alpha = 0.85f),
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(
                                    Brush.horizontalGradient(
                                        listOf(Color(0xFFE11D48), Color(0xFFF59E0B))
                                    )
                                )
                                .border(
                                    width = 1.5.dp,
                                    color = Color.White,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable { tabAktif = 4 }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = "🔥 Talent Mapping",
                                color = Color.White,
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1
                            )
                        }

                        Surface(
                            color = Color.White.copy(alpha = 0.22f),
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text(
                                text = "⚡ $xpPengguna",
                                color = Color(0xFFFDE047),
                                fontSize = 10.5.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                            )
                        }

                        IconButton(
                            onClick = onLogout,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                Icons.Default.Logout,
                                contentDescription = "Keluar",
                                tint = Color(0xFFFECACA),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }
        },
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 12.dp,
                modifier = Modifier.border(1.dp, Color(0xFFE2E8F0))
            ) {
                NavigationBarItem(
                    selected = tabAktif == 0,
                    onClick = { tabAktif = 0 },
                    icon = { Icon(Icons.Default.MenuBook, contentDescription = null) },
                    label = { Text("Kelas", fontSize = 9.5.sp, fontWeight = FontWeight.Black, maxLines = 1) }
                )
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
                            if (isAdmin) "Admin"
                            else if (isGuru) "Studio"
                            else "Beasiswa",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                    }
                )
                NavigationBarItem(
                    selected = tabAktif == 2,
                    onClick = { tabAktif = 2 },
                    icon = { Icon(Icons.Default.SettingsRemote, contentDescription = null) },
                    label = { Text("IoT Lab", fontSize = 9.5.sp, fontWeight = FontWeight.Black, maxLines = 1) }
                )
                NavigationBarItem(
                    selected = tabAktif == 3,
                    onClick = { tabAktif = 3 },
                    icon = { Icon(Icons.Default.Memory, contentDescription = null) },
                    label = { Text("Wokwi", fontSize = 9.5.sp, fontWeight = FontWeight.Black, maxLines = 1) }
                )
                NavigationBarItem(
                    selected = tabAktif == 4,
                    onClick = { tabAktif = 4 },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color.White,
                        selectedTextColor = Color(0xFFE11D48),
                        indicatorColor = Color(0xFFE11D48),
                        unselectedIconColor = Color(0xFFE11D48),
                        unselectedTextColor = Color(0xFFBE123C)
                    ),
                    icon = { Icon(Icons.Default.AutoAwesome, contentDescription = null) },
                    label = {
                        Text(
                            "🔥 Talent",
                            fontSize = 9.5.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(temaAktif.bgUtama)
                .padding(innerPadding)
        ) {
            when (tabAktif) {
                0 -> MenuRuangBelajarNative(
                    sedangMemuat = sedangMemuatKonten,
                    pesanError = pesanErrorKonten,
                    daftarJenjang = daftarJenjang,
                    onRefresh = { muatUlangDataServer() },
                    onTambahXp = { xpPengguna += it },
                    onBukaTalentMapping = { tabAktif = 4 },
                    onBukaForumLive = { tabAktif = 5 }
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
                2 -> {
                    var subModeTabIot by remember { mutableStateOf(0) }
                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.White)
                                .padding(horizontal = 14.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = { subModeTabIot = 0 },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (subModeTabIot == 0) temaAktif.aksenPrimer else Color(0xFFE2E8F0)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "🔬 Multi-Lab & Osiloskop",
                                    color = if (subModeTabIot == 0) Color.White else Color(0xFF1E293B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1
                                )
                            }
                            Button(
                                onClick = { subModeTabIot = 1 },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (subModeTabIot == 1) temaAktif.aksenPrimer else Color(0xFFE2E8F0)
                                ),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    "📱 Remote Blynk Cloud",
                                    color = if (subModeTabIot == 1) Color.White else Color(0xFF1E293B),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1
                                )
                            }
                        }

                        if (subModeTabIot == 0) {
                            LayarSimulatorMultiLabWebSync(
                                user = user,
                                tokenAktif = tokenAktif,
                                onTambahXp = { xpPengguna += it }
                            )
                        } else {
                            LayarRemoteBlynkIotNative(
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
                        }
                    }
                }
                3 -> {
                    LayarSimulatorWokwiWebSync(
                        user = user,
                        tokenAktif = tokenAktif,
                        onTambahXp = { xpPengguna += it }
                    )
                }
                4 -> {
                    TalentMappingMainScreen(
                        namaUserDefault = user.nama,
                        emailUserDefault = user.email,
                        onBack = { tabAktif = 0 }
                    )
                }
                5 -> MenuForumDanWebinarNative(
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
// KOMPONEN MENU RUANG BELAJAR, STUDIO GURU, ADMIN, BEASISWA & REMOTE BLYNK
// (DENGAN TEMA CERAH DEFAULT YANG SEGAR & KONTRAS TINGGI)
// ============================================================================
@Composable
fun MenuRuangBelajarNative(
    sedangMemuat: Boolean,
    pesanError: String?,
    daftarJenjang: List<JenjangNativeItem>,
    onRefresh: () -> Unit,
    onTambahXp: (Int) -> Unit,
    onBukaTalentMapping: () -> Unit = {},
    onBukaForumLive: () -> Unit = {}
) {
    val tema = LocalHibahTheme.current
    var jenjangDipilih by remember { mutableStateOf<JenjangNativeItem?>(null) }
    var modulDipilih by remember { mutableStateOf<KelasModulNative?>(null) }
    var jawabanKuisMap by remember { mutableStateOf(mapOf<String, Int>()) }

    if (sedangMemuat) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator(color = tema.aksenPrimer)
        }
        return
    }

    if (pesanError != null) {
        Box(modifier = Modifier.fillMaxSize().padding(20.dp), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(pesanError, color = Color(0xFFDC2626), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(12.dp))
                Button(onClick = onRefresh) { Text("Muat Ulang dari Server") }
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onBukaTalentMapping,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
                ) {
                    Text("🔥 Buka Tes Talent Mapping Sekarang", color = Color.White, fontWeight = FontWeight.Black)
                }
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
                    colors = ButtonDefaults.buttonColors(containerColor = tema.aksenPrimer)
                ) {
                    Text("← Kembali ke Daftar Modul", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            item {
                Surface(
                    shape = RoundedCornerShape(22.dp),
                    color = tema.bgKartu,
                    shadowElevation = 3.dp,
                    modifier = Modifier.fillMaxWidth().border(1.5.dp, tema.borderKartu, RoundedCornerShape(22.dp))
                ) {
                    Column(modifier = Modifier.padding(18.dp)) {
                        Text("${mdl.level} • ${mdl.durasi} • KKM ${mdl.syaratSkorMinimal}", color = tema.aksenPrimer, fontSize = 11.sp, fontWeight = FontWeight.Black)
                        Text(mdl.judul, color = tema.teksUtama, fontSize = 20.sp, fontWeight = FontWeight.Black)
                        Text("Pengajar: ${mdl.pembuatNama}", color = tema.teksSekunder, fontSize = 12.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(mdl.rangkumanTeks, color = tema.teksUtama, fontSize = 13.sp)
                    }
                }
            }

            items(mdl.daftarBab) { bab ->
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = tema.bgKartu,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth().border(1.dp, tema.borderKartu, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${bab.judulBab} (${bab.durasiBab})", color = tema.teksUtama, fontWeight = FontWeight.Black, fontSize = 14.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(bab.isiTeks, color = tema.teksSekunder, fontSize = 12.5.sp)
                        if (bab.rumus.isNotBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Surface(color = Color(0xFFF0FDF4), shape = RoundedCornerShape(10.dp)) {
                                Text("📐 Rumus: ${bab.rumus}", color = Color(0xFF065F46), fontSize = 11.sp, fontFamily = FontFamily.Monospace, fontWeight = FontWeight.Bold, modifier = Modifier.padding(10.dp))
                            }
                        }
                    }
                }
            }

            items(mdl.daftarKuis) { kuis ->
                val dipilih: Int? = jawabanKuisMap[kuis.id]
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = tema.bgKartu,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth().border(1.dp, tema.borderKartu, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(kuis.tanya, color = tema.teksUtama, fontWeight = FontWeight.Black, fontSize = 13.5.sp)
                        Spacer(modifier = Modifier.height(10.dp))
                        kuis.opsi.forEachIndexed { idx, teksOpsi ->
                            val isSelected = dipilih != null && dipilih == idx
                            val isCorrect = idx == kuis.jawabanBenar
                            val bgWarna = if (!isSelected) Color(0xFFF1F5F9) else if (isCorrect) Color(0xFFDCFCE7) else Color(0xFFFEE2E2)
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(bgWarna)
                                    .clickable {
                                        val baru = jawabanKuisMap.toMutableMap()
                                        baru[kuis.id] = idx
                                        jawabanKuisMap = baru
                                        if (isCorrect) onTambahXp(25)
                                    }
                                    .padding(12.dp)
                            ) {
                                Text("${('A' + idx)}. $teksOpsi", color = Color(0xFF0F172A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                        if (dipilih != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("💡 Pembahasan: ${kuis.pembahasan}", color = Color(0xFFB45309), fontSize = 11.5.sp, fontWeight = FontWeight.Bold)
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
                    colors = ButtonDefaults.buttonColors(containerColor = tema.aksenPrimer)
                ) {
                    Text("← Kembali ke Semua Jenjang", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
            items(jnj.kelas) { kls ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = tema.bgKartu,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, tema.borderKartu, RoundedCornerShape(20.dp))
                        .clickable { modulDipilih = kls }
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("${kls.level} • ${kls.durasi}", color = tema.aksenPrimer, fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                        Text(kls.judul, color = tema.teksUtama, fontSize = 16.sp, fontWeight = FontWeight.Black)
                        Text("Oleh: ${kls.pembuatNama}", color = tema.teksSekunder, fontSize = 11.5.sp)
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("${kls.daftarBab.size} Bab • ${kls.daftarKuis.size} Kuis → Buka Modul", color = Color(0xFFD97706), fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
        return
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(26.dp))
                    .background(
                        Brush.linearGradient(
                            colors = listOf(
                                Color(0xFFE11D48),
                                Color(0xFFF43F5E),
                                Color(0xFFD97706)
                            )
                        )
                    )
                    .border(2.dp, Color(0xFFFACC15), RoundedCornerShape(26.dp))
                    .clickable { onBukaTalentMapping() }
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFFACC15),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "🔥 FITUR UTAMA & PENTING",
                            color = Color(0xFF0F172A),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    Text(
                        text = "🔒 Confidential PDF",
                        color = Color(0xFFFDE68A),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Text(
                    text = "Tes Talent Mapping® & Peta Kekuatan Diri (4 Kategori Usia)",
                    color = Color.White,
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black
                )
                Text(
                    text = "Kenali bakat alami, buku mewarnai anak (Touchscreen/Stylus), VAK SMP, jurusan kuliah SMA, hingga 34 Bakat & Sertifikat Landscape!",
                    color = Color.White.copy(alpha = 0.92f),
                    fontSize = 11.5.sp
                )

                Button(
                    onClick = onBukaTalentMapping,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFACC15)),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier.fillMaxWidth().height(48.dp)
                ) {
                    Text(
                        text = "🚀 Mulai Tes Talent Mapping Sekarang",
                        color = Color(0xFF0F172A),
                        fontWeight = FontWeight.Black,
                        fontSize = 12.5.sp,
                        maxLines = 1
                    )
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "📚 KURIKULUM & MODUL KELAS INTERAKTIF:",
                    color = tema.aksenPrimer,
                    fontSize = 12.5.sp,
                    fontWeight = FontWeight.Black
                )
                TextButton(onClick = onBukaForumLive) {
                    Text("💬 Tanya PR & Live →", color = Color(0xFF0284C7), fontSize = 11.5.sp, fontWeight = FontWeight.Black)
                }
            }
        }

        items(daftarJenjang) { item ->
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = tema.bgKartu,
                shadowElevation = 3.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.5.dp, tema.borderKartu, RoundedCornerShape(22.dp))
                    .clickable { jenjangDipilih = item }
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.nama, color = tema.teksUtama, fontSize = 17.sp, fontWeight = FontWeight.Black)
                        Surface(
                            color = tema.aksenPrimer.copy(alpha = 0.12f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text(
                                "${item.kelas.size} Modul",
                                color = tema.aksenPrimer,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Black,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                    }
                    Text(item.tagline, color = Color(0xFF0284C7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(item.deskripsi, color = tema.teksSekunder, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
fun MenuStudioPengajarGuruNative(
    guru: UserAccount,
    daftarJenjang: List<JenjangNativeItem>,
    daftarAkun: List<UserAccount>,
    onSuksesSimpanKeServer: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var judulModul by remember { mutableStateOf("") }
    var rangkumanModul by remember { mutableStateOf("") }
    var isiBab by remember { mutableStateOf("") }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth().border(1.5.dp, Color(0xFFCBD5E1), RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("👨‍🏫 STUDIO GURU & DOSEN", color = Color(0xFF4F46E5), fontSize = 12.sp, fontWeight = FontWeight.Black)
                    OutlinedTextField(value = judulModul, onValueChange = { judulModul = it }, label = { Text("Judul Modul Kelas Baru") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = rangkumanModul, onValueChange = { rangkumanModul = it }, label = { Text("Rangkuman Modul") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = isiBab, onValueChange = { isiBab = it }, label = { Text("Uraian Materi Bab 1") }, modifier = Modifier.fillMaxWidth())
                    Button(
                        onClick = {
                            if (judulModul.isBlank()) return@Button
                            scope.launch {
                                val payload = JSONObject().apply {
                                    put("action", "guru_tambah_modul")
                                    put("idJenjang", "sma")
                                    put("judul", judulModul)
                                    put("pembuatId", guru.id)
                                    put("pembuatNama", guru.nama)
                                    put("rangkuman", rangkumanModul)
                                    put("judulBab", "Bab 1")
                                    put("isiBab", isiBab)
                                }
                                IotCloudApiService.kirimAksiKontenKeServer(payload).onSuccess {
                                    Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
                                    judulModul = ""
                                    onSuksesSimpanKeServer()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Terbitkan Modul ke Server", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

@Composable
fun MenuPanelAdminServerNative(
    daftarAkun: List<UserAccount>,
    daftarBeasiswa: List<PengajuanBeasiswaNative>,
    onRefresh: () -> Unit
) {
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
                Text("Data Akun Server (${daftarAkun.size})", color = Color(0xFF0F172A), fontSize = 18.sp, fontWeight = FontWeight.Black)
                Button(onClick = onRefresh) { Text("🔄 Refresh") }
            }
        }
        items(daftarAkun) { acc ->
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(acc.nama, color = Color(0xFF0F172A), fontWeight = FontWeight.Black, fontSize = 15.sp)
                    Text("${acc.email} • ${acc.role.uppercase()}", color = Color(0xFF0284C7), fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(acc.instansi, color = Color(0xFF475569), fontSize = 11.sp)
                }
            }
        }
    }
}

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

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth().border(1.5.dp, Color(0xFFF59E0B), RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("🏆 LEADERBOARD XP PELAJAR NASIONAL", color = Color(0xFFD97706), fontSize = 12.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(8.dp))
                    daftarAkun.sortedByDescending { it.xp }.take(5).forEachIndexed { idx, acc ->
                        Row(
                            modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("#${idx + 1} ${acc.nama}", color = Color(0xFF0F172A), fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
                            Text("🔥 ${acc.xp} XP", color = Color(0xFF059669), fontSize = 12.5.sp, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth().border(1.5.dp, Color(0xFF10B981), RoundedCornerShape(22.dp))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("🎓 AJUKAN BEASISWA WAKAF PENDIDIKAN", color = Color(0xFF059669), fontSize = 12.5.sp, fontWeight = FontWeight.Black)
                    OutlinedTextField(value = alasanBeasiswa, onValueChange = { alasanBeasiswa = it }, label = { Text("Alasan Pengajuan & Prestasi") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = linkBerkasBeasiswa, onValueChange = { linkBerkasBeasiswa = it }, label = { Text("Link Google Drive Rapor / Sertifikat") }, singleLine = true, modifier = Modifier.fillMaxWidth())
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
                                    onRefresh()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kirim Pengajuan Beasiswa", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }
    }
}

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
    var inputBalasanMap by remember { mutableStateOf(mapOf<String, String>()) }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text("🔴 AGENDA WEBINAR NASIONAL:", color = Color(0xFFE11D48), fontSize = 12.sp, fontWeight = FontWeight.Black)
        }
        items(daftarWebinar) { wb ->
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("${wb.kategoriAcara} • ${wb.platform}", color = Color(0xFF059669), fontSize = 10.5.sp, fontWeight = FontWeight.Black)
                    Text(wb.judulAcara, color = Color(0xFF0F172A), fontSize = 15.sp, fontWeight = FontWeight.Black)
                    Text("Narasumber: ${wb.narasumber} | ${wb.tanggalWaktu}", color = Color(0xFF475569), fontSize = 11.5.sp)
                }
            }
        }

        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("💬 TANYA PR / DISKUSI BARU:", color = Color(0xFFD97706), fontSize = 11.5.sp, fontWeight = FontWeight.Black)
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
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF059669)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Kirim Pertanyaan ke Server", color = Color.White, fontWeight = FontWeight.Black)
                    }
                }
            }
        }

        items(daftarForum) { d ->
            val teksBalas: String = inputBalasanMap[d.id] ?: ""
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = Color.White,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth().border(1.5.dp, if (d.solved) Color(0xFF10B981) else Color(0xFFCBD5E1), RoundedCornerShape(18.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("${d.mapel} • Oleh ${d.penanya}", color = Color(0xFF0284C7), fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
                    Text(d.judul, color = Color(0xFF0F172A), fontSize = 14.5.sp, fontWeight = FontWeight.Black)
                    Text(d.pertanyaan, color = Color(0xFF334155), fontSize = 12.sp)

                    Spacer(modifier = Modifier.height(8.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = teksBalas,
                            onValueChange = { nilaiBaru ->
                                val salinan = inputBalasanMap.toMutableMap()
                                salinan[d.id] = nilaiBaru
                                inputBalasanMap = salinan
                            },
                            label = { Text("Tulis jawaban...") },
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
                                        val salinan = inputBalasanMap.toMutableMap()
                                        salinan.remove(d.id)
                                        inputBalasanMap = salinan
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
}

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

    LaunchedEffect(tokenAktif, brokerRunning) {
        while (brokerRunning) {
            val res = IotCloudApiService.ambilStatePinDariServer(tokenAktif)
            res.onSuccess { st ->
                statusKoneksi = "ONLINE • hibahilmu.my.id (${st.terakhirUpdate})"
                st.virtualPins["V0"]?.let { suhuV0 = it }
                st.virtualPins["V1"]?.let { jarakV1 = it }
                st.statusAktuator["V1"]?.let { sakelarSolenoidV2 = it }
                st.statusAktuator["V2"]?.let { sakelarPompaV3 = it }
            }.onFailure {
                statusKoneksi = "Menunggu respon /api/iot/cloud..."
            }
            delay(2200)
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.White,
                shadowElevation = 3.dp,
                modifier = Modifier.fillMaxWidth().border(1.5.dp, Color(0xFF10B981), RoundedCornerShape(22.dp))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("SERVER BROKER HIBAHILMU.MY.ID", color = Color(0xFF059669), fontSize = 10.sp, fontWeight = FontWeight.Black)
                            Text(statusKoneksi, color = Color(0xFF0F172A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
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
                                color = Color.White,
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
                                Icon(Icons.Default.ContentCopy, contentDescription = "Salin", tint = Color(0xFF059669))
                            }
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        item {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f).border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("VIRTUAL PIN V0", color = Color(0xFFD97706), fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text("${suhuV0.roundToInt()} °C", color = Color(0xFF0F172A), fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text("Suhu DHT22", color = Color(0xFF475569), fontSize = 11.sp)
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
                }

                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 2.dp,
                    modifier = Modifier.weight(1f).border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("VIRTUAL PIN V1", color = Color(0xFF0284C7), fontSize = 10.sp, fontWeight = FontWeight.Black)
                        Text("${jarakV1.roundToInt()} cm", color = Color(0xFF0F172A), fontSize = 24.sp, fontWeight = FontWeight.Black)
                        Text("Sonar HC-SR04", color = Color(0xFF475569), fontSize = 11.sp)
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
        }
    }
}