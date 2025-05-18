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
        holder.nameTextView.text = item.name
        holder.upcTextView.text = "UPC: ${item.upc}"
        holder.unitTextView.text = "Ед. изм.: ${item.unit}"
    }

    override fun getItemCount(): Int = goodsList.size

    class GoodsViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.nameTextView)
        val upcTextView: TextView = view.findViewById(R.id.upcTextView)
        val unitTextView: TextView = view.findViewById(R.id.unitTextView)
    }
}
