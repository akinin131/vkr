package com.example.automationofinventory

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class InventoryAdapter(private val goodsList: MutableList<GoodsItem>) :
    RecyclerView.Adapter<InventoryAdapter.InventoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): InventoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_inventory, parent, false)
        return InventoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: InventoryViewHolder, position: Int) {
        val item = goodsList[position]
        holder.nameTextView.text = item.name
        holder.unitTextView.text = item.unit
        holder.quantityInput.setText(item.quantity.toString())

        holder.quantityInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                item.quantity = s.toString().toIntOrNull() ?: 0
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    override fun getItemCount(): Int = goodsList.size

    class InventoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.goodsNameTextView)
        val unitTextView: TextView = view.findViewById(R.id.goodsUnitTextView)
        val quantityInput: EditText = view.findViewById(R.id.goodsQuantityInput)
    }
}
