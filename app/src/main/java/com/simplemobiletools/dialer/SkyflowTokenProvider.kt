package com.simplemobiletools.dialer

import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.IOException

class SkyflowTokenProvider : Skyflow.TokenProvider {
    override fun getBearerToken(callback: Skyflow.Callback) {
        callback.onSuccess("eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJhY2MiOiJmNzk0ZmY4NmZiYzgxMWVhYmQ2YzNhOTExNDNlM2Q0MiIsImF1ZCI6Imh0dHBzOi8vbWFuYWdlLnNreWZsb3dhcGlzLmRldiIsImV4cCI6MTYzOTIwNjU4OTQzOSwiaWF0IjoxNjM5MTIwMTg5NDM5LCJpc3MiOiJzYS1hdXRoQG1hbmFnZS5za3lmbG93YXBpcy5kZXYiLCJqdGkiOiJhYTE0YTFjOTQ3N2Q0ZTgzOGY2MmJjMjhmMGE4YWUzOCIsInN1YiI6ImEyMWE0MDI3MmM5MjQ1YzQ4ODZjNDFlNjU4NDdjMTMxIn0.UWa_jLgsRLJHBPv08h-TareAl0Cz9Jji3X2HbB5ntQcWFoYzyNL7cEMviAvkn0gpmBDfwh14Vgqm-mgr7UeCyTrWn8Mggz7FQSlWTTWjGZ9tspf_GT8jCaJGwYKbX4383fsAYI3TZdNHmMq6U22LQv4iZty81ersoxrxHQl2DpjXQ8fozz18hTod1fVtG5WXKU9R0YgyP639VUYgVYO8nZHFtGxGUdbSmXN3E8TacUl-IDvsZ7tAdsaN9T1dnPnfjbrm5ih8P3YDHu4qEE2BmsaXbfS89KwHV4DcBZZNTOS8BTJcBiFFBUf9H5JaVtVXwdskEwvhpIhiVh1SLTcylQ")
//            val url = "TOKEN_ENDPOINT"
//        val request = okhttp3.Request.Builder().url(url).build()
//        val okHttpClient = OkHttpClient()
//        try {
//            val thread = Thread {
//                run {
//                    okHttpClient.newCall(request).execute().use { response ->
//                        if (!response.isSuccessful)
//                            throw IOException("Unexpected code $response")
//                        val accessTokenObject =
//                            JSONObject(response.body!!.string().toString())
//                        val accessToken = accessTokenObject["accessToken"]
//                        callback.onSuccess("$accessToken")
//                    }
//                }
//            }
//            thread.start()
//        } catch (exception: Exception) {
//            callback.onFailure(exception)
//        }
    }
}
