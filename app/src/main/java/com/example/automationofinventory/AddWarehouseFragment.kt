package com.example.automationofinventory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.automationofinventory.databinding.FragmentAddWarehouseBinding

import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.google.firebase.firestore.FirebaseFirestore

class AddWarehouseFragment : Fragment() {

    private var _binding: FragmentAddWarehouseBinding? = null
    private val binding get() = _binding!!

    private val firestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddWarehouseBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.addWarehouseButton.setOnClickListener {
            val warehouseName = binding.warehouseNameInput.text.toString().trim()
            val warehouseLocation = binding.warehouseLocationInput.text.toString().trim()

            if (warehouseName.isEmpty() || warehouseLocation.isEmpty()) {
                Toast.makeText(requireContext(), "Пожалуйста, заполните все поля", Toast.LENGTH_SHORT).show()
            } else {
                addWarehouseToFirestore(warehouseName, warehouseLocation)
            }
        }
    }

    private fun addWarehouseToFirestore(name: String, location: String) {
        val warehouse = hashMapOf(
            "name" to name,
            "location" to location,
            "created_at" to System.currentTimeMillis()
        )

        firestore.collection("warehouses")
            .add(warehouse)
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Склад успешно добавлен!", Toast.LENGTH_SHORT).show()
                binding.warehouseNameInput.text?.clear()
                binding.warehouseLocationInput.text?.clear()
                findNavController().navigate(R.id.action_addWarehouseFragment_to_adminDashboardFragment)
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Ошибка добавления склада: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
