package com.example.automationofinventory

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.firebase.firestore.FirebaseFirestore

class InventoryFragment : Fragment() {

    private lateinit var inventoryRecyclerView: RecyclerView
    private lateinit var selectedWarehouseTextView: TextView
    private val goodsList = mutableListOf<GoodsItem>()
    private lateinit var inventoryAdapter: InventoryAdapter
    private val firestore = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_inventory, container, false)

        selectedWarehouseTextView = view.findViewById(R.id.selectedWarehouseTextView)
        inventoryRecyclerView = view.findViewById(R.id.inventoryRecyclerView)
        inventoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        inventoryAdapter = InventoryAdapter(goodsList)
        inventoryRecyclerView.adapter = inventoryAdapter

        view.findViewById<View>(R.id.selectWarehouseButton).setOnClickListener {
            showWarehouseSelectionDialog()
        }

        view.findViewById<View>(R.id.saveInventoryButton).setOnClickListener {
            saveInventory()
        }

        return view
    }

    private fun showWarehouseSelectionDialog() {
        firestore.collection("warehouses")
            .get()
            .addOnSuccessListener { documents ->
                val warehouseNames = documents.map { it.getString("name") ?: "Безымянный склад" }
                AlertDialog.Builder(requireContext())
                    .setTitle("Выберите склад")
                    .setItems(warehouseNames.toTypedArray()) { _, which ->
                        selectedWarehouseTextView.text = warehouseNames[which]
                        loadGoodsForInventory(warehouseNames[which])
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Ошибка загрузки складов", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadGoodsForInventory(warehouseName: String) {
        firestore.collection("warehouses")
            .whereEqualTo("name", warehouseName)
            .get()
            .addOnSuccessListener { documents ->
                val warehouseId = documents.firstOrNull()?.id ?: return@addOnSuccessListener
                firestore.collection("warehouses/$warehouseId/goods")
                    .get()
                    .addOnSuccessListener { goodsDocs ->
                        goodsList.clear()
                        goodsDocs.forEach { doc ->
                            val name = doc.getString("name") ?: ""
                            val upc = doc.getString("upc") ?: ""
                            val unit = doc.getString("unit") ?: ""
                            val quantity = doc.getLong("quantity")?.toInt() ?: 0
                            goodsList.add(GoodsItem(name, upc, unit, quantity))
                        }
                        inventoryAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Ошибка загрузки товаров", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Ошибка загрузки склада", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveInventory() {
        val warehouseName = selectedWarehouseTextView.text.toString()
        if (warehouseName == "Склад не выбран") {
            Toast.makeText(requireContext(), "Выберите склад перед сохранением", Toast.LENGTH_SHORT).show()
            return
        }

        // Проверка на отрицательные значения
        goodsList.find { it.quantity < 0 }?.let { invalidItem ->
            Toast.makeText(
                requireContext(),
                "Количество товара '${invalidItem.name}' не может быть отрицательным",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Показываем прогресс
        val progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Сохранение инвентаризации...")
            setCancelable(false)
            show()
        }

        firestore.collection("warehouses")
            .whereEqualTo("name", warehouseName)
            .get()
            .addOnSuccessListener { documents ->
                val warehouseId = documents.firstOrNull()?.id ?: run {
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), "Склад не найден", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                // Создаем batch операцию
                val batch = firestore.batch()
                val goodsCollection = firestore.collection("warehouses/$warehouseId/goods")

                // Добавляем все обновления в batch
                goodsList.forEach { item ->
                    val docRef = goodsCollection.document(item.upc)
                    batch.update(docRef, mapOf("quantity" to item.quantity))
                }

                // Выполняем batch операцию
                batch.commit()
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(requireContext(), "Инвентаризация сохранена", Toast.LENGTH_SHORT).show()
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(
                            requireContext(),
                            "Ошибка сохранения: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    "Ошибка доступа к складу: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }
}