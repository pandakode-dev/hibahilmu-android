package id.my.hibahilmu.app

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

@Composable
fun TalentMappingMainScreen(
    namaUserDefault: String = "Muhammad Saputra",
    emailUserDefault: String = "saputra@hibahilmu.id",
    onBack: (() -> Unit)? = null
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var jenjangAktif by remember { mutableStateOf(JenjangTalentAndroid.ANAK_TK_SD) }
    var tahapLayar by remember { mutableStateOf("tes") }
    var autoNextAktif by remember { mutableStateOf(true) }

    var namaPeserta by remember { mutableStateOf(namaUserDefault) }
    var instansiPeserta by remember { mutableStateOf("HibahIlmu Indonesia") }
    var statusSimpanCloud by remember { mutableStateOf("") }

    var subTahapAnak by remember { mutableStateOf("kanvas") }
    var misiKanvasAnakId by remember { mutableStateOf("rumah_pohon") }
    var warnaZonePerMisi by remember {
        mutableStateOf(mapOf<String, Map<String, Color>>())
    }
    var goresanPerMisi by remember {
        mutableStateOf(mapOf<String, List<GoresanKuasAnak>>())
    }
    var indeksKartuAnak by remember { mutableStateOf(0) }
    var jawabanKartuAnak by remember { mutableStateOf(mapOf<Int, String>()) }
    var jawabanObservasiOrtu by remember { mutableStateOf(mapOf<Int, Int>()) }

    var indeksSoalRemaja by remember { mutableStateOf(0) }
    var jawabanSmp by remember { mutableStateOf(mapOf<Int, Int>()) }
    var jawabanSmaDewasa by remember { mutableStateOf(mapOf<Int, Int>()) }

    fun hitungHasilLengkapSaatIni(): List<HasilEvaluasiDimensiAndroid> {
        return when (jenjangAktif) {
            JenjangTalentAndroid.ANAK_TK_SD -> {
                val skorMap = mutableMapOf(
                    "seni" to 28, "fisik" to 28, "logika" to 28, "musik" to 28,
                    "bahasa" to 28, "sosial" to 28, "alam" to 28, "mandiri" to 28
                )
                val jmlMisiDiwarnai = warnaZonePerMisi.keys.size + goresanPerMisi.keys.size
                skorMap["seni"] = (skorMap["seni"] ?: 28) + (jmlMisiDiwarnai * 6).coerceAtMost(25)
                skorMap["fisik"] = (skorMap["fisik"] ?: 28) + (goresanPerMisi.values.sumOf { it.size } * 3).coerceAtMost(22)

                DAFTAR_8_MISI_GAMBAR_ANDROID.forEach { m ->
                    if (!warnaZonePerMisi[m.id].isNullOrEmpty() || !goresanPerMisi[m.id].isNullOrEmpty()) {
                        skorMap[m.dimensiUtama] = (skorMap[m.dimensiUtama] ?: 28) + 12
                        skorMap[m.dimensiPendukung] = (skorMap[m.dimensiPendukung] ?: 28) + 7
                    }
                }
                jawabanKartuAnak.values.forEach { kode ->
                    skorMap[kode] = (skorMap[kode] ?: 28) + 12
                }
                TalentMappingRepositoryData.OBSERVASI_ORTU_LIST.forEach { ob ->
                    val v = jawabanObservasiOrtu[ob.no] ?: 0
                    if (v > 0) {
                        skorMap[ob.kodeDimensi] = (skorMap[ob.kodeDimensi] ?: 28) + v * 8
                    }
                }

                TalentMappingRepositoryData.DIMENSI_ANAK_LIST
                    .map { dim ->
                        val mentah = skorMap[dim.id] ?: 30
                        val persen = mentah.coerceIn(40, 98)
                        HasilEvaluasiDimensiAndroid(dim, mentah, persen, 0, "")
                    }
                    .sortedByDescending { it.skorMentah }
                    .mapIndexed { idx, item ->
                        item.copy(
                            peringkat = idx + 1,
                            kategoriLabel = if (idx < 3) "Kekuatan Utama" else if (idx < 6) "Kekuatan Pendukung" else "Area Perlu Stimulasi"
                        )
                    }
            }

            JenjangTalentAndroid.SMP_REMAJA -> {
                val skorMap = mutableMapOf<String, Int>()
                TalentMappingRepositoryData.SOAL_SMP_LIST.forEach { s ->
                    val v = jawabanSmp[s.no] ?: 3
                    skorMap[s.kodeDimensi] = (skorMap[s.kodeDimensi] ?: 0) + v
                }
                TalentMappingRepositoryData.DIMENSI_SMP_LIST
                    .map { dim ->
                        val mentah = skorMap[dim.id] ?: 6
                        val persen = ((mentah / 10f) * 100).toInt().coerceIn(38, 98)
                        HasilEvaluasiDimensiAndroid(dim, mentah, persen, 0, "")
                    }
                    .sortedByDescending { it.skorMentah }
                    .mapIndexed { idx, item ->
                        item.copy(
                            peringkat = idx + 1,
                            kategoriLabel = if (idx < 2) "Kekuatan Utama" else if (idx < 4) "Kekuatan Pendukung" else "Area Perlu Siasat"
                        )
                    }
            }

            else -> {
                val skorMap = mutableMapOf<String, Int>()
                TalentMappingRepositoryData.SOAL_SMA_DAN_DEWASA_LIST.forEach { s ->
                    val v = jawabanSmaDewasa[s.no] ?: 3
                    skorMap[s.kodeDimensi] = (skorMap[s.kodeDimensi] ?: 0) + v
                }
                TalentMappingRepositoryData.DIMENSI_RIASEC_SMA_LIST
                    .map { dim ->
                        val mentah = skorMap[dim.id] ?: 9
                        val persen = ((mentah / 15f) * 100).toInt().coerceIn(40, 99)
                        HasilEvaluasiDimensiAndroid(dim, mentah, persen, 0, "")
                    }
                    .sortedByDescending { it.skorMentah }
                    .mapIndexed { idx, item ->
                        item.copy(
                            peringkat = idx + 1,
                            kategoriLabel = if (idx < 2) "Kekuatan Utama" else if (idx < 4) "Kekuatan Pendukung" else "Area Perlu Siasat"
                        )
                    }
            }
        }
    }

    fun sinkronkanHasilKeCloudWeb(hasilList: List<HasilEvaluasiDimensiAndroid>) {
        statusSimpanCloud = "Menyimpan ke Akun Cloud..."
        scope.launch {
            val sukses = withContext(Dispatchers.IO) {
                try {
                    val url = URL("https://hibahilmu.my.id/api/talent-mapping")
                    val conn = (url.openConnection() as HttpURLConnection).apply {
                        requestMethod = "POST"
                        setRequestProperty("Content-Type", "application/json")
                        connectTimeout = 5000
                        doOutput = true
                    }
                    val topArr = JSONArray()
                    hasilList.take(3).forEach { h ->
                        topArr.put(JSONObject().apply {
                            put("id", h.dimensi.id)
                            put("nama", h.dimensi.nama)
                            put("peringkat", h.peringkat)
                            put("deskripsiKuat", h.dimensi.deskripsiKekuatan)
                        })
                    }
                    val payload = JSONObject().apply {
                        put("action", "save")
                        put("result", JSONObject().apply {
                            put("idHasil", "TM-ANDROID-${System.currentTimeMillis()}")
                            put("namaPeserta", namaPeserta)
                            put("emailPeserta", emailUserDefault)
                            put("instansiPeserta", "$instansiPeserta (${jenjangAktif.judul})")
                            put("tanggalTes", "Hari ini (Android App)")
                            put("nomorDokumenRahasia", "CONF-AND-${System.currentTimeMillis() % 100000}")
                            put("bakatUtama1_7", topArr)
                        })
                    }
                    OutputStreamWriter(conn.outputStream).use { it.write(payload.toString()) }
                    conn.responseCode in 200..299
                } catch (_: Exception) {
                    false
                }
            }
            statusSimpanCloud = if (sukses) "Tersimpan di Akun Cloud Server ✓" else "Tersimpan di Memori Aplikasi ✓"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
            .verticalScroll(rememberScrollState())
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // HEADER BANNER GRADASI MERAH-EMAS
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(
                    Brush.horizontalGradient(
                        listOf(Color(0xFF881337), Color(0xFFBE123C), Color(0xFFB45309))
                    )
                )
                .border(1.5.dp, Color(0xFFFACC15), RoundedCornerShape(24.dp))
                .padding(18.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (onBack != null) {
                            IconButton(
                                onClick = onBack,
                                modifier = Modifier.size(30.dp)
                            ) {
                                Icon(Icons.Default.ArrowBack, contentDescription = "Kembali", tint = Color.White)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                        }
                        Surface(
                            color = Color(0xFFFACC15),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text(
                                text = "🔥 MULTI-JENJANG TALENT MAPPING",
                                color = Color(0xFF0F172A),
                                fontWeight = FontWeight.Black,
                                fontSize = 10.sp,
                                maxLines = 1,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Text(
                        text = "🔒 RAHASIA",
                        color = Color(0xFFFDE68A),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1
                    )
                }

                Text(
                    text = "Panda HibahIlmu Assessment Center",
                    color = Color.White,
                    fontWeight = FontWeight.Black,
                    fontSize = 19.sp
                )
                Text(
                    text = "Pilih kategori usia di bawah. Tersedia buku mewarnai touchscreen/stylus anak, VAK SMP, RIASEC SMA, & Sertifikat Landscape!",
                    color = Color(0xFFFFE4E6),
                    fontSize = 11.5.sp
                )
            }
        }

        // PILIHAN 4 KATEGORI JENJANG USIA (KARTU PROPORSIONAL RAPI)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            JenjangTalentAndroid.entries.forEach { j ->
                val dipilih = jenjangAktif == j
                Surface(
                    shape = RoundedCornerShape(18.dp),
                    color = if (dipilih) Color(0xFF0F172A) else Color.White,
                    shadowElevation = 4.dp,
                    modifier = Modifier
                        .width(235.dp)
                        .border(
                            width = 2.dp,
                            color = if (dipilih) Color(0xFFFACC15) else Color(0xFFCBD5E1),
                            shape = RoundedCornerShape(18.dp)
                        )
                        .clickable {
                            jenjangAktif = j
                            tahapLayar = "tes"
                            indeksSoalRemaja = 0
                        }
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(j.ikon, fontSize = 22.sp)
                            Surface(
                                color = if (dipilih) Color(0xFFFACC15) else Color(0xFFECFDF5),
                                shape = RoundedCornerShape(50)
                            ) {
                                Text(
                                    text = j.rentangUsia,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (dipilih) Color(0xFF0F172A) else Color(0xFF047857),
                                    maxLines = 1,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                        Text(
                            text = j.judul,
                            fontWeight = FontWeight.Black,
                            fontSize = 13.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            color = if (dipilih) Color.White else Color(0xFF0F172A)
                        )
                        Text(
                            text = j.metodeSingkat,
                            fontSize = 10.5.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis,
                            color = if (dipilih) Color(0xFFCBD5E1) else Color(0xFF64748B)
                        )
                    }
                }
            }
        }

        // INPUT IDENTITAS PESERTA & TOMBOL MODE (TIDAK TERPOTONG KE BAWAH)
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = namaPeserta,
                    onValueChange = { namaPeserta = it },
                    label = { Text("Nama Peserta (Tercetak di Sertifikat)") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        onClick = { tahapLayar = "tes" },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (tahapLayar == "tes") Color(0xFFE11D48) else Color(0xFF64748B)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "📝 Kerjakan Tes",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                    Button(
                        onClick = {
                            tahapLayar = "hasil"
                            sinkronkanHasilKeCloudWeb(hitungHasilLengkapSaatIni())
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (tahapLayar == "hasil") Color(0xFF059669) else Color(0xFF0F172A)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = "🏅 Sertifikat & Hasil",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            softWrap = false
                        )
                    }
                }
            }
        }

        if (tahapLayar == "tes") {
            if (jenjangAktif == JenjangTalentAndroid.ANAK_TK_SD) {
                // SUB-TAB ANAK MENGGUNAKAN HORIZONTAL SCROLL CHIP AGAR TEKS 1 BARIS RAPI!
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf(
                        "kanvas" to "🎨 1. Buku Mewarnai (8 Tema)",
                        "kartu" to "🖼️ 2. Pilih Gambar (${jawabanKartuAnak.size}/12)",
                        "observasi" to "📋 3. Observasi Ortu (${jawabanObservasiOrtu.size}/12)"
                    ).forEach { (idSub, labelSub) ->
                        val aktif = subTahapAnak == idSub
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = if (aktif) Color(0xFFDB2777) else Color(0xFF1E293B),
                            modifier = Modifier
                                .border(
                                    width = 1.5.dp,
                                    color = if (aktif) Color(0xFFFACC15) else Color.Transparent,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { subTahapAnak = idSub }
                        ) {
                            Text(
                                text = labelSub,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color.White,
                                maxLines = 1,
                                softWrap = false,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
                            )
                        }
                    }
                }

                if (subTahapAnak == "kanvas") {
                    KidsInteractiveColoringStudio(
                        misiAktifId = misiKanvasAnakId,
                        onPilihMisi = { misiKanvasAnakId = it },
                        warnaZonePerMisi = warnaZonePerMisi,
                        onWarnaiZoneCatTumpah = { mId, zId, warna ->
                            val lama = warnaZonePerMisi[mId] ?: mapOf()
                            val baruMisi = lama.toMutableMap()
                            baruMisi[zId] = warna
                            val baruSemua = warnaZonePerMisi.toMutableMap()
                            baruSemua[mId] = baruMisi
                            warnaZonePerMisi = baruSemua
                        },
                        goresanPerMisi = goresanPerMisi,
                        onTambahGoresanKuas = { mId, goresan ->
                            val lama = goresanPerMisi[mId] ?: listOf()
                            val baruSemua = goresanPerMisi.toMutableMap()
                            baruSemua[mId] = lama + goresan
                            goresanPerMisi = baruSemua
                        },
                        onBersihkanMisiIni = { mId ->
                            val zBaru = warnaZonePerMisi.toMutableMap()
                            zBaru.remove(mId)
                            warnaZonePerMisi = zBaru
                            val gBaru = goresanPerMisi.toMutableMap()
                            gBaru.remove(mId)
                            goresanPerMisi = gBaru
                        },
                        onLanjutKeKartu = { subTahapAnak = "kartu" }
                    )
                } else if (subTahapAnak == "kartu") {
                    val kartu = TalentMappingRepositoryData.KARTU_VISUAL_ANAK[indeksKartuAnak]
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color(0xFF0F172A),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "Kartu Visual Anak #${kartu.id} dari 12 (Awal Kosong & Auto-Next)",
                                color = Color(0xFFFACC15),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = kartu.pertanyaan,
                                color = Color.White,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Black
                            )
                            kartu.opsi.forEach { op ->
                                val dipilih = jawabanKartuAnak[kartu.id] == op.kodeDimensi
                                Surface(
                                    shape = RoundedCornerShape(16.dp),
                                    color = if (dipilih) Color(0xFFFACC15) else Color(0xFF1E293B),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            val baru = jawabanKartuAnak.toMutableMap()
                                            baru[kartu.id] = op.kodeDimensi
                                            jawabanKartuAnak = baru
                                            if (autoNextAktif && indeksKartuAnak < 11) {
                                                scope.launch {
                                                    delay(150)
                                                    indeksKartuAnak += 1
                                                }
                                            }
                                        }
                                ) {
                                    Row(
                                        modifier = Modifier.padding(14.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        Text(op.ikon, fontSize = 26.sp)
                                        Text(
                                            text = op.teks,
                                            fontWeight = FontWeight.Black,
                                            fontSize = 13.5.sp,
                                            color = if (dipilih) Color(0xFF0F172A) else Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Surface(
                        shape = RoundedCornerShape(24.dp),
                        color = Color.White,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "📋 Pengamatan Orang Tua / Guru (12 Indikator Perilaku Harian)",
                                fontWeight = FontWeight.Black,
                                fontSize = 15.sp,
                                color = Color(0xFF0F172A)
                            )
                            TalentMappingRepositoryData.OBSERVASI_ORTU_LIST.forEach { ob ->
                                val nilai = jawabanObservasiOrtu[ob.no]
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(Color(0xFFF8FAFC))
                                        .padding(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "${ob.no}. ${ob.pernyataan}",
                                        fontSize = 12.5.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = Color(0xFF1E293B)
                                    )
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        listOf(1 to "Belum", 2 to "Kadang", 3 to "Sering!").forEach { (v, l) ->
                                            val aktif = nilai == v
                                            Surface(
                                                shape = RoundedCornerShape(10.dp),
                                                color = if (aktif) Color(0xFF059669) else Color(0xFFE2E8F0),
                                                modifier = Modifier
                                                    .weight(1f)
                                                    .clickable {
                                                        val baru = jawabanObservasiOrtu.toMutableMap()
                                                        baru[ob.no] = v
                                                        jawabanObservasiOrtu = baru
                                                    }
                                            ) {
                                                Box(
                                                    contentAlignment = Alignment.Center,
                                                    modifier = Modifier.padding(vertical = 9.dp)
                                                ) {
                                                    Text(
                                                        text = l,
                                                        fontSize = 11.sp,
                                                        maxLines = 1,
                                                        softWrap = false,
                                                        color = if (aktif) Color.White else Color(0xFF0F172A),
                                                        fontWeight = FontWeight.Black
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Button(
                                onClick = {
                                    tahapLayar = "hasil"
                                    sinkronkanHasilKeCloudWeb(hitungHasilLengkapSaatIni())
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier.fillMaxWidth().height(50.dp)
                            ) {
                                Text(
                                    text = "🎉 Lihat Sertifikat Landscape & Hasil Anak!",
                                    fontWeight = FontWeight.Black,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            } else {
                val daftarSoal = if (jenjangAktif == JenjangTalentAndroid.SMP_REMAJA)
                    TalentMappingRepositoryData.SOAL_SMP_LIST
                else
                    TalentMappingRepositoryData.SOAL_SMA_DAN_DEWASA_LIST

                val idxAman = indeksSoalRemaja.coerceIn(0, daftarSoal.size - 1)
                val soalAktif = daftarSoal[idxAman]
                val mapJawabanAktif = if (jenjangAktif == JenjangTalentAndroid.SMP_REMAJA) jawabanSmp else jawabanSmaDewasa

                Surface(
                    shape = RoundedCornerShape(24.dp),
                    color = Color(0xFF0F172A),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(18.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "Soal #${soalAktif.no}/${daftarSoal.size} (Terisi: ${mapJawabanAktif.size})",
                                color = Color(0xFFFACC15),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = if (mapJawabanAktif[soalAktif.no] != null) "✓ Dijawab (${mapJawabanAktif[soalAktif.no]})" else "○ Kosong",
                                color = Color(0xFF34D399),
                                fontSize = 11.5.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "\"${soalAktif.pernyataan}\"",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )

                        listOf(
                            5 to "5 — Sangat Menggambarkan Diriku / Sangat Kuat!",
                            4 to "4 — Cocok / Setuju",
                            3 to "3 — Netral / Kadang-Kadang",
                            2 to "2 — Kurang Cocok",
                            1 to "1 — Sangat Tidak Cocok"
                        ).forEach { (nilai, label) ->
                            val dipilih = mapJawabanAktif[soalAktif.no] == nilai
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = if (dipilih) Color(0xFF10B981) else Color(0xFF1E293B),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        if (jenjangAktif == JenjangTalentAndroid.SMP_REMAJA) {
                                            val baru = jawabanSmp.toMutableMap()
                                            baru[soalAktif.no] = nilai
                                            jawabanSmp = baru
                                        } else {
                                            val baru = jawabanSmaDewasa.toMutableMap()
                                            baru[soalAktif.no] = nilai
                                            jawabanSmaDewasa = baru
                                        }
                                        if (autoNextAktif && idxAman < daftarSoal.size - 1) {
                                            scope.launch {
                                                delay(150)
                                                indeksSoalRemaja = idxAman + 1
                                            }
                                        }
                                    }
                            ) {
                                Text(
                                    text = label,
                                    color = if (dipilih) Color(0xFF0F172A) else Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 12.5.sp,
                                    modifier = Modifier.padding(14.dp)
                                )
                            }
                        }

                        Row(
                            horizontalArrangement = Arrangement.SpaceBetween,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(
                                enabled = idxAman > 0,
                                onClick = { indeksSoalRemaja = (idxAman - 1).coerceAtLeast(0) }
                            ) {
                                Text("⬅ Sebelumnya", color = Color.White)
                            }
                            Button(
                                onClick = {
                                    tahapLayar = "hasil"
                                    sinkronkanHasilKeCloudWeb(hitungHasilLengkapSaatIni())
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFACC15))
                            ) {
                                Text("Selesai & Lihat Hasil ➔", color = Color(0xFF0F172A), fontWeight = FontWeight.Black)
                            }
                        }
                    }
                }
            }
        } else {
            // ================================================================
            // LAYAR HASIL & 2 TOMBOL PRINT PDF FULL-WIDTH (ANTI TEKS KE BAWAH)
            // ================================================================
            val hasilUrut = hitungHasilLengkapSaatIni()

            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color(0xFF0F172A),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "🖨️ 2 Pilihan Cetak / Simpan PDF Terpisah (${statusSimpanCloud.ifBlank { "Siap Cetak" }})",
                        color = Color(0xFF34D399),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black
                    )

                    Button(
                        onClick = {
                            TalentMappingPdfGenerator.cetakSertifikatLandscapePdf(
                                context = context,
                                namaPeserta = namaPeserta,
                                instansi = instansiPeserta,
                                jenjang = jenjangAktif,
                                hasilUrut = hasilUrut
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFACC15)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "🏅 1. Print Sertifikat Saja (A4 Landscape)",
                            color = Color(0xFF0F172A),
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                    }

                    Button(
                        onClick = {
                            TalentMappingPdfGenerator.cetakLaporanPortraitPdf(
                                context = context,
                                namaPeserta = namaPeserta,
                                instansi = instansiPeserta,
                                jenjang = jenjangAktif,
                                hasilUrut = hasilUrut
                            )
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48)),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.fillMaxWidth().height(48.dp)
                    ) {
                        Text(
                            text = "📄 2. Print Laporan Hasil Saja (A4 Portrait)",
                            color = Color.White,
                            fontSize = 12.5.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                    }
                }
            }

            Surface(
                shape = RoundedCornerShape(24.dp),
                color = Color(0xFFFFFDF5),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(3.dp, Color(0xFFD97706), RoundedCornerShape(24.dp))
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "🐼 SERTIFIKAT PENGHARGAAN BAKAT EMAS",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFB45309)
                    )
                    Text(
                        text = namaPeserta.ifBlank { "Peserta Berbakat" },
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFF0F172A)
                    )
                    Text(
                        text = "🌟 Gelar Utama: ${hasilUrut.firstOrNull()?.dimensi?.julukan ?: ""}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFFE11D48)
                    )
                }
            }

            Text(
                text = "🌟 3 Kekuatan Utama & Rencana Aksi (Harus Ngapain Saja):",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = Color(0xFF0F172A)
            )
            hasilUrut.take(3).forEach { item ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color.White,
                    shadowElevation = 3.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, Color(item.dimensi.warnaHex), RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "#${item.peringkat}. ${item.dimensi.ikon} ${item.dimensi.nama} (${item.persen}%)",
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp,
                            color = Color(0xFF0F172A)
                        )
                        Text(
                            text = item.dimensi.deskripsiKekuatan,
                            fontSize = 12.sp,
                            color = Color(0xFF334155)
                        )
                        Text(
                            text = "✅ Harus Ngapain Saja:",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.5.sp,
                            color = Color(0xFF059669)
                        )
                        item.dimensi.harusNgapainSaja.forEach { aksi ->
                            Text("• $aksi", fontSize = 11.5.sp, color = Color(0xFF1E293B))
                        }
                        Text(
                            text = "🎯 Rekomendasi Les / Ekskul / Jurusan:",
                            fontWeight = FontWeight.Black,
                            fontSize = 11.5.sp,
                            color = Color(0xFF2563EB)
                        )
                        item.dimensi.rekomendasiLesAtauJurusan.forEach { rek ->
                            Text("• $rek", fontSize = 11.5.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B))
                        }
                    }
                }
            }

            Text(
                text = "🛡️ Area Lemah & Solusi Agar Tidak Menghambat:",
                fontWeight = FontWeight.Black,
                fontSize = 15.sp,
                color = Color(0xFF991B1B)
            )
            hasilUrut.takeLast(2).forEach { lemah ->
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFFFEF2F2),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, Color(0xFFFECACA), RoundedCornerShape(20.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "#${lemah.peringkat}. ${lemah.dimensi.ikon} ${lemah.dimensi.nama} (${lemah.persen}%)",
                            fontWeight = FontWeight.Black,
                            fontSize = 13.5.sp,
                            color = Color(0xFF7F1D1D)
                        )
                        Text(
                            text = "⚠️ Risiko: ${lemah.dimensi.dampakJikaLemah}",
                            fontSize = 11.5.sp,
                            color = Color(0xFF991B1B)
                        )
                        Text(
                            text = "💡 Cara Mengatasi: ${lemah.dimensi.solusiAgarTidakMenghambat}",
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF065F46)
                        )
                    }
                }
            }
        }
    }
}