package com.example.pmdm.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import coil3.compose.AsyncImage
import coil3.request.ImageRequest
import coil3.request.crossfade
import com.example.pmdm.R

/**
 * Foto de un producto descargada de internet con Coil.
 *
 * @param imageUrl dirección de la imagen.
 * @param contentDescription texto para lectores de pantalla (TalkBack). Pasa `null` solo si la
 * imagen es decorativa, es decir, si no aporta información que no esté ya en el texto de al lado.
 */
@Composable
fun ProductImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: Modifier = Modifier,
) {
    AsyncImage(
        // ImageRequest describe qué cargar y cómo. `crossfade` hace un fundido suave del placeholder a la foto.
        model = ImageRequest.Builder(LocalContext.current)
            .data(imageUrl)
            .crossfade(true)
            .build(),
        contentDescription = contentDescription,
        // `placeholder`: se ve mientras se descarga. `error`: se ve si falla (sin red, URL rota, servidor caído...).
        placeholder = painterResource(R.drawable.ic_image_placeholder),
        error = painterResource(R.drawable.ic_image_error),
        // Crop rellena todo el hueco recortando lo que sobre, sin deformar la foto.
        contentScale = ContentScale.Crop,
        modifier = modifier
            // Las fotos de la API son 400x300 (4:3): reservar esa proporción evita que la lista "salte"
            // al llegar cada imagen.
            .aspectRatio(4f / 3f)
            // Fondo del tema para el placeholder y el error, así sigue el modo claro/oscuro.
            .background(MaterialTheme.colorScheme.surfaceVariant),
    )
}
