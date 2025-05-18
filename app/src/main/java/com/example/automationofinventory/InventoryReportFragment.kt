package com.example.automationofinventory

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.Toast
import com.google.firebase.firestore.FirebaseFirestore

class InventoryReportFragment : Fragment() {

    private lateinit var warehouseSpinner: Spinner
    private lateinit var generateReportButton: Button
    private val db = FirebaseFirestore.getInstance()
    private val warehouseList = mutableListOf<Pair<String, String>>() // name to ID
    private var selectedWarehouseId: String? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_inventory_report, container, false)

        warehouseSpinner = view.findViewById(R.id.warehouseSpinner)
        generateReportButton = view.findViewById(R.id.generateReportButton)

        loadWarehouses()

        generateReportButton.setOnClickListener {
            selectedWarehouseId?.let {
                loadGoodsAndShowReport(it)
            } ?: Toast.makeText(requireContext(), "Выберите склад", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun loadWarehouses() {
        db.collection("warehouses").get()
            .addOnSuccessListener { documents ->
                warehouseList.clear()
                val names = mutableListOf<String>()
                for (doc in documents) {
                    val name = doc.getString("name") ?: "Без названия"
                    warehouseList.add(name to doc.id)
                    names.add(name)
                }

                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                warehouseSpinner.adapter = adapter

                warehouseSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                        selectedWarehouseId = warehouseList[position].second
                    }

                    override fun onNothingSelected(parent: AdapterView<*>) {
                        selectedWarehouseId = null
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Ошибка загрузки складов", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadGoodsAndShowReport(warehouseId: String) {
        db.collection("warehouses").document(warehouseId).collection("goods").get()
            .addOnSuccessListener { documents ->
                val report = StringBuilder()
                report.append("📦 Отчёт по складу: ${getWarehouseNameById(warehouseId)}\n\n")
                report.append(String.format("%-25s %-10s %-10s\n", "Название", "Ед.", "Кол-во"))
                report.append("=".repeat(33)).append("\n")

                for (doc in documents) {
                    val name = doc.getString("name") ?: "-"
                    val unit = doc.getString("unit") ?: "-"
                    val quantity = doc.getLong("quantity") ?: 0L
                    report.append(String.format("%-25s %-15s %-10d\n", name, unit, quantity))
                }

                showReportDialog(report.toString())
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Ошибка загрузки товаров", Toast.LENGTH_SHORT).show()
            }
    }

    private fun getWarehouseNameById(id: String): String {
        return warehouseList.find { it.second == id }?.first ?: "Неизвестный склад"
    }

    private fun showReportDialog(report: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("📊 Отчёт")
            .setMessage(report)
            .setPositiveButton("ОК", null)
            .setNegativeButton("Скопировать") { _, _ ->
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText("Inventory Report", report)
                clipboard.setPrimaryClip(clip)
                Toast.makeText(requireContext(), "Скопировано в буфер", Toast.LENGTH_SHORT).show()
            }
            .show()
    }
}
