package com.example.pmdm.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight

// Partimos de la escala tipográfica por defecto de Material 3 (tamaños, interlineado y espaciado ya
// calibrados) y solo cambiamos lo que da personalidad a MiTienda: familia y peso de los títulos.
// Los tamaños van en sp (no dp) para respetar el tamaño de letra que el usuario elige en Ajustes.
private val defaultTypography = Typography()

// Familia serif del sistema para los títulos. Usar fuentes propias (res/font) o descargables
// (Google Fonts) requiere dependencias o archivos extra: se deja como ejercicio.
private val TitleFontFamily = FontFamily.Serif

val Typography = Typography(
    headlineMedium = defaultTypography.headlineMedium.copy(
        fontFamily = TitleFontFamily,
        fontWeight = FontWeight.SemiBold,
    ),
    headlineSmall = defaultTypography.headlineSmall.copy(
        fontFamily = TitleFontFamily,
        fontWeight = FontWeight.SemiBold,
    ),
    titleLarge = defaultTypography.titleLarge.copy(
        fontFamily = TitleFontFamily,
        fontWeight = FontWeight.SemiBold,
    ),
    // titleMedium se usa para el nombre y el precio en las tarjetas: algo más de peso para destacarlos.
    titleMedium = defaultTypography.titleMedium.copy(fontWeight = FontWeight.SemiBold),
    // El resto de estilos (body, label...) se queda con los valores por defecto.
)
