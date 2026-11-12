// Build raíz: solo declara los plugins disponibles para los módulos (apply false).
// Cada módulo aplica después los que necesita; así todos usan la misma versión del catálogo.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.compose) apply false
}
