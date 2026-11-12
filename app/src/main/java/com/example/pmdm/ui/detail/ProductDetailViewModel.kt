package com.example.pmdm.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.pmdm.MiTiendaApp
import com.example.pmdm.domain.model.Product
import com.example.pmdm.domain.usecase.GetProductsUseCase
import com.example.pmdm.ui.navigation.Routes
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Estado de la pantalla de detalle.
 *
 * @property product el producto, o `null` si no existe ningún producto con ese `id`.
 * @property loading `true` hasta que llega la primera lista (evita parpadear "no encontrado").
 */
data class ProductDetailUiState(
    val product: Product? = null,
    val loading: Boolean = true,
)

/**
 * ViewModel del detalle: busca entre los productos el que tiene el [productId] recibido.
 */
class ProductDetailViewModel(
    private val productId: Int,
    getProducts: GetProductsUseCase,
) : ViewModel() {

    /** Estado observable por la UI; se actualiza si cambia el producto (por ejemplo, un favorito). */
    val uiState: StateFlow<ProductDetailUiState> =
        getProducts(filter = "")
            .map { products -> ProductDetailUiState(products.find { it.id == productId }, loading = false) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProductDetailUiState())

    companion object {
        /** Fábrica: el id llega en el SavedStateHandle porque Navigation lo guarda ahí. */
        val Factory = viewModelFactory {
            initializer {
                val container = (this[APPLICATION_KEY] as MiTiendaApp).container
                val productId: Int = checkNotNull(createSavedStateHandle()[Routes.PRODUCT_ID_ARG])
                ProductDetailViewModel(productId, container.getProductsUseCase)
            }
        }
    }
}
