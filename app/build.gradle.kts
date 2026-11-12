// PLUGINS
plugins {
    alias(libs.plugins.android.application) // Convierte el módulo en una app Android (genera el APK)
    alias(libs.plugins.kotlin.compose)      // Compilador de Compose (transforma las funciones @Composable)
}

android {
    namespace = "com.example.pmdm"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.example.pmdm"
        minSdk = 24
        targetSdk = 37

        // VERSIONADO
        // versionCode: entero que Google Play usa para ordenar versiones; debe subir en cada publicación.
        versionCode = 1
        // versionName: texto que ve el usuario; no tiene ningún efecto técnico.
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    // BUILD TYPES
    buildTypes {
        debug {
            // Sufijo en el id: permite tener instaladas a la vez la versión debug y la release.
            applicationIdSuffix = ".debug"
            // Sufijo visible en "Información de la app": distingue de un vistazo qué build es.
            versionNameSuffix = "-debug"
            // Los logs de depuración solo se activan aquí (ver util/Logger.kt).
            buildConfigField("boolean", "DEBUG_LOGS", "true")
        }
        release {
            // Minificación con R8: reduce y ofusca el código, y elimina clases sin usar.
            // Las reglas de conservación están en src/main/keepRules.
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
            // En producción no se escribe nada en Logcat.
            buildConfigField("boolean", "DEBUG_LOGS", "false")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    buildFeatures {
        compose = true
        // Necesitamos `buildConfig` para DEBUG_LOGS.
        buildConfig = true
    }

    // LINT
    lint {
        // Si lint encuentra un error (no un aviso), la tarea falla: evita llegar a producción con errores.
        abortOnError = true
        // Los avisos (warnings) se muestran, pero no rompen la compilación.
        warningsAsErrors = false
        // Los informes (HTML, SARIF...) ya no se configuran: AGP los genera siempre.
        // El HTML queda en app/build/reports/lint-results-debug.html
    }
}

// DEPENDENCIAS
dependencies {
    // Compose: el BOM fija las versiones de todas las librerías de Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)

    // AndroidX
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.navigation.compose)

    // Imágenes: Coil (AsyncImage) y su módulo de red para descargar por internet
    implementation(libs.coil.compose)
    implementation(libs.coil.network.okhttp)

    // Coroutines: incluye kotlinx-coroutines-core (Flow, suspend) y Dispatchers.Main de Android
    implementation(libs.kotlinx.coroutines.android)

    // Tests unitarios (JVM)
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)

    // Tests instrumentados (dispositivo/emulador)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    // Solo en debug: herramientas de Preview y de test de Compose
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}
