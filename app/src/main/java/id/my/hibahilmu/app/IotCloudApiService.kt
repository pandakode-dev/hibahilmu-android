package id.my.hibahilmu.app

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

data class ServerContentBundle(
    val jenjangList: List<JenjangNativeItem>,
    val tryoutList: List<PaketTryoutNative>,
    val webinarList: List<WebinarNativeItem>,
    val forumList: List<DiskusiPrNativeItem>,
    val beasiswaList: List<PengajuanBeasiswaNative>,
    val daftarAkunServer: List<UserAccount>
)

object IotCloudApiService {
    const val BASE_WEB_URL = "https://hibahilmu.my.id"
    private val DAFTAR_BASE_URL = listOf(
        "https://hibahilmu.my.id",
        "https://www.hibahilmu.my.id"
    )

    // WEB CLIENT ID RESMI DARI GOOGLE CLOUD CONSOLE ANDA
    const val GOOGLE_WEB_CLIENT_ID =
        "1043466129331-1b2t0m91ku0i6l02b0ve6o45dbg5coof.apps.googleusercontent.com"

    const val IOT_CLOUD_ENDPOINT = "$BASE_WEB_URL/api/iot/cloud"

    private fun eksekusiPostJsonAntiRedirect(
        pathEndpoint: String,
        payload: JSONObject
    ): Pair<Int, String> {
        var errorTerakhir = "Gagal menghubungi server"
        for (base in DAFTAR_BASE_URL) {
            try {
                var targetUrl = "$base$pathEndpoint"
                var batasRedirect = 4

                while (batasRedirect > 0) {
                    val conn = (URL(targetUrl).openConnection() as HttpURLConnection).apply {
                        instanceFollowRedirects = false
                        requestMethod = "POST"
                        doInput = true
                        doOutput = true
                        connectTimeout = 10000
                        readTimeout = 10000
                        setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                        setRequestProperty("Accept", "application/json")
                        setRequestProperty("User-Agent", "HibahIlmu-Android-Native/3.0")
                    }

                    OutputStreamWriter(conn.outputStream, Charsets.UTF_8).use { writer ->
                        writer.write(payload.toString())
                        writer.flush()
                    }

                    val code = conn.responseCode
                    if (code in listOf(301, 302, 303, 307, 308)) {
                        val location = conn.getHeaderField("Location") ?: break
                        targetUrl = if (location.startsWith("http")) location else "$base$location"
                        batasRedirect--
                        continue
                    }

                    val stream = if (code in 200..299) conn.inputStream else conn.errorStream
                    val responseBody = if (stream != null) {
                        BufferedReader(InputStreamReader(stream, Charsets.UTF_8)).use { it.readText() }
                    } else {
                        ""
                    }
                    return Pair(code, responseBody)
                }
            } catch (e: Exception) {
                errorTerakhir = "${e.javaClass.simpleName}: ${e.localizedMessage ?: "Network Error"}"
            }
        }
        throw Exception(errorTerakhir)
    }

    private fun eksekusiGetJson(pathEndpoint: String): String {
        var errorTerakhir = "Gagal mengambil data"
        for (base in DAFTAR_BASE_URL) {
            try {
                val conn = (URL("$base$pathEndpoint").openConnection() as HttpURLConnection).apply {
                    instanceFollowRedirects = true
                    requestMethod = "GET"
                    connectTimeout = 10000
                    readTimeout = 10000
                    setRequestProperty("Accept", "application/json")
                    setRequestProperty("User-Agent", "HibahIlmu-Android-Native/3.0")
                }
                val code = conn.responseCode
                if (code in 200..299) {
                    return BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use {
                        it.readText()
                    }
                } else {
                    errorTerakhir = "HTTP $code pada $base$pathEndpoint"
                }
            } catch (e: Exception) {
                errorTerakhir = "${e.javaClass.simpleName}: ${e.localizedMessage}"
            }
        }
        throw Exception(errorTerakhir)
    }

    // 1A. LOGIN & REGISTER REGULER
    suspend fun autentikasiKeServer(
        action: String,
        email: String,
        password: String,
        nama: String = "",
        role: String = "siswa",
        instansi: String = ""
    ): Result<UserAccount> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("action", action)
                put("email", email.trim())
                put("password", password)
                put("nama", nama.trim())
                put("role", role)
                put("instansi", instansi.trim())
            }

            val (code, bodyText) = eksekusiPostJsonAntiRedirect("/api/mobile/auth", payload)
            val json = JSONObject(bodyText)

            if (code !in 200..299 || !json.optBoolean("ok", false)) {
                val msg = json.optString("message", "Gagal autentikasi (HTTP $code)")
                return@withContext Result.failure(Exception(msg))
            }

            val u = json.getJSONObject("user")
            Result.success(
                UserAccount(
                    id = u.optString("id"),
                    nama = u.optString("nama"),
                    email = u.optString("email"),
                    role = u.optString("role", "siswa"),
                    instansi = u.optString("instansi", "HibahIlmu Indonesia"),
                    status = u.optString("status", "approved"),
                    xp = u.optInt("xp", 500)
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Kendala koneksi ke hibahilmu.my.id: ${e.localizedMessage}"))
        }
    }

    // 1B. LOGIN / REGISTER VIA GOOGLE SSO
    suspend fun loginDenganGoogleSsoKeServer(
        idTokenGoogle: String,
        rolePilihan: String = "siswa",
        instansi: String = "Android Google SSO"
    ): Result<UserAccount> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("action", "google_sso")
                put("idToken", idTokenGoogle)
                put("role", rolePilihan)
                put("instansi", instansi)
            }

            val (code, bodyText) = eksekusiPostJsonAntiRedirect("/api/mobile/auth", payload)
            val json = JSONObject(bodyText)

            if (code !in 200..299 || !json.optBoolean("ok", false)) {
                val msg = json.optString("message", "Verifikasi Google SSO ditolak (HTTP $code)")
                return@withContext Result.failure(Exception(msg))
            }

            val u = json.getJSONObject("user")
            Result.success(
                UserAccount(
                    id = u.optString("id"),
                    nama = u.optString("nama"),
                    email = u.optString("email"),
                    role = u.optString("role", rolePilihan),
                    instansi = u.optString("instansi", "Google Verified Account"),
                    status = u.optString("status", "approved"),
                    xp = u.optInt("xp", 550)
                )
            )
        } catch (e: Exception) {
            Result.failure(Exception("Gagal verifikasi Google ke server: ${e.localizedMessage}"))
        }
    }

    // 2. AMBIL SELURUH KONTEN KURIKULUM + DAFTAR AKUN TERDAFTAR DARI SERVER
    suspend fun ambilKontenUtamaDariServer(): Result<ServerContentBundle> =
        withContext(Dispatchers.IO) {
            try {
                val text = eksekusiGetJson("/api/mobile/content")
                val root = JSONObject(text)

                // Ambil juga daftar akun dari /api/mobile/auth untuk Leaderboard & Monitoring Siswa/Admin
                val listAkunServer = mutableListOf<UserAccount>()
                try {
                    val authText = eksekusiGetJson("/api/mobile/auth")
                    val authRoot = JSONObject(authText)
                    val accArr = authRoot.optJSONArray("daftarEmailTerdaftar")
                    if (accArr != null) {
                        for (i in 0 until accArr.length()) {
                            val a = accArr.getJSONObject(i)
                            listAkunServer.add(
                                UserAccount(
                                    id = a.optString("id", "usr-$i"),
                                    nama = a.optString("nama"),
                                    email = a.optString("email"),
                                    role = a.optString("role", "siswa"),
                                    instansi = a.optString("infoTambahan", "HibahIlmu"),
                                    status = a.optString("status", "approved"),
                                    xp = a.optInt("xp", 500)
                                )
                            )
                        }
                    }
                } catch (_: Exception) {}

                val jenjangArr = root.optJSONArray("jenjangList")
                val listJenjang = mutableListOf<JenjangNativeItem>()
                if (jenjangArr != null) {
                    for (i in 0 until jenjangArr.length()) {
                        val jObj = jenjangArr.getJSONObject(i)
                        val kelasArr = jObj.optJSONArray("kelas")
                        val listKelas = mutableListOf<KelasModulNative>()

                        if (kelasArr != null) {
                            for (k in 0 until kelasArr.length()) {
                                val kObj = kelasArr.getJSONObject(k)

                                val babArr = kObj.optJSONArray("daftarBab")
                                val listBab = mutableListOf<BabMateriNative>()
                                if (babArr != null) {
                                    for (b in 0 until babArr.length()) {
                                        val bObj = babArr.getJSONObject(b)
                                        listBab.add(
                                            BabMateriNative(
                                                id = bObj.optString("id"),
                                                judulBab = bObj.optString("judulBab"),
                                                durasiBab = bObj.optString("durasiBab"),
                                                isiTeks = bObj.optString("isiTeks"),
                                                rumus = bObj.optString("rumus")
                                            )
                                        )
                                    }
                                }

                                val kuisArr = kObj.optJSONArray("daftarKuis")
                                val listKuis = mutableListOf<KuisNativeItem>()
                                if (kuisArr != null) {
                                    for (q in 0 until kuisArr.length()) {
                                        val qObj = kuisArr.getJSONObject(q)
                                        val opsiArr = qObj.optJSONArray("opsi")
                                        val opsiList = mutableListOf<String>()
                                        if (opsiArr != null) {
                                            for (o in 0 until opsiArr.length()) {
                                                opsiList.add(opsiArr.optString(o))
                                            }
                                        }
                                        listKuis.add(
                                            KuisNativeItem(
                                                id = qObj.optString("id"),
                                                tanya = qObj.optString("tanya"),
                                                opsi = opsiList,
                                                jawabanBenar = qObj.optInt("jawabanBenar", 0),
                                                pembahasan = qObj.optString("pembahasan")
                                            )
                                        )
                                    }
                                }

                                listKelas.add(
                                    KelasModulNative(
                                        id = kObj.optString("id"),
                                        judul = kObj.optString("judul"),
                                        pembuatNama = kObj.optString("pembuatNama"),
                                        level = kObj.optString("level"),
                                        durasi = kObj.optString("durasi"),
                                        syaratSkorMinimal = kObj.optInt("syaratSkorMinimal", 75),
                                        rangkumanTeks = kObj.optString("rangkumanTeks"),
                                        daftarBab = listBab,
                                        daftarKuis = listKuis
                                    )
                                )
                            }
                        }

                        listJenjang.add(
                            JenjangNativeItem(
                                id = jObj.optString("id"),
                                nama = jObj.optString("nama"),
                                tagline = jObj.optString("tagline"),
                                deskripsi = jObj.optString("deskripsi"),
                                kelas = listKelas
                            )
                        )
                    }
                }

                val tryoutArr = root.optJSONArray("tryoutList")
                val listTryout = mutableListOf<PaketTryoutNative>()
                if (tryoutArr != null) {
                    for (i in 0 until tryoutArr.length()) {
                        val tObj = tryoutArr.getJSONObject(i)
                        val soalArr = tObj.optJSONArray("daftarSoal")
                        val listSoal = mutableListOf<KuisNativeItem>()
                        if (soalArr != null) {
                            for (s in 0 until soalArr.length()) {
                                val sObj = soalArr.getJSONObject(s)
                                val opArr = sObj.optJSONArray("opsi")
                                val opList = mutableListOf<String>()
                                if (opArr != null) {
                                    for (o in 0 until opArr.length()) opList.add(opArr.optString(o))
                                }
                                listSoal.add(
                                    KuisNativeItem(
                                        id = sObj.optString("id"),
                                        tanya = sObj.optString("pertanyaan"),
                                        opsi = opList,
                                        jawabanBenar = sObj.optInt("jawabanBenar", 0),
                                        pembahasan = sObj.optString("pembahasan")
                                    )
                                )
                            }
                        }
                        listTryout.add(
                            PaketTryoutNative(
                                id = tObj.optString("id"),
                                judul = tObj.optString("judul"),
                                kategoriTes = tObj.optString("kategoriTes"),
                                jenjang = tObj.optString("jenjang"),
                                durasiMenit = tObj.optInt("durasiMenit", 15),
                                daftarSoal = listSoal
                            )
                        )
                    }
                }

                val webArr = root.optJSONArray("webinarList")
                val listWebinar = mutableListOf<WebinarNativeItem>()
                if (webArr != null) {
                    for (i in 0 until webArr.length()) {
                        val wObj = webArr.getJSONObject(i)
                        listWebinar.add(
                            WebinarNativeItem(
                                id = wObj.optString("id"),
                                judulAcara = wObj.optString("judulAcara"),
                                kategoriAcara = wObj.optString("kategoriAcara"),
                                narasumber = wObj.optString("narasumber"),
                                tanggalWaktu = wObj.optString("tanggalWaktu"),
                                platform = wObj.optString("platform"),
                                sedangLive = wObj.optBoolean("sedangLive", false)
                            )
                        )
                    }
                }

                val forumArr = root.optJSONArray("forumList")
                val listForum = mutableListOf<DiskusiPrNativeItem>()
                if (forumArr != null) {
                    for (i in 0 until forumArr.length()) {
                        val fObj = forumArr.getJSONObject(i)
                        val jwbArr = fObj.optJSONArray("jawaban")
                        val listJwb = mutableListOf<JawabanDiskusiNative>()
                        if (jwbArr != null) {
                            for (j in 0 until jwbArr.length()) {
                                val jo = jwbArr.getJSONObject(j)
                                listJwb.add(
                                    JawabanDiskusiNative(
                                        id = jo.optString("id"),
                                        penjawab = jo.optString("penjawab"),
                                        role = jo.optString("role", "Guru"),
                                        isi = jo.optString("isi"),
                                        waktu = jo.optString("waktu", "Baru saja")
                                    )
                                )
                            }
                        }
                        listForum.add(
                            DiskusiPrNativeItem(
                                id = fObj.optString("id"),
                                penanya = fObj.optString("penanya"),
                                jenjang = fObj.optString("jenjang"),
                                mapel = fObj.optString("mapel"),
                                judul = fObj.optString("judul"),
                                pertanyaan = fObj.optString("pertanyaan"),
                                waktu = fObj.optString("waktu"),
                                likes = fObj.optInt("likes", 1),
                                solved = fObj.optBoolean("solved", false),
                                jawaban = listJwb
                            )
                        )
                    }
                }

                val bswArr = root.optJSONArray("beasiswaList")
                val listBeasiswa = mutableListOf<PengajuanBeasiswaNative>()
                if (bswArr != null) {
                    for (i in 0 until bswArr.length()) {
                        val bObj = bswArr.getJSONObject(i)
                        listBeasiswa.add(
                            PengajuanBeasiswaNative(
                                id = bObj.optString("id"),
                                namaPemohon = bObj.optString("namaPemohon"),
                                asalSekolahKampus = bObj.optString("asalSekolahKampus"),
                                jenjang = bObj.optString("jenjang"),
                                alasanPengajuan = bObj.optString("alasanPengajuan"),
                                statusVerifikasi = bObj.optString("statusVerifikasi", "pending"),
                                waktuPengajuan = bObj.optString("waktuPengajuan")
                            )
                        )
                    }
                }

                Result.success(
                    ServerContentBundle(
                        listJenjang,
                        listTryout,
                        listWebinar,
                        listForum,
                        listBeasiswa,
                        listAkunServer
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // 3. AKSI KHUSUS GURU & SISWA KE SERVER (/api/mobile/content)
    suspend fun kirimAksiKontenKeServer(payload: JSONObject): Result<String> =
        withContext(Dispatchers.IO) {
            try {
                val (code, body) = eksekusiPostJsonAntiRedirect("/api/mobile/content", payload)
                val json = JSONObject(body)
                if (code in 200..299 && json.optBoolean("ok", false)) {
                    Result.success(json.optString("message", "Berhasil disimpan ke server!"))
                } else {
                    Result.failure(Exception("Gagal menyimpan ke server (HTTP $code)"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // 4. AKSI KHUSUS ADMIN (UBAH STATUS / HAPUS AKUN DI SERVER)
    suspend fun adminKelolaAkunServer(action: String, userId: String, statusBaru: String = "approved"): Result<Boolean> =
        withContext(Dispatchers.IO) {
            try {
                val payload = JSONObject().apply {
                    put("action", action)
                    put("userId", userId)
                    put("status", statusBaru)
                }
                val (code, _) = eksekusiPostJsonAntiRedirect("/api/mobile/auth", payload)
                Result.success(code in 200..299)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // 5. SINKRONISASI REAL-TIME IOT CLOUD & BLYNK BROKER
    suspend fun ambilStatePinDariServer(token: String): Result<CloudIotState> =
        withContext(Dispatchers.IO) {
            try {
                val encodedToken = URLEncoder.encode(token.trim(), "UTF-8")
                val responseText = eksekusiGetJson("/api/iot/cloud?token=$encodedToken")
                val root = JSONObject(responseText)
                val dataObj = root.optJSONObject("data") ?: JSONObject()

                val vPinsMap = mutableMapOf<String, Double>()
                val vPinsJson = dataObj.optJSONObject("virtualPins")
                if (vPinsJson != null) {
                    for (k in vPinsJson.keys()) {
                        vPinsMap[k] = vPinsJson.optDouble(k, 0.0)
                    }
                }

                val aktuatorMap = mutableMapOf<String, Boolean>()
                val aktJson = dataObj.optJSONObject("statusAktuator")
                if (aktJson != null) {
                    for (k in aktJson.keys()) {
                        aktuatorMap[k] = aktJson.optBoolean(k, false)
                    }
                }

                val logs = mutableListOf<LogPesanIot>()
                val logArr = dataObj.optJSONArray("logPesan")
                if (logArr != null) {
                    for (i in 0 until logArr.length()) {
                        val item = logArr.getJSONObject(i)
                        logs.add(
                            LogPesanIot(
                                waktu = item.optString("waktu", "-"),
                                sumber = item.optString("sumber", "ANDROID_NATIVE"),
                                topik = item.optString("topik", "-"),
                                payload = item.optString("payload", "{}")
                            )
                        )
                    }
                }

                Result.success(
                    CloudIotState(
                        token = dataObj.optString("token", token),
                        virtualPins = vPinsMap,
                        statusAktuator = aktuatorMap,
                        logPesan = logs,
                        terakhirUpdate = dataObj.optString("terakhirUpdate", "Baru saja")
                    )
                )
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    suspend fun kirimPerubahanPinKeServer(
        token: String,
        pemilikNama: String,
        namaProject: String,
        pin: String,
        nilai: Any
    ): Result<CloudIotState> = withContext(Dispatchers.IO) {
        try {
            val payloadJson = JSONObject().apply {
                put("token", token.trim())
                put("pemilikNama", pemilikNama)
                put("namaProject", namaProject)
                put("sumber", "ANDROID_APP")
                put("pin", pin)
                put("value", nilai)
            }
            eksekusiPostJsonAntiRedirect("/api/iot/cloud", payloadJson)
            ambilStatePinDariServer(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}