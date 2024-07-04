package com.tencent.virtualman_demo_app.ui.adapter

import androidx.recyclerview.widget.RecyclerView
import android.view.ViewGroup
import android.view.LayoutInflater
import com.tencent.virtualman_demo_app.R
import android.annotation.SuppressLint
import android.view.View
import android.widget.Button

class SingleChoiceAdapter(
    private val list: List<String?>,
    private val onClick: OnItemClickListener
) : RecyclerView.Adapter<SingleChoiceAdapter.MyHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_single_choice, parent, false)
        return MyHolder(view)
    }

    override fun onBindViewHolder(holder: MyHolder, @SuppressLint("RecyclerView") position: Int) {
        holder.btnSingleChoice.text = list[position]

        //设置点击事件
        holder.itemView.setOnClickListener { view: View? ->
            onClick.onClick(
                position,
                list[position]
            )
        }
    }

    inner class MyHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val btnSingleChoice: Button

        init {
            btnSingleChoice = itemView.findViewById(R.id.btnSingleChoice)
        }
    }

    interface OnItemClickListener {
        //在初始化时获得事件
        fun onClick(position: Int, url: String?)
    }

    override fun getItemCount(): Int {
        return list.size
    }
}