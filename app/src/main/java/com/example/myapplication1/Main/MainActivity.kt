package com.example.myapplication1.Main

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.example.myapplication1.R
import com.example.myapplication1.databinding.ActivityMainBinding
import com.example.myapplication1.newfeatures.Notificacion.NotificationService


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var notificationsManager: NotificationService
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
       notificationsManager = NotificationService(this)
        // Check login state
        val sharedPreferences = getSharedPreferences("MyApp", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            // If user is not logged in, redirect to Login activity
            val intent = Intent(this, Login::class.java)
            startActivity(intent)
            finish()
            return
        }

        // Get isAdmin value from SharedPreferences
        val isAdmin = sharedPreferences.getBoolean("isAdmin", false)

        if (isAdmin) {
            // Load admin fragment
            checkForNotifications()
            initAdminNavigation()
        } else {
            // Load normal user fragment
            initUserNavigation()
        }
    }

    fun checkForNotifications() {
        notificationsManager.checkForNotifications()
    }

    private fun initAdminNavigation() {
        val navGraphId = R.navigation.nav_admin
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController
        navController.graph = navController.navInflater.inflate(navGraphId)

        // Establecer el menú
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.menu.clear()
        binding.bottomNavigationView.inflateMenu(R.menu.menu_admin)
    }

    private fun initUserNavigation() {
        val navGraphId = R.navigation.nav_user
        val navHost =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHost.navController
        navController.graph = navController.navInflater.inflate(navGraphId)

        // Establecer el menú
        binding.bottomNavigationView.setupWithNavController(navController)
        binding.bottomNavigationView.menu.clear()
        binding.bottomNavigationView.inflateMenu(R.menu.menu_user)


        binding.bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.ordersFragmentUser -> {
                    navController.popBackStack(R.id.ordersFragmentUser, false)
                    navController.navigate(R.id.ordersFragmentUser)
                    true
                }

                else -> {
                    navController.navigate(item.itemId)
                    true
                }
            }
        }
    }
}