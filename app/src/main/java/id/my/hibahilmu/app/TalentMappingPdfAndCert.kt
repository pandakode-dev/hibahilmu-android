package id.my.hibahilmu.app

import android.content.Context
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color as AndroidColor
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

object TalentMappingPdfGenerator {

    private fun bagikanAtauBukaFilePdf(context: Context, file: File, judulChooser: String) {
        try {
            val uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.provider",
                file
            )
            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(intent, judulChooser))
        } catch (e: Exception) {
            Toast.makeText(
                context,
                "PDF Tersimpan di: ${file.absolutePath}",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    // ========================================================================
    // 1. CETAK SERTIFIKAT PENGHARGAAN EMAS (1 HALAMAN A4 LANDSCAPE UTUH!)
    // ========================================================================
    fun cetakSertifikatLandscapePdf(
        context: Context,
        namaPeserta: String,
        instansi: String,
        jenjang: JenjangTalentAndroid,
        hasilUrut: List<HasilEvaluasiDimensiAndroid>
    ) {
        val pdf = PdfDocument()
        // Ukuran A4 Landscape (842 x 595 pt)
        val pageInfo = PdfDocument.PageInfo.Builder(842, 595, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paintBg = Paint().apply { color = AndroidColor.parseColor("#FFFDF5") }
        canvas.drawRect(0f, 0f, 842f, 595f, paintBg)

        // Bingkai Emas Ganda
        val paintGoldOuter = Paint().apply {
            color = AndroidColor.parseColor("#D97706")
            style = Paint.Style.STROKE
            strokeWidth = 6f
        }
        canvas.drawRoundRect(RectF(24f, 24f, 818f, 571f), 18f, 18f, paintGoldOuter)

        val paintGoldInner = Paint().apply {
            color = AndroidColor.parseColor("#F59E0B")
            style = Paint.Style.STROKE
            strokeWidth = 2f
        }
        canvas.drawRoundRect(RectF(34f, 34f, 808f, 561f), 14f, 14f, paintGoldInner)

        val paintTitle = Paint().apply {
            color = AndroidColor.parseColor("#064E3B")
            textSize = 22f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        val paintSub = Paint().apply {
            color = AndroidColor.parseColor("#B45309")
            textSize = 12f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }

        canvas.drawText("🐼 PANDA HIBAHILMU & PANDAMUSLIM TALENT CENTER", 421f, 75f, paintSub)
        canvas.drawText("SERTIFIKAT PENGHARGAAN BAKAT & POTENSI EMAS", 421f, 106f, paintTitle)

        val paintBody = Paint().apply {
            color = AndroidColor.parseColor("#475569")
            textSize = 12f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("Diberikan dengan bangga kepada:", 421f, 145f, paintBody)

        val paintNama = Paint().apply {
            color = AndroidColor.parseColor("#0F172A")
            textSize = 30f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(namaPeserta.ifBlank { "Ananda / Siswa Berbakat" }, 421f, 185f, paintNama)

        val top3 = hasilUrut.take(3)
        val gelarUtama = top3.firstOrNull()?.dimensi?.julukan ?: "Bintang Masa Depan"
        canvas.drawText(
            "Gelar Bakat Utama: \"$gelarUtama\" • Kategori: ${jenjang.judul} (${jenjang.rentangUsia})",
            421f,
            215f,
            paintSub
        )

        // Gambar 3 Kotak Medali Bakat Utama Secara Horizontal (Landscape)
        val cardW = 235f
        val startX = 55f
        val topY = 245f
        val paintCard = Paint().apply {
            color = AndroidColor.WHITE
            style = Paint.Style.FILL
        }
        val paintCardBorder = Paint().apply {
            color = AndroidColor.parseColor("#F59E0B")
            style = Paint.Style.STROKE
            strokeWidth = 2.5f
        }
        val paintCardTitle = Paint().apply {
            color = AndroidColor.parseColor("#0F172A")
            textSize = 13f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintCardText = Paint().apply {
            color = AndroidColor.parseColor("#334155")
            textSize = 10.5f
        }

        top3.forEachIndexed { idx, item ->
            val left = startX + idx * (cardW + 18f)
            val rect = RectF(left, topY, left + cardW, topY + 195f)
            canvas.drawRoundRect(rect, 14f, 14f, paintCard)
            canvas.drawRoundRect(rect, 14f, 14f, paintCardBorder)

            canvas.drawText(
                "MEDALI BAKAT #${idx + 1} (${item.persen}%)",
                left + 14f,
                topY + 28f,
                paintCardTitle
            )
            canvas.drawText(
                item.dimensi.nama.take(28),
                left + 14f,
                topY + 52f,
                paintCardTitle
            )
            canvas.drawText(
                item.dimensi.julukan,
                left + 14f,
                topY + 72f,
                paintCardText
            )
            canvas.drawText(
                "Rekomendasi Utama:",
                left + 14f,
                topY + 102f,
                paintCardTitle
            )
            item.dimensi.rekomendasiLesAtauJurusan.take(2).forEachIndexed { rIdx, rek ->
                canvas.drawText(
                    "• ${rek.take(32)}",
                    left + 14f,
                    topY + 122f + rIdx * 18f,
                    paintCardText
                )
            }
        }

        // Footer Pengesahan
        val tanggal = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID")).format(Date())
        val paintFooter = Paint().apply {
            color = AndroidColor.parseColor("#065F46")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("Instansi / Sekolah: $instansi", 55f, 510f, paintFooter)
        canvas.drawText("Dokumen Resmi Terverifikasi • Diterbitkan: $tanggal", 55f, 530f, paintCardText)

        paintFooter.textAlign = Paint.Align.RIGHT
        canvas.drawText("Disahkan oleh: Panda HibahIlmu Talent Center", 785f, 510f, paintFooter)
        paintCardText.textAlign = Paint.Align.RIGHT
        canvas.drawText("https://hibahilmu.my.id • Wakaf & Hibah Ilmu Gratis", 785f, 530f, paintCardText)

        pdf.finishPage(page)

        val namaFile = "Sertifikat_Landscape_${namaPeserta.replace(" ", "_")}.pdf"
        val file = File(context.cacheDir, namaFile)
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()

        bagikanAtauBukaFilePdf(context, file, "Cetak / Simpan Sertifikat Landscape (PDF)")
    }

    // ========================================================================
    // 2. CETAK LAPORAN ANALISIS LENGKAP & SOLUSI KELEMAHAN (A4 PORTRAIT)
    // ========================================================================
    fun cetakLaporanPortraitPdf(
        context: Context,
        namaPeserta: String,
        instansi: String,
        jenjang: JenjangTalentAndroid,
        hasilUrut: List<HasilEvaluasiDimensiAndroid>
    ) {
        val pdf = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        val page = pdf.startPage(pageInfo)
        val canvas: Canvas = page.canvas

        val paintHeader = Paint().apply {
            color = AndroidColor.parseColor("#064E3B")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintConfidential = Paint().apply {
            color = AndroidColor.parseColor("#E11D48")
            textSize = 10f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintBold = Paint().apply {
            color = AndroidColor.parseColor("#0F172A")
            textSize = 11f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val paintReg = Paint().apply {
            color = AndroidColor.parseColor("#334155")
            textSize = 10f
        }

        canvas.drawText("🔒 DOKUMEN BERSIFAT RAHASIA (STRICTLY CONFIDENTIAL)", 36f, 38f, paintConfidential)
        canvas.drawText("LAPORAN EKSEKUTIF TALENT MAPPING — PANDA HIBAHILMU", 36f, 60f, paintHeader)
        canvas.drawText("Nama Peserta: $namaPeserta  |  Instansi: $instansi", 36f, 80f, paintBold)
        canvas.drawText("Kategori Jenjang: ${jenjang.judul} (${jenjang.rentangUsia})", 36f, 96f, paintReg)

        var yPos = 126f
        canvas.drawText("1. URUTAN PERINGKAT KEKUATAN & REKOMENDASI UTAMA:", 36f, yPos, paintBold)
        yPos += 20f

        hasilUrut.forEach { item ->
            canvas.drawText(
                "#${item.peringkat}. ${item.dimensi.nama} (${item.persen}%) — ${item.dimensi.julukan}",
                42f,
                yPos,
                paintBold
            )
            yPos += 15f
            canvas.drawText(
                "   • Langkah Aksi: ${item.dimensi.harusNgapainSaja.firstOrNull() ?: ""}",
                42f,
                yPos,
                paintReg
            )
            yPos += 14f
            canvas.drawText(
                "   • Rekomendasi Les/Jurusan: ${item.dimensi.rekomendasiLesAtauJurusan.joinToString(", ").take(75)}",
                42f,
                yPos,
                paintReg
            )
            yPos += 20f
        }

        yPos += 10f
        canvas.drawText("2. ANALISIS AREA LEMAH & SOLUSI AGAR TIDAK MENGHAMBAT:", 36f, yPos, paintBold)
        yPos += 20f

        hasilUrut.takeLast(2).forEach { lemah ->
            canvas.drawText(
                "Area Tantangan #${lemah.peringkat}: ${lemah.dimensi.nama} (${lemah.persen}%)",
                42f,
                yPos,
                paintBold
            )
            yPos += 15f
            canvas.drawText(
                "   • Risiko: ${lemah.dimensi.dampakJikaLemah.take(85)}",
                42f,
                yPos,
                paintReg
            )
            yPos += 14f
            canvas.drawText(
                "   • Solusi Mengatasi: ${lemah.dimensi.solusiAgarTidakMenghambat.take(85)}",
                42f,
                yPos,
                paintReg
            )
            yPos += 22f
        }

        canvas.drawText(
            "© Panda HibahIlmu.id — Dicetak resmi dari Aplikasi Android PandaMuslim",
            36f,
            810f,
            paintReg
        )

        pdf.finishPage(page)

        val namaFile = "Laporan_TalentMapping_Portrait_${namaPeserta.replace(" ", "_")}.pdf"
        val file = File(context.cacheDir, namaFile)
        FileOutputStream(file).use { pdf.writeTo(it) }
        pdf.close()

        bagikanAtauBukaFilePdf(context, file, "Cetak / Simpan Laporan Hasil Portrait (PDF)")
    }
}