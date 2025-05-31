package com.example.automationofinventory

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class GoodsAdapter(private val goodsList: List<GoodsItem>) :
    RecyclerView.Adapter<GoodsAdapter.GoodsViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoodsViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_goods, parent, false)
        return GoodsViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoodsViewHolder, position: Int) {
        val item = goodsList[position]
        holder.name.text = item.name
        holder.upcTw.text = "UPC: ${item.upc}"
        holder.unit.text = "Ед.изм: ${item.unit}"
    }

    override fun getItemCount(): Int = goodsList.size

    class GoodsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.nameTextView)
        val upcTw: TextView = view.findViewById(R.id.upcTextView)
        val unit: TextView = view.findViewById(R.id.unitTextView)
    }
}
