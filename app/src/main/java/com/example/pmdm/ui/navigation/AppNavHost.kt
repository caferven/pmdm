package com.example.pmdm.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.pmdm.ui.catalog.ComponentCatalogScreen
import com.example.pmdm.ui.detail.ProductDetailScreen
import com.example.pmdm.ui.products.ProductsScreen

/**
 * Grafo de navegación de la app: define qué pantalla corresponde a cada ruta.
 *
 * Las pantallas no conocen el NavController: reciben funciones (`onProductClick`, `onBack`...)
 * y este es el único sitio que decide a dónde se navega. Así cada pantalla se puede reutilizar
 * y previsualizar sin necesitar navegación.
 */
@Composable
fun AppNavHost(modifier: Modifier = Modifier) {
    // El NavController guarda la pila de pantallas (back stack). rememberNavController lo conserva
    // entre recomposiciones y rotaciones.
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.PRODUCTS, // primera pantalla al abrir la app
        modifier = modifier,
    ) {
        // Destino 1: lista de productos.
        composable(Routes.PRODUCTS) {
            ProductsScreen(
                onProductClick = { id -> navController.navigate(Routes.productDetail(id)) },
                onOpenCatalog = { navController.navigate(Routes.CATALOG) },
            )
        }

        // Destino 2: detalle. La ruta lleva un argumento {id}; se declara su tipo para que
        // Navigation lo convierta a Int y lo deje en el SavedStateHandle del ViewModel.
        composable(
            route = Routes.PRODUCT_DETAIL,
            arguments = listOf(navArgument(Routes.PRODUCT_ID_ARG) { type = NavType.IntType }),
        ) {
            ProductDetailScreen(onBack = { navController.popBackStack() })
        }

        // Destino 3: catálogo de componentes (no necesita argumentos).
        composable(Routes.CATALOG) {
            ComponentCatalogScreen(onBack = { navController.popBackStack() })
        }
    }
}
