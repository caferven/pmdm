package com.example.pmdm.ui.catalog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Badge
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.semantics.Role
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import com.example.pmdm.R
import com.example.pmdm.ui.theme.PMDMTheme
import kotlinx.coroutines.launch

/**
 * Catálogo de componentes **con estado**: obtiene el ViewModel (que gestiona el interruptor de error de red)
 * y se lo pasa a [ComponentCatalogContent] como un dato y una lambda.
 *
 * @param onBack se llama al pulsar la flecha de la barra superior.
 */
@Composable
fun ComponentCatalogScreen(onBack: () -> Unit, modifier: Modifier = Modifier) {
    val viewModel: ComponentCatalogViewModel = viewModel(factory = ComponentCatalogViewModel.Factory)
    val forceNetworkError by viewModel.forceNetworkError.collectAsStateWithLifecycle()

    ComponentCatalogContent(
        forceNetworkError = forceNetworkError,
        onForceNetworkErrorChange = viewModel::setForceNetworkError,
        onBack = onBack,
        modifier = modifier,
    )
}

/**
 * Ejemplo de los layouts y controles más usados. No forma parte del flujo de compra.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ComponentCatalogContent(
    forceNetworkError: Boolean,
    onForceNetworkErrorChange: (Boolean) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // El SnackbarHostState es quien gestiona la cola de mensajes; el SnackbarHost del Scaffold los dibuja.
    // Se recuerda con remember para que sobreviva a las recomposiciones.
    val snackbarHostState = remember { SnackbarHostState() }
    // showSnackbar es una función suspend (espera hasta que el mensaje desaparece),
    // así que hay que llamarla desde una corrutina.
    val scope = rememberCoroutineScope()
    // Estado puramente visual: rememberSaveable lo conserva al rotar la pantalla (remember lo perdería).
    var showDialog by rememberSaveable { mutableStateOf(false) }

    // Los textos se leen aquí, en el Composable: stringResource no se puede llamar dentro de la corrutina.
    val deletedMessage = stringResource(R.string.snackbar_item_deleted)
    val undoLabel = stringResource(R.string.snackbar_undo)
    val undoneMessage = stringResource(R.string.snackbar_undone)

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.open_catalog)) },
                navigationIcon = {
                    // IconButton ya mide 48 dp (objetivo táctil mínimo recomendado).
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
            )
        },
        // Slot donde el Scaffold coloca los Snackbars (abajo, por encima del contenido).
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(dimensionResource(R.dimen.padding_normal)),
            verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_normal)),
        ) {
            LayoutsSection()
            HorizontalDivider()
            ButtonsSection()
            HorizontalDivider()
            TextFieldsSection()
            HorizontalDivider()
            CardListItemSection()
            HorizontalDivider()
            DialogSnackbarSection(onDeleteClick = { showDialog = true })
            HorizontalDivider()
            SimulationSection(
                forceNetworkError = forceNetworkError,
                onForceNetworkErrorChange = onForceNetworkErrorChange,
            )
        }
    }

    // El diálogo se declara fuera del Scaffold: se dibuja encima de toda la pantalla.
    if (showDialog) {
        AlertDialog(
            // Se llama al tocar fuera del diálogo o pulsar "atrás".
            onDismissRequest = { showDialog = false },
            title = { Text(stringResource(R.string.dialog_title)) },
            text = { Text(stringResource(R.string.dialog_text)) },
            confirmButton = {
                TextButton(onClick = {
                    showDialog = false
                    scope.launch {
                        // showSnackbar devuelve cómo terminó: acción pulsada o descartado.
                        val result = snackbarHostState.showSnackbar(
                            message = deletedMessage,
                            actionLabel = undoLabel,
                            duration = SnackbarDuration.Short,
                        )
                        if (result == SnackbarResult.ActionPerformed) {
                            snackbarHostState.showSnackbar(message = undoneMessage)
                        }
                    }
                }) { Text(stringResource(R.string.dialog_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) { Text(stringResource(R.string.dialog_cancel)) }
            },
        )
    }
}

/** Título de cada sección del catálogo. */
@Composable
private fun SectionTitle(textRes: Int) {
    Text(text = stringResource(textRes), style = MaterialTheme.typography.titleLarge)
}

// LAYOUTS
@Composable
private fun LayoutsSection() {
    val small = dimensionResource(R.dimen.padding_small)

    Column(verticalArrangement = Arrangement.spacedBy(small)) {
        SectionTitle(R.string.section_layouts)

        // Column: apila los hijos en vertical. spacedBy pone el mismo hueco entre ellos,
        // sin tener que añadir padding a cada elemento.
        Text(stringResource(R.string.layout_column_title), style = MaterialTheme.typography.labelLarge)
        Column(verticalArrangement = Arrangement.spacedBy(small)) {
            repeat(3) { index ->
                Surface(color = MaterialTheme.colorScheme.secondaryContainer, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.sample_item, index + 1), Modifier.padding(small))
                }
            }
        }

        // Row: coloca los hijos en horizontal. SpaceEvenly reparte el espacio sobrante a partes iguales
        // y CenterVertically alinea al centro los elementos de distinta altura.
        Text(stringResource(R.string.layout_row_title), style = MaterialTheme.typography.labelLarge)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(Icons.Filled.ShoppingCart, contentDescription = null)
            Text(stringResource(R.string.sample_item, 1), style = MaterialTheme.typography.headlineSmall)
            Text(stringResource(R.string.sample_item, 2))
        }

        // Box: apila los hijos unos encima de otros. Cada hijo se alinea con Modifier.align dentro del Box.
        Text(stringResource(R.string.layout_box_title), style = MaterialTheme.typography.labelLarge)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(dimensionResource(R.dimen.demo_box_height))
                .background(MaterialTheme.colorScheme.primaryContainer),
        ) {
            Text(stringResource(R.string.sample_item, 1), Modifier.align(Alignment.Center))
            Badge(Modifier.align(Alignment.TopEnd).padding(small)) { Text(stringResource(R.string.badge_new)) }
        }
    }
}

// BOTONES
@Composable
private fun ButtonsSection() {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
        SectionTitle(R.string.section_buttons)
        // De más a menos énfasis: Button (acción principal), Elevated y Outlined (secundarias),
        // TextButton (acción de menor importancia, sin borde ni fondo).
        Button(onClick = {}) { Text(stringResource(R.string.button_filled)) }
        ElevatedButton(onClick = {}) { Text(stringResource(R.string.button_elevated)) }
        OutlinedButton(onClick = {}) { Text(stringResource(R.string.button_outlined)) }
        TextButton(onClick = {}) { Text(stringResource(R.string.button_text)) }
    }
}

// CAMPOS DE TEXTO
@Composable
private fun TextFieldsSection() {
    // El texto escrito es estado: remember lo conserva entre recomposiciones y 'by' permite leerlo y
    // asignarlo como una variable normal. Cada vez que cambia, Compose redibuja el campo.
    // rememberSaveable además lo conserva al rotar la pantalla o si Android recrea la Activity.
    var email by rememberSaveable { mutableStateOf("") }
    // La contraseña usa remember a propósito: rememberSaveable la escribiría en el estado guardado del sistema
    // (un Bundle en memoria que puede acabar en disco), y los datos sensibles no deben guardarse ahí.
    var password by remember { mutableStateOf("") }

    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
        SectionTitle(R.string.section_text_fields)
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text(stringResource(R.string.email_label)) },
            // KeyboardType.Email muestra un teclado con la @ a mano.
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text(stringResource(R.string.password_label)) },
            // PasswordVisualTransformation dibuja puntos en lugar del texto; el valor real no cambia.
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

// 4. CARD CON LISTITEM
@Composable
private fun CardListItemSection() {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
        SectionTitle(R.string.section_card_list_item)
        Card(modifier = Modifier.fillMaxWidth()) {
            // ListItem es una fila estándar de Material 3 con huecos para titular, texto secundario e iconos.
            ListItem(
                headlineContent = { Text(stringResource(R.string.list_item_headline)) },
                supportingContent = { Text(stringResource(R.string.list_item_supporting)) },
                leadingContent = {
                    Icon(
                        Icons.Filled.ShoppingCart,
                        contentDescription = null, // decorativo: el texto ya dice lo importante
                        modifier = Modifier.size(dimensionResource(R.dimen.icon_size)),
                    )
                },
                // Transparente para que se vea el color de la Card y no el de la propia fila.
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
            )
        }
    }
}

// ALERTDIALOG Y SNACKBAR
@Composable
private fun DialogSnackbarSection(onDeleteClick: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
        SectionTitle(R.string.section_dialog_snackbar)
        // Al pulsar se abre el AlertDialog de confirmación; si el usuario confirma, aparece el Snackbar con "Deshacer".
        Button(onClick = onDeleteClick) { Text(stringResource(R.string.delete_item)) }
    }
}

// 6. SIMULACIÓN
@Composable
private fun SimulationSection(
    forceNetworkError: Boolean,
    onForceNetworkErrorChange: (Boolean) -> Unit,
) {
    Column(verticalArrangement = Arrangement.spacedBy(dimensionResource(R.dimen.padding_small))) {
        SectionTitle(R.string.section_simulation)
        // toggleable en la fila entera: el área táctil es todo el ancho (mucho más de 48 dp) y TalkBack lo
        // anuncia como un único interruptor. Por eso el Switch no gestiona el clic (onCheckedChange = null).
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .toggleable(
                    value = forceNetworkError,
                    onValueChange = onForceNetworkErrorChange,
                    role = Role.Switch,
                )
                .padding(vertical = dimensionResource(R.dimen.padding_small)),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(stringResource(R.string.force_network_error))
            Switch(checked = forceNetworkError, onCheckedChange = null)
        }
        Text(
            text = stringResource(R.string.force_network_error_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Preview(showBackground = true, heightDp = 1600)
@Composable
private fun ComponentCatalogContentPreview() {
    PMDMTheme {
        ComponentCatalogContent(forceNetworkError = false, onForceNetworkErrorChange = {}, onBack = {})
    }
}
