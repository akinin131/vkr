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
    private val firestore: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateNewEmployeeBinding.inflate(inflater, container, false)

        val roles = arrayOf("admins", "employees")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, roles)
        binding.spinnerRole.setAdapter(adapter)

        binding.spinnerRole.setOnItemClickListener { _, _, position, _ ->
            val selectedRole = roles[position]
            Toast.makeText(requireContext(), "Выбрана роль: $selectedRole", Toast.LENGTH_SHORT).show()
        }

        binding.buttonSaveEmployee.setOnClickListener {
            createEmployee()
        }

        return binding.root
    }

    private fun createEmployee() {
        val firstName = binding.editTextFirstName.text.toString().trim()
        val lastName = binding.editTextLastName.text.toString().trim()
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()
        val role = binding.spinnerRole.text.toString().trim()

        if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || role.isEmpty()) {
            Toast.makeText(requireContext(), "Заполните все поля", Toast.LENGTH_SHORT).show()
            return
        }

        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userId = firebaseAuth.currentUser?.uid ?: ""
                    saveEmployeeToFirestore(userId, firstName, lastName, email, role)
                } else {
                    Toast.makeText(requireContext(), "Ошибка регистрации: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveEmployeeToFirestore(userId: String, firstName: String, lastName: String, email: String, role: String) {
        val employee = hashMapOf(
            "id" to userId,
            "firstName" to firstName,
            "lastName" to lastName,
            "email" to email,
            "role" to role,
            "createdAt" to System.currentTimeMillis()
        )

        firestore.collection("employees").document(userId).set(employee)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(requireContext(), "Сотрудник успешно создан", Toast.LENGTH_SHORT).show()
                    clearInputs()
                } else {
                    Toast.makeText(requireContext(), "Ошибка сохранения: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun clearInputs() {
        binding.editTextFirstName.text?.clear()
        binding.editTextLastName.text?.clear()
        binding.editTextEmail.text?.clear()
        binding.editTextPassword.text?.clear()
        binding.spinnerRole.text.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}