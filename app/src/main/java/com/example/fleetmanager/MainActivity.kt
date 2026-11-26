package com.example.fleetmanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.navigation.compose.rememberNavController
import com.example.fleetmanager.ui.AppNavigation
import com.example.fleetmanager.ui.theme.MyApplicationTheme
import com.example.fleetmanager.viewmodel.AppViewModel

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme(dynamicColor = false) {
                val navController = rememberNavController()
                AppNavigation(navController = navController, vm = vm)
            }
        }
    }
}
