package com.example.automationofinventory

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InventoryAdapter(private val goodsList: List<GoodsItem>?) :
    RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inventory, parent, false)
        return InventoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val item = goodsList!![position]
        holder.name.text = item.name
        holder.unit.text = item.unit
        holder.goods_quantity.setText(item.quantity.toString())

        holder.goods_quantity.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                item.quantity = s.toString().toInt()
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun getItemCount(): Int = goodsList?.size!!

    class InventoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.goods_name)
        val unit: TextView = view.findViewById(R.id.goods_unit)
        val goods_quantity: EditText = view.findViewById(R.id.goods_quantity)
    }
}
