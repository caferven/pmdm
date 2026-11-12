package com.example.pmdm.ui.products

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.pmdm.R
import com.example.pmdm.domain.model.Product
import com.example.pmdm.ui.theme.PMDMTheme
import kotlinx.coroutines.launch

/**
 * Pantalla de productos con estado. Es el único punto donde se toca el [ProductsViewModel]:
 * recoge su estado y convierte cada evento de la UI en una llamada al ViewModel.
 * Los Composables de más abajo ([ProductsContent], [ProductCard]...) solo reciben datos y lambdas.
 *
 * Dónde se eleva cada evento (*state hoisting*):
 * - **Datos de negocio** (favorito, borrar, buscar, refrescar): suben hasta aquí y se resuelven en el ViewModel.
 * - **Estado puramente visual** (Snackbar, menú abierto): se queda en la UI. El [SnackbarHostState] se crea aquí
 *   porque mostrar el "Deshacer" necesita una corrutina y decidir si llamar a [ProductsViewModel.restoreProduct].
 * - **Navegación** (abrir detalle o catálogo): sube todavía más, hasta el `NavHost`.
 */
@Composable
fun ProductsScreen(
    onProductClick: (Int) -> Unit,
    onOpenCatalog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // El ViewModel se obtiene aquí dentro y no como parámetro: así ningún Composable recibe un ViewModel.
    val viewModel: ProductsViewModel = viewModel(factory = ProductsViewModel.Factory)

    // collectAsStateWithLifecycle deja de recoger el flujo cuando la app pasa a segundo plano.
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val resources = LocalResources.current

    ProductsContent(
        state = state,
        query = query,
        snackbarHostState = snackbarHostState,
        onQueryChange = viewModel::onQueryChange,
        onRefresh = viewModel::refresh,
        onProductClick = onProductClick,
        onFavoriteClick = viewModel::toggleFavorite,
        onShareClick = { product ->
            shareProduct(
                context = context,
                text = resources.getString(R.string.share_text, product.name, product.price),
                chooserTitle = resources.getString(R.string.share_chooser_title),
            )
        },
        onDeleteClick = { product ->
            viewModel.deleteProduct(product.id)
            scope.launch {
                // Si ya había un Snackbar en pantalla se descarta para mostrar el del último borrado.
                snackbarHostState.currentSnackbarData?.dismiss()
                val result = snackbarHostState.showSnackbar(
                    message = resources.getString(R.string.product_deleted, product.name),
                    actionLabel = resources.getString(R.string.snackbar_undo),
                    duration = SnackbarDuration.Short,
                )
                if (result == SnackbarResult.ActionPerformed) viewModel.restoreProduct(product.id)
            }
        },
        onOpenCatalog = onOpenCatalog,
        modifier = modifier,
    )
}

/** Abre el selector de aplicaciones de Android para compartir [text]. */
private fun shareProduct(context: Context, text: String, chooserTitle: String) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(intent, chooserTitle))
}

/**
 * Contenido **sin estado** de la pantalla: dibuja lo que dice [state] y avisa con callbacks.
 * Al no depender de un ViewModel es fácil de probar y de previsualizar.
 */
@OptIn(ExperimentalMaterial3Api::class) // TopAppBar puede ser experimental según la versión de Material 3
@Composable
fun ProductsContent(
    state: ProductsUiState,
    query: String,
    snackbarHostState: SnackbarHostState,
    onQueryChange: (String) -> Unit,
    onRefresh: () -> Unit,
    onProductClick: (Int) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    onShareClick: (Product) -> Unit,
    onDeleteClick: (Product) -> Unit,
    onOpenCatalog: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Scaffold coloca la barra superior y reparte el espacio restante (innerPadding) al contenido.
    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                actions = {
                    // Los IconButton ya miden 48 dp (objetivo táctil mínimo recomendado) aunque el icono sea de 24 dp.
                    // Cada icono es el único contenido del botón: necesita descripción para TalkBack.
                    IconButton(onClick = onRefresh) {
                        Icon(Icons.Filled.Refresh, contentDescription = stringResource(R.string.refresh))
                    }
                    IconButton(onClick = onOpenCatalog) {
                        Icon(Icons.Filled.Info, contentDescription = stringResource(R.string.open_catalog))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding).fillMaxSize()) {
            // La búsqueda se pinta siempre, también en Loading/Error/Empty: así se puede corregir un texto sin resultados.
            SearchField(query = query, onQueryChange = onQueryChange)

            Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                // when sobre una sealed interface: el compilador exige tratar los cuatro estados.
                when (state) {
                    ProductsUiState.Loading -> LoadingContent(Modifier.align(Alignment.Center))
                    is ProductsUiState.Error -> ErrorContent(
                        message = state.message,
                        onRetry = onRefresh,
                        modifier = Modifier.align(Alignment.Center),
                    )
                    ProductsUiState.Empty -> Text(
                        text = stringResource(if (query.isBlank()) R.string.no_products else R.string.no_results),
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.align(Alignment.Center).padding(dimensionResource(R.dimen.padding_normal)),
                    )
                    is ProductsUiState.Success -> ProductList(
                        products = state.products,
                        onProductClick = onProductClick,
                        onFavoriteClick = onFavoriteClick,
                        onShareClick = onShareClick,
                        onDeleteClick = onDeleteClick,
                    )
                }
            }
        }
    }
}

/** Barra de búsqueda: filtra mientras se escribe y, al pulsar "Buscar" en el teclado, lo oculta. */
@Composable
private fun SearchField(query: String, onQueryChange: (String) -> Unit) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusManager = LocalFocusManager.current

    OutlinedTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = dimensionResource(R.dimen.padding_normal)),
        placeholder = { Text(stringResource(R.string.search_hint)) },
        // Decorativo (null): el propio campo ya anuncia su texto de ayuda.
        leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
        trailingIcon = {
            if (query.isNotEmpty()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Filled.Clear, contentDescription = stringResource(R.string.clear_search))
                }
            }
        },
        singleLine = true,
        // ImeAction.Search cambia la tecla "intro" del teclado por una lupa.
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        // onSearch se ejecuta al pulsar esa tecla: se oculta el teclado y se quita el foco del campo.
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                focusManager.clearFocus()
            },
        ),
    )
}

@Composable
private fun LoadingContent(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
    ) {
        CircularProgressIndicator()
        Text(stringResource(R.string.loading))
    }
}

@Composable
private fun ErrorContent(message: String, onRetry: () -> Unit, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(dimensionResource(R.dimen.padding_normal)),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
    ) {
        Text(
            text = stringResource(R.string.error_message, message),
            color = MaterialTheme.colorScheme.error,
        )
        Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}

@Composable
private fun ProductList(
    products: List<Product>,
    onProductClick: (Int) -> Unit,
    onFavoriteClick: (Int) -> Unit,
    onShareClick: (Product) -> Unit,
    onDeleteClick: (Product) -> Unit,
) {
    val padding = dimensionResource(R.dimen.padding_normal)
    Column {
        val count = products.size
        Text(
            // pluralStringResource elige "one" u "other"; el 0 se trata aparte (ver ProductsUiState.Empty).
            text = pluralStringResource(R.plurals.found_products, count, count),
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(padding),
        )
        LazyColumn(
            contentPadding = PaddingValues(start = padding, end = padding, bottom = padding),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
        ) {
            // key = { it.id }: identifica cada elemento por su id y no por su posición en la lista.
            // Así, si la lista cambia (un filtro, un refresco, un producto que se marca como favorito o se elimina),
            // Compose sabe qué elemento es cuál: reutiliza el que ya existe, conserva su estado (menú abierto,
            // posición del swipe) y sus animaciones, y solo recompone los que han cambiado de verdad. Sin key,
            // Compose usa la posición, y al insertar o quitar un elemento reaprovecha el hueco equivocado.
            items(products, key = { it.id }) { product ->
                SwipeToDeleteBox(
                    onDelete = { onDeleteClick(product) },
                    // animateItem anima la entrada, salida y reordenación del elemento (necesita la key).
                    modifier = Modifier.animateItem(),
                ) {
                    ProductCard(
                        product = product,
                        onClick = { onProductClick(product.id) },
                        onFavoriteClick = { onFavoriteClick(product.id) },
                        onShareClick = { onShareClick(product) },
                        onDeleteClick = { onDeleteClick(product) },
                    )
                }
            }
        }
    }
}

private val previewProducts = listOf(
    Product(1, "Desk lamp", "Home", 24.90, "https://example.com/1.jpg", favorite = true),
    Product(2, "Ceramic mug", "Home", 8.50, "https://example.com/2.jpg"),
)

@Composable
private fun PreviewContent(state: ProductsUiState, query: String = "") {
    PMDMTheme {
        ProductsContent(
            state = state,
            query = query,
            snackbarHostState = remember { SnackbarHostState() },
            onQueryChange = {},
            onRefresh = {},
            onProductClick = {},
            onFavoriteClick = {},
            onShareClick = {},
            onDeleteClick = {},
            onOpenCatalog = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun ProductsSuccessPreview() = PreviewContent(ProductsUiState.Success(previewProducts))

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
private fun ProductsSuccessDarkPreview() = PreviewContent(ProductsUiState.Success(previewProducts))

@Preview(showBackground = true)
@Composable
private fun ProductsLoadingPreview() = PreviewContent(ProductsUiState.Loading)

@Preview(showBackground = true)
@Composable
private fun ProductsEmptyPreview() = PreviewContent(ProductsUiState.Empty, query = "zzz")

@Preview(showBackground = true)
@Composable
private fun ProductsErrorPreview() = PreviewContent(ProductsUiState.Error("Simulated network error"))
