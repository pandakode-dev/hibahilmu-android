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

object IotCloudApiService {
    const val BASE_WEB_URL = "https://www.hibahilmu.my.id"
    const val IOT_CLOUD_ENDPOINT = "$BASE_WEB_URL/api/iot/cloud"

    suspend fun ambilStatePinDariServer(token: String): Result<CloudIotState> = withContext(Dispatchers.IO) {
        try {
            val encodedToken = URLEncoder.encode(token.trim(), "UTF-8")
            val url = URL("$IOT_CLOUD_ENDPOINT?token=$encodedToken")
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("Accept", "application/json")
            }

            val code = conn.responseCode
            if (code !in 200..299) {
                return@withContext Result.failure(Exception("HTTP Error $code dari hibahilmu.my.id"))
            }

            val reader = BufferedReader(InputStreamReader(conn.inputStream))
            val responseText = reader.readText()
            reader.close()

            val root = JSONObject(responseText)
            val dataObj = root.optJSONObject("data") ?: JSONObject()

            val vPinsMap = mutableMapOf<String, Double>()
            val vPinsJson = dataObj.optJSONObject("virtualPins")
            if (vPinsJson != null) {
                val keys = vPinsJson.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
                    vPinsMap[k] = vPinsJson.optDouble(k, 0.0)
                }
            }

            val aktuatorMap = mutableMapOf<String, Boolean>()
            val aktJson = dataObj.optJSONObject("statusAktuator")
            if (aktJson != null) {
                val keys = aktJson.keys()
                while (keys.hasNext()) {
                    val k = keys.next()
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
                            sumber = item.optString("sumber", "ANDROID_APP"),
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
            val url = URL(IOT_CLOUD_ENDPOINT)
            val conn = (url.openConnection() as HttpURLConnection).apply {
                requestMethod = "POST"
                doOutput = true
                connectTimeout = 7000
                readTimeout = 7000
                setRequestProperty("Content-Type", "application/json")
            }

            val payloadJson = JSONObject().apply {
                put("token", token.trim())
                put("pemilikNama", pemilikNama)
                put("namaProject", namaProject)
                put("sumber", "ANDROID_APP")
                put("pin", pin)
                put("value", nilai)
            }

            val writer = OutputStreamWriter(conn.outputStream)
            writer.write(payloadJson.toString())
            writer.flush()
            writer.close()

            val code = conn.responseCode
            if (code !in 200..299) {
                return@withContext Result.failure(Exception("Gagal kirim ke server (HTTP $code)"))
            }

            ambilStatePinDariServer(token)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}