package id.my.hibahilmu.app

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.abs

data class UserAccount(
    val id: String,
    val nama: String,
    val email: String,
    val password: String,
    val role: String, // "siswa" atau "guru"
    val instansi: String,
    val xp: Int = 500
)

data class LogPesanIot(
    val waktu: String,
    val sumber: String,
    val topik: String,
    val payload: String
)

data class CloudIotState(
    val token: String,
    val virtualPins: Map<String, Double>,
    val statusAktuator: Map<String, Boolean>,
    val logPesan: List<LogPesanIot>,
    val terakhirUpdate: String
)

object AuthAndTokenManager {
    private const val PREF_NAME = "hibahilmu_auth_prefs"
    private const val KEY_USERS_JSON = "registered_users_json"
    private const val KEY_ACTIVE_USER = "active_logged_in_user"
    private const val KEY_CUSTOM_TOKEN = "active_blynk_token"

    // Akun Default Bawaan (Sama dengan di Web hibahilmu.my.id)
    private val defaultUsers = listOf(
        UserAccount("usr-siswa-1", "Aisyah Putri", "aisyah@siswa.id", "123456", "siswa", "SMAN 1 Pekanbaru", 650),
        UserAccount("usr-guru-1", "Budi Santoso, M.Pd", "budi@guru.id", "123456", "guru", "hibahilmu.my.id", 1200)
    )

    fun ambilDaftarAkun(context: Context): List<UserAccount> {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_USERS_JSON, null) ?: return defaultUsers
        return try {
            val arr = JSONArray(raw)
            val list = mutableListOf<UserAccount>()
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                list.add(
                    UserAccount(
                        id = o.optString("id"),
                        nama = o.optString("nama"),
                        email = o.optString("email"),
                        password = o.optString("password"),
                        role = o.optString("role", "siswa"),
                        instansi = o.optString("instansi", "Umum"),
                        xp = o.optInt("xp", 500)
                    )
                )
            }
            if (list.isEmpty()) defaultUsers else list
        } catch (e: Exception) {
            defaultUsers
        }
    }

    fun registrasiAkunBaru(context: Context, baru: UserAccount): Result<UserAccount> {
        val sekarang = ambilDaftarAkun(context).toMutableList()
        if (sekarang.any { it.email.equals(baru.email, ignoreCase = true) }) {
            return Result.failure(Exception("Email sudah terdaftar! Silakan langsung Login."))
        }
        sekarang.add(0, baru)
        simpanDaftarAkun(context, sekarang)
        simpanSesiLogin(context, baru)
        return Result.success(baru)
    }

    fun loginAkun(context: Context, email: String, pass: String, rolePilih: String): Result<UserAccount> {
        val semua = ambilDaftarAkun(context)
        val cocok = semua.find {
            it.email.equals(email.trim(), ignoreCase = true) && it.password == pass
        } ?: return Result.failure(Exception("Email atau Password salah! Silakan cek kembali atau Daftar Akun Baru."))

        simpanSesiLogin(context, cocok)
        return Result.success(cocok)
    }

    fun simpanSesiLogin(context: Context, user: UserAccount?) {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        if (user == null) {
            prefs.edit().remove(KEY_ACTIVE_USER).apply()
            return
        }
        val o = JSONObject().apply {
            put("id", user.id)
            put("nama", user.nama)
            put("email", user.email)
            put("password", user.password)
            put("role", user.role)
            put("instansi", user.instansi)
            put("xp", user.xp)
        }
        prefs.edit().putString(KEY_ACTIVE_USER, o.toString()).apply()
    }

    fun ambilSesiLoginAktif(context: Context): UserAccount? {
        val prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        val raw = prefs.getString(KEY_ACTIVE_USER, null) ?: return null
        return try {
            val o = JSONObject(raw)
            UserAccount(
                id = o.optString("id"),
                nama = o.optString("nama"),
                email = o.optString("email"),
                password = o.optString("password"),
                role = o.optString("role", "siswa"),
                instansi = o.optString("instansi", "Umum"),
                xp = o.optInt("xp", 500)
            )
        } catch (e: Exception) {
            null
        }
    }

    private fun simpanDaftarAkun(context: Context, list: List<UserAccount>) {
        val arr = JSONArray()
        list.forEach { u ->
            arr.put(JSONObject().apply {
                put("id", u.id)
                put("nama", u.nama)
                put("email", u.email)
                put("password", u.password)
                put("role", u.role)
                put("instansi", u.instansi)
                put("xp", u.xp)
            })
        }
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_USERS_JSON, arr.toString())
            .apply()
    }

    // Generator Token Unik yang Persis Sama Algoritmanya dengan Web https://www.hibahilmu.my.id
    fun buatTokenBlynkUnik(user: UserAccount, idProject: String): String {
        val slugUser = user.id.lowercase().replace(Regex("[^a-z0-9]"), "").take(10)
        val slugProj = idProject.lowercase().replace(Regex("[^a-z0-9]"), "").take(12)
        val gabung = "${slugUser}_${slugProj}"
        var hash = 0
        for (ch in gabung) {
            hash = (hash shl 5) - hash + ch.code
        }
        val kodeHex = abs(hash).toString(16).padStart(6, '0')
        return "hbh_${slugUser}_${slugProj}_${kodeHex}"
    }

    fun simpanTokenKustom(context: Context, token: String) {
        context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_CUSTOM_TOKEN, token)
            .apply()
    }

    fun ambilTokenKustom(context: Context): String? {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
            .getString(KEY_CUSTOM_TOKEN, null)
    }
}