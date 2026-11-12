package com.example.pmdm.ui.products

import android.content.res.Configuration
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.tooling.preview.Preview
import com.example.pmdm.R
import com.example.pmdm.domain.model.Product
import com.example.pmdm.ui.components.ProductImage
import com.example.pmdm.ui.theme.PMDMTheme

/**
 * Tarjeta que muestra un [Product]: foto, nombre, categoría, precio y corazón de favorito.
 *
 * Gestos de la tarjeta (todos con *state hoisting*: la tarjeta no decide qué pasa, solo avisa):
 * - **Toque** → [onClick] (abrir el detalle).
 * - **Doble toque** → [onFavoriteClick] (marcar/desmarcar favorito).
 * - **Pulsación larga** → vibración y menú con [onShareClick] y [onDeleteClick].
 *
 * Los colores salen de `MaterialTheme.colorScheme` (no de colores fijos), así la tarjeta se adapta sola al
 * tema claro, oscuro y a los colores dinámicos.
 */
@OptIn(ExperimentalFoundationApi::class) // combinedClickable puede ser experimental según la versión de Compose
@Composable
fun ProductCard(
    product: Product,
    onClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onShareClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Estado puramente visual (¿está abierto el menú?): vive aquí, no en el ViewModel. rememberSaveable lo
    // conserva al rotar la pantalla; con remember se cerraría. Dentro de una LazyColumn con key se guarda por `id`.
    var menuExpanded by rememberSaveable { mutableStateOf(false) }
    val haptic = LocalHapticFeedback.current

    // El Box sirve de ancla: el DropdownMenu se abre justo encima de la tarjeta.
    Box(modifier = modifier) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                // clip antes del clic: así el efecto de pulsación (ripple) respeta las esquinas redondeadas.
                .clip(CardDefaults.shape)
                // combinedClickable permite distinguir toque, doble toque y pulsación larga en el mismo elemento
                // (Modifier.clickable solo detecta el toque simple y Card(onClick) tampoco ofrece los demás).
                .combinedClickable(
                    role = Role.Button,
                    onClick = onClick,
                    onDoubleClick = onFavoriteClick,
                    // Etiqueta que TalkBack anuncia para la pulsación larga.
                    onLongClickLabel = stringResource(R.string.show_options),
                    onLongClick = {
                        // Vibración corta: confirma al usuario que la pulsación larga se ha registrado.
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        menuExpanded = true
                    },
                ),
        ) {
            ProductImage(
                imageUrl = product.imageUrl,
                // null a propósito: la foto es decorativa aquí porque el nombre del producto aparece justo debajo
                // como texto. Si tuviera descripción, TalkBack leería el nombre dos veces.
                contentDescription = null,
            )
            Row(
                modifier = Modifier.padding(
                    start = dimensionResource(R.dimen.padding_normal),
                    top = dimensionResource(R.dimen.padding_small),
                    bottom = dimensionResource(R.dimen.padding_small),
                ),
                horizontalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = product.name, style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = product.category,
                        style = MaterialTheme.typography.bodySmall,
                        // onSurfaceVariant: el color del tema para texto secundario.
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
                Text(
                    // stringResource con argumentos: rellena el %1$.2f de price_format según el idioma
                    text = stringResource(R.string.price_format, product.price),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
                // El doble toque es un atajo, pero no todo el mundo puede usarlo (p. ej. con TalkBack): el corazón es
                // la alternativa accesible. IconButton ya mide 48 dp y además muestra el estado actual.
                IconButton(onClick = onFavoriteClick) {
                    Icon(
                        imageVector = if (product.favorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = stringResource(
                            if (product.favorite) R.string.remove_favorite else R.string.add_favorite,
                        ),
                        tint = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        }

        // Menú contextual de la pulsación larga.
        DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.share)) },
                // Iconos decorativos (null): el texto de al lado ya dice lo mismo.
                leadingIcon = { Icon(Icons.Filled.Share, contentDescription = null) },
                onClick = {
                    menuExpanded = false
                    onShareClick()
                },
            )
            DropdownMenuItem(
                text = { Text(stringResource(R.string.delete)) },
                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                onClick = {
                    menuExpanded = false
                    onDeleteClick()
                },
            )
        }
    }
}

private val previewProduct = Product(1, "Desk lamp", "Home", 24.90, "https://example.com/1.jpg")

@Preview(showBackground = true)
@Composable
private fun ProductCardPreview() {
    PMDMTheme {
        ProductCard(product = previewProduct, onClick = {}, onFavoriteClick = {}, onShareClick = {}, onDeleteClick = {})
    }
}

// Misma tarjeta en modo oscuro y con favorito: sirve para comprobar que no queda ningún color fijo.
@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProductCardDarkPreview() {
    PMDMTheme {
        ProductCard(
            product = previewProduct.copy(favorite = true),
            onClick = {}, onFavoriteClick = {}, onShareClick = {}, onDeleteClick = {},
        )
    }
}
