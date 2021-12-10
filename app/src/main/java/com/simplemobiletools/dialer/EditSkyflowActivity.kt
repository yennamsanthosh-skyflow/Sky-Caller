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
    val blockListOfCountry = mutableListOf<String>()
    var spamCalls = false
    val listOfRedaction= mutableListOf<String>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_skyflow)
        setTitle("Edit Skyflow Security")
        getSupportActionBar()!!.setDisplayHomeAsUpEnabled(true);
        getSupportActionBar()!!.setDisplayShowHomeEnabled(true);
        listOfRedaction.add("PLAIN_TEXT")
        listOfRedaction.add("REDACTED")
        listOfRedaction.add("MASKED")
        val spam_calls = intent.getStringExtra("spam_calls")
        val foreign_calls = intent.getStringExtra("countries")
        val business_calls = intent.getStringExtra("business_calls")

        if(spam_calls.equals("BLOCKED")) {
            spamSwitch.isChecked = true
            spamCalls = true
        } else {
            spamSwitch.isChecked = false
            spamCalls = false
        }

        if(business_calls.equals("BLOCKED")) {
            bswitch.isChecked = true
            businessCalls = true

        } else {
            bswitch.isChecked = false
            businessCalls = false
        }
        val list = foreign_calls!!.split(",")
        Log.d("list",list.toString())
        if(list.isEmpty())
        {
            fswitch.isChecked = false
        }
        else
        {
            fswitch.isChecked =true
            country_layout.visibility = View.VISIBLE
            blockListOfCountry.addAll(list)
        }
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
        val url = "https://sb.area51.vault.skyflowapis.dev/v1/vaults/e728297fbdf846cfacff7fa13adb8b15/table1/"+getSkyflowId()
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
        call.put("blocked_countries",blockListOfCountry.toString().replace("[","").replace("]",""))
        call.put("block_spam",spamCalls)
        config.put("view_me",view)
        config.put("blocked_calls",call)
        fields.put("config",config)
        record.put("fields",fields)
        jsonBody.put("record",record)
        val body: RequestBody = jsonBody.toString().toRequestBody("application/json".toMediaTypeOrNull())
        SkyflowTokenProvider().getBearerToken( object : Skyflow.Callback{
            override fun onFailure(exception: Any) {
                Log.d("error",exception.toString())
                callback.onFailure(Exception("error occured"))
            }

            override fun onSuccess(responseBody: Any) {

                val request = Request
                    .Builder()
                    .method("PUT", body)
                    .addHeader("Authorization", "Bearer $responseBody")
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
                                    //callback.onSuccess(JSONObject())
                                }

                            })
                        }
                    }
                    thread.start()
                }
                catch (exception: Exception) {

                }



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

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun getDetails(user: String) :String {
        return ""
    }

    private fun editView() {

        val contactID = listOfRedaction.indexOf(intent.getStringExtra("view_contact"))
        val businessID = listOfRedaction.indexOf(intent.getStringExtra("view_business"))
        val foreignID = listOfRedaction.indexOf(intent.getStringExtra("view_foreign"))
        val adapter: ArrayAdapter<String> = ArrayAdapter<String>(this,
            android.R.layout.simple_spinner_item, listOfRedaction)
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
                contactRedaction = listOfRedaction.get(position)
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
                businessRedaction = listOfRedaction.get(position)
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
                foreignRedaction = listOfRedaction.get(position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        })



    }

    private fun editCalls() {
        val listOfCountries = mutableListOf<String>()
        listOfCountries.add("Select Country")
        listOfCountries.add("India")
        listOfCountries.add("USA")
        listOfCountries.add("Australia")
        listOfCountries.add("Pakistan")
        listOfCountries.add("Russia")
        listOfCountries.add("England")
        listOfCountries.add("South Africa")

        val arr = ArrayAdapter<String>(this@EditSkyflowActivity,R.layout.support_simple_spinner_dropdown_item,
            blockListOfCountry)
        countryList.setAdapter(arr)
        bswitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            businessCalls = isChecked
        })
        spamSwitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            spamCalls = isChecked
        })
        fswitch.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if(isChecked){
                country_layout.visibility = View.VISIBLE
            }
            else
            {
                country_layout.visibility = View.INVISIBLE
                blockListOfCountry.clear()
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
                if(position !=0) {
                    blockListOfCountry.add(listOfCountries.get(position))
                    arr.notifyDataSetChanged()
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        })

        countryList.setOnItemClickListener(object : AdapterView.OnItemClickListener
        {
            override fun onItemClick(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                blockListOfCountry.removeAt(p2)
                arr.notifyDataSetChanged()
            }
        })

        country.setPrompt("select country")

    }

}
