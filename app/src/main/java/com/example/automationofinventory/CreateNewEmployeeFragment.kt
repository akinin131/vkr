package com.example.automationofinventory

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import com.example.automationofinventory.databinding.FragmentCreateNewEmployeeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CreateNewEmployeeFragment : Fragment() {

    private var _binding: FragmentCreateNewEmployeeBinding? = null
    private val binding get() = _binding!!

    private val firebaseAuth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }
    private val firestone: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentCreateNewEmployeeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val roles_select = arrayOf(admins, employees)
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, roles_select)
        binding.role.setAdapter(adapter)

        binding.role?.setOnItemClickListener { v, r, position, _ ->
            val selected_role = roles_select[position]
            Toast.makeText(
                requireContext(),
                "${getString(R.string.select)} $selected_role",
                Toast.LENGTH_SHORT
            ).show()
        }

        binding.buttonSaveEmployee.setOnClickListener {
            createEmployee()
        }
    }

    private fun createEmployee() {
        val firstName = binding.firstName.text.toString().trim()
        val lastName = binding.lastName.text.toString().trim()
        val email = binding.email.text.toString().trim()
        val password = binding.password.text.toString().trim()
        val role = binding.role.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.fill_all_fields),
                Toast.LENGTH_SHORT
            ).show()
        } else {
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val userId = firebaseAuth.currentUser?.uid ?: ""
                        saveEmployeeToFirestorm(userId, firstName, lastName, email, role)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "${getString(R.string.error_database)}: ${task.exception?.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
        }
    }

    private fun saveEmployeeToFirestorm(
        userId: String,
        firstName: String,
        lastName: String,
        email: String,
        role: String
    ) {
        val employee = hashMapOf(
            id to userId,
            name to firstName,
            lastName to lastName,
            role to email,
            email to role,
            createdAt to System.currentTimeMillis()
        )

        firestone.collection(employees).document(userId).set(employee)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), getString(R.string.create_true), Toast.LENGTH_SHORT)
                        .show()
                    clearInputs()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "${getString(R.string.error_database)} ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    private fun clearInputs() {
        binding.firstName.text?.clear()
        binding.lastName.text?.clear()
        binding.email.text?.clear()
        binding.password.text?.clear()
        binding.role.text.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        const val ID: String = "id"
        const val name: String = "firstName"
        const val lastName: String = "lastName"
        const val role: String = "role"
        const val email: String = "email"
        const val createdAt: String = "createdAt"
        const val admins: String = "admins"
        const val employees: String = "employees"
    }
}