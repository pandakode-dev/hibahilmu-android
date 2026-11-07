package id.my.hibahilmu.app

import androidx.compose.ui.graphics.Color

enum class JenjangTalentAndroid(
    val id: String,
    val judul: String,
    val rentangUsia: String,
    val ikon: String,
    val metodeSingkat: String
) {
    ANAK_TK_SD(
        id = "anak_tk_sd",
        judul = "1. Anak Usia Dini (TK–SD)",
        rentangUsia = "3–8 Tahun",
        ikon = "👶",
        metodeSingkat = "8 Kanvas Mewarnai (Cat Tumpah & Stylus) + 24 Kartu + Observasi Ortu"
    ),
    SMP_REMAJA(
        id = "smp",
        judul = "2. Siswa SD Atas & SMP",
        rentangUsia = "9–15 Tahun",
        ikon = "🧭",
        metodeSingkat = "8 Bakat Remaja + Gaya Belajar VAK (Visual, Auditori, Kinestetik)"
    ),
    SMA_SMK(
        id = "sma",
        judul = "3. Remaja SMA / SMK / MA",
        rentangUsia = "15–18 Tahun",
        ikon = "🎓",
        metodeSingkat = "Penjurusan Kuliah (SNBP/SNBT), Mapel Pilihan & 6 RIASEC Holland"
    ),
    DEWASA_PRO(
        id = "dewasa_284",
        judul = "4. Mahasiswa & Umum",
        rentangUsia = "18+ Tahun",
        ikon = "🔥",
        metodeSingkat = "34 Tema Bakat Alami (PSP), 8 Klaster SCM (PSS) & Personal Branding"
    )
}

data class DimensiBakatItemAndroid(
    val id: String,
    val nama: String,
    val julukan: String,
    val ikon: String,
    val warnaHex: Long,
    val deskripsiKekuatan: String,
    val dampakJikaLemah: String,
    val solusiAgarTidakMenghambat: String,
    val harusNgapainSaja: List<String>,
    val rekomendasiLesAtauJurusan: List<String>,
    val potensiMasaDepan: List<String>
)

data class HasilEvaluasiDimensiAndroid(
    val dimensi: DimensiBakatItemAndroid,
    val skorMentah: Int,
    val persen: Int,
    val peringkat: Int,
    val kategoriLabel: String
)

data class KartuVisualAnakItem(
    val id: Int,
    val pertanyaan: String,
    val opsi: List<OpsiVisualAnak>
)

data class OpsiVisualAnak(
    val kodeDimensi: String,
    val ikon: String,
    val teks: String
)

data class SoalLikertRemajaDewasa(
    val no: Int,
    val sesi: Int,
    val kodeDimensi: String,
    val pernyataan: String
)

object TalentMappingRepositoryData {

    // ========================================================================
    // 1. DEFINISI 8 DIMENSI KECERDASAN & MOTORIK ANAK (3-8 TAHUN)
    // ========================================================================
    val DIMENSI_ANAK_LIST = listOf(
        DimensiBakatItemAndroid(
            id = "seni",
            nama = "Visual-Spasial & Seni Kreatif",
            julukan = "Si Pelukis & Perancang Imajinatif",
            ikon = "🎨",
            warnaHex = 0xFFE11D48,
            deskripsiKekuatan = "Ananda memiliki kepekaan mata yang sangat baik terhadap warna, bentuk, ruang, dan gambar. Ia mudah menuangkan imajinasi menjadi coretan gambar dan susunan warna yang indah.",
            dampakJikaLemah = "Jika kurang terstimulasi, anak mungkin cepat bingung membaca arah/denah atau kurang rapi menulis di garis buku.",
            solusiAgarTidakMenghambat = "Jangan paksa anak menggambar persis aslinya. Gunakan buku berpetak besar saat belajar menulis dan kartu bergambar warna-warni.",
            harusNgapainSaja = listOf(
                "Sediakan kertas gambar A3, krayon/cat air aman, dan plastisin di rumah.",
                "Ajak anak menceritakan arti gambar yang baru ia buat.",
                "Pajang karya gambarnya di dinding kamar sebagai apresiasi."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Les Menggambar & Melukis Kanvas Anak",
                "Kelas Kerajinan Tangan (Clay, Origami & Papercraft)",
                "Kelas Animasi & Desain Visual Anak (Scratch Jr)"
            ),
            potensiMasaDepan = listOf("Arsitek & Desainer Interior", "Desainer UI/UX & Ilustrator", "Animator Kreatif")
        ),
        DimensiBakatItemAndroid(
            id = "fisik",
            nama = "Kinestetik-Jasmani & Motorik Fisik",
            julukan = "Si Gesit Berenergi Tinggi",
            ikon = "⚽",
            warnaHex = 0xFFEA580C,
            deskripsiKekuatan = "Ananda memiliki koordinasi tubuh, keseimbangan, ketangkasan gerak, dan stamina fisik yang menonjol. Ia cepat paham jika belajar sambil bergerak dan mempraktikkan langsung.",
            dampakJikaLemah = "Jika motorik kurang dilatih, jari tangan cepat pegal saat menulis lama atau mudah lelah saat kegiatan fisik.",
            solusiAgarTidakMenghambat = "Berikan jeda gerak setiap 20 menit belajar, serta latih otot jari lewat meremas spons air, menggunting kertas pola, dan meronce.",
            harusNgapainSaja = listOf(
                "Berikan waktu bermain aktif di luar ruangan minimal 45 menit setiap sore.",
                "Gunakan metode belajar sambil bergerak (berhitung sambil melompat).",
                "Latih kemandirian memakai sepatu dan merapikan mainan sendiri."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Les Renang / Akuatik (Bagus untuk postur & pernapasan)",
                "Sekolah Sepak Bola (SSB) / Futsal / Bulutangkis",
                "Bela Diri Anak (Taekwondo / Karate / Pencak Silat)"
            ),
            potensiMasaDepan = listOf("Atlet Profesional", "Dokter Bedah / Fisioterapis", "Insinyur Lapangan & Pilot")
        ),
        DimensiBakatItemAndroid(
            id = "logika",
            nama = "Logika-Matematika & Sains",
            julukan = "Si Ilmuwan & Pemecah Teka-Teki Cilik",
            ikon = "🧩",
            warnaHex = 0xFF2563EB,
            deskripsiKekuatan = "Ananda gemar mencari tahu cara kerja benda, menyukai pola angka, menyusun balok konstruksi (Lego), memecahkan puzzle, dan kritis bertanya 'Mengapa?'.",
            dampakJikaLemah = "Jika logika dasar kurang terasah, anak bisa takut duluan melihat pelajaran berhitung di SD.",
            solusiAgarTidakMenghambat = "Kenalkan angka lewat benda nyata sehari-hari seperti menghitung buah, bermain jual-beli, dan mengelompokkan warna mainan.",
            harusNgapainSaja = listOf(
                "Ajak eksperimen sains sederhana di rumah setiap akhir pekan.",
                "Sediakan mainan Lego, tangram, rubik pemula, atau catur anak.",
                "Jawab pertanyaan kritis anak dengan sabar dan ajak ia menebak sebabnya."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Les Robotika & Coding Visual Anak",
                "Kursus Matematika Logika Menyenangkan / Sempoa",
                "Klub Sains Cilik (Little Scientist) & Catur"
            ),
            potensiMasaDepan = listOf("AI & Data Scientist", "Software Engineer", "Peneliti Sains & Analis Keuangan")
        ),
        DimensiBakatItemAndroid(
            id = "musik",
            nama = "Musikal-Irama & Auditori",
            julukan = "Si Peka Nada & Irama Harmonis",
            ikon = "🎹",
            warnaHex = 0xFF9333EA,
            deskripsiKekuatan = "Telinga dan perasaan Ananda sangat peka terhadap nada, ketukan irama, dan suara. Ia cepat menghafal lagu dan senang bergerak mengikuti musik.",
            dampakJikaLemah = "Anak mungkin kurang fokus mendengarkan instruksi lisan yang panjang di ruangan yang bising.",
            solusiAgarTidakMenghambat = "Lakukan kontak mata sejajar saat berbicara dengan anak dan bantu ia menghafal doa/pelajaran menggunakan nada lagu ceria.",
            harusNgapainSaja = listOf(
                "Putarkan lagu edukatif dan instrumen musik untuk keseimbangan otak.",
                "Ajak bermain tebak suara dan menepuk pola irama.",
                "Gunakan metode lagu saat menghafal kosakata atau surat pendek."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Les Piano / Keyboard Dasar",
                "Kelas Vokal / Paduan Suara Anak",
                "Les Gitar Akustik, Biola, atau Perkusi Drum"
            ),
            potensiMasaDepan = listOf("Komposer & Produser Musik", "Sound Engineer", "Pendidik Seni & Kreator Audio")
        ),
        DimensiBakatItemAndroid(
            id = "bahasa",
            nama = "Linguistik-Verbal & Bercerita",
            julukan = "Si Pendongeng & Pembicara Cilik",
            ikon = "📚",
            warnaHex = 0xFF059669,
            deskripsiKekuatan = "Ananda cepat menyerap kosakata baru, senang mendengarkan dongeng, pandai merangkai cerita, dan berani mengungkapkan pendapatnya.",
            dampakJikaLemah = "Jika verbal kurang terlatih, anak mungkin kesulitan mengungkapkan perasaannya dengan kata-kata saat kesal.",
            solusiAgarTidakMenghambat = "Dengarkan dengan sabar saat anak bercerita tanpa memotongnya, dan bacakan buku cerita bergambar setiap malam.",
            harusNgapainSaja = listOf(
                "Bacakan buku cerita bergambar (Read Aloud) 15 menit sebelum tidur.",
                "Ajak bermain sambung cerita dan bermain peran (jadi guru/dokter).",
                "Kenalkan kosakata bahasa Inggris dan Arab lewat lagu harian."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Kelas Storytelling (Mendongeng) & Public Speaking Cilik",
                "Kursus Bahasa Inggris / Bahasa Arab Interaktif Anak",
                "Klub Baca-Tulis & Jurnalistik Cilik"
            ),
            potensiMasaDepan = listOf("Penulis & Jurnalis", "Diplomat & Pengacara", "Dosen & Content Presenter")
        ),
        DimensiBakatItemAndroid(
            id = "sosial",
            nama = "Interpersonal, Empati & Kepemimpinan",
            julukan = "Si Sahabat Ramah & Pemimpin Cilik",
            ikon = "🤝",
            warnaHex = 0xFF0284C7,
            deskripsiKekuatan = "Ananda mudah akrab dengan teman baru, peka menolong teman yang sedih, senang berbagi, dan berani memimpin permainan bersama.",
            dampakJikaLemah = "Anak mungkin butuh waktu lebih lama beradaptasi di tempat baru atau malu bertanya di kelas.",
            solusiAgarTidakMenghambat = "Jangan melabeli anak pemalu. Ajak 1-2 teman bermain ke rumah agar ia nyaman bersosialisasi secara bertahap.",
            harusNgapainSaja = listOf(
                "Biasakan 4 kata santun: Tolong, Terima kasih, Maaf, dan Permisi.",
                "Beri kepercayaan memimpin doa makan di keluarga.",
                "Ajak berbagi makanan/mainan kepada teman dan sesama."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Pramuka Siaga / Klub Kepanduan Anak",
                "Kelas Teater & Drama Musikal Anak",
                "Olahraga Beregu (Futsal / Basket Mini)"
            ),
            potensiMasaDepan = listOf("Pemimpin Organisasi / CEO", "Psikolog & HR Specialist", "Dokter & Pendidik")
        ),
        DimensiBakatItemAndroid(
            id = "alam",
            nama = "Naturalis & Cinta Lingkungan",
            julukan = "Si Penjelajah Alam & Penyayang Satwa",
            ikon = "🌿",
            warnaHex = 0xFF16A34A,
            deskripsiKekuatan = "Ananda sangat antusias di alam terbuka, gemar mengamati hewan, tanaman, cuaca, dan penuh kasih sayang terhadap makhluk hidup.",
            dampakJikaLemah = "Anak cenderung terlalu terpaku pada layar gawai di dalam ruangan dan takut pada serangga/rumput.",
            solusiAgarTidakMenghambat = "Jadwalkan jalan pagi bebas gawai di taman setiap akhir pekan dan ajak menanam benih tanaman sederhana.",
            harusNgapainSaja = listOf(
                "Beri anak 1 pot tanaman atau ikan hias untuk dirawat setiap hari.",
                "Ajak ke kebun binatang, taman bunga, atau wisata alam.",
                "Biasakan membuang dan memilah sampah pada tempatnya."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Klub Sains Alam & Berkebun Anak (Little Gardener)",
                "Kegiatan Berkuda / Memanah Outdoor",
                "Klub Dokter Kecil & Pecinta Satwa"
            ),
            potensiMasaDepan = listOf("Dokter Hewan & Ahli Biologi", "Pakar Lingkungan (ESG)", "Ahli Agroteknologi")
        ),
        DimensiBakatItemAndroid(
            id = "mandiri",
            nama = "Intrapersonal, Fokus & Kemandirian",
            julukan = "Si Mandiri & Teguh Pendirian",
            ikon = "🌟",
            warnaHex = 0xFFD97706,
            deskripsiKekuatan = "Ananda tahu apa yang ia sukai, mampu fokus menyelesaikan permainannya sendiri sampai tuntas, dan memiliki kemauan kuat.",
            dampakJikaLemah = "Anak mudah menyerah saat menemui kesulitan kecil dan selalu bergantung penuh pada bantuan orang tua.",
            solusiAgarTidakMenghambat = "Jangan langsung mengambil alih tugas anak saat ia baru mencoba; puji usaha dan ketekunannya.",
            harusNgapainSaja = listOf(
                "Buat papan Stiker Bintang Kebiasaan Baik di kamar anak.",
                "Ajarkan anak mengenali emosinya dan tarik napas saat kesal.",
                "Berikan pilihan sederhana (memilih baju/buku sendiri)."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Kelas Montessori & Life-Skills Mandiri",
                "Les Memanah / Catur (Melatih fokus & ketenangan)",
                "Kelas Memasak Anak (Junior Chef) & Bela Diri"
            ),
            potensiMasaDepan = listOf("Entrepreneur Mandiri", "Perencana Strategis", "Pemimpin Berintegritas")
        )
    )

    val KARTU_VISUAL_ANAK = listOf(
        KartuVisualAnakItem(1, "1. Saat waktu bermain bebas, mana yang paling suka kamu mainkan?", listOf(
            OpsiVisualAnak("seni", "🎨", "Menggambar & Mewarnai"),
            OpsiVisualAnak("fisik", "⚽", "Main Bola & Bersepeda"),
            OpsiVisualAnak("logika", "🧩", "Menyusun Lego & Puzzle"),
            OpsiVisualAnak("sosial", "👫", "Main Rame-Rame Teman")
        )),
        KartuVisualAnakItem(2, "2. Kalau diberi hadiah kejutan, benda mana yang paling kamu inginkan?", listOf(
            OpsiVisualAnak("musik", "🎹", "Piano Mini / Gitar / Drum"),
            OpsiVisualAnak("alam", "🐰", "Kelinci / Ikan / Tanaman"),
            OpsiVisualAnak("bahasa", "📚", "Buku Dongeng Bergambar"),
            OpsiVisualAnak("mandiri", "⛺", "Tenda Kemah & Tas Sendiri")
        )),
        KartuVisualAnakItem(3, "3. Tempat liburan mana yang paling ingin kamu kunjungi?", listOf(
            OpsiVisualAnak("fisik", "🏊", "Kolam Renang & Outbound"),
            OpsiVisualAnak("alam", "🦁", "Kebun Binatang & Taman"),
            OpsiVisualAnak("logika", "🚀", "Museum Sains & Robot"),
            OpsiVisualAnak("seni", "🎭", "Studio Lukis & Istana")
        )),
        KartuVisualAnakItem(4, "4. Di sekolah, kegiatan apa yang paling membuatmu gembira?", listOf(
            OpsiVisualAnak("musik", "🎵", "Menyanyi & Gerak Lagu"),
            OpsiVisualAnak("bahasa", "🎤", "Maju Bercerita di Kelas"),
            OpsiVisualAnak("sosial", "👑", "Jadi Kapten Kelompok"),
            OpsiVisualAnak("mandiri", "🏅", "Menuntaskan Tugas Sendiri")
        )),
        KartuVisualAnakItem(5, "5. Kalau melihat pelangi di langit, apa yang ingin kamu lakukan?", listOf(
            OpsiVisualAnak("seni", "🌈", "Menggambar Pelangi Indah"),
            OpsiVisualAnak("logika", "🔍", "Bertanya Kenapa Muncul Pelangi"),
            OpsiVisualAnak("alam", "🌱", "Melihat Bunga & Pohon Segar"),
            OpsiVisualAnak("musik", "🎶", "Menyanyikan Lagu Pelangi")
        )),
        KartuVisualAnakItem(6, "6. Saat melihat temanmu sedih, apa yang kamu lakukan?", listOf(
            OpsiVisualAnak("sosial", "🤗", "Menolong & Menghiburnya"),
            OpsiVisualAnak("bahasa", "💬", "Mengajak Ngobrol Lucu"),
            OpsiVisualAnak("mandiri", "🩹", "Mengambilkan Air / Obat"),
            OpsiVisualAnak("seni", "🎁", "Memberi Gambar Buatanku")
        )),
        KartuVisualAnakItem(7, "7. Permainan tantangan mana yang paling seru buatmu?", listOf(
            OpsiVisualAnak("logika", "🔢", "Tebak Angka & Labirin"),
            OpsiVisualAnak("fisik", "🏃", "Lomba Lari & Melompat"),
            OpsiVisualAnak("musik", "🥁", "Tebak Lagu & Irama"),
            OpsiVisualAnak("alam", "🦋", "Mencari Kupu-Kupu di Taman")
        )),
        KartuVisualAnakItem(8, "8. Kalau membantu Ayah & Ibu di rumah, tugas mana yang kamu pilih?", listOf(
            OpsiVisualAnak("alam", "🌻", "Menyiram Tanaman & Hewan"),
            OpsiVisualAnak("mandiri", "🧺", "Merapikan Kamar Sendiri"),
            OpsiVisualAnak("fisik", "🧹", "Membantu Menyapu & Cuci"),
            OpsiVisualAnak("sosial", "🍽️", "Menyiapkan Meja Makan")
        )),
        KartuVisualAnakItem(9, "9. Kalau besar nanti, kamu ingin jadi tokoh hebat seperti apa?", listOf(
            OpsiVisualAnak("logika", "👨‍🔬", "Penemu Robot & Astronot"),
            OpsiVisualAnak("sosial", "👩‍⚕️", "Dokter & Guru Penolong"),
            OpsiVisualAnak("seni", "👩‍🎨", "Pelukis & Arsitek Hebat"),
            OpsiVisualAnak("fisik", "🏆", "Juara Olahraga & Pilot")
        )),
        KartuVisualAnakItem(10, "10. Bagaimana caramu paling cepat menghafal doa atau lagu baru?", listOf(
            OpsiVisualAnak("musik", "🎵", "Dinyanyikan dengan Irama"),
            OpsiVisualAnak("seni", "🖼️", "Melihat Gambar Warna-Warni"),
            OpsiVisualAnak("fisik", "🤸", "Sambil Praktik Gerakan"),
            OpsiVisualAnak("bahasa", "📖", "Dibacakan Lalu Kuceritakan")
        )),
        KartuVisualAnakItem(11, "11. Kalau bermain di pantai, apa yang paling seru dilakukan?", listOf(
            OpsiVisualAnak("seni", "🏰", "Membuat Istana Pasir Indah"),
            OpsiVisualAnak("fisik", "🌊", "Berenang & Lompat Ombak"),
            OpsiVisualAnak("alam", "🦀", "Mencari Kepiting & Kerang"),
            OpsiVisualAnak("logika", "🪣", "Membuat Saluran Air Pasir")
        )),
        KartuVisualAnakItem(12, "12. Setelah selesai membuat karya bagus, apa yang kamu lakukan?", listOf(
            OpsiVisualAnak("mandiri", "⭐", "Menyimpannya dengan Rapi"),
            OpsiVisualAnak("sosial", "👨‍👩‍👧", "Memamerkannya ke Keluarga"),
            OpsiVisualAnak("bahasa", "📢", "Menceritakan Cara Buatnya"),
            OpsiVisualAnak("musik", "💃", "Menari & Bernyanyi Senang")
        ))
    )

    val OBSERVASI_ORTU_LIST = listOf(
        SoalLikertRemajaDewasa(1, 1, "seni", "Anak gemar menggambar, memadukan banyak warna, dan membuat bentuk plastisin/origami."),
        SoalLikertRemajaDewasa(2, 1, "fisik", "Anak sangat aktif bergerak (berlari, melompat, berenang) dan tangkas motorik tangannya."),
        SoalLikertRemajaDewasa(3, 1, "logika", "Anak suka bertanya 'mengapa/bagaimana cara kerjanya' dan menyukai angka/puzzle/lego."),
        SoalLikertRemajaDewasa(4, 1, "musik", "Anak cepat menghafal nada lagu, suka bersenandung, dan bergerak mengikuti irama."),
        SoalLikertRemajaDewasa(5, 1, "bahasa", "Anak kosakatanya kaya, senang mendengarkan dongeng, dan pandai bercerita panjang."),
        SoalLikertRemajaDewasa(6, 1, "sosial", "Anak mudah bergaul dengan teman, peka menolong, dan berani mengajak bermain bersama."),
        SoalLikertRemajaDewasa(7, 1, "alam", "Anak sangat menyukai hewan peliharaan, tanaman, dan antusias saat diajak ke alam."),
        SoalLikertRemajaDewasa(8, 1, "mandiri", "Anak fokus menyelesaikan permainannya sendiri dan mandiri dalam rutinitas harian."),
        SoalLikertRemajaDewasa(9, 1, "seni", "Anak peka terhadap detail visual dan suka menata benda berdasarkan keindahan warna."),
        SoalLikertRemajaDewasa(10, 1, "logika", "Saat mainannya macet/sulit, anak berusaha mencari akal memperbaikinya sebelum menyerah."),
        SoalLikertRemajaDewasa(11, 1, "sosial", "Anak berani tampil di depan keluarga atau kelas (menyanyi, menyapa, menunjukkan karya)."),
        SoalLikertRemajaDewasa(12, 1, "mandiri", "Anak mampu menenangkan dirinya kembali dengan baik setelah mengalami kekecewaan kecil.")
    )

    // ========================================================================
    // 2. DEFINISI 8 BAKAT REMAJA SMP (9-15 TAHUN) + SOAL
    // ========================================================================
    val DIMENSI_SMP_LIST = listOf(
        DimensiBakatItemAndroid(
            id = "logika_sains",
            nama = "Logika, Sains & Teknologi Digital",
            julukan = "The Young Scientist & Coder",
            ikon = "🔬",
            warnaHex = 0xFF2563EB,
            deskripsiKekuatan = "Kamu berpikir logis, kritis, dan penasaran dengan cara kerja sains maupun teknologi. Kamu menyukai teka-teki matematika, eksperimen IPA, atau koding.",
            dampakJikaLemah = "Mudah merasa pusing atau malas duluan saat bertemu pelajaran Matematika/Fisika yang banyak rumusnya.",
            solusiAgarTidakMenghambat = "Gunakan simulasi visual/animasi eksperimen agar melihat wujud nyata rumus tersebut, dan belajar bareng teman yang jago sains.",
            harusNgapainSaja = listOf(
                "Berlatih logika lewat koding visual/Python dasar 20 menit sehari.",
                "Buat ringkasan rumus dengan contoh soal nyata yang mudah dipahami.",
                "Ikuti praktikum IPA atau rakit elektronik di simulator."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Ekskul Karya Ilmiah Remaja (KIR) / Klub Olimpiade OSN",
                "Kursus Coding, Robotika & Game Development",
                "Persiapan: SMA MIPA / SMK Rekayasa Perangkat Lunak (RPL)"
            ),
            potensiMasaDepan = listOf("AI & Software Engineer", "Dokter & Peneliti Sains", "Data Scientist")
        ),
        DimensiBakatItemAndroid(
            id = "bahasa_komunikasi",
            nama = "Bahasa, Komunikasi & Public Speaking",
            julukan = "The Confident Communicator",
            ikon = "🎙️",
            warnaHex = 0xFF059669,
            deskripsiKekuatan = "Kamu pandai merangkai kata saat bicara di depan kelas, berdiskusi, maupun menulis cerita, serta cepat menyerap bahasa asing.",
            dampakJikaLemah = "Sering gugup saat presentasi di depan kelas atau kesulitan menuliskan ide menjadi karangan rapi.",
            solusiAgarTidakMenghambat = "Siapkan 3-5 poin inti di kartu kecil (cue card) dan berlatih 2 kali di depan cermin sebelum maju presentasi.",
            harusNgapainSaja = listOf(
                "Angkat tangan minimal 1 kali seminggu untuk bertanya/menjawab di kelas.",
                "Catat 5 kosakata baru Bahasa Inggris/Arab setiap hari.",
                "Tulis ulasan singkat buku atau film favoritmu."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Kursus Bahasa Inggris (Conversation / Debate) & Bahasa Asing",
                "Ekskul Jurnalistik, Podcast & Klub Pidato/Debat",
                "Persiapan: SMA Bahasa & Sosial / Hubungan Internasional"
            ),
            potensiMasaDepan = listOf("Diplomat & Jurnalis", "Content Creator & Presenter", "Pengacara & Dosen")
        ),
        DimensiBakatItemAndroid(
            id = "visual_desain",
            nama = "Desain Visual, Seni & Imajinasi",
            julukan = "The Visual Creator & Designer",
            ikon = "🎨",
            warnaHex = 0xFFE11D48,
            deskripsiKekuatan = "Matamu sangat peka terhadap keindahan warna, tata letak, gambar, dan video. Kamu punya banyak ide kreatif untuk mendesain karya visual.",
            dampakJikaLemah = "Cepat mengantuk jika harus membaca buku teks tebal yang penuh tulisan tanpa gambar.",
            solusiAgarTidakMenghambat = "Gunakan stabilo 3 warna untuk menandai kata kunci penting dan ubah catatan menjadi bagan kotak-kotak (Mind-Map).",
            harusNgapainSaja = listOf(
                "Ubah catatan pelajaran menjadi Mind-Map berwarna.",
                "Kuasai Canva, Figma, atau aplikasi edit video untuk tugas sekolah.",
                "Simpan karya desainmu di portofolio digital."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Ekskul Desain Grafis, Fotografi & Multimedia",
                "Kelas Ilustrasi Digital & Animasi",
                "Persiapan: SMK DKV / Multimedia atau SMA Seni-Teknologi"
            ),
            potensiMasaDepan = listOf("UI/UX Product Designer", "Arsitek", "Creative Director & Animator")
        ),
        DimensiBakatItemAndroid(
            id = "kinestetik_olahraga",
            nama = "Kinestetik, Olahraga & Praktik",
            julukan = "The Active Champion & Builder",
            ikon = "🏆",
            warnaHex = 0xFFEA580C,
            deskripsiKekuatan = "Tubuhmu tangkas, energik, dan paling cepat paham kalau langsung mempraktikkan dengan tangan sendiri di lapangan atau bengkel/lab.",
            dampakJikaLemah = "Cepat merasa bosan dan pegal jika harus duduk diam mendengarkan ceramah berjam-jam.",
            solusiAgarTidakMenghambat = "Gunakan teknik Pomodoro: 25 menit fokus belajar duduk, lalu 5 menit berdiri melakukan peregangan/jalan.",
            harusNgapainSaja = listOf(
                "Ikuti olahraga rutin minimal 2 kali seminggu.",
                "Praktikkan materi pelajaran dengan merakit alat/percobaan nyata.",
                "Jaga pola tidur dan nutrisi agar stamina tetap prima."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Ekskul Olahraga Prestasi (O2SN: Futsal, Basket, Renang, Bela Diri)",
                "Klub Robotika & Bengkel Praktik",
                "Persiapan: SMK Teknik / Kesehatan atau SMA Keolahragaan"
            ),
            potensiMasaDepan = listOf("Insinyur Mekanik & IoT", "Atlet Profesional", "Dokter Bedah & Pilot")
        ),
        DimensiBakatItemAndroid(
            id = "kepemimpinan_sosial",
            nama = "Kepemimpinan, Organisasi & Sosial",
            julukan = "The Team Leader & Peacemaker",
            ikon = "👑",
            warnaHex = 0xFF0284C7,
            deskripsiKekuatan = "Kamu mudah dipercaya teman, peka menolong sesama, berani memimpin kerja kelompok, dan pintar mengoordinasikan acara.",
            dampakJikaLemah = "Sungkan menegur teman kelompok yang pasif atau canggung di lingkungan baru.",
            solusiAgarTidakMenghambat = "Ambil peran spesifik yang kamu kuasai di kelompok dan komunikasikan pembagian tugas secara tertulis di grup.",
            harusNgapainSaja = listOf(
                "Aktif menjadi pengurus kelas, OSIS, Pramuka, atau PMR.",
                "Latih membagi tugas secara adil saat kerja kelompok.",
                "Jadilah penengah yang bijak saat teman berbeda pendapat."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Organisasi Siswa Intra Sekolah (OSIS) / Pramuka / PMR",
                "Klub Kewirausahaan Remaja",
                "Persiapan: SMA Soshum (Ekonomi/Sosiologi) / SMK Bisnis"
            ),
            potensiMasaDepan = listOf("CEO / Entrepreneur", "Manajer Proyek", "Psikolog & HRBP")
        ),
        DimensiBakatItemAndroid(
            id = "keteraturan_disiplin",
            nama = "Keteraturan, Ketelitian & Fokus",
            julukan = "The Organized & Reliable Planner",
            ikon = "📋",
            warnaHex = 0xFF0D9488,
            deskripsiKekuatan = "Kamu rapi, taat aturan, teliti memeriksa detail, dan konsisten mengumpulkan tugas tepat waktu dengan hasil terukur.",
            dampakJikaLemah = "Rentan menunda PR sampai malam terakhir (deadliner) atau kurang teliti memeriksa lembar ujian.",
            solusiAgarTidakMenghambat = "Pasang alarm pengingat PR pukul 16.00 di HP dan luangkan 5 menit memeriksa ulang jawaban sebelum dikumpulkan.",
            harusNgapainSaja = listOf(
                "Gunakan To-Do List harian untuk mencentang PR yang selesai.",
                "Siapkan buku pelajaran di dalam tas pada malam sebelumnya.",
                "Latih pengelolaan uang saku dan tabungan dengan rapi."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Pelatihan Manajemen Waktu & Komputer Data (Excel)",
                "Paskibra & Pengurus Koperasi/Perpustakaan",
                "Persiapan: SMA Ekonomi-Akuntansi / SMK Akuntansi"
            ),
            potensiMasaDepan = listOf("Auditor Keuangan & Aktuaris", "Analis Sistem", "Quality Assurance Lead")
        )
    )

    val SOAL_SMP_LIST = listOf(
        SoalLikertRemajaDewasa(1, 1, "logika_sains", "Saya senang mencari tahu alasan ilmiah di balik fenomena alam atau cara kerja teknologi/komputer."),
        SoalLikertRemajaDewasa(2, 1, "bahasa_komunikasi", "Saya mudah merangkai kata-kata saat bercerita, menulis karangan, atau menjelaskan sesuatu ke teman."),
        SoalLikertRemajaDewasa(3, 1, "visual_desain", "Saya suka menggambar, mendesain poster/slide yang estetik, atau mengedit foto dan video."),
        SoalLikertRemajaDewasa(4, 1, "kinestetik_olahraga", "Saya paling bersemangat saat pelajaran olahraga, kegiatan lapangan, atau praktik merakit benda nyata."),
        SoalLikertRemajaDewasa(5, 1, "kepemimpinan_sosial", "Saya senang membantu teman yang kesulitan dan berani mengatur pembagian tugas saat kerja kelompok."),
        SoalLikertRemajaDewasa(6, 1, "keteraturan_disiplin", "Saya terbiasa mencatat jadwal PR dengan rapi dan fokus mengejar target nilai yang saya tetapkan."),
        SoalLikertRemajaDewasa(7, 1, "logika_sains", "Memecahkan soal teka-teki logika, matematika, atau mengutak-atik kode program sangat menantang bagi saya."),
        SoalLikertRemajaDewasa(8, 1, "bahasa_komunikasi", "Saya tertarik mempelajari bahasa asing (Inggris/Arab) dan berani tampil bicara di depan kelas."),
        SoalLikertRemajaDewasa(9, 1, "visual_desain", "Saya lebih mudah memahami pelajaran kalau melihat gambar diagram, peta warna, atau video animasi."),
        SoalLikertRemajaDewasa(10, 1, "kinestetik_olahraga", "Tangan saya terampil menggunakan peralatan kerja, merakit komponen, atau melakukan gerakan olahraga."),
        SoalLikertRemajaDewasa(11, 1, "kepemimpinan_sosial", "Saya senang aktif di kegiatan organisasi sekolah (OSIS/Pramuka/Kepanitiaan) dan kegiatan sosial."),
        SoalLikertRemajaDewasa(12, 1, "keteraturan_disiplin", "Saya teliti memeriksa kembali jawaban ulangan dan mampu belajar mandiri tanpa harus disuruh.")
    )

    // ========================================================================
    // 3. DEFINISI 6 KEPRIBADIAN KARIER RIASEC SMA/SMK & MAHASISWA/UMUM
    // ========================================================================
    val DIMENSI_RIASEC_SMA_LIST = listOf(
        DimensiBakatItemAndroid(
            id = "I",
            nama = "Investigative (Sains, IT, Data & Medis)",
            julukan = "The Analytical Thinker & Scientist",
            ikon = "🧬",
            warnaHex = 0xFF2563EB,
            deskripsiKekuatan = "Unggul dalam analisis logis, riset ilmiah, pemrograman algoritma, pengolahan data, serta pemecahan masalah medis/teknologi.",
            dampakJikaLemah = "Cepat jenuh menghadapi analisis statistik yang rumit atau riset teoretis yang abstrak.",
            solusiAgarTidakMenghambat = "Terjemahkan teori abstrak menjadi studi kasus praktis dan gunakan tools analitik visual.",
            harusNgapainSaja = listOf(
                "Pilih Mapel Pilihan: Informatika, Matematika Lanjut, Fisika/Biologi/Kimia.",
                "Bangun portofolio aplikasi web/mobile, IoT, atau karya tulis ilmiah.",
                "Latih penalaran kuantitatif & literasi sains untuk SNBT/UTBK."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Jurusan Kuliah: Teknik Informatika / Ilmu Komputer / AI / Sains Data",
                "Jurusan Kuliah: Kedokteran / Farmasi / Teknik Biomedis / Statistika",
                "Mapel Pilihan: Informatika, Matematika Lanjut, Fisika/Biologi"
            ),
            potensiMasaDepan = listOf("AI & Full-Stack Engineer", "Dokter Spesialis & Peneliti", "Data Scientist & Cybersecurity")
        ),
        DimensiBakatItemAndroid(
            id = "A",
            nama = "Artistic (Desain, Arsitektur, Media & Inovasi)",
            julukan = "The Visionary Designer & Creator",
            ikon = "🎨",
            warnaHex = 0xFFE11D48,
            deskripsiKekuatan = "Unggul dalam imajinasi orisinal, kepekaan estetika visual/audio, dan perancangan produk kreatif yang menyentuh emosi audiens.",
            dampakJikaLemah = "Penyajian presentasi atau produk cenderung kaku dan terpaku pada pola standar.",
            solusiAgarTidakMenghambat = "Gunakan sistem desain terstruktur dan kolaborasikan ide kreatif dengan jadwal kerja yang disiplin.",
            harusNgapainSaja = listOf(
                "Pilih Mapel Pilihan: Seni Rupa/Desain, Informatika Multimedia, Bahasa Inggris Lanjut.",
                "Susun portofolio minimal 5 karya desain/video/arsitektur terbaik.",
                "Pelajari UI/UX Design, 3D Modeling, dan Creative Coding."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Jurusan Kuliah: Desain Komunikasi Visual (DKV) & Desain Produk",
                "Jurusan Kuliah: Arsitektur, Film & Animasi, Ilmu Komunikasi Kreatif",
                "Mapel Pilihan: Seni Budaya/Desain, Informatika, Bahasa Inggris"
            ),
            potensiMasaDepan = listOf("UI/UX Product Designer", "Arsitek & Urban Designer", "Creative Director & Animator")
        ),
        DimensiBakatItemAndroid(
            id = "S",
            nama = "Social (Edukasi, Psikologi, HRBP & Kesehatan)",
            julukan = "The Empathetic Mentor & People Developer",
            ikon = "🤝",
            warnaHex = 0xFF059669,
            deskripsiKekuatan = "Unggul dalam kecerdasan interpersonal, empati, komunikasi persuasif, serta kepuasan saat mengajar, membimbing, dan mengembangkan manusia.",
            dampakJikaLemah = "Cenderung terlalu lugas/dingin saat mengkritik atau kurang sabar membimbing rekan yang lambat.",
            solusiAgarTidakMenghambat = "Terapkan komunikasi empatik (apresiasi sebelum koreksi) dalam kerja tim.",
            harusNgapainSaja = listOf(
                "Pilih Mapel Pilihan: Sosiologi, Biologi/Kesehatan, Bahasa Indonesia & Asing.",
                "Aktif sebagai tutor sebaya, konselor remaja, atau relawan edukasi.",
                "Dalami psikologi organisasi, HR Management, dan komunikasi publik."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Jurusan Kuliah: Psikologi, Manajemen SDM (Human Capital), Bimbingan Konseling",
                "Jurusan Kuliah: Pendidikan/Keguruan, Kesehatan Masyarakat, Hubungan Internasional",
                "Mapel Pilihan: Sosiologi, Biologi, Bahasa Inggris/Asing"
            ),
            potensiMasaDepan = listOf("HR Business Partner (HRBP) & OD Specialist", "Psikolog & Konselor", "Dosen & EdTech Leader")
        ),
        DimensiBakatItemAndroid(
            id = "E",
            nama = "Enterprising (Bisnis, Kepemimpinan, Hukum & Strategi)",
            julukan = "The Strategic Leader & Entrepreneur",
            ikon = "🚀",
            warnaHex = 0xFFD97706,
            deskripsiKekuatan = "Unggul dalam kepemimpinan tegas, negosiasi, melihat peluang pasar, serta menggerakkan tim mencapai target organisasi.",
            dampakJikaLemah = "Kurang percaya diri saat harus mempresentasikan nilai jual karya atau mengambil komando keputusan.",
            solusiAgarTidakMenghambat = "Bangun kepemimpinan berbasis keahlian (expert leadership) dan latih teknik presentasi terstruktur.",
            harusNgapainSaja = listOf(
                "Pilih Mapel Pilihan: Ekonomi, Kewirausahaan, Sosiologi/PPKn, Matematika.",
                "Pimpin proyek organisasi sekolah/kampus atau ikuti lomba Business Plan.",
                "Asah public speaking, negosiasi, dan literasi manajemen proyek."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Jurusan Kuliah: Manajemen Bisnis, Kewirausahaan, Bisnis Digital",
                "Jurusan Kuliah: Ilmu Hukum, Hubungan Internasional, Manajemen Industri",
                "Mapel Pilihan: Ekonomi, Kewirausahaan, Sosiologi"
            ),
            potensiMasaDepan = listOf("Tech Startup Founder / CEO", "Product & Marketing Manager", "Konsultan Bisnis & Hukum")
        ),
        DimensiBakatItemAndroid(
            id = "R",
            nama = "Realistic (Rekayasa Teknik, IoT, Mesin & Lapangan)",
            julukan = "The Practical Engineer & Builder",
            ikon = "⚙️",
            warnaHex = 0xFFEA580C,
            deskripsiKekuatan = "Unggul dalam keahlian teknis praktis, merakit perangkat keras/IoT, mengendalikan mesin, konstruksi infrastruktur, dan agroteknologi.",
            dampakJikaLemah = "Kurang terampil menangani perbaikan perangkat keras fisik secara langsung.",
            solusiAgarTidakMenghambat = "Berkolaborasilah dengan teknisi/engineer perangkat keras saat mengimplementasikan proyek lapangan.",
            harusNgapainSaja = listOf(
                "Pilih Mapel Pilihan: Fisika Terapan, Informatika & Elektronika, Kimia.",
                "Rakit prototipe nyata menggunakan ESP32/Arduino dan sensor IoT.",
                "Ikuti kompetisi robotika dan inovasi teknologi tepat guna."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Jurusan Kuliah: Teknik Elektro, Teknik Mesin, Teknik Sipil, Teknik Industri",
                "Jurusan Kuliah: Sistem Tertanam (IoT/Robotika), Agroteknologi",
                "Mapel Pilihan: Fisika, Matematika Lanjut, Informatika"
            ),
            potensiMasaDepan = listOf("IoT & Robotics Engineer", "Civil & Mechanical Engineer", "Smart Farming Specialist")
        ),
        DimensiBakatItemAndroid(
            id = "C",
            nama = "Conventional (Keuangan, Akuntansi, Audit & Data)",
            julukan = "The Precision Guardian & Financial Architect",
            ikon = "📊",
            warnaHex = 0xFF0D9488,
            deskripsiKekuatan = "Unggul dalam ketelitian tinggi, tata kelola data, kepatuhan SOP/regulasi, penyusunan anggaran, dan pemeriksaan mutu.",
            dampakJikaLemah = "Rentan kurang rapi dalam dokumentasi administrasi atau pembukuan anggaran.",
            solusiAgarTidakMenghambat = "Gunakan aplikasi pencatat keuangan dan template otomatis untuk menjaga ketertiban administrasi.",
            harusNgapainSaja = listOf(
                "Pilih Mapel Pilihan: Ekonomi/Akuntansi, Matematika, Informatika Data.",
                "Kuasai spreadsheet tingkat lanjut, database SQL, dan akuntansi dasar.",
                "Susun arsip portofolio dan jadwal target belajar secara sistematis."
            ),
            rekomendasiLesAtauJurusan = listOf(
                "Jurusan Kuliah: Akuntansi, Perpajakan, Keuangan & Perbankan",
                "Jurusan Kuliah: Ilmu Aktuaria, Statistika, Sistem Informasi",
                "Mapel Pilihan: Ekonomi & Akuntansi, Matematika"
            ),
            potensiMasaDepan = listOf("Financial Auditor & CFO", "Actuary (Aktuaris)", "Quality Assurance & Database Admin")
        )
    )

    val SOAL_SMA_DAN_DEWASA_LIST = listOf(
        SoalLikertRemajaDewasa(1, 1, "R", "Saya senang merakit perangkat elektronik/IoT, mengutak-atik mesin, atau menggunakan peralatan teknis secara langsung."),
        SoalLikertRemajaDewasa(2, 1, "I", "Saya antusias memecahkan persoalan sains, matematika lanjut, atau menulis kode algoritma pemrograman."),
        SoalLikertRemajaDewasa(3, 1, "A", "Saya menikmati kebebasan berkreasi membuat desain visual (UI/UX/Grafis), karya arsitektur, musik, atau konten kreatif."),
        SoalLikertRemajaDewasa(4, 1, "S", "Saya merasa bahagia saat bisa mengajar, membimbing, atau membantu orang lain mengembangkan potensinya."),
        SoalLikertRemajaDewasa(5, 1, "E", "Saya menyukai tantangan memimpin organisasi/tim, merintis usaha bisnis, dan meyakinkan orang lain."),
        SoalLikertRemajaDewasa(6, 1, "C", "Saya teliti dalam menyusun laporan keuangan, mengelola tabel data di komputer, dan menjaga keteraturan aturan."),
        SoalLikertRemajaDewasa(7, 1, "R", "Saya lebih suka kuliah/bekerja yang banyak praktik laboratorium atau terjun ke lapangan daripada hanya membaca teori."),
        SoalLikertRemajaDewasa(8, 1, "I", "Menganalisis kumpulan data besar (Big Data/Statistika) untuk menemukan pola sebab-akibat sangat menarik bagi saya."),
        SoalLikertRemajaDewasa(9, 1, "A", "Saya selalu berusaha menampilkan ide yang orisinal, estetik, dan berbeda dari cara-cara standar yang membosankan."),
        SoalLikertRemajaDewasa(10, 1, "S", "Saya tertarik mendalami psikologi manusia, pengembangan SDM (HRBP/People Development), pendidikan, atau kesehatan."),
        SoalLikertRemajaDewasa(11, 1, "E", "Saya berani mengambil keputusan penting di situasi mendesak dan menetapkan target prestasi yang tinggi."),
        SoalLikertRemajaDewasa(12, 1, "C", "Saya selalu memeriksa ulang detail pekerjaan saya agar bebas dari kesalahan ketik maupun selisih perhitungan."),
        SoalLikertRemajaDewasa(13, 2, "I", "[Aktivitas Kerja] Merancang kode program aplikasi, arsitektur piranti lunak, atau riset ilmiah terstruktur."),
        SoalLikertRemajaDewasa(14, 2, "A", "[Aktivitas Kerja] Mencetuskan ide-ide kreatif segar dan merancang cetak biru desain visual/produk baru."),
        SoalLikertRemajaDewasa(15, 2, "S", "[Aktivitas Kerja] Melatih keterampilan, membimbing (mentoring), dan melayani sesama dengan tulus."),
        SoalLikertRemajaDewasa(16, 2, "E", "[Aktivitas Kerja] Mengomunikasikan gagasan di depan publik, bernegosiasi, dan memimpin koordinasi tim."),
        SoalLikertRemajaDewasa(17, 2, "C", "[Aktivitas Kerja] Menjalankan kepatuhan regulasi (compliancing), mengaudit anggaran, dan mengorganisir dokumen."),
        SoalLikertRemajaDewasa(18, 2, "R", "[Aktivitas Kerja] Merakit, menguji coba fungsi teknis alat, dan melakukan troubleshooting kerusakan sistem.")
    )
}