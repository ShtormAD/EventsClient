package com.shtormad.eventsclient
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter


class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val client = OkHttpClient()
        val items: ArrayList<Event> = arrayListOf()

        val job: Job = GlobalScope.launch(Dispatchers.IO) {

            val request = Request.Builder().url("http://158.46.31.49:5003/api/events").build()

            println(request.toString())

            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")
                val resStr = response.body!!.string()
                val json = JSONArray(resStr)
                for(i in 0 until json.length()){
                    items.add(Event(json.getJSONObject(i).getInt("id"), json.getJSONObject(i).getString("name"), json.getJSONObject(i).getString("description"),
                                    LocalDate.parse(json.getJSONObject(i).getString("dateStart").substringBefore("T")),
                                    LocalDate.parse(json.getJSONObject(i).getString("dateEnd").substringBefore("T")),
                                    LocalTime.parse(json.getJSONObject(i).getString("timeStart").substringAfter("T")),
                                    LocalTime.parse(json.getJSONObject(i).getString("timeEnd").substringAfter("T")),
                                    json.getJSONObject(i).getString("author")))
                }
            }

            val rv: RecyclerView = findViewById(R.id.mainRV)
            rv.layoutManager = LinearLayoutManager(applicationContext)
            rv.adapter = MyRvAdapter(items)
        }
    }

}