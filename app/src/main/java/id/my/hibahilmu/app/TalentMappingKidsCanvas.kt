package id.my.hibahilmu.app

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin

data class GoresanKuasAnak(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

data class TitikCatTumpahZone(
    val idZone: String,
    val namaZone: String,
    val centerRatioX: Float,
    val centerRatioY: Float
)

data class MisiGambarAnakAndroid(
    val id: String,
    val judul: String,
    val subJudul: String,
    val dimensiUtama: String,
    val dimensiPendukung: String,
    val daftarZoneCat: List<TitikCatTumpahZone>
)

val DAFTAR_8_MISI_GAMBAR_ANDROID = listOf(
    MisiGambarAnakAndroid(
        id = "rumah_pohon",
        judul = "🏡 1. Rumah Impian & Pegunungan",
        subJudul = "Warnai matahari, gunung kembar, atap rumah, pintu, dan pohon apel!",
        dimensiUtama = "seni",
        dimensiPendukung = "alam",
        daftarZoneCat = listOf(
            TitikCatTumpahZone("langit", "Langit", 0.50f, 0.12f),
            TitikCatTumpahZone("matahari", "Matahari", 0.12f, 0.20f),
            TitikCatTumpahZone("awan", "Awan Putih", 0.65f, 0.18f),
            TitikCatTumpahZone("gunung_kiri", "Gunung Kiri", 0.20f, 0.55f),
            TitikCatTumpahZone("gunung_kanan", "Gunung Kanan", 0.52f, 0.50f),
            TitikCatTumpahZone("atap", "Atap Rumah", 0.42f, 0.38f),
            TitikCatTumpahZone("dinding", "Dinding Rumah", 0.42f, 0.68f),
            TitikCatTumpahZone("pintu", "Pintu Rumah", 0.42f, 0.78f),
            TitikCatTumpahZone("jendela_kiri", "Jendela Kiri", 0.34f, 0.62f),
            TitikCatTumpahZone("jendela_kanan", "Jendela Kanan", 0.50f, 0.62f),
            TitikCatTumpahZone("daun_pohon", "Daun Pohon Rindang", 0.82f, 0.40f),
            TitikCatTumpahZone("batang_pohon", "Batang Pohon", 0.82f, 0.72f),
            TitikCatTumpahZone("rumput", "Halaman Rumput", 0.50f, 0.92f)
        )
    ),
    MisiGambarAnakAndroid(
        id = "roket_robot",
        judul = "🚀 2. Roket Antariksa & Robot",
        subJudul = "Warnai planet bercincin, roket luar angkasa, api pendorong, dan robot pintar!",
        dimensiUtama = "logika",
        dimensiPendukung = "mandiri",
        daftarZoneCat = listOf(
            TitikCatTumpahZone("planet", "Planet Saturnus", 0.16f, 0.28f),
            TitikCatTumpahZone("cincin_planet", "Cincin Planet", 0.16f, 0.34f),
            TitikCatTumpahZone("ucuk_roket", "Hidung Roket", 0.42f, 0.22f),
            TitikCatTumpahZone("badan_roket", "Badan Roket", 0.42f, 0.48f),
            TitikCatTumpahZone("jendela_roket", "Kaca Astronot", 0.42f, 0.44f),
            TitikCatTumpahZone("sirip_kiri", "Sirip Kiri", 0.31f, 0.64f),
            TitikCatTumpahZone("sirip_kanan", "Sirip Kanan", 0.53f, 0.64f),
            TitikCatTumpahZone("api_roket", "Semburan Api", 0.42f, 0.80f),
            TitikCatTumpahZone("kepala_robot", "Kepala Robot", 0.77f, 0.34f),
            TitikCatTumpahZone("layar_robot", "Layar Dada Robot", 0.77f, 0.60f),
            TitikCatTumpahZone("badan_robot", "Badan Robot", 0.77f, 0.72f)
        )
    ),
    MisiGambarAnakAndroid(
        id = "taman_kupu",
        judul = "🦋 3. Kupu-Kupu Raja & Taman Bunga",
        subJudul = "Warnai sayap kupu-kupu berpola indah dan kelopak bunga mekar!",
        dimensiUtama = "seni",
        dimensiPendukung = "alam",
        daftarZoneCat = listOf(
            TitikCatTumpahZone("sayap_kiri_atas", "Sayap Kiri Atas", 0.35f, 0.32f),
            TitikCatTumpahZone("sayap_kanan_atas", "Sayap Kanan Atas", 0.65f, 0.32f),
            TitikCatTumpahZone("sayap_kiri_bawah", "Sayap Kiri Bawah", 0.38f, 0.60f),
            TitikCatTumpahZone("sayap_kanan_bawah", "Sayap Kanan Bawah", 0.62f, 0.60f),
            TitikCatTumpahZone("badan_kupu", "Badan Kupu-Kupu", 0.50f, 0.45f),
            TitikCatTumpahZone("bunga_kiri", "Kelopak Bunga Kiri", 0.16f, 0.60f),
            TitikCatTumpahZone("inti_bunga_kiri", "Inti Bunga Kiri", 0.16f, 0.60f),
            TitikCatTumpahZone("bunga_kanan", "Kelopak Bunga Kanan", 0.84f, 0.60f),
            TitikCatTumpahZone("rumput_taman", "Rumput Taman", 0.50f, 0.92f)
        )
    ),
    MisiGambarAnakAndroid(
        id = "dino_purba",
        judul = "🦕 4. Dinosaurus Ramah & Gunung Api",
        subJudul = "Warnai dinosaurus leher panjang, bercak punggungnya, dan gunung berapi!",
        dimensiUtama = "alam",
        dimensiPendukung = "fisik",
        daftarZoneCat = listOf(
            TitikCatTumpahZone("badan_dino", "Tubuh Dinosaurus", 0.35f, 0.66f),
            TitikCatTumpahZone("leher_dino", "Leher & Kepala Dino", 0.24f, 0.32f),
            TitikCatTumpahZone("bercak_dino", "Bercak Punggung", 0.40f, 0.66f),
            TitikCatTumpahZone("gunung_api", "Gunung Berapi", 0.80f, 0.65f),
            TitikCatTumpahZone("lava_api", "Lelehan Lava", 0.80f, 0.46f),
            TitikCatTumpahZone("awan_purba", "Awan Asap", 0.80f, 0.22f),
            TitikCatTumpahZone("tanah_purba", "Padang Purba", 0.50f, 0.92f)
        )
    ),
    MisiGambarAnakAndroid(
        id = "bawah_laut",
        judul = "🐠 5. Kapal Selam & Ikan Bawah Laut",
        subJudul = "Warnai kapal selam penjelajah, ikan tropis bersirip, dan bintang laut!",
        dimensiUtama = "alam",
        dimensiPendukung = "logika",
        daftarZoneCat = listOf(
            TitikCatTumpahZone("air_laut", "Air Samudra", 0.50f, 0.18f),
            TitikCatTumpahZone("kapal_selam", "Lambung Kapal Selam", 0.28f, 0.42f),
            TitikCatTumpahZone("jendela_kapal", "Jendela Kapal Selam", 0.28f, 0.42f),
            TitikCatTumpahZone("badan_ikan", "Ikan Tropis", 0.70f, 0.52f),
            TitikCatTumpahZone("ekor_ikan", "Sirip Ekor Ikan", 0.86f, 0.52f),
            TitikCatTumpahZone("bintang_laut", "Bintang Laut", 0.48f, 0.82f),
            TitikCatTumpahZone("pasir_laut", "Pasir Dasar Laut", 0.50f, 0.94f)
        )
    ),
    MisiGambarAnakAndroid(
        id = "masjid_indah",
        judul = "🕌 6. Masjid Megah Berkubah & Menara",
        subJudul = "Warnai kubah emas masjid, dinding utama, pintu lengkung, dan 2 menara!",
        dimensiUtama = "sosial",
        dimensiPendukung = "bahasa",
        daftarZoneCat = listOf(
            TitikCatTumpahZone("kubah_utama", "Kubah Utama Masjid", 0.50f, 0.36f),
            TitikCatTumpahZone("dinding_masjid", "Bangunan Utama Masjid", 0.50f, 0.68f),
            TitikCatTumpahZone("pintu_masjid", "Pintu Gerbang Lengkung", 0.50f, 0.78f),
            TitikCatTumpahZone("menara_kiri", "Menara Kiri", 0.20f, 0.60f),
            TitikCatTumpahZone("kubah_kiri", "Kubah Menara Kiri", 0.20f, 0.30f),
            TitikCatTumpahZone("menara_kanan", "Menara Kanan", 0.80f, 0.60f),
            TitikCatTumpahZone("kubah_kanan", "Kubah Menara Kanan", 0.80f, 0.30f),
            TitikCatTumpahZone("halaman_masjid", "Taman Masjid", 0.50f, 0.93f)
        )
    ),
    MisiGambarAnakAndroid(
        id = "panggung_musik",
        judul = "🎸 7. Panggung Gitar Akustik & Piano",
        subJudul = "Warnai bodi gitar kayu, lubang suara, tuts piano klasik, dan bintang nada!",
        dimensiUtama = "musik",
        dimensiPendukung = "bahasa",
        daftarZoneCat = listOf(
            TitikCatTumpahZone("bodi_gitar", "Bodi Gitar Akustik", 0.44f, 0.35f),
            TitikCatTumpahZone("lubang_gitar", "Lubang Suara Gitar", 0.44f, 0.35f),
            TitikCatTumpahZone("leher_gitar", "Stang Senar Gitar", 0.65f, 0.35f),
            TitikCatTumpahZone("papan_piano", "Tuts Piano Putih", 0.50f, 0.75f),
            TitikCatTumpahZone("tuts_hitam", "Tuts Piano Hitam", 0.50f, 0.66f),
            TitikCatTumpahZone("bintang_musik", "Bintang Panggung", 0.18f, 0.32f)
        )
    ),
    MisiGambarAnakAndroid(
        id = "bebas",
        judul = "✨ 8. Kanvas Lukis Imajinasi Bebas",
        subJudul = "Gunakan kuas stylus atau jari untuk menggambar karya imajinasi bebasmu!",
        dimensiUtama = "mandiri",
        dimensiPendukung = "seni",
        daftarZoneCat = listOf(
            TitikCatTumpahZone("latar_bebas", "Latar Kanvas", 0.50f, 0.50f)
        )
    )
)

@Composable
fun KidsInteractiveColoringStudio(
    misiAktifId: String,
    onPilihMisi: (String) -> Unit,
    warnaZonePerMisi: Map<String, Map<String, Color>>,
    onWarnaiZoneCatTumpah: (String, String, Color) -> Unit,
    goresanPerMisi: Map<String, List<GoresanKuasAnak>>,
    onTambahGoresanKuas: (String, GoresanKuasAnak) -> Unit,
    onBersihkanMisiIni: (String) -> Unit,
    onLanjutKeKartu: () -> Unit
) {
    var modeAlat by remember { mutableStateOf("cat_tumpah") }
    var warnaPilihan by remember { mutableStateOf(Color(0xFFEF4444)) }
    var tebalKuas by remember { mutableStateOf(14f) }
    var titikGoresSaatIni by remember { mutableStateOf(listOf<Offset>()) }

    val misiObj = DAFTAR_8_MISI_GAMBAR_ANDROID.find { it.id == misiAktifId }
        ?: DAFTAR_8_MISI_GAMBAR_ANDROID[0]
    val mapZoneMisiIni = warnaZonePerMisi[misiAktifId] ?: mapOf()
    val listGoresanMisiIni = goresanPerMisi[misiAktifId] ?: listOf()

    val daftarPaletWarna = listOf(
        Color(0xFFEF4444), // Merah Cerah
        Color(0xFFF97316), // Oranye
        Color(0xFFFACC15), // Kuning Emas
        Color(0xFF84CC16), // Hijau Daun Muda
        Color(0xFF22C55E), // Hijau Rumput
        Color(0xFF14B8A6), // Toska Laut
        Color(0xFF38BDF8), // Biru Langit
        Color(0xFF2563EB), // Biru Tua
        Color(0xFF8B5CF6), // Ungu
        Color(0xFFD946EF), // Magenta
        Color(0xFFF472B6), // Pink Bunga
        Color(0xFFFCD34D), // Krem Pasir
        Color(0xFF92400E), // Cokelat Kayu
        Color(0xFF334155)  // Abu Gelap
    )

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Judul & Deskripsi Rapi (Tidak Terpotong ke Bawah)
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = Color(0xFFDCFCE7),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "✓ TOUCHSCREEN, STYLUS PEN & CAT TUMPAH",
                            color = Color(0xFF065F46),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                    TextButton(
                        onClick = onLanjutKeKartu,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "Lanjut ke Kartu ➔",
                            color = Color(0xFFDB2777),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1
                        )
                    }
                }

                Text(
                    text = misiObj.judul,
                    fontWeight = FontWeight.Black,
                    fontSize = 16.sp,
                    color = Color(0xFF0F172A),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = misiObj.subJudul,
                    fontSize = 11.5.sp,
                    color = Color(0xFF475569)
                )
            }

            // Pilihan 8 Tema Gambar (Horizontal Scrollable Chips — Teks 1 Baris Rapi!)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                DAFTAR_8_MISI_GAMBAR_ANDROID.forEach { m ->
                    val aktif = m.id == misiAktifId
                    val sudahDiwarnai =
                        !warnaZonePerMisi[m.id].isNullOrEmpty() || !goresanPerMisi[m.id].isNullOrEmpty()
                    Surface(
                        shape = RoundedCornerShape(14.dp),
                        color = if (aktif) Color(0xFF4F46E5)
                        else if (sudahDiwarnai) Color(0xFFECFDF5)
                        else Color(0xFFF1F5F9),
                        modifier = Modifier
                            .border(
                                width = 2.dp,
                                color = if (aktif) Color(0xFFFACC15)
                                else if (sudahDiwarnai) Color(0xFF10B981)
                                else Color(0xFFCBD5E1),
                                shape = RoundedCornerShape(14.dp)
                            )
                            .clickable { onPilihMisi(m.id) }
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = m.judul,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                maxLines = 1,
                                softWrap = false,
                                color = if (aktif) Color.White else Color(0xFF0F172A)
                            )
                            if (sudahDiwarnai) {
                                Text(
                                    text = "✓",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Black,
                                    color = if (aktif) Color(0xFFFDE047) else Color(0xFF059669)
                                )
                            }
                        }
                    }
                }
            }

            // Bar Alat Mewarnai (Horizontal Scrollable — Anti Sempit & Anti Teks Turun!)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                listOf(
                    "cat_tumpah" to "🪣 Cat Tumpah (1x Ketuk)",
                    "kuas" to "🖌️ Kuas / Stylus Pen",
                    "hapus" to "🧼 Penghapus Warna"
                ).forEach { (idAlat, labelAlat) ->
                    val dipilih = modeAlat == idAlat
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (dipilih) Color(0xFFE11D48) else Color(0xFFF8FAFC),
                        modifier = Modifier
                            .border(
                                width = 1.5.dp,
                                color = if (dipilih) Color(0xFFFACC15) else Color(0xFFCBD5E1),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable { modeAlat = idAlat }
                    ) {
                        Text(
                            text = labelAlat,
                            fontSize = 11.5.sp,
                            fontWeight = FontWeight.Black,
                            maxLines = 1,
                            softWrap = false,
                            color = if (dipilih) Color.White else Color(0xFF334155),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFEE2E2),
                    modifier = Modifier
                        .border(1.dp, Color(0xFFFCA5A5), RoundedCornerShape(12.dp))
                        .clickable { onBersihkanMisiIni(misiAktifId) }
                ) {
                    Text(
                        text = "🔄 Ulangi Gambar Ini",
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        softWrap = false,
                        color = Color(0xFFB91C1C),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    )
                }
            }

            // Palet 14 Warna Cat Ceria
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(vertical = 2.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                daftarPaletWarna.forEach { c ->
                    val aktif = warnaPilihan == c
                    Box(
                        modifier = Modifier
                            .size(if (aktif) 36.dp else 30.dp)
                            .clip(CircleShape)
                            .background(c)
                            .border(
                                width = if (aktif) 3.dp else 1.5.dp,
                                color = if (aktif) Color(0xFF0F172A) else Color(0xFFCBD5E1),
                                shape = CircleShape
                            )
                            .clickable {
                                warnaPilihan = c
                                if (modeAlat == "hapus") modeAlat = "cat_tumpah"
                            }
                    )
                }
            }

            // KANVAS BUKU MEWARNAI UTAMA (TINGGI 270.dp DENGAN ILUSTRASI VECTOR ASLI)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(270.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(Color.White)
                    .border(2.5.dp, Color(0xFF334155), RoundedCornerShape(20.dp))
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxSize()
                        .pointerInput(misiAktifId, modeAlat, warnaPilihan) {
                            detectTapGestures { tapOffset ->
                                if (modeAlat == "cat_tumpah") {
                                    val w = size.width.toFloat()
                                    val h = size.height.toFloat()
                                    val zoneTerdekat = misiObj.daftarZoneCat.minByOrNull { z ->
                                        hypot(
                                            tapOffset.x - z.centerRatioX * w,
                                            tapOffset.y - z.centerRatioY * h
                                        )
                                    }
                                    if (zoneTerdekat != null) {
                                        onWarnaiZoneCatTumpah(
                                            misiAktifId,
                                            zoneTerdekat.idZone,
                                            warnaPilihan
                                        )
                                    }
                                } else {
                                    val wGores = if (modeAlat == "hapus") Color.White else warnaPilihan
                                    onTambahGoresanKuas(
                                        misiAktifId,
                                        GoresanKuasAnak(
                                            points = listOf(tapOffset, tapOffset + Offset(1f, 1f)),
                                            color = wGores,
                                            strokeWidth = tebalKuas
                                        )
                                    )
                                }
                            }
                        }
                        .pointerInput(misiAktifId, modeAlat, warnaPilihan) {
                            detectDragGestures(
                                onDragStart = { startOffset ->
                                    if (modeAlat != "cat_tumpah") {
                                        titikGoresSaatIni = listOf(startOffset)
                                    }
                                },
                                onDrag = { change, _ ->
                                    if (modeAlat != "cat_tumpah") {
                                        change.consume()
                                        titikGoresSaatIni = titikGoresSaatIni + change.position
                                    }
                                },
                                onDragEnd = {
                                    if (modeAlat != "cat_tumpah" && titikGoresSaatIni.size > 1) {
                                        val wGores = if (modeAlat == "hapus") Color.White else warnaPilihan
                                        onTambahGoresanKuas(
                                            misiAktifId,
                                            GoresanKuasAnak(
                                                points = titikGoresSaatIni,
                                                color = wGores,
                                                strokeWidth = if (modeAlat == "hapus") tebalKuas * 1.8f else tebalKuas
                                            )
                                        )
                                    }
                                    titikGoresSaatIni = listOf()
                                }
                            )
                        }
                ) {
                    // 1. Lapisan Isian Warna Cat Tumpah (Fill)
                    gambarIlustrasiBukuMewarnaiVectorAsli(
                        misiId = misiAktifId,
                        warnaZones = mapZoneMisiIni,
                        hanyaOutline = false
                    )

                    // 2. Lapisan Goresan Kuas / Stylus Anak
                    listGoresanMisiIni.forEach { goresan ->
                        if (goresan.points.size > 1) {
                            val p = Path().apply {
                                moveTo(goresan.points.first().x, goresan.points.first().y)
                                goresan.points.drop(1).forEach { pt -> lineTo(pt.x, pt.y) }
                            }
                            drawPath(
                                path = p,
                                color = goresan.color,
                                style = Stroke(
                                    width = goresan.strokeWidth,
                                    cap = StrokeCap.Round,
                                    join = StrokeJoin.Round
                                )
                            )
                        }
                    }

                    if (titikGoresSaatIni.size > 1) {
                        val pAktif = Path().apply {
                            moveTo(titikGoresSaatIni.first().x, titikGoresSaatIni.first().y)
                            titikGoresSaatIni.drop(1).forEach { pt -> lineTo(pt.x, pt.y) }
                        }
                        drawPath(
                            path = pAktif,
                            color = if (modeAlat == "hapus") Color.White else warnaPilihan,
                            style = Stroke(
                                width = tebalKuas,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }

                    // 3. Lapisan Garis Sketsa Hitam Tajam di Atas (Outline)
                    gambarIlustrasiBukuMewarnaiVectorAsli(
                        misiId = misiAktifId,
                        warnaZones = mapZoneMisiIni,
                        hanyaOutline = true
                    )
                }
            }
        }
    }
}

// ============================================================================
// MESIN PENGGAMBAR 8 ILUSTRASI VECTOR BUKU MEWARNAI ANAK (KURVA BEZIER ASLI!)
// ============================================================================
fun DrawScope.gambarIlustrasiBukuMewarnaiVectorAsli(
    misiId: String,
    warnaZones: Map<String, Color>,
    hanyaOutline: Boolean
) {
    val w = size.width
    val h = size.height
    val strokeGaris = Stroke(width = 5.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val strokeTipis = Stroke(width = 3.5f, cap = StrokeCap.Round, join = StrokeJoin.Round)
    val hitam = Color(0xFF1E293B)

    fun gambarPathZone(idZone: String, path: Path, defaultColor: Color = Color.White) {
        if (!hanyaOutline) {
            drawPath(path = path, color = warnaZones[idZone] ?: defaultColor, style = Fill)
        } else {
            drawPath(path = path, color = hitam, style = strokeGaris)
        }
    }

    fun gambarLingkaranZone(idZone: String, cx: Float, cy: Float, r: Float, defaultColor: Color = Color.White) {
        if (!hanyaOutline) {
            drawCircle(color = warnaZones[idZone] ?: defaultColor, radius = r, center = Offset(cx, cy))
        } else {
            drawCircle(color = hitam, radius = r, center = Offset(cx, cy), style = strokeGaris)
        }
    }

    fun gambarKotakZone(idZone: String, x: Float, y: Float, kw: Float, kh: Float, corner: Float = 0f, defaultColor: Color = Color.White) {
        if (!hanyaOutline) {
            drawRoundRect(
                color = warnaZones[idZone] ?: defaultColor,
                topLeft = Offset(x, y),
                size = Size(kw, kh),
                cornerRadius = CornerRadius(corner, corner)
            )
        } else {
            drawRoundRect(
                color = hitam,
                topLeft = Offset(x, y),
                size = Size(kw, kh),
                cornerRadius = CornerRadius(corner, corner),
                style = strokeGaris
            )
        }
    }

    fun buatPathBintang(cx: Float, cy: Float, rLuar: Float, rDalam: Float): Path {
        return Path().apply {
            for (i in 0 until 10) {
                val r = if (i % 2 == 0) rLuar else rDalam
                val angle = (PI / 5.0) * i - PI / 2.0
                val px = (cx + r * cos(angle)).toFloat()
                val py = (cy + r * sin(angle)).toFloat()
                if (i == 0) moveTo(px, py) else lineTo(px, py)
            }
            close()
        }
    }

    fun buatPathAwan(cx: Float, cy: Float, scale: Float = 1f): Path {
        return Path().apply {
            moveTo(cx - 35f * scale, cy + 12f * scale)
            cubicTo(
                cx - 52f * scale, cy + 12f * scale,
                cx - 52f * scale, cy - 18f * scale,
                cx - 26f * scale, cy - 18f * scale
            )
            cubicTo(
                cx - 18f * scale, cy - 38f * scale,
                cx + 18f * scale, cy - 38f * scale,
                cx + 26f * scale, cy - 18f * scale
            )
            cubicTo(
                cx + 52f * scale, cy - 18f * scale,
                cx + 52f * scale, cy + 12f * scale,
                cx + 35f * scale, cy + 12f * scale
            )
            close()
        }
    }

    when (misiId) {
        "rumah_pohon" -> {
            // Latar langit
            if (!hanyaOutline) {
                drawRect(color = warnaZones["langit"] ?: Color.White, size = Size(w, h * 0.85f))
            }

            // Gunung Kiri & Gunung Kanan
            val pGunungKiri = Path().apply {
                moveTo(0f, h * 0.85f)
                lineTo(w * 0.20f, h * 0.34f)
                lineTo(w * 0.42f, h * 0.85f)
                close()
            }
            gambarPathZone("gunung_kiri", pGunungKiri)

            val pGunungKanan = Path().apply {
                moveTo(w * 0.30f, h * 0.85f)
                lineTo(w * 0.54f, h * 0.28f)
                lineTo(w * 0.76f, h * 0.85f)
                close()
            }
            gambarPathZone("gunung_kanan", pGunungKanan)

            // Matahari Bersinar & Senyum
            gambarLingkaranZone("matahari", w * 0.12f, h * 0.20f, h * 0.11f)
            if (hanyaOutline) {
                for (i in 0 until 10) {
                    val a = (2 * PI * i) / 10.0
                    val sx1 = (w * 0.12f + cos(a) * h * 0.13f).toFloat()
                    val sy1 = (h * 0.20f + sin(a) * h * 0.13f).toFloat()
                    val sx2 = (w * 0.12f + cos(a) * h * 0.17f).toFloat()
                    val sy2 = (h * 0.20f + sin(a) * h * 0.17f).toFloat()
                    drawLine(hitam, Offset(sx1, sy1), Offset(sx2, sy2), strokeWidth = 4f)
                }
            }

            // Awan
            gambarPathZone("awan", buatPathAwan(w * 0.65f, h * 0.18f, 1.1f))

            // Cerobong & Rumah Impian
            gambarKotakZone("atap", w * 0.48f, h * 0.24f, w * 0.045f, h * 0.16f)
            val pAtap = Path().apply {
                moveTo(w * 0.26f, h * 0.50f)
                lineTo(w * 0.42f, h * 0.22f)
                lineTo(w * 0.58f, h * 0.50f)
                close()
            }
            gambarPathZone("atap", pAtap)
            gambarKotakZone("dinding", w * 0.29f, h * 0.50f, w * 0.26f, h * 0.35f)

            // Pintu Lengkung & 2 Jendela
            val pPintu = Path().apply {
                moveTo(w * 0.385f, h * 0.85f)
                lineTo(w * 0.385f, h * 0.67f)
                quadraticBezierTo(w * 0.42f, h * 0.60f, w * 0.455f, h * 0.67f)
                lineTo(w * 0.455f, h * 0.85f)
                close()
            }
            gambarPathZone("pintu", pPintu)
            gambarKotakZone("jendela_kiri", w * 0.31f, h * 0.56f, w * 0.055f, h * 0.11f, 6f)
            gambarKotakZone("jendela_kanan", w * 0.475f, h * 0.56f, w * 0.055f, h * 0.11f, 6f)

            // Pohon Rindang Berbuah Apel di Kanan
            gambarKotakZone("batang_pohon", w * 0.795f, h * 0.52f, w * 0.05f, h * 0.33f)
            gambarLingkaranZone("daun_pohon", w * 0.82f, h * 0.38f, h * 0.19f)
            if (hanyaOutline) {
                listOf(
                    Offset(w * 0.79f, h * 0.34f),
                    Offset(w * 0.85f, h * 0.36f),
                    Offset(w * 0.82f, h * 0.44f)
                ).forEach { apel ->
                    drawCircle(color = hitam, radius = 7f, center = apel, style = strokeTipis)
                }
            }

            // Halaman Rumput Bawah
            gambarKotakZone("rumput", 0f, h * 0.85f, w, h * 0.15f)
        }

        "roket_robot" -> {
            // Planet Saturnus Bercincin
            gambarLingkaranZone("planet", w * 0.16f, h * 0.28f, h * 0.12f)
            val pCincin = Path().apply {
                moveTo(w * 0.05f, h * 0.33f)
                quadraticBezierTo(w * 0.16f, h * 0.40f, w * 0.27f, h * 0.24f)
                quadraticBezierTo(w * 0.16f, h * 0.32f, w * 0.05f, h * 0.33f)
                close()
            }
            gambarPathZone("cincin_planet", pCincin)

            gambarPathZone("planet", buatPathBintang(w * 0.10f, h * 0.68f, 20f, 9f))
            gambarPathZone("planet", buatPathBintang(w * 0.58f, h * 0.18f, 18f, 8f))

            // Roket Antariksa Melengkung Aerodinamis
            val pBadanRoket = Path().apply {
                moveTo(w * 0.42f, h * 0.10f)
                quadraticBezierTo(w * 0.52f, h * 0.30f, w * 0.49f, h * 0.66f)
                lineTo(w * 0.35f, h * 0.66f)
                quadraticBezierTo(w * 0.32f, h * 0.30f, w * 0.42f, h * 0.10f)
                close()
            }
            gambarPathZone("badan_roket", pBadanRoket)

            // Hidung Kerucut Roket
            val pHidungRoket = Path().apply {
                moveTo(w * 0.42f, h * 0.10f)
                quadraticBezierTo(w * 0.46f, h * 0.18f, w * 0.47f, h * 0.26f)
                lineTo(w * 0.37f, h * 0.26f)
                quadraticBezierTo(w * 0.38f, h * 0.18f, w * 0.42f, h * 0.10f)
                close()
            }
            gambarPathZone("ucuk_roket", pHidungRoket)

            // Jendela Bulat Astronot
            gambarLingkaranZone("jendela_roket", w * 0.42f, h * 0.43f, h * 0.085f)

            // Sirip Kiri & Kanan Roket
            val pSiripKiri = Path().apply {
                moveTo(w * 0.35f, h * 0.50f)
                lineTo(w * 0.27f, h * 0.72f)
                lineTo(w * 0.35f, h * 0.66f)
                close()
            }
            gambarPathZone("sirip_kiri", pSiripKiri)

            val pSiripKanan = Path().apply {
                moveTo(w * 0.49f, h * 0.50f)
                lineTo(w * 0.57f, h * 0.72f)
                lineTo(w * 0.49f, h * 0.66f)
                close()
            }
            gambarPathZone("sirip_kanan", pSiripKanan)

            // Semburan Api Roket
            val pApi = Path().apply {
                moveTo(w * 0.37f, h * 0.66f)
                lineTo(w * 0.35f, h * 0.84f)
                lineTo(w * 0.39f, h * 0.77f)
                lineTo(w * 0.42f, h * 0.93f)
                lineTo(w * 0.45f, h * 0.77f)
                lineTo(w * 0.49f, h * 0.84f)
                lineTo(w * 0.47f, h * 0.66f)
                close()
            }
            gambarPathZone("api_roket", pApi)

            // Robot Pintar Ramah di Kanan
            if (hanyaOutline) {
                drawLine(hitam, Offset(w * 0.77f, h * 0.22f), Offset(w * 0.77f, h * 0.12f), strokeWidth = 5f)
            }
            gambarLingkaranZone("kepala_robot", w * 0.77f, h * 0.10f, 9f)
            gambarKotakZone("kepala_robot", w * 0.68f, h * 0.22f, w * 0.18f, h * 0.22f, 14f)
            if (hanyaOutline) {
                drawCircle(hitam, radius = 9f, center = Offset(w * 0.73f, h * 0.30f), style = strokeTipis)
                drawCircle(hitam, radius = 9f, center = Offset(w * 0.81f, h * 0.30f), style = strokeTipis)
                drawRect(hitam, topLeft = Offset(w * 0.73f, h * 0.37f), size = Size(w * 0.08f, h * 0.03f), style = strokeTipis)
            }

            gambarKotakZone("badan_robot", w * 0.65f, h * 0.46f, w * 0.24f, h * 0.36f, 16f)
            gambarKotakZone("layar_robot", w * 0.69f, h * 0.52f, w * 0.16f, h * 0.16f, 8f)
            gambarKotakZone("badan_robot", w * 0.60f, h * 0.49f, w * 0.045f, h * 0.24f, 8f)
            gambarKotakZone("badan_robot", w * 0.895f, h * 0.49f, w * 0.045f, h * 0.24f, 8f)
            gambarKotakZone("badan_robot", w * 0.70f, h * 0.82f, w * 0.05f, h * 0.11f, 6f)
            gambarKotakZone("badan_robot", w * 0.79f, h * 0.82f, w * 0.05f, h * 0.11f, 6f)
        }

        "taman_kupu" -> {
            val bx = w * 0.50f
            val by = h * 0.44f

            // Sayap Atas Kiri & Kanan (Kurva Bezier Kupu-Kupu Asli!)
            val pSayapKiriAtas = Path().apply {
                moveTo(bx - 8f, by - 20f)
                cubicTo(bx - w * 0.22f, by - h * 0.38f, bx - w * 0.28f, by - h * 0.02f, bx - 8f, by + 6f)
                close()
            }
            gambarPathZone("sayap_kiri_atas", pSayapKiriAtas)

            val pSayapKananAtas = Path().apply {
                moveTo(bx + 8f, by - 20f)
                cubicTo(bx + w * 0.22f, by - h * 0.38f, bx + w * 0.28f, by - h * 0.02f, bx + 8f, by + 6f)
                close()
            }
            gambarPathZone("sayap_kanan_atas", pSayapKananAtas)

            // Sayap Bawah Kiri & Kanan
            val pSayapKiriBawah = Path().apply {
                moveTo(bx - 8f, by + 6f)
                cubicTo(bx - w * 0.22f, by + h * 0.05f, bx - w * 0.18f, by + h * 0.34f, bx - 6f, by + h * 0.16f)
                close()
            }
            gambarPathZone("sayap_kiri_bawah", pSayapKiriBawah)

            val pSayapKananBawah = Path().apply {
                moveTo(bx + 8f, by + 6f)
                cubicTo(bx + w * 0.22f, by + h * 0.05f, bx + w * 0.18f, by + h * 0.34f, bx + 6f, by + h * 0.16f)
                close()
            }
            gambarPathZone("sayap_kanan_bawah", pSayapKananBawah)

            // Pola Hiasan Bulat di Dalam Sayap Kupu-Kupu
            if (hanyaOutline) {
                drawCircle(hitam, radius = 16f, center = Offset(bx - w * 0.11f, by - h * 0.11f), style = strokeTipis)
                drawCircle(hitam, radius = 16f, center = Offset(bx + w * 0.11f, by - h * 0.11f), style = strokeTipis)
                drawCircle(hitam, radius = 12f, center = Offset(bx - w * 0.09f, by + h * 0.12f), style = strokeTipis)
                drawCircle(hitam, radius = 12f, center = Offset(bx + w * 0.09f, by + h * 0.12f), style = strokeTipis)
            }

            // Badan, Kepala & Antena Kupu-Kupu
            gambarKotakZone("badan_kupu", bx - 9f, by - h * 0.12f, 18f, h * 0.30f, 9f)
            gambarLingkaranZone("badan_kupu", bx, by - h * 0.16f, 12f)
            if (hanyaOutline) {
                val pAntena = Path().apply {
                    moveTo(bx - 4f, by - h * 0.20f)
                    quadraticBezierTo(bx - 28f, by - h * 0.32f, bx - 40f, by - h * 0.28f)
                    moveTo(bx + 4f, by - h * 0.20f)
                    quadraticBezierTo(bx + 28f, by - h * 0.32f, bx + 40f, by - h * 0.28f)
                }
                drawPath(pAntena, color = hitam, style = strokeTipis)
            }

            // 2 Bunga Mekar Berkelopak 6 di Kiri & Kanan
            fun gambarBunga(fx: Float, fy: Float, zoneKelopak: String) {
                if (hanyaOutline) {
                    drawLine(hitam, Offset(fx, fy + 16f), Offset(fx, h * 0.88f), strokeWidth = 5f)
                }
                for (i in 0 until 6) {
                    val a = (2 * PI * i) / 6.0
                    val kx = (fx + cos(a) * 28f).toFloat()
                    val ky = (fy + sin(a) * 28f).toFloat()
                    gambarLingkaranZone(zoneKelopak, kx, ky, 16f)
                }
                gambarLingkaranZone("inti_bunga_kiri", fx, fy, 16f)
            }
            gambarBunga(w * 0.16f, h * 0.60f, "bunga_kiri")
            gambarBunga(w * 0.84f, h * 0.60f, "bunga_kanan")

            gambarKotakZone("rumput_taman", 0f, h * 0.88f, w, h * 0.12f)
        }

        "dino_purba" -> {
            // Gunung Berapi & Lelehan Lava di Kanan
            val pGunungApi = Path().apply {
                moveTo(w * 0.62f, h * 0.86f)
                lineTo(w * 0.76f, h * 0.38f)
                lineTo(w * 0.85f, h * 0.38f)
                lineTo(w * 0.98f, h * 0.86f)
                close()
            }
            gambarPathZone("gunung_api", pGunungApi)

            val pLava = Path().apply {
                moveTo(w * 0.76f, h * 0.38f)
                quadraticBezierTo(w * 0.78f, h * 0.52f, w * 0.80f, h * 0.45f)
                quadraticBezierTo(w * 0.83f, h * 0.54f, w * 0.85f, h * 0.38f)
                close()
            }
            gambarPathZone("lava_api", pLava)
            gambarPathZone("awan_purba", buatPathAwan(w * 0.80f, h * 0.20f, 1.0f))

            // Dinosaurus Brachiosaurus Leher Panjang Ramah (Kurva Organik Utuh!)
            val pDino = Path().apply {
                moveTo(w * 0.20f, h * 0.65f)
                quadraticBezierTo(w * 0.17f, h * 0.35f, w * 0.21f, h * 0.18f) // Leher naik
                quadraticBezierTo(w * 0.23f, h * 0.10f, w * 0.29f, h * 0.13f) // Kepala atas
                quadraticBezierTo(w * 0.33f, h * 0.16f, w * 0.31f, h * 0.24f) // Moncong ramah
                quadraticBezierTo(w * 0.27f, h * 0.27f, w * 0.25f, h * 0.26f) // Dagu
                quadraticBezierTo(w * 0.25f, h * 0.46f, w * 0.29f, h * 0.56f) // Pangkal leher
                quadraticBezierTo(w * 0.44f, h * 0.52f, w * 0.50f, h * 0.66f) // Punggung bulat
                quadraticBezierTo(w * 0.58f, h * 0.78f, w * 0.64f, h * 0.80f) // Ujung ekor
                quadraticBezierTo(w * 0.56f, h * 0.85f, w * 0.49f, h * 0.77f) // Bawah ekor
                lineTo(w * 0.49f, h * 0.86f)
                lineTo(w * 0.43f, h * 0.86f)
                lineTo(w * 0.43f, h * 0.76f)
                lineTo(w * 0.31f, h * 0.76f)
                lineTo(w * 0.31f, h * 0.86f)
                lineTo(w * 0.25f, h * 0.86f)
                lineTo(w * 0.25f, h * 0.74f)
                quadraticBezierTo(w * 0.20f, h * 0.72f, w * 0.20f, h * 0.65f)
                close()
            }
            gambarPathZone("badan_dino", pDino)

            gambarLingkaranZone("bercak_dino", w * 0.36f, h * 0.64f, 15f)
            gambarLingkaranZone("bercak_dino", w * 0.43f, h * 0.67f, 18f)
            if (hanyaOutline) {
                drawCircle(hitam, radius = 5f, center = Offset(w * 0.27f, h * 0.17f))
            }

            gambarKotakZone("tanah_purba", 0f, h * 0.86f, w, h * 0.14f)
        }

        "bawah_laut" -> {
            if (!hanyaOutline) {
                drawRect(color = warnaZones["air_laut"] ?: Color.White, size = Size(w, h * 0.88f))
            }

            // Kapal Selam Penjelajah Lengkap Periskop & 3 Jendela
            if (hanyaOutline) {
                val pPeriskop = Path().apply {
                    moveTo(w * 0.28f, h * 0.24f)
                    lineTo(w * 0.28f, h * 0.14f)
                    lineTo(w * 0.33f, h * 0.14f)
                }
                drawPath(pPeriskop, color = hitam, style = strokeGaris)
            }
            gambarKotakZone("kapal_selam", w * 0.23f, h * 0.22f, w * 0.10f, h * 0.08f, 6f)
            gambarKotakZone("kapal_selam", w * 0.12f, h * 0.30f, w * 0.32f, h * 0.24f, 45f)
            listOf(w * 0.19f, w * 0.28f, w * 0.37f).forEach { jx ->
                gambarLingkaranZone("jendela_kapal", jx, h * 0.42f, 14f)
            }

            // Ikan Tropis Besar Bersirip & Ekor Segitiga
            val pEkorIkan = Path().apply {
                moveTo(w * 0.81f, h * 0.52f)
                lineTo(w * 0.92f, h * 0.36f)
                lineTo(w * 0.92f, h * 0.68f)
                close()
            }
            gambarPathZone("ekor_ikan", pEkorIkan)
            gambarKotakZone("badan_ikan", w * 0.56f, h * 0.36f, w * 0.26f, h * 0.32f, 55f)
            if (hanyaOutline) {
                drawCircle(hitam, radius = 6f, center = Offset(w * 0.62f, h * 0.48f))
            }

            // Bintang Laut & Pasir Dasar Laut
            gambarPathZone("bintang_laut", buatPathBintang(w * 0.48f, h * 0.78f, 30f, 13f))
            gambarKotakZone("pasir_laut", 0f, h * 0.88f, w, h * 0.12f)
        }

        "masjid_indah" -> {
            // Kubah Utama Masjid (Kurva Bawang Islami)
            val pKubahUtama = Path().apply {
                moveTo(w * 0.34f, h * 0.48f)
                cubicTo(w * 0.34f, h * 0.18f, w * 0.66f, h * 0.18f, w * 0.66f, h * 0.48f)
                close()
            }
            gambarPathZone("kubah_utama", pKubahUtama)
            if (hanyaOutline) {
                drawCircle(hitam, radius = 10f, center = Offset(w * 0.50f, h * 0.18f), style = strokeTipis)
            }

            // Dinding Bangunan Utama Masjid & Pintu Lengkung
            gambarKotakZone("dinding_masjid", w * 0.31f, h * 0.48f, w * 0.38f, h * 0.40f)
            val pPintuMasjid = Path().apply {
                moveTo(w * 0.44f, h * 0.88f)
                lineTo(w * 0.44f, h * 0.66f)
                quadraticBezierTo(w * 0.50f, h * 0.55f, w * 0.56f, h * 0.66f)
                lineTo(w * 0.56f, h * 0.88f)
                close()
            }
            gambarPathZone("pintu_masjid", pPintuMasjid)

            // Menara Kembar Kiri & Kanan
            gambarKotakZone("menara_kiri", w * 0.16f, h * 0.34f, w * 0.08f, h * 0.54f)
            val pKubahKiri = Path().apply {
                moveTo(w * 0.15f, h * 0.34f)
                quadraticBezierTo(w * 0.20f, h * 0.16f, w * 0.25f, h * 0.34f)
                close()
            }
            gambarPathZone("kubah_kiri", pKubahKiri)

            gambarKotakZone("menara_kanan", w * 0.76f, h * 0.34f, w * 0.08f, h * 0.54f)
            val pKubahKanan = Path().apply {
                moveTo(w * 0.75f, h * 0.34f)
                quadraticBezierTo(w * 0.80f, h * 0.16f, w * 0.85f, h * 0.34f)
                close()
            }
            gambarPathZone("kubah_kanan", pKubahKanan)

            gambarKotakZone("halaman_masjid", 0f, h * 0.88f, w, h * 0.12f)
        }

        "panggung_musik" -> {
            // Bodi Gitar Akustik & Leher Gitar
            gambarKotakZone("leher_gitar", w * 0.54f, h * 0.31f, w * 0.24f, h * 0.08f, 6f)
            gambarLingkaranZone("bodi_gitar", w * 0.37f, h * 0.35f, h * 0.16f)
            gambarLingkaranZone("bodi_gitar", w * 0.48f, h * 0.35f, h * 0.13f)
            gambarLingkaranZone("lubang_gitar", w * 0.44f, h * 0.35f, h * 0.055f)

            // Papan Tuts Piano Klasik di Bawah
            gambarKotakZone("papan_piano", w * 0.16f, h * 0.58f, w * 0.68f, h * 0.32f, 8f)
            if (hanyaOutline) {
                for (i in 1..7) {
                    val tx = w * 0.16f + i * (w * 0.68f / 8f)
                    drawLine(hitam, Offset(tx, h * 0.58f), Offset(tx, h * 0.90f), strokeWidth = 4f)
                }
            }
            for (i in 1..6) {
                if (i != 3) {
                    val tx = w * 0.16f + i * (w * 0.68f / 8f) - w * 0.02f
                    gambarKotakZone("tuts_hitam", tx, h * 0.58f, w * 0.04f, h * 0.18f, 4f, defaultColor = hitam)
                }
            }

            gambarPathZone("bintang_musik", buatPathBintang(w * 0.16f, h * 0.30f, 24f, 10f))
            gambarPathZone("bintang_musik", buatPathBintang(w * 0.86f, h * 0.30f, 24f, 10f))
        }

        else -> {
            gambarKotakZone("latar_bebas", w * 0.03f, h * 0.05f, w * 0.94f, h * 0.90f, 18f)
            gambarPathZone("latar_bebas", buatPathBintang(w * 0.10f, h * 0.16f, 20f, 9f))
            gambarPathZone("latar_bebas", buatPathBintang(w * 0.90f, h * 0.16f, 20f, 9f))
        }
    }
}