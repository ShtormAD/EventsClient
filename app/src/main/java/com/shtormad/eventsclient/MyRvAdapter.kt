package com.shtormad.eventsclient

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyRvAdapter(private val events: List<Event>) : RecyclerView.Adapter<MyRvAdapter.MyViewHolder>()
{

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){

        var TvName: TextView? = null
        var TvDescription: TextView? = null
        var TvAuthor: TextView? = null

        init{
            TvName = itemView.findViewById(R.id.RvItemName)
            TvDescription = itemView.findViewById(R.id.RvItemDescription)
            TvAuthor = itemView.findViewById(R.id.RvItemAuthor)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context)
                .inflate(R.layout.rv_event_item, parent, false)
        return MyViewHolder(itemView)
    }


    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.TvName?.text = events.get(position).Name
        holder.TvDescription?.text = events.get(position).Description
        holder.TvAuthor?.text = "Автор: " + events.get(position).Author//TODO перенести бы в ресурсы
    }

    override fun getItemCount(): Int {
        return events.size
    }
}