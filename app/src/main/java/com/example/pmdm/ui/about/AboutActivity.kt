package com.example.pmdm.ui.about

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import com.example.pmdm.BuildConfig
import com.example.pmdm.R
import com.example.pmdm.ui.theme.PMDMTheme

/**
 * Pantalla «Acerca de»: sirve de destino de un **intent explícito**.
 *
 * Es una Activity aparte (no un destino del NavHost) a propósito: así se ve cómo Android arranca un
 * componente de la propia app nombrando su clase. Se declara en el manifest; sin eso, `startActivity`
 * fallaría con `ActivityNotFoundException`.
 */
class AboutActivity : ComponentActivity() {

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Los datos que viajan en un Intent se leen de sus «extras» (pares clave-valor). Si quien la lanzó
        // no mandó nada, getStringExtra devuelve null.
        val source = intent.getStringExtra(EXTRA_SOURCE)

        setContent {
            PMDMTheme {
                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = { Text(stringResource(R.string.about_title)) },
                            navigationIcon = {
                                // finish() cierra esta Activity y Android vuelve a la anterior de la pila.
                                IconButton(onClick = { finish() }) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.ArrowBack,
                                        contentDescription = stringResource(R.string.back),
                                    )
                                }
                            },
                        )
                    },
                ) { innerPadding ->
                    Column(
                        modifier = Modifier.padding(innerPadding).padding(dimensionResource(R.dimen.padding_normal)),
                        verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small)),
                    ) {
                        Text(stringResource(R.string.app_name), style = MaterialTheme.typography.headlineMedium)
                        Text(stringResource(R.string.about_version, BuildConfig.VERSION_NAME))
                        if (source != null) Text(stringResource(R.string.about_opened_from, source))
                    }
                }
            }
        }
    }

    companion object {
        private const val EXTRA_SOURCE = "com.example.pmdm.extra.SOURCE"

        /**
         * Crea el **intent explícito** que abre esta pantalla.
         *
         * `Intent(context, AboutActivity::class.java)` indica el componente exacto que debe responder.
         * Mantener esta función junto a la Activity hace que quien la lanza no necesite conocer la clave del extra.
         *
         * @param source texto que se mostrará como «abierta desde...».
         */
        fun createIntent(context: Context, source: String): Intent =
            Intent(context, AboutActivity::class.java).putExtra(EXTRA_SOURCE, source)
    }
}
