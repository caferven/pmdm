package com.example.pmdm.ui.products

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.dimensionResource
import com.example.pmdm.R

/**
 * Envuelve [content] para poder **deslizarlo hacia la izquierda** y eliminarlo.
 *
 * Gesto: *deslizar* (swipe). Al soltar pasado el umbral se llama a [onDelete]; deshacer lo decide
 * quien llama (aquí, con un Snackbar). Es un gesto sin alternativa táctil sencilla, por eso la tarjeta
 * también ofrece "Eliminar" en su menú contextual (accesibilidad).
 */
@Composable
fun SwipeToDeleteBox(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val state = rememberSwipeToDismissBoxState()
    // rememberUpdatedState evita que el efecto de abajo use un onDelete antiguo.
    val currentOnDelete by rememberUpdatedState(onDelete)

    // En lugar de "vetar" el gesto con un callback (deprecado), se reacciona cuando la tarjeta termina de
    // deslizarse. Solo se permite el sentido EndToStart (ver enableDismissFromStartToEnd más abajo), así que
    // el otro sentido ni siquiera existe como destino.
    LaunchedEffect(state.currentValue) {
        if (state.currentValue == SwipeToDismissBoxValue.EndToStart) currentOnDelete()
    }

    SwipeToDismissBox(
        state = state,
        modifier = modifier,
        enableDismissFromStartToEnd = false, // solo se permite deslizar de derecha a izquierda
        backgroundContent = {
            // Lo que se ve "detrás" de la tarjeta mientras se desliza.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(MaterialTheme.shapes.medium)
                    .background(MaterialTheme.colorScheme.errorContainer)
                    .padding(end = dimensionResource(R.dimen.padding_normal)),
                contentAlignment = Alignment.CenterEnd,
            ) {
                Icon(
                    Icons.Filled.Delete,
                    // null a propósito: es decorativo, la acción ya se anuncia en el menú "Eliminar".
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                )
            }
        },
    ) {
        content()
    }
}
