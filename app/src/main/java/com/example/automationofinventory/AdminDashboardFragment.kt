package com.example.automationofinventory

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.automationofinventory.databinding.FragmentAdminDashboardBinding
import com.google.firebase.auth.FirebaseAuth

class AdminDashboardFragment : Fragment() {

    private var _binding: FragmentAdminDashboardBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminDashboardBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_admin_dashboard, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.cardEmployee.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_createNewEmployeeFragment)
        }
        binding.cardWarehouse.setOnClickListener{
            findNavController().navigate(R.id.action_adminDashboardFragment_to_addWarehouseFragment)
        }
        binding.cardReceipt.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_addGoodsFragment)
        }
        binding.cardInventory.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_inventoryFragment)
        }
        binding.report.setOnClickListener {
            findNavController().navigate(R.id.action_adminDashboardFragment_to_inventoryReportFragment)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_logout -> {
                performLogout()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun performLogout() {
        val sharedPreferences = requireContext().getSharedPreferences(
            AuthorizationFragment.PREFS_NAME, Context.MODE_PRIVATE
        )
        sharedPreferences.edit().clear().apply()

        FirebaseAuth.getInstance().signOut()
        findNavController().navigate(R.id.action_adminDashboardFragment_to_firstFragment)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}