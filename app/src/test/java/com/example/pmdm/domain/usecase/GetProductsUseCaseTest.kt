package com.example.pmdm.domain.usecase

import com.example.pmdm.domain.model.Product
import com.example.pmdm.testing.FakeProductRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class GetProductsUseCaseTest {

    private fun product(id: Int, name: String) =
        Product(id, name, "General", 10.0, "https://example.com/$id.jpg")

    private val repository = FakeProductRepository(
        listOf(product(1, "Desk lamp"), product(2, "Blue mug"), product(3, "Solar lamp")),
    )
    private val getProducts = GetProductsUseCase(repository)

    @Test
    fun emptyFilter_returnsAll() = runTest {
        assertEquals(3, getProducts("").first().size)
    }

    @Test
    fun blankFilter_returnsAll() = runTest {
        assertEquals(3, getProducts("   ").first().size)
    }

    @Test
    fun partialFilter_returnsMatches() = runTest {
        val names = getProducts("lam").first().map { it.name }
        assertEquals(listOf("Desk lamp", "Solar lamp"), names)
    }

    @Test
    fun filter_ignoresCase() = runTest {
        assertEquals(listOf("Blue mug"), getProducts("MUG").first().map { it.name })
    }

    @Test
    fun noMatches_returnsEmptyList() = runTest {
        assertEquals(emptyList<Product>(), getProducts("shoe").first())
    }

    @Test
    fun whenRepositoryChanges_resultIsUpdated() = runTest {
        val flow = getProducts("mug")
        assertEquals(false, flow.first().single().favorite)

        repository.toggleFavorite(2)

        assertEquals(true, flow.first().single().favorite)
    }
}
