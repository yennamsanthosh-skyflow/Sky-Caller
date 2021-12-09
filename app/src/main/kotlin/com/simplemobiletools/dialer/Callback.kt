package com.simplemobiletools.dialer

import org.json.JSONObject

interface Callback {
    fun onSuccess(response:JSONObject)
    fun onFailure(error:Exception)
}
