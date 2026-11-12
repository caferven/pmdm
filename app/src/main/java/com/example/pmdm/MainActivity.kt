package com.example.pmdm

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.pmdm.ui.navigation.AppNavHost
import com.example.pmdm.ui.theme.PMDMTheme
import com.example.pmdm.util.logDebug

/** Única Activity de la app: solo aloja la UI de Compose y su navegación. */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Demostración de Logcat: aparece en debug y se omite en release.
        logDebug("MainActivity.onCreate: app started")
        enableEdgeToEdge()
        setContent {
            // Cada pantalla trae su propio Scaffold (con su barra superior), por eso aquí no hay otro.
            PMDMTheme {
                AppNavHost()
            }
        }
    }
}
