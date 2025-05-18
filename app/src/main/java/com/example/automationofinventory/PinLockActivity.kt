package com.example.automationofinventory

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.automationofinventory.databinding.ActivityPinLockBinding

class PinLockActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityPinLockBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPinLockBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_pin_lock)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.adminDashboardFragment) {
                // Отключаем кнопку "Назад" в панели действий
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            } else {
                // Включаем кнопку "Назад" для других фрагментов
                supportActionBar?.setDisplayHomeAsUpEnabled(true)
            }
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_pin_lock)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }
}