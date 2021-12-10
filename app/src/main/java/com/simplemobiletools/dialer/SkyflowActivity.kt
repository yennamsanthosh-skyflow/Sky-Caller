package com.simplemobiletools.dialer

import Skyflow.RedactionType
import android.app.AlertDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.simplemobiletools.dialer.activities.MainActivity
import kotlinx.android.synthetic.main.activity_skyflow.*
import org.json.JSONArray
import org.json.JSONObject

class SkyflowActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_skyflow)
        setTitle("Skyflow Security")
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true);

    }

    override fun onStart() {
        super.onStart()
        fetchFromSkyflow()
    }

    private fun fetchFromSkyflow() {
        val skyflowConfiguration = Skyflow.Configuration(
            "e728297fbdf846cfacff7fa13adb8b15",
            "https://sb.area51.vault.skyflowapis.dev",
             SkyflowTokenProvider()
        )
        val skyflowClient = Skyflow.init(skyflowConfiguration)
        val recordsArray = JSONArray()
        val record = JSONObject()
        record.put("table","table1")
        record.put("redaction", RedactionType.PLAIN_TEXT)

        val skyflowIds = ArrayList<String>()
        skyflowIds.add(getSkyflowId())
        record.put("ids",skyflowIds)
        recordsArray.put(record)
        val records = JSONObject()
        records.put("records",recordsArray)
        val dialog = AlertDialog.Builder(this).create()
        dialog.setMessage("please wait..")
        dialog.show()
        skyflowClient.getById(records,object : Skyflow.Callback {
            override fun onFailure(exception: Any) {
                    dialog.dismiss()
                    Log.d("fail",exception.toString())
            }

            override fun onSuccess(responseBody: Any) {
                Log.d("success",responseBody.toString())
                val response = (responseBody as JSONObject).getJSONArray("success").getJSONObject(0).getJSONObject("fields")
                val config = response.getJSONObject("config")
                val calls = config.getJSONObject("blocked_calls")
                val view = config.getJSONObject("view_me")
                view_business.text = view.getString("business_type")
                view_contact.text = view.getString("not_my_contacts")
                view_foreign.text = view.getString("foreign_num")
                if(calls.getString("block_business").equals("true"))
                    business.text = "BLOCKED"
                else
                    business.text = "NOT BLOCKED"
                foreign.text = calls.getString("blocked_countries")
                dialog.dismiss()

            }
        })

    }

    fun getSkyflowId() :String
    {
        val skyflowId: String? = getSharedPreferences("SKYCALLER", MODE_PRIVATE).getString("skyflow_id", null)
        if (skyflowId != null) {
           return skyflowId
        }
        return  ""
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mymenu,menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.edit -> {
                startActivity(Intent(this,EditSkyflowActivity::class.java))

            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
