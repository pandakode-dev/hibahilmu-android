package id.my.hibahilmu.app

import android.content.Context
import kotlin.math.abs

data class UserAccount(
    val id: String,
    val nama: String,
    val email: String,
    val role: String, // "siswa", "guru", atau "admin"
    val instansi: String,
    val status: String = "approved",
    val xp: Int = 500
)

data class BabMateriNative(
    val id: String,
    val judulBab: String,
    val durasiBab: String,
    val isiTeks: String,
    val rumus: String
)

data class KuisNativeItem(
    val id: String,
    val tanya: String,
    val opsi: List<String>,
    val jawabanBenar: Int,
    val pembahasan: String
)

data class KelasModulNative(
    val id: String,
    val judul: String,
    val pembuatNama: String,
    val level: String,
    val durasi: String,
    val syaratSkorMinimal: Int,
    val rangkumanTeks: String,
    val daftarBab: List<BabMateriNative>,
    val daftarKuis: List<KuisNativeItem>
)

data class JenjangNativeItem(
    val id: String,
    val nama: String,
    val tagline: String,
    val deskripsi: String,
    val kelas: List<KelasModulNative>
)

data class PaketTryoutNative(
    val id: String,
    val judul: String,
    val kategoriTes: String,
    val jenjang: String,
    val durasiMenit: Int,
    val daftarSoal: List<KuisNativeItem>
)

data class WebinarNativeItem(
    val id: String,
    val judulAcara: String,
    val kategoriAcara: String,
    val narasumber: String,
    val tanggalWaktu: String,
    val platform: String,
    val sedangLive: Boolean
)

data class JawabanDiskusiNative(
    val id: String,
    val penjawab: String,
    val role: String,
    val isi: String,
    val waktu: String
)

data class DiskusiPrNativeItem(
    val id: String,
    val penanya: String,
    val jenjang: String,
    val mapel: String,
    val judul: String,
    val pertanyaan: String,
    val waktu: String,
    val likes: Int,
    val solved: Boolean = false,
    val jawaban: List<JawabanDiskusiNative> = emptyList()
)

data class PengajuanBeasiswaNative(
    val id: String,
    val namaPemohon: String,
    val asalSekolahKampus: String,
    val jenjang: String,
    val alasanPengajuan: String,
    val statusVerifikasi: String,
    val waktuPengajuan: String
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

object SessionTokenStore {
    private const val PREF_NAME = "hibahilmu_native_prefs"
    private const val KEY_CUSTOM_TOKEN = "active_blynk_token"

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