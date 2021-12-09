package com.simplemobiletools.dialer

import android.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CompoundButton
import android.widget.Toast
import kotlinx.android.synthetic.main.activity_edit_skyflow.*
import kotlinx.android.synthetic.main.activity_edit_skyflow.business
import kotlinx.android.synthetic.main.activity_edit_skyflow.foreign
import kotlinx.android.synthetic.main.activity_skyflow.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class EditSkyflowActivity : AppCompatActivity() {

    var contactRedaction = ""
    var businessRedaction = ""
    var foreignRedaction = ""
    var businessCalls = false
    val selectedList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_skyflow)
        setTitle("Edit Skyflow Security")
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true);
        editCalls()
        editView()

    }
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.mymenu1,menu)
        return super.onCreateOptionsMenu(menu)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId) {
            R.id.done -> {
                val dialog = AlertDialog.Builder(this).create()
                dialog.setMessage("please wait..")
                dialog.show()
                doRequest(object : Callback {
                    override fun onSuccess(response: JSONObject) {
                        dialog.dismiss()
                        finish()
                    }

                    override fun onFailure(error: Exception) {
                        dialog.dismiss()
                    }

                })
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun doRequest(callback:Callback) {

//        val url = "https://sb.area51.vault.skyflowapis.dev/v1/vaults/e728297fbdf846cfacff7fa13adb8b15/table1/"+getDetails("mynumber")
        val url = "https://sb.area51.vault.skyflowapis.dev/v1/vaults/e728297fbdf846cfacff7fa13adb8b15/table1/"+"5a2e2d3d-436a-4d98-a64b-4e846adc180d"
        val okHttpClient = OkHttpClient()
        val jsonBody = JSONObject()
        val record = JSONObject()
        val fields = JSONObject()
        val config = JSONObject()
        val view = JSONObject()
        view.put("not_my_contacts",contactRedaction)
        view.put("business_type",businessRedaction)
        view.put("foreign_num",foreignRedaction)
        val call = JSONObject()
        call.put("block_business",businessCalls)
        call.put("blocked_countries",selectedList.toString())
        config.put("view_me",view)
        config.put("blocked_calls",call)
        fields.put("config",config)
        record.put("fields",fields)
        jsonBody.put("record",record)
        val body: RequestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
        val request = Request
            .Builder()
            .method("PUT", body)
            .addHeader("Authorization", "Bearer token")
            .url(url)
            .build()
        try {
            val thread = Thread {
                run {
                    okHttpClient.newCall(request).enqueue(object : okhttp3.Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            callback.onFailure(Exception("Server error"))
                        }

                        override fun onResponse(call: Call, response: Response) {
                            response.use {
                                val responsebody = JSONObject(response.body!!.string())
                                if (!response.isSuccessful) {
                                    // send call
                                    Log.d("failure",responsebody.toString())
                                    callback.onFailure(Exception("server error"))
                                } else {
                                    Log.d("success",responsebody.toString())
                                    callback.onSuccess(responsebody)
                                }
                            }
                            callback.onSuccess(JSONObject())
                        }

                    })
                }
            }
            thread.start()
        }
        catch (exception: Exception) {

        }

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun getDetails(user: String) :String {
        return ""
    }

    private fun editView() {
        val list= mutableListOf<String>()
        list.add("PLAIN_TEXT")
        list.add("REDACTED")
        list.add("MASKED")
        val contactID = list.indexOf(intent.getStringExtra("contacts"))
        val businessID = list.indexOf(intent.getStringExtra("business"))
        val foreignID = list.indexOf(intent.getStringExtra("foreign"))



        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_item, list)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        contacts.setAdapter(adapter)
        business.setAdapter(adapter)
        foreign.setAdapter(adapter)

        if(contactID !=-1){
            contacts.setSelection(contactID)
        }
        if(businessID != -1){
            business.setSelection(businessID)
        }
        if(foreignID != -1){
            foreign.setSelection(foreignID)
        }
        contacts.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long,
            ) {
                contactRedaction = list.get(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        })

        business.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long,
            ) {
                businessRedaction = list.get(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        })

        foreign.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long,
            ) {
                foreignRedaction = list.get(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        })



    }

    private fun editCalls() {
        val listOfCountries = mutableListOf<String>()
        listOfCountries.add("India")
        listOfCountries.add("USA")
        listOfCountries.add("Australia")
        listOfCountries.add("Pakistan")
        listOfCountries.add("Russia")
        listOfCountries.add("England")
        listOfCountries.add("South Africa")

        val arr = ArrayAdapter<String>(this@EditSkyflowActivity,R.layout.support_simple_spinner_dropdown_item,
            selectedList)
        countryList.setAdapter(arr)
        bswitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            businessCalls = isChecked
        })
        fswitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                country_layout.visibility = View.VISIBLE
            }
            else
            {
                country_layout.visibility = View.INVISIBLE
                selectedList.clear()
                arr.notifyDataSetChanged()
            }
        })

        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_item, listOfCountries)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        country.setAdapter(adapter)



        country.setOnItemSelectedListener(object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long,
            ) {
                selectedList.add(listOfCountries.get(position))
                arr.notifyDataSetChanged()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        })

        countryList.setOnItemClickListener(object : AdapterView.OnItemClickListener
        {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                selectedList.removeAt(p2)
                arr.notifyDataSetChanged()
            }
        })

        country.setPrompt("select country")

    }

}