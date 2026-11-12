package com.example.pmdm.ui.products

import androidx.lifecycle.SavedStateHandle
import com.example.pmdm.domain.model.Product
import com.example.pmdm.domain.usecase.GetProductsUseCase
import com.example.pmdm.testing.FakeProductRepository
import java.io.IOException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductsViewModelTest {

    private val lamp = Product(1, "Desk lamp", "Home", 24.90, "https://example.com/1.jpg")
    private val mug = Product(2, "Blue mug", "Home", 8.50, "https://example.com/2.jpg")

    // viewModelScope usa Dispatchers.Main, que no existe en tests de JVM: se sustituye por uno de test.
    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    private fun createViewModel(
        repository: FakeProductRepository,
        savedState: SavedStateHandle = SavedStateHandle(),
    ) = ProductsViewModel(GetProductsUseCase(repository), repository, savedState)

    // stateIn(WhileSubscribed) solo trabaja si hay alguien recogiendo el flujo.
    private fun TestScope.keepCollecting(viewModel: ProductsViewModel) {
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }
    }

    @Test
    fun initialState_isLoading() {
        val viewModel = createViewModel(FakeProductRepository(listOf(lamp)))
        assertEquals(ProductsUiState.Loading, viewModel.uiState.value)
    }

    @Test
    fun withProducts_exposesSuccess() = runTest {
        val viewModel = createViewModel(FakeProductRepository(listOf(lamp, mug)))
        keepCollecting(viewModel)

        assertEquals(ProductsUiState.Success(listOf(lamp, mug)), viewModel.uiState.value)
    }

    @Test
    fun withoutProducts_exposesEmpty() = runTest {
        val viewModel = createViewModel(FakeProductRepository(emptyList()))
        keepCollecting(viewModel)

        assertEquals(ProductsUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun whenRefreshFails_exposesError() = runTest {
        val repository = FakeProductRepository(listOf(lamp)).apply { refreshError = IOException("boom") }
        val viewModel = createViewModel(repository)
        keepCollecting(viewModel)

        assertEquals(ProductsUiState.Error("boom"), viewModel.uiState.value)
    }

    @Test
    fun retryAfterError_recovers() = runTest {
        val repository = FakeProductRepository(listOf(lamp)).apply { refreshError = IOException("boom") }
        val viewModel = createViewModel(repository)
        keepCollecting(viewModel)

        repository.refreshError = null
        viewModel.refresh()

        assertEquals(ProductsUiState.Success(listOf(lamp)), viewModel.uiState.value)
    }

    @Test
    fun query_filtersProducts() = runTest {
        val viewModel = createViewModel(FakeProductRepository(listOf(lamp, mug)))
        keepCollecting(viewModel)

        viewModel.onQueryChange("mug")

        assertEquals("mug", viewModel.query.value)
        assertEquals(ProductsUiState.Success(listOf(mug)), viewModel.uiState.value)
    }

    @Test
    fun queryWithoutMatches_exposesEmpty() = runTest {
        val viewModel = createViewModel(FakeProductRepository(listOf(lamp, mug)))
        keepCollecting(viewModel)

        viewModel.onQueryChange("shoe")

        assertEquals(ProductsUiState.Empty, viewModel.uiState.value)
    }

    @Test
    fun query_isRestoredFromSavedState() = runTest {
        // Simula la recreación tras la muerte del proceso: el SavedStateHandle llega ya con el texto guardado.
        val restored = SavedStateHandle(mapOf("query" to "lamp"))
        val viewModel = createViewModel(FakeProductRepository(listOf(lamp, mug)), restored)
        keepCollecting(viewModel)

        assertEquals("lamp", viewModel.query.value)
        assertEquals(ProductsUiState.Success(listOf(lamp)), viewModel.uiState.value)
    }

    @Test
    fun deleteAndRestore_hidesAndShowsProduct() = runTest {
        val viewModel = createViewModel(FakeProductRepository(listOf(lamp, mug)))
        keepCollecting(viewModel)

        viewModel.deleteProduct(1)
        assertEquals(ProductsUiState.Success(listOf(mug)), viewModel.uiState.value)

        viewModel.restoreProduct(1)
        assertEquals(ProductsUiState.Success(listOf(lamp, mug)), viewModel.uiState.value)
    }

    @Test
    fun toggleFavorite_updatesProduct() = runTest {
        val viewModel = createViewModel(FakeProductRepository(listOf(lamp)))
        keepCollecting(viewModel)

        viewModel.toggleFavorite(1)

        val state = viewModel.uiState.value as ProductsUiState.Success
        assertTrue(state.products.single().favorite)
    }
}
