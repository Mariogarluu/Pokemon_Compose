package com.turingalan.pokemon.ui.list

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.turingalan.pokemon.PokemonUiState
import com.turingalan.pokemon.PokemonViewModel
import com.turingalan.pokemon.data.model.Pokemon

/**
 * Pantalla principal que muestra la lista de Pokémon.
 * 
 * Esta es la "View" en el patrón MVVM:
 * - Observa el estado del ViewModel
 * - Se recompone automáticamente cuando el estado cambia
 * - Delega la lógica al ViewModel
 * 
 * @param navController Controlador de navegación para ir a detalles
 * @param viewModel ViewModel que gestiona el estado. Se obtiene automáticamente
 *                  usando viewModel() o se puede inyectar para testing
 */
@Composable
fun PokemonListScreen(
    navController: NavController,
    viewModel: PokemonViewModel = viewModel() // Obtiene o crea el ViewModel
) {
    /**
     * Observa el estado del ViewModel como State de Compose
     * 
     * - collectAsState(): Convierte StateFlow en State de Compose
     * - "by": Delegación de propiedad, permite usar uiState directamente
     * - Recomposición automática: Cuando _uiState cambia en el ViewModel,
     *   este composable se recompone automáticamente
     */
    val uiState by viewModel.uiState.collectAsState()

    /**
     * LaunchedEffect: Ejecuta código suspending cuando el composable entra en composición
     * 
     * - Unit como key: Se ejecuta solo una vez (la primera vez)
     * - Si usamos otra key, se re-ejecutaría cuando esa key cambie
     * - Perfecto para disparar cargas iniciales de datos
     * 
     * Equivalente a onCreate() o onViewCreated() en Views tradicionales
     */
    LaunchedEffect(Unit) {
        viewModel.loadPokemons() // Dispara la carga de Pokémon
    }

    /**
     * Pattern matching con when para renderizar UI según el estado
     * 
     * Sealed class garantiza exhaustividad:
     * - El compilador verifica que manejemos todos los casos
     * - No necesitamos un else branch
     * - Si añadimos un nuevo estado, el compilador nos avisará
     */
    when (uiState) {
        // Estado inicial: pantalla vacía esperando acción
        is PokemonUiState.Idle -> EmptyScreen()
        
        // Cargando: muestra indicador de progreso
        is PokemonUiState.Loading -> LoadingScreen()
        
        // Éxito: muestra la lista de Pokémon
        is PokemonUiState.Success -> {
            // Smart cast: el compilador sabe que uiState es Success aquí
            val pokemons = (uiState as PokemonUiState.Success).pokemons
            PokemonList(pokemons = pokemons) { pokemon ->
                // Callback cuando se hace click en un Pokémon
                navController.navigate("Pokemon/${pokemon.id}")
            }
        }
        
        // Error: muestra mensaje de error
        is PokemonUiState.Error -> ErrorScreen((uiState as PokemonUiState.Error).message)
    }
}

/**
 * Composable que muestra la lista scrollable de Pokémon.
 * 
 * @param pokemons Lista de Pokémon a mostrar
 * @param onClick Callback cuando se hace click en un Pokémon
 */
@Composable
fun PokemonList(pokemons: List<Pokemon>, onClick: (Pokemon) -> Unit) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        items(pokemons) { pokemon ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onClick(pokemon) }
                    .padding(4.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(id = pokemon.spriteId),
                        contentDescription = pokemon.name,
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    Text(
                        text = pokemon.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

/**
 * Composable para el estado Idle (vacío/inicial)
 */
@Composable
fun EmptyScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Nada que mostrar todavía...")
    }
}

/**
 * Composable para el estado Loading (cargando)
 * Muestra un indicador de progreso circular centrado
 */
@Composable
fun LoadingScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

/**
 * Composable para el estado Error
 * 
 * @param message Mensaje de error a mostrar al usuario
 */
@Composable
fun ErrorScreen(message: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = "Error: $message", color = MaterialTheme.colorScheme.error)
    }
}
