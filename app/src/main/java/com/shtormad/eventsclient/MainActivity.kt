package com.shtormad.eventsclient

import android.app.Dialog
import android.os.Bundle
import android.view.Window
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.shtormad.eventsclient.models.Event
import kotlinx.coroutines.*
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.json.Json
import kotlinx.serialization.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import java.io.IOException

/**
 * Базовая активность для отображения и создания мероприятий
 */

class MainActivity : AppCompatActivity() {

    private val client = OkHttpClient()
    private var items: ArrayList<Event> = arrayListOf()
    private lateinit var rv: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)

        rv = findViewById(R.id.mainRV)
        rv.layoutManager = LinearLayoutManager(applicationContext)

        fab.setOnClickListener{
            showDialog()
        }

        GlobalScope.launch (Dispatchers.Default) {
            items = getEvents()
            withContext(Dispatchers.Main){rv.adapter = MyRvAdapter(items)}
        }
    }

    /**
     * Функция для отправки GET на сервер
     * Возвращаем список мероприятий
     */
    private fun getEvents() : ArrayList<Event>{

        val items: ArrayList<Event> = arrayListOf()
        val request = Request.Builder().url("http://158.46.31.49:5003/api/events").build()
        println(request.toString())
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected code $response")
            val resStr = response.body!!.string()
            val json = JSONArray(resStr)
            for(i in 0 until json.length()){
                items.add(Json.decodeFromString(json.get(i).toString()))
            }
        }
        return items
    }

    /**
     * Метод отображения диалога, для создания мероприятий
     */
    private fun showDialog() {
        val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog_event)
        val etName = dialog.findViewById(R.id.dialogEtName) as EditText
        val etDesc = dialog.findViewById(R.id.dialogEtDesc) as EditText
        val btnAccept = dialog.findViewById(R.id.dialogBtnAccept) as Button
        val btnCancel = dialog.findViewById(R.id.dialogBtnCancel) as Button

        btnAccept.setOnClickListener {

            GlobalScope.launch(Dispatchers.Default) {
                items.add(postEvent(
                    Json.encodeToString(
                        Event(
                            0,
                            etName.text.toString(),
                            etDesc.text.toString(),
                            LocalDateTime.parse("0001-01-01T00:00:00"),
                            LocalDateTime.parse("0001-01-01T00:00:00"),
                            LocalDateTime.parse("0001-01-01T00:00:00"),
                            LocalDateTime.parse("0001-01-01T00:00:00"),
                            "default"
                        )
                    )
                ))
                withContext(Dispatchers.Main){rv.adapter?.notifyItemInserted(items.size-1)}
            }
            dialog.dismiss()
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    /**
     * Функция для отправки POST на сервер
     * Аргумент на вход - JSON-строка
     * Возвращаем - добавленное событие
     */
    private fun postEvent(formBody: String) : Event{

        val body = formBody.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder().url("http://158.46.31.49:5003/api/events").post(body).build()
        println(request)

        val response: Response = client.newCall(request).execute()

        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        return Json.decodeFromString(response.body!!.string())
    }

}