package com.example.boogi_trainer.ui.exercise

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import androidx.recyclerview.widget.RecyclerView
import com.example.boogi_trainer.PoseActivity
import com.example.boogi_trainer.R


class itemAdapter(val context: Context, val items: ArrayList<exerciseData>) : RecyclerView.Adapter<itemAdapter.ViewHolder>() {


    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: itemAdapter.ViewHolder, position: Int) {

        val item = items[position]
        val listener = View.OnClickListener { it ->
            val intent = Intent(context, PoseActivity::class.java)
            intent.putExtra("exerciseKinds", position)
            context.startActivity(intent)

        }
        holder.apply {
            bind(listener, item)
            itemView.tag = item
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflatedView = LayoutInflater.from(parent.context).inflate(R.layout.recycleview_item, parent, false)
        return itemAdapter.ViewHolder(inflatedView)
    }

    // 각 항목에 필요한 기능을 구현
    class ViewHolder(v: View) : RecyclerView.ViewHolder(v) {
        //private val name : TextView = v.findViewById(R.id.pose_item)
        private val img : ImageButton = v.findViewById(R.id.pose_item)
        var view : View = v

        fun bind(listener: View.OnClickListener, item: exerciseData) {
            //name.text = item.name
            img.setImageResource(item.img)
            view.setOnClickListener(listener)

        }
    }
}

