package com.elektro.monitoring.app

import android.Manifest
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.elektro.monitoring.R
import com.elektro.monitoring.data.service.BackgroundService
import com.elektro.monitoring.databinding.ActivityMainBinding
import com.elektro.monitoring.helper.Constants
import com.elektro.monitoring.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val authViewModel: AuthViewModel by viewModels()

    private val navController by lazy {
        (supportFragmentManager.findFragmentById(R.id.nav_host_activity) as NavHostFragment).navController
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) {
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)

        authViewModel.service.postValue(false)

        binding.bottomNavBar.setupWithNavController(navController)
        binding.bottomNavBar.setOnItemSelectedListener {
            val navAccount = R.id.nav_account
            val navHome = R.id.nav_home
            val clearNav = NavOptions.Builder().setLaunchSingleTop(true)
                .setPopUpTo(R.id.mobile_navigation, true).build()
            when (it.itemId) {
                navAccount -> {
                    navController.navigate(R.id.accountFragment, null, clearNav)
                    true
                }

                navHome -> {
                    navController.navigate(R.id.homeFragment, null, clearNav)
                    true
                }

                else -> {
                    false
                }
            }
        }

        navController.addOnDestinationChangedListener { _, destination, _ ->
            binding.bottomNavBar.isVisible = when (destination.id) {
                R.id.homeFragment -> true
                R.id.accountFragment -> true
                else -> false
            }
        }

        setContentView(binding.root)
    }


    override fun onPostResume() {
        super.onPostResume()
        authViewModel.loggedUser()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(
                Manifest.permission.POST_NOTIFICATIONS
            )
        }

        lifecycleScope.launch {
            lifecycle.repeatOnLifecycle(Lifecycle.State.RESUMED) {
                authViewModel.user.collect {
                    if (it.data != null) {
                        authViewModel.service.postValue(true)
                    }
                }
            }
        }

        authViewModel.service.observe(this){ service ->
            if (service == false) {
                val serviceIntent = Intent(this, BackgroundService::class.java)
                stopService(serviceIntent)
            } else {
                val serviceIntent = Intent(this, BackgroundService::class.java)
                startService(serviceIntent)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        authViewModel.service.postValue(false)
        val serviceIntent = Intent(this, BackgroundService::class.java)
        stopService(serviceIntent)
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, MainActivity::class.java).apply {
            putExtra(
                Constants.NOTIFICATION_MESSAGE_TAG, "Hi ☕\uD83C\uDF77\uD83C\uDF70"
            )
        }
    }
}