package com.example.automationofinventory

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.automationofinventory.databinding.FragmentAddGoodsBinding
import com.google.firebase.firestore.FirebaseFirestore

class AddGoodsFragment : Fragment() {
    private var _binding: FragmentAddGoodsBinding? = null
    private val binding get() = _binding!!

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private var selectedWarehouseId: String? = null
    private var goodsList = mutableListOf<GoodsItem>()
    private val goodsAdapter by lazy { GoodsAdapter(goodsList) }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddGoodsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.goodsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.goodsRecyclerView.adapter = goodsAdapter

        binding.selectWarehouseButton.setOnClickListener { showWarehouseSelectionDialog() }
        binding.addGoodsButton.setOnClickListener { addGoodsToList() }
        binding.saveGoodsButton.setOnClickListener { saveGoodsToFirestore() }
    }

    private fun showWarehouseSelectionDialog() {
        firestore.collection("warehouses")
            .get()
            .addOnSuccessListener { result ->
                val warehouseNames = result.map { it.getString("name") ?: "Без названия" }
                val warehouseIds = result.map { it.id }

                AlertDialog.Builder(requireContext())
                    .setTitle("Выберите склад")
                    .setItems(warehouseNames.toTypedArray()) { _, which ->
                        selectedWarehouseId = warehouseIds[which]
                        binding.selectedWarehouseTextView.text = "Склад: ${warehouseNames[which]}"
                    }
                    .setNegativeButton("Отмена", null)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Ошибка загрузки складов: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun addGoodsToList() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_goods, null)
        val unitSpinner = dialogView.findViewById<Spinner>(R.id.spinnerUnit)

        val unitOptions = listOf("шт.", "кг", "л", "м")
        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, unitOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        unitSpinner.adapter = adapter

        val dialog = AlertDialog.Builder(requireContext())
            .setTitle("Добавить товар")
            .setView(dialogView)
            .setPositiveButton("Добавить") { _, _ ->
                val nameInput = dialogView.findViewById<EditText>(R.id.editTextGoodsName)
                val upcInput = dialogView.findViewById<EditText>(R.id.editTextGoodsUPC)

                val name = nameInput.text.toString().trim()
                val upc = upcInput.text.toString().trim()
                val unit = unitSpinner.selectedItem?.toString()?.trim() ?: ""

                if (name.isNotEmpty() && upc.isNotEmpty() && unit.isNotEmpty()) {
                    // Проверяем, есть ли товар с таким же UPC
                    if (goodsList.any { it.upc == upc }) {
                        Toast.makeText(requireContext(), "Товар с таким UPC уже существует", Toast.LENGTH_SHORT).show()
                    } else {
                        goodsList.add(GoodsItem(name, upc, unit, 0))
                        goodsAdapter.notifyDataSetChanged()
                    }
                } else {
                    Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Отмена", null)
            .create()

        dialog.show()
    }



    private fun saveGoodsToFirestore() {
        if (selectedWarehouseId == null) {
            Toast.makeText(requireContext(), "Выберите склад", Toast.LENGTH_SHORT).show()
            return
        }

        if (goodsList.isEmpty()) {
            Toast.makeText(requireContext(), "Добавьте хотя бы один товар", Toast.LENGTH_SHORT)
                .show()
            return
        }

        val warehouseRef = firestore.collection("warehouses").document(selectedWarehouseId!!)
        val batch = firestore.batch()

        goodsList.forEach { goodsItem ->
            val goodsData = hashMapOf(
                "name" to goodsItem.name,
                "upc" to goodsItem.upc,
                "unit" to goodsItem.unit,
                "quantity" to goodsItem.quantity
            )
            warehouseRef.collection("goods").document(goodsItem.upc).set(goodsData)
        }

        batch.commit()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Товары успешно сохранены", Toast.LENGTH_SHORT)
                    .show()
                goodsList.clear()
                goodsAdapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    "Ошибка сохранения: ${it.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

data class GoodsItem(
    var name: String,
    var upc: String,
    var unit: String,
    var quantity: Int
)