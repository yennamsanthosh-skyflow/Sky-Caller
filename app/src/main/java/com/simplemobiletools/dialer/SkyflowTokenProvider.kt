package com.simplemobiletools.dialer

import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.IOException

class SkyflowTokenProvider : Skyflow.TokenProvider {
    override fun getBearerToken(callback: Skyflow.Callback) {
            val url = "TOKEN_ENDPOINT"
        val request = okhttp3.Request.Builder().url(url).build()
        val okHttpClient = OkHttpClient()
        try {
            val thread = Thread {
                run {
                    okHttpClient.newCall(request).execute().use { response ->
                        if (!response.isSuccessful)
                            throw IOException("Unexpected code $response")
                        val accessTokenObject =
                            JSONObject(response.body!!.string().toString())
                        val accessToken = accessTokenObject["accessToken"]
                        callback.onSuccess("$accessToken")
                    }
                }
            }
            thread.start()
        } catch (exception: Exception) {
            callback.onFailure(exception)
        }
    }
}
