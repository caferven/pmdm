package com.example.pmdm.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Test del DAO contra una base de datos **en memoria**: Room es real (SQLite incluido), pero nada se escribe
 * en disco y cada test empieza con la tabla vacía. Necesita un dispositivo o emulador.
 */
@RunWith(AndroidJUnit4::class)
class ProductDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ProductDao

    private val lamp = ProductEntity(1, "Desk lamp", "Home", 24.90, "url1", favorite = false, deleted = false)
    private val mug = ProductEntity(2, "Ceramic mug", "Home", 8.50, "url2", favorite = false, deleted = false)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).build()
        dao = database.productDao()
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun upsert_insertsAndUpdates() = runTest {
        dao.upsertAll(listOf(lamp, mug))
        dao.upsertAll(listOf(lamp.copy(price = 20.0)))

        val saved = dao.observeVisible().first()
        assertEquals(listOf(20.0, 8.50), saved.map { it.price })
    }

    @Test
    fun toggleFavorite_flipsTheFlag() = runTest {
        dao.upsertAll(listOf(lamp))

        dao.toggleFavorite(1)
        assertTrue(dao.observeVisible().first().single().favorite)

        dao.toggleFavorite(1)
        assertEquals(false, dao.observeVisible().first().single().favorite)
    }

    @Test
    fun setDeleted_hidesAndRestores() = runTest {
        dao.upsertAll(listOf(lamp, mug))

        dao.setDeleted(1, deleted = true)
        assertEquals(listOf(2), dao.observeVisible().first().map { it.id })
        // La fila sigue en la tabla: por eso «Deshacer» funciona.
        assertEquals(2, dao.getAll().size)

        dao.setDeleted(1, deleted = false)
        assertEquals(listOf(1, 2), dao.observeVisible().first().map { it.id })
    }
}
