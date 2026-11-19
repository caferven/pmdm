package com.example.pmdm.ui.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.foundation.gestures.rememberTransformableState
import androidx.compose.foundation.gestures.transformable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pmdm.R
import com.example.pmdm.domain.model.Product
import com.example.pmdm.ui.components.ProductImage
import com.example.pmdm.ui.theme.PMDMTheme

/**
 * Pantalla de detalle con estado: obtiene su ViewModel y delega el dibujo en [ProductDetailContent],
 * que solo recibe datos y lambdas (nunca el ViewModel).
 *
 * @param onBack se llama al pulsar la flecha de la barra superior y el evento sube hasta el `NavHost`.
 */
@Composable
fun ProductDetailScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewModel: ProductDetailViewModel = viewModel(factory = ProductDetailViewModel.Factory)
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    ProductDetailContent(state = state, onBack = onBack, modifier = modifier)
}

/** Contenido sin estado del detalle: barra con botón atrás y los datos del producto. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductDetailContent(
    state: ProductDetailUiState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.product_detail_title)) },
                navigationIcon = {
                    // IconButton ya mide 48 dp (objetivo táctil mínimo recomendado).
                    IconButton(onClick = onBack) {
                        // AutoMirrored: en idiomas de derecha a izquierda la flecha se voltea sola.
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Box(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            val product = state.product
            when {
                state.loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                product == null -> Text(
                    text = stringResource(R.string.product_not_found),
                    modifier = Modifier.align(Alignment.Center),
                )
                else -> Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                    ZoomableProductImage(
                        imageUrl = product.imageUrl,
                        // Aquí la foto SÍ es informativa (es el contenido principal), así que lleva descripción.
                        contentDescription = stringResource(R.string.product_image_description, product.name),
                    )
                    Column(modifier = Modifier.padding(dimensionResource(R.dimen.padding_normal))) {
                        Text(text = product.name, style = MaterialTheme.typography.headlineMedium)
                        Text(
                            text = stringResource(R.string.detail_category, product.category),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(R.string.price_label),
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(R.string.price_format, product.price),
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        }
    }
}

/**
 * Foto del producto que se puede ampliar pellizcando (zoom con dos dedos) y mover con un dedo cuando está ampliada.
 *
 * Gesto: pellizco (pinch). `Modifier.transformable` detecta zoom, desplazamiento y rotación y los entrega
 * como cambios incrementales; aquí se acumulan en `scale`, `offsetX` y `offsetY` y se aplican con `graphicsLayer`.
 *
 * El zoom es estado puramente visual (no afecta a los datos): vive en este Composable y se guarda con
 * `rememberSaveable` para que no se pierda al rotar la pantalla.
 */
@Composable
private fun ZoomableProductImage(imageUrl: String, contentDescription: String) {
    var scale by rememberSaveable { mutableFloatStateOf(1f) }
    var offsetX by rememberSaveable { mutableFloatStateOf(0f) }
    var offsetY by rememberSaveable { mutableFloatStateOf(0f) }
    // Tamaño del contenedor en píxeles: hace falta para no dejar mover la foto más allá de sus bordes.
    var size by remember { mutableStateOf(IntSize.Zero) }

    // La lambda recibe el centroide (el punto entre los dedos), el factor de `zoom`, el desplazamiento y la rotación.
    // Aquí el `zoom` se hace siempre desde el centro de la foto y no hay rotación, así que se ignoran (_).
    val transformState = rememberTransformableState { _, zoomChange, panChange, _ ->
        // zoomChange es un factor (1.1 = un 10 % más grande): se multiplica y se limita entre 1× y 5×.
        scale = (scale * zoomChange).coerceIn(1f, 5f)
        // Al ampliar, la foto sobresale por los lados; ese sobrante es lo máximo que se puede desplazar.
        val maxX = size.width * (scale - 1f) / 2f
        val maxY = size.height * (scale - 1f) / 2f
        offsetX = (offsetX + panChange.x).coerceIn(-maxX, maxX)
        offsetY = (offsetY + panChange.y).coerceIn(-maxY, maxY)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(4f / 3f)
            .onSizeChanged { size = it }
            // La foto ampliada no debe invadir el resto de la pantalla.
            .clipToBounds()
            // canPan: con `zoom` 1× no se captura el desplazamiento con un dedo, así la pantalla puede seguir haciendo scroll.
            .transformable(state = transformState, canPan = { scale > 1f }),
    ) {
        ProductImage(
            imageUrl = imageUrl,
            contentDescription = contentDescription,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(
                    scaleX = scale,
                    scaleY = scale,
                    translationX = offsetX,
                    translationY = offsetY,
                ),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductDetailContentPreview() {
    PMDMTheme {
        ProductDetailContent(
            state = ProductDetailUiState(
                product = Product(1, "Desk lamp", "Home", 24.90, "https://example.com/1.jpg"),
                loading = false,
            ),
            onBack = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductDetailNotFoundPreview() {
    PMDMTheme {
        ProductDetailContent(state = ProductDetailUiState(loading = false), onBack = {})
    }
}
