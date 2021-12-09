package com.simplemobiletools.dialer.services

import android.R
import android.annotation.SuppressLint
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.InCallService
import android.widget.ArrayAdapter
import com.simplemobiletools.dialer.App
import com.simplemobiletools.dialer.Callback
import com.simplemobiletools.dialer.activities.CallActivity
import com.simplemobiletools.dialer.helpers.CallManager
import com.simplemobiletools.dialer.helpers.CallNotificationManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

class CallService : InCallService() {
    private val callNotificationManager by lazy { CallNotificationManager(this) }
    private val callDurationHelper by lazy { (application as App).callDurationHelper }

    private val callListener = object : Call.Callback() {
        override fun onStateChanged(call: Call, state: Int) {
            super.onStateChanged(call, state)

            if (state != Call.STATE_DISCONNECTED) {
                callNotificationManager.setupNotification()
            }
            if (state == Call.STATE_ACTIVE) {
                callDurationHelper.start()
            } else if (state == Call.STATE_DISCONNECTED || state == Call.STATE_DISCONNECTING) {
                callDurationHelper.cancel()
            }
        }

    }

    override fun onCallAdded(call: Call) {
        super.onCallAdded(call)
        startActivity(CallActivity.getStartIntent(this))
        CallManager.call = call
        val sender = ""
        var receiver = ""
        val handle = try {
            call.details?.handle?.toString()
        } catch (e: NullPointerException) {
            null
        }
        val uri = Uri.decode(handle)
        if (uri.startsWith("tel:")) {
            val number = uri.substringAfter("tel:")
            receiver = number
//            if(number.equals("+918608200493"))
//                CallManager.reject()
        }
        val isInContact=isPresentInContact(receiver)

        val senderSkyflowID = getDetails(sender)
        val receiverSkyflowID = getDetails(receiver)
        fetchDetails(senderSkyflowID,receiverSkyflowID,isInContact,object : Callback{
            override fun onSuccess(response: JSONObject) {
                val isAllowed = response.getBoolean("allow")
                val inComing_number = response.get("incoming_number")
                CallManager.setMaskedNumber(inComing_number.toString())
                if(!isAllowed)
                {
                    CallManager.reject()
                }
                else
                {
                    CallManager.inCallService = this@CallService
                    CallManager.registerCallback(callListener)
                    callNotificationManager.setupNotification()
                }

            }

            override fun onFailure(error: Exception) {
                        CallManager.inCallService = this@CallService
                        CallManager.registerCallback(callListener)
                        callNotificationManager.setupNotification()
            }
        })
//        CallManager.setMaskedNumber("+91xxxxxxxx93")
//        CallManager.inCallService = this
//        CallManager.registerCallback(callListener)
//        callNotificationManager.setupNotification()
    }

    private fun getDetails(user: String) :String {
        return ""
    }

    private fun fetchDetails(sender:String,receiver:String,isInContact:Boolean,callback: Callback) {


        val okHttpClient = OkHttpClient()
        val jsonBody = JSONObject()
        jsonBody.put("sender",sender)
        jsonBody.put("receiver",receiver)
        jsonBody.put("isInContact",isInContact)
        val body: RequestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request
            .Builder()
            .method("GET", body)
            .addHeader("Authorization", "responseBody")
            .url("url")
            .build()
        try {
            val thread = Thread {
                run {
                    okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {


                        override fun onFailure(call: okhttp3.Call, e: IOException) {

                        }

                        override fun onResponse(call: okhttp3.Call, response: Response) {
                            response.use {
                                if (!response.isSuccessful) {
                                        // send call
                                    callback.onFailure(Exception("server error"))
                                } else {
                                    val responsebody = JSONObject(response.body!!.string())
                                    callback.onSuccess(responsebody)
                                }
                            }
                        }

                    })
                }
            }
            thread.start()
        }
        catch (exception: Exception) {

        }
    }

    override fun onCallRemoved(call: Call) {
        super.onCallRemoved(call)
        CallManager.call = null
        CallManager.inCallService = null
        callNotificationManager.cancelNotification()
    }

    override fun onDestroy() {
        super.onDestroy()
        CallManager.unregisterCallback(callListener)
        callNotificationManager.cancelNotification()
        callDurationHelper.cancel()
    }

    @SuppressLint("Range")
    private fun isPresentInContact(userNumber:String) : Boolean {
       // val list = mutableListOf<String>()
        val phones = contentResolver.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            null,
            null,
            null,
            null)

        // Loop Through All The Numbers
        while (phones!!.moveToNext()) {
            val name =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME))
            var phoneNumber =
                phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))

            // Cleanup the phone number
            phoneNumber = phoneNumber.replace("[()\\s-]+".toRegex(), "")
            if(phoneNumber.equals(userNumber))
                return true
            //list.add(phoneNumber)
        }
        phones.close()
        return false
    }


}
