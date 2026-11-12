package com.example.pmdm.ui.products

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pmdm.MiTiendaApp
import com.example.pmdm.domain.model.Product
import com.example.pmdm.domain.repository.ProductRepository
import com.example.pmdm.domain.usecase.GetProductsUseCase
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Estado de la pantalla de productos. Es una `sealed interface`: solo puede ser uno de estos cuatro
 * casos, y el `when` de la UI obliga a tratarlos todos (si añades uno nuevo, no compila hasta que lo gestiones).
 */
sealed interface ProductsUiState {
    /** Se están pidiendo los datos. */
    data object Loading : ProductsUiState

    /** Hay productos que mostrar. */
    data class Success(val products: List<Product>) : ProductsUiState

    /** La carga terminó bien pero no hay ningún producto (o ninguno coincide con la búsqueda). */
    data object Empty : ProductsUiState

    /** La carga falló; [message] describe el problema. */
    data class Error(val message: String) : ProductsUiState
}

/**
 * ViewModel de la pantalla de productos. Sobrevive a rotaciones y mantiene el estado de la UI.
 *
 * @param savedStateHandle guarda el texto de búsqueda. A diferencia de los campos del ViewModel, **sobrevive
 * también a la muerte del proceso** (cuando Android cierra la app en segundo plano para liberar memoria).
 */
class ProductsViewModel(
    getProducts: GetProductsUseCase,
    private val repository: ProductRepository,
    private val savedStateHandle: SavedStateHandle,
) : ViewModel() {

    // Estado de carga/error separado de los datos: los datos vienen del repositorio (Flow)
    // y esto lo controla el propio ViewModel.
    private val loading = MutableStateFlow(false)
    private val error = MutableStateFlow<String?>(null)

    /**
     * Texto de la barra de búsqueda. Se lee del [SavedStateHandle]: cada vez que se escribe en él, Android
     * lo guarda y, si el proceso muere, lo devuelve al recrear la pantalla.
     */
    val query: StateFlow<String> = savedStateHandle.getStateFlow(QUERY_KEY, "")

    /** Estado observable por la UI. Combina los productos (ya filtrados) con los indicadores de carga y error. */
    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<ProductsUiState> =
        // flatMapLatest: cada vez que cambia el texto se cancela la consulta anterior y se empieza otra.
        combine(query.flatMapLatest { getProducts(it) }, loading, error) { products, loading, error ->
            when {
                error != null -> ProductsUiState.Error(error)
                loading -> ProductsUiState.Loading
                products.isEmpty() -> ProductsUiState.Empty
                else -> ProductsUiState.Success(products)
            }
        }.stateIn(
            scope = viewModelScope,
            // Se deja de escuchar 5 s después de que la UI desaparezca: así una rotación
            // de pantalla no reinicia el flujo.
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProductsUiState.Loading,
        )

    init {
        refresh()
    }

    /** Guarda el nuevo texto de búsqueda; la lista se vuelve a filtrar sola. */
    fun onQueryChange(newQuery: String) {
        savedStateHandle[QUERY_KEY] = newQuery
    }

    /** Pide los productos de nuevo mostrando el indicador de carga y capturando errores. */
    fun refresh() {
        viewModelScope.launch {
            loading.value = true
            error.value = null
            try {
                repository.refresh()
            } catch (e: Exception) {
                // El texto de la excepción no se traduce: es información técnica del origen.
                error.value = e.message ?: e.javaClass.simpleName
            } finally {
                loading.value = false
            }
        }
    }

    /** Marca o desmarca como favorito el producto [id]. */
    fun toggleFavorite(id: Int) {
        viewModelScope.launch { repository.toggleFavorite(id) }
    }

    /** Elimina el producto [id] de la lista (se puede deshacer con [restoreProduct]). */
    fun deleteProduct(id: Int) {
        viewModelScope.launch { repository.delete(id) }
    }

    /** Deshace el borrado del producto [id]. */
    fun restoreProduct(id: Int) {
        viewModelScope.launch { repository.restore(id) }
    }

    companion object {
        private const val QUERY_KEY = "query"

        /** Fábrica que obtiene las dependencias del [com.example.pmdm.di.AppContainer]. */
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as MiTiendaApp).container
                ProductsViewModel(
                    container.getProductsUseCase,
                    container.productRepository,
                    createSavedStateHandle(),
                )
            }
        }
    }
}
