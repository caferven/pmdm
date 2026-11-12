package com.example.pmdm.ui.detail

import com.example.pmdm.domain.model.Product
import com.example.pmdm.domain.usecase.GetProductsUseCase
import com.example.pmdm.testing.FakeProductRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProductDetailViewModelTest {

    private val repository = FakeProductRepository(
        listOf(
            Product(1, "Desk lamp", "Home", 24.90, "https://example.com/1.jpg"),
            Product(2, "Blue mug", "Home", 8.50, "https://example.com/2.jpg"),
        ),
    )

    // viewModelScope usa Dispatchers.Main, que no existe en tests de JVM: se sustituye por uno de test.
    @Before
    fun setUp() = Dispatchers.setMain(UnconfinedTestDispatcher())

    @After
    fun tearDown() = Dispatchers.resetMain()

    @Test
    fun existingId_exposesProduct() = runTest {
        val viewModel = ProductDetailViewModel(2, GetProductsUseCase(repository))
        // stateIn(WhileSubscribed) solo trabaja si hay alguien recogiendo el flujo.
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        val state = viewModel.uiState.value
        assertEquals("Blue mug", state.product?.name)
        assertEquals(false, state.loading)
    }

    @Test
    fun unknownId_exposesNoProduct() = runTest {
        val viewModel = ProductDetailViewModel(99, GetProductsUseCase(repository))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        assertNull(viewModel.uiState.value.product)
        assertEquals(false, viewModel.uiState.value.loading)
    }

    @Test
    fun whenFavoriteChanges_stateIsUpdated() = runTest {
        val viewModel = ProductDetailViewModel(1, GetProductsUseCase(repository))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect {} }

        repository.toggleFavorite(1)

        assertEquals(true, viewModel.uiState.value.product?.favorite)
    }
}
