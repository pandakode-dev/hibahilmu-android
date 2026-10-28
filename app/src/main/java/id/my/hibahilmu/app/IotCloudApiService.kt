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
    val forumList: List<DiskusiPrNativeItem>
)

object IotCloudApiService {
    // Menggunakan domain utama tanpa www sesuai tangkapan layar browser Anda (plus fallback www)
    const val BASE_WEB_URL = "https://hibahilmu.my.id"
    private val DAFTAR_BASE_URL = listOf(
        "https://hibahilmu.my.id",
        "https://www.hibahilmu.my.id"
    )

    const val IOT_CLOUD_ENDPOINT = "$BASE_WEB_URL/api/iot/cloud"

    // Helper HTTP POST yang tahan terhadap Redirect 301/302/307/308 Vercel
    private fun eksekusiPostJsonAntiRedirect(pathEndpoint: String, payload: JSONObject): Pair<Int, String> {
        var errorTerakhir = "Gagal menghubungi server"
        for (base in DAFTAR_BASE_URL) {
            try {
                var targetUrl = "$base$pathEndpoint"
                var batasRedirect = 4

                while (batasRedirect > 0) {
                    val conn = (URL(targetUrl).openConnection() as HttpURLConnection).apply {
                        instanceFollowRedirects = false // Kita tangani manual agar POST tidak berubah jadi GET
                        requestMethod = "POST"
                        doInput = true
                        doOutput = true
                        connectTimeout = 10000
                        readTimeout = 10000
                        setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                        setRequestProperty("Accept", "application/json")
                        setRequestProperty("User-Agent", "HibahIlmu-Android-Native/2.0")
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

    // Helper HTTP GET yang otomatis mengikuti domain aktif
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
                    setRequestProperty("User-Agent", "HibahIlmu-Android-Native/2.0")
                }
                val code = conn.responseCode
                if (code in 200..299) {
                    return BufferedReader(InputStreamReader(conn.inputStream, Charsets.UTF_8)).use { it.readText() }
                } else {
                    errorTerakhir = "HTTP $code pada $base$pathEndpoint"
                }
            } catch (e: Exception) {
                errorTerakhir = "${e.javaClass.simpleName}: ${e.localizedMessage}"
            }
        }
        throw Exception(errorTerakhir)
    }

    // ========================================================================
    // 1. LOGIN & REGISTER WAJIB ONLINE KE SERVER HIBAHILMU.MY.ID
    // ========================================================================
    suspend fun autentikasiKeServer(
        action: String, // "login" atau "register"
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
                val msg = json.optString(
                    "message",
                    json.optString("error", "Gagal autentikasi ke hibahilmu.my.id (HTTP $code)")
                )
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
                    xp = u.optInt("xp", 500)
                )
            )
        } catch (e: Exception) {
            Result.failure(
                Exception(
                    "Kendala koneksi ke hibahilmu.my.id: ${e.localizedMessage}. Pastikan izin INTERNET di AndroidManifest.xml sudah terpasang!"
                )
            )
        }
    }

    // ========================================================================
    // 2. AMBIL DATA KELAS, MODUL, KUIS, TRYOUT CBT & FORUM DARI SERVER
    // ========================================================================
    suspend fun ambilKontenUtamaDariServer(): Result<ServerContentBundle> = withContext(Dispatchers.IO) {
        try {
            val text = eksekusiGetJson("/api/mobile/content")
            val root = JSONObject(text)

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
                    listForum.add(
                        DiskusiPrNativeItem(
                            id = fObj.optString("id"),
                            penanya = fObj.optString("penanya"),
                            jenjang = fObj.optString("jenjang"),
                            mapel = fObj.optString("mapel"),
                            judul = fObj.optString("judul"),
                            pertanyaan = fObj.optString("pertanyaan"),
                            waktu = fObj.optString("waktu"),
                            likes = fObj.optInt("likes", 1)
                        )
                    )
                }
            }

            Result.success(ServerContentBundle(listJenjang, listTryout, listWebinar, listForum))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun kirimPertanyaanForumKeServer(
        penanya: String,
        mapel: String,
        judul: String,
        pertanyaan: String
    ): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val payload = JSONObject().apply {
                put("action", "tambah_diskusi")
                put("penanya", penanya)
                put("mapel", mapel)
                put("judul", judul)
                put("pertanyaan", pertanyaan)
            }
            val (code, _) = eksekusiPostJsonAntiRedirect("/api/mobile/content", payload)
            Result.success(code in 200..299)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ========================================================================
    // 3. SINKRONISASI REAL-TIME IOT CLOUD & BLYNK BROKER
    // ========================================================================
    suspend fun ambilStatePinDariServer(token: String): Result<CloudIotState> = withContext(Dispatchers.IO) {
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