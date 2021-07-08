package com.shtormad.eventsclient

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.shtormad.eventsclient.models.Event

/**
 * Адаптер данных для RecyclerView на главной активности
 */

class MyRvAdapter(private val events: List<Event>) : RecyclerView.Adapter<MyRvAdapter.MyViewHolder>()
{

    /**
     * ViewHolder - храним элементы интерфейса
     */
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var tvName: TextView? = null
        var tvDescription: TextView? = null
        var tvAuthor: TextView? = null
        var tvDate: TextView? = null
        var tvTime: TextView? = null

        init{
            tvName = itemView.findViewById(R.id.RvItemName)
            tvDescription = itemView.findViewById(R.id.RvItemDescription)
            tvAuthor = itemView.findViewById(R.id.RvItemAuthor)
            tvDate = itemView.findViewById(R.id.RvItemDate)
            tvTime = itemView.findViewById(R.id.RvItemTime)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.rv_event_item, parent, false)
        return MyViewHolder(itemView)
    }

    /**
     * Заполняем элементы интерфейса данными
     */
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.tvName?.text = events[position].name
        holder.tvDescription?.text = events[position].description
        holder.tvAuthor?.text = "Автор: " + events[position].author//TODO перенести бы в ресурсы

        if(events[position].dateStart == events[position].dateEnd)
            holder.tvDate?.text = events[position].dateStart.toString().substringBeforeLast("T").replace("-",".", false)
        else
            holder.tvDate?.text = events[position].dateStart.toString().substringBeforeLast("T").replace("-",".", false) +" - "+ events[position].dateEnd.toString().substringBeforeLast("T").replace("-",".", false)
        if(events[position].timeStart == events[position].timeEnd)
            holder.tvTime?.text = events[position].timeStart.toString().substringAfterLast("T")
        else
            holder.tvTime?.text = events[position].timeStart.toString().substringAfterLast("T") +"-"+ events[position].timeEnd.toString().substringAfterLast("T")

    }

    /**
     * Считаем количество элементов в коллекции
     */
    override fun getItemCount(): Int {
        return events.size
    }
}