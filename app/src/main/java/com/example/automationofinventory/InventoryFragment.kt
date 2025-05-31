package com.example.automationofinventory

import android.annotation.SuppressLint
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

    private val inventoryAdapter: InventoryAdapter by lazy { InventoryAdapter(goodsList) }
    private val firestorm = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_inventory, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initUi()
    }

    private fun initUi() {
        selectedWarehouseTextView = requireView().findViewById(R.id.selectedWarehouseTextView)
        inventoryRecyclerView = requireView().findViewById(R.id.inventoryRecyclerView)
        inventoryRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        view?.findViewById<View>(R.id.selectWarehouseButton)?.setOnClickListener {
            showWarehouseSelectionDialog()
        }

        view?.findViewById<View>(R.id.saveInventoryButton)?.setOnClickListener {
            saveInventory()
        }
    }

    private fun showWarehouseSelectionDialog() {
        firestorm.collection(warehouses)
            .get()
            .addOnSuccessListener { documents ->
                val warehouseNames = documents.map { it.getString(name) ?: getString(R.string.nameless_warehouse) }
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.select_warehouse))
                    .setItems(warehouseNames.toTypedArray()) { _, which ->
                        selectedWarehouseTextView.text = warehouseNames[which]
                        loadGoodsForInventory(warehouseNames[which])
                    }
                    .setNegativeButton(getString(R.string.voids), null)
                    .show()
            }
            .addOnFailureListener {
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_database),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    @SuppressLint("NotifyDataSetChanged")
    private fun loadGoodsForInventory(warehouseName: String) {
        firestorm.collection(warehouses)
            .whereEqualTo(name, warehouseName)
            .get()
            .addOnSuccessListener { documents ->
                val warehouseId = documents.firstOrNull()?.id ?: return@addOnSuccessListener
                firestorm.collection("${warehouses}/$warehouseId/${goods}")
                    .get()
                    .addOnSuccessListener { goodsDocs ->
                        goodsList.clear()
                        goodsDocs.forEach { doc ->
                            val name = doc.getString(name) ?: ""
                            val upc = doc.getString(upc) ?: ""
                            val unit = doc.getString(unit) ?: ""
                            val quantity = doc.getLong(quantity)?.toInt() ?: 0
                            goodsList.add(GoodsItem(name, upc, unit, quantity))
                        }
                        inventoryAdapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.error_loading_products),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(),  getString(R.string.error_loading_products), Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun saveInventory() {
        val warehouseName = selectedWarehouseTextView.text.toString()
        if (warehouseName == getString(R.string.warehouse_not_selected)) {
            Toast.makeText(requireContext(), getString(R.string.warehouse_save), Toast.LENGTH_SHORT)
                .show()
            return
        }

        goodsList.find { it.quantity < 0 }?.let { invalidItem ->
            Toast.makeText(
                requireContext(),
                "${getString(R.string.count)} '${invalidItem.name}' ${getString(R.string.not_negative)}",
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        val progressDialog = ProgressDialog(requireContext()).apply {
            setMessage(getString(R.string.save_inv))
            setCancelable(false)
            show()
        }

        firestorm.collection(warehouses)
            .whereEqualTo(name, warehouseName)
            .get()
            .addOnSuccessListener { documents ->
                val warehouseId = documents.firstOrNull()?.id ?: run {
                    progressDialog.dismiss()
                    Toast.makeText(requireContext(), getString(R.string.not_found), Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }

                val batch = firestorm.batch()
                val goodsCollection = firestorm.collection("${warehouses}/$warehouseId/${goods}")

                goodsList.forEach { item ->
                    val docRef = goodsCollection.document(item.upc)
                    batch.update(docRef, mapOf(quantity to item.quantity))
                }

                batch.commit()
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.save),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        Toast.makeText(
                            requireContext(),
                            "${getString(R.string.error_database)} ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Toast.makeText(
                    requireContext(),
                    getString(R.string.error_database),
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    companion object {
        const val name: String = "firstName"
        const val warehouses: String = "warehouses"
        const val upc: String = "upc"
        const val goods: String = "goods"
        const val unit: String = "unit"
        const val quantity: String = "quantity"
    }
}