package com.example.automationofinventory

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.navigation.fragment.findNavController
import com.example.automationofinventory.databinding.FragmentAuthorizationBinding
import com.google.android.material.snackbar.Snackbar

/**
 * Фрагмент авторизации пользователей в системе.
 *
 * Предоставляет функционал для:
 * - Авторизации пользователей через Firebase Authentication
 * - Проверки роли пользователя (сотрудник/администратор)
 * - Сохранения сессии авторизованного пользователя
 * - Автоматического входа при наличии сохраненной сессии
 *
 * Основные компоненты:
 * - Firebase Authentication для авторизации
 * - Cloud Firestore для проверки ролей пользователей
 * - SharedPreferences для хранения данных сессии
 *
 * Процесс авторизации:
 * 1. Пользователь выбирает роль и вводит учетные данные
 * 2. Происходит авторизация через Firebase
 * 3. Проверяется наличие пользователя в соответствующей коллекции Firestore
 * 4. При успешной проверке данные сохраняются локально
 * 5. Осуществляется переход на соответствующий экран (панель сотрудника/администратора)
 *
 * @property firebaseAuth Firebase Authentication instance для авторизации
 * @property firestore Firestore instance для работы с базой данных
 * @property sharedPreferences SharedPreferences для хранения локальных данных
 */

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AuthorizationFragment : Fragment() {

    private var _binding: FragmentAuthorizationBinding? = null
    private val binding get() = _binding!!

    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences

    companion object {
        const val PREFS_NAME = "UserPrefs"

        private const val KEY_USER_EMAIL = "user_email"
        private const val KEY_USER_ROLE = "user_role"
        private const val ROLE_EMPLOYEE = "employees"
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAuthorizationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeComponents()
        checkExistingSession()
    }

    private fun initializeComponents() {
        (requireActivity() as AppCompatActivity).supportActionBar?.title =
            getString(R.string.authorization)

        firebaseAuth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        setupSpinner()
        setupClickListeners()
    }

    private fun setupSpinner() {
        val roles = listOf(
            getString(R.string.role_employee),
            getString(R.string.role_admin)
        )

        ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_item,
            roles
        ).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            binding.spinnerRole.adapter = this
        }
    }

    private fun setupClickListeners() {
        binding.buttonNext.setOnClickListener {
            if (validateInputs()) {
                performAuthorization()
            }
        }
    }

    private fun validateInputs(): Boolean {
        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()
        var isValid = true

        when {
            email.isEmpty() -> {
                binding.textInputEmail.error = getString(R.string.error_empty_email)
                isValid = false
            }

            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                binding.textInputEmail.error = getString(R.string.error_invalid_email)
                isValid = false
            }

            else -> binding.textInputEmail.error = null
        }

        when {
            password.isEmpty() -> {
                binding.textInputPassword.error = getString(R.string.error_empty_password)
                isValid = false
            }

            password.length < 6 -> {
                binding.textInputPassword.error = getString(R.string.error_short_password)
                isValid = false
            }

            else -> binding.textInputPassword.error = null
        }

        return isValid
    }

    private fun performAuthorization() {
        showLoading(true)

        val email = binding.editTextEmail.text.toString().trim()
        val password = binding.editTextPassword.text.toString().trim()
        val selectedRole = binding.spinnerRole.selectedItem.toString()

        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    checkUserRole(email, selectedRole)
                } else {
                    showLoading(false)
                    showError(getString(R.string.auth_failed))
                }
            }
    }

    private fun checkUserRole(email: String, selectedRole: String) {
        val collectionName =
            if (selectedRole == getString(R.string.role_employee)) ROLE_EMPLOYEE else "admins"

        firestore.collection(collectionName)
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                showLoading(false)
                if (!querySnapshot.isEmpty) {
                    saveUserSession(email, selectedRole)
                    navigateToappropriateDashboard(selectedRole)
                } else {
                    showError(getString(R.string.user_not_found))
                    firebaseAuth.signOut()
                }
            }
            .addOnFailureListener {
                showLoading(false)
                showError(getString(R.string.error_database))
                firebaseAuth.signOut()
            }
    }

    private fun saveUserSession(email: String, role: String) {
        sharedPreferences.edit().apply {
            putString(KEY_USER_EMAIL, email)
            putString(KEY_USER_ROLE, role)
            apply()
        }
    }

    private fun checkExistingSession() {
        val savedEmail = sharedPreferences.getString(KEY_USER_EMAIL, null)
        val savedRole = sharedPreferences.getString(KEY_USER_ROLE, null)

        if (savedEmail != null && savedRole != null && firebaseAuth.currentUser != null) {
            navigateToappropriateDashboard(savedRole)
        }
    }

    private fun navigateToappropriateDashboard(role: String) {
        when (role) {
            getString(R.string.role_employee) -> {
                // findNavController().navigate(R.id.action_authorizationFragment_to_employeeDashboardFragment)
            }

            getString(R.string.role_admin) -> {
                findNavController().navigate(R.id.action_firstFragment_to_adminDashboardFragment)
            }
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.isVisible = isLoading
        binding.buttonNext.isEnabled = !isLoading
    }

    private fun showError(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_LONG)
            .setAction("OK") { }
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}