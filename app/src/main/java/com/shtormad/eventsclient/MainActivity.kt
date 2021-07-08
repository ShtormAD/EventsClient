package com.shtormad.eventsclient

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.SharedPreferences
import android.icu.util.Calendar
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.shtormad.eventsclient.models.Event
import kotlinx.coroutines.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.Clock
import kotlinx.serialization.*
import kotlinx.serialization.json.Json
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
    private var username : String = ""
    private var items: ArrayList<Event> = arrayListOf()
    private lateinit var rv: RecyclerView
    private lateinit var sp : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sp = getSharedPreferences("CONF", Context.MODE_PRIVATE)
        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton)

        if(sp.getString("username", "") == ""){
            showDialogUsername(0)
        }
        username = sp.getString("username", "default").toString()

        rv = findViewById(R.id.mainRV)
        rv.layoutManager = LinearLayoutManager(applicationContext)

        fab.setOnClickListener{
            showDialogAddEvent()
        }

        GlobalScope.launch (Dispatchers.Default) {
            items = getEvents()
            withContext(Dispatchers.Main){rv.adapter = MyRvAdapter(items)}
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater : MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId){
            R.id.menuChangeUsername -> {
                showDialogUsername(1)
            }
        }
        return super.onOptionsItemSelected(item)
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
     * Метод отображения диалога для создания мероприятий
     */
    private fun showDialogAddEvent() {
        val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setCancelable(true)
            dialog.setContentView(R.layout.dialog_event)
        val etName = dialog.findViewById(R.id.dialogEtName) as EditText
        val etDesc = dialog.findViewById(R.id.dialogEtDesc) as EditText
        val etDateStart = dialog.findViewById(R.id.dialogEtDateStart) as EditText
        val etDateEnd = dialog.findViewById(R.id.dialogEtDateEnd) as EditText
        val etTimeStart = dialog.findViewById(R.id.dialogEtTimeStart) as EditText
        val etTimeEnd = dialog.findViewById(R.id.dialogEtTimeEnd) as EditText
        val btnDateStart = dialog.findViewById(R.id.dialogBtnDateStart) as ImageButton
        val btnDateEnd = dialog.findViewById(R.id.dialogBtnDateEnd) as ImageButton
        val btnTimeStart = dialog.findViewById(R.id.dialogBtnTimeStart) as ImageButton
        val btnTimeEnd = dialog.findViewById(R.id.dialogBtnTimeEnd) as ImageButton
        val btnAccept = dialog.findViewById(R.id.dialogBtnAccept) as Button
        val btnCancel = dialog.findViewById(R.id.dialogBtnCancel) as Button
        var messageString :String = ""

        btnDateStart.setOnClickListener {
            datePick(etDateStart)
        }
        btnDateEnd.setOnClickListener{
            datePick(etDateEnd)
        }
        btnTimeStart.setOnClickListener{timePick(etTimeStart)}
        btnTimeEnd.setOnClickListener {timePick(etTimeEnd)}
        btnAccept.setOnClickListener {

            GlobalScope.launch(Dispatchers.Default) {
                //Если поля не пустые
                if( etName.text.toString().isNotBlank() && etDesc.text.toString().isNotBlank() && etDateStart.text.toString().isNotBlank()
                    && etDateEnd.text.toString().isNotBlank() && etTimeStart.text.toString().isNotBlank() && etTimeEnd.text.toString().isNotBlank()){
                        //Если даты начала раньше или равны датам конца
                    if(LocalDate.parse(etDateStart.text.toString())<=LocalDate.parse(etDateEnd.text.toString()) &&
                        LocalDateTime.parse("0001-01-01T"+etTimeStart.text.toString()+":00")<=LocalDateTime.parse("0001-01-01T"+etTimeEnd.text.toString()+":00")){
                        items.add(postEvent(
                            Json.encodeToString(
                                Event(
                                    0,
                                    etName.text.toString(),
                                    etDesc.text.toString(),
                                    LocalDateTime.parse(etDateStart.text.toString()+"T00:00:00"),
                                    LocalDateTime.parse(etDateEnd.text.toString()+"T00:00:00"),
                                    LocalDateTime.parse("0001-01-01T"+etTimeStart.text.toString()+":00"),
                                    LocalDateTime.parse("0001-01-01T"+etTimeEnd.text.toString()+":00"),
                                    username
                                ))))
                        withContext(Dispatchers.Main){rv.adapter?.notifyItemInserted(items.size-1)}
                        dialog.dismiss()

                    } else if (LocalDate.parse(etDateStart.text.toString())>LocalDate.parse(etDateEnd.text.toString()))
                        messageString = "Дата конца раньше даты начала!"
                    else if (LocalDateTime.parse("0001-01-01T"+etTimeStart.text.toString()+":00")>LocalDateTime.parse("0001-01-01T"+etTimeEnd.text.toString()+":00"))
                        messageString = "Время конца раньше времени начала!"
                } else
                    messageString = "Заполнены не все поля!"

                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        messageString,
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
        btnCancel.setOnClickListener { dialog.dismiss() }
        dialog.show()
    }

    /**
     * Метод отображения диалога для изменения имени пользователя
     * Mode 0: задать имя пользователя, если его нет
     * Mode 1: изменить, если есть
     */
    private fun showDialogUsername(Mode: Int){
        val dialog = Dialog(this)
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.setContentView(R.layout.dialog_username)
        val etUsername = dialog.findViewById(R.id.dialogEtUsername) as EditText
        val btnAccept = dialog.findViewById(R.id.dialogBtnUsername) as Button

        if(Mode == 0){
            dialog.setCancelable(false)
            btnAccept.setOnClickListener {
                if(etUsername.text.toString().trim() != ""){
                    val editor = sp.edit()
                    editor.putString("username", etUsername.text.toString().trim())
                    editor.apply()
                    username = etUsername.text.toString().trim()
                    dialog.dismiss()
                } else Toast.makeText(applicationContext, "Пустое поле!", Toast.LENGTH_LONG).show()
            }
        }
        else{
            dialog.setCancelable(true)
            etUsername.setText(username)
            btnAccept.setOnClickListener {
                if(etUsername.text.toString().trim() != ""){
                    val editor = sp.edit()
                    editor.putString("username", etUsername.text.toString().trim())
                    editor.apply()
                    username = etUsername.text.toString().trim()
                    dialog.dismiss()
                } else Toast.makeText(applicationContext, "Пустое поле!", Toast.LENGTH_LONG).show()
            }
        }
        dialog.show()
    }

    /**
     * Функция для отправки POST на сервер
     * Аргумент на вход - JSON-строка
     * Возвращаем - добавленное мероприятие
     */
    private fun postEvent(formBody: String) : Event{

        val body = formBody.toRequestBody("application/json".toMediaTypeOrNull())

        val request = Request.Builder().url("http://158.46.31.49:5003/api/events").post(body).build()
        println(request)

        val response: Response = client.newCall(request).execute()

        if (!response.isSuccessful) throw IOException("Unexpected code $response")

        return Json.decodeFromString(response.body!!.string())
    }

    /**
     * Диалог выбора даты
     * Аргумент на вход - EditText для вывода выбранной даты
     */
    private fun datePick(et: EditText){
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)

        val dpd = DatePickerDialog(this, { _, yearY, monthOfYear, dayOfMonth ->
            et.setText(LocalDate(yearY,monthOfYear+1,dayOfMonth).toString())
        }, year, month, day)

        dpd.show()
    }

    /**
     * Диалог выбора времени
     * Аргумент на вход - EditText для вывода выбранного времени
     */
    private fun timePick(et: EditText){
        val c = Calendar.getInstance()
        val hour = c.get(Calendar.HOUR_OF_DAY)
        val minute = c.get(Calendar.MINUTE)

        val tpd = TimePickerDialog(this, { _, Hour, Minute ->
            et.setText(LocalDateTime(1,1,1,Hour,Minute,0).toString().substringAfterLast("T"))
        }, hour, minute, true)

        tpd.show()
    }
}