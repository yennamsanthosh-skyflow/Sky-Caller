package com.simplemobiletools.dialer

import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.simplemobiletools.dialer.activities.MainActivity
import kotlinx.android.synthetic.main.activity_signup.*
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.io.IOException

class SignupActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)
        val skyflowId: String? = getSharedPreferences("SKYCALLER", MODE_PRIVATE).getString("skyflow_id",null)
        if (skyflowId != null) {
            startActivity(Intent(this@SignupActivity,MainActivity::class.java))
            finish()
        }
        signup_submit.setOnClickListener {
            val dialog = AlertDialog.Builder(this).create()
            dialog.setMessage("please wait..")
            dialog.show()
            getData(object : Callback
            {
                override fun onSuccess(response: JSONObject) {
                        dialog.dismiss()
                        startActivity(Intent(this@SignupActivity,MainActivity::class.java))
                        finish()
                }

                override fun onFailure(error: Exception) {
                        dialog.dismiss()
                        Log.d("error",error.toString())
                }

            })
        }
    }

    private fun getData(callback: Callback) {
        val ph = phone_number.text.toString()
        if(ph.length<10)
            callback.onFailure(Exception("invalid mobile number"))
        else {
            val url = "url/"+ph
            val request = okhttp3.Request.Builder().url(url).build()
            val okHttpClient = OkHttpClient()
            try {
                val thread = Thread {
                    run {
                        okHttpClient.newCall(request).execute().use { response ->
                            if (!response.isSuccessful)
                                throw IOException("Unexpected code $response")
                            val shared = getSharedPreferences("SKYCALLER", MODE_PRIVATE)
                            val editor = shared.edit()
                            editor.putString("skyflow_id","")
                            editor.putString("phone_number",ph)
                            editor.apply()
                        }
                    }
                }
                thread.start()
            } catch (exception: Exception) {
                callback.onFailure(exception)
            }
        }
    }
}
