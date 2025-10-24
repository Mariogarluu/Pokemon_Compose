package com.turingalan.pokemon

import com.turingalan.pokemon.data.model.Pokemon

/**
 * Representa todos los posibles estados de la UI para la pantalla de Pokémon.
 * 
 * Sealed class: Solo puede tener subclases definidas en este mismo archivo.
 * 
 * Ventajas:
 * - Type-safe: El compilador conoce todos los estados posibles
 * - Exhaustividad: En un "when", el compilador te obliga a manejar todos los casos
 * - Sin estados inconsistentes: Solo puede estar en UN estado a la vez
 * 
 * Estados mutuamente excluyentes:
 * - No puedes estar en Loading y Success simultáneamente
 * - No puedes estar en Loading y Error al mismo tiempo
 * - Siempre hay un estado claro y único
 */
sealed class PokemonUiState {
    
    /**
     * Estado Idle (Inactivo/Inicial)
     * 
     * - Estado inicial antes de cargar datos
     * - Se usa cuando aún no se ha realizado ninguna acción
     * - object: Es un singleton, solo existe una instancia
     * - No necesita datos adicionales
     * 
     * Cuándo se usa:
     * - Al crear el ViewModel por primera vez
     * - Antes de que el usuario dispare la carga de datos
     */
    object Idle : PokemonUiState()
    
    /**
     * Estado Loading (Cargando)
     * 
     * - Indica que se están cargando los datos
     * - La UI debería mostrar un indicador de progreso (spinner)
     * - object: No necesita datos adicionales, solo indica "estoy cargando"
     * 
     * Cuándo se usa:
     * - Justo al iniciar loadPokemons()
     * - Durante el delay(1000) o llamada a API
     * - Hasta que se reciban datos o un error
     */
    object Loading : PokemonUiState()
    
    /**
     * Estado Success (Éxito)
     * 
     * - Los datos se cargaron correctamente
     * - Contiene la lista de Pokémon para mostrar
     * - data class: Puede tener múltiples instancias con diferentes datos
     * 
     * @param pokemons Lista de Pokémon obtenida del repositorio
     * 
     * Cuándo se usa:
     * - Después de cargar exitosamente desde el repository
     * - La UI muestra la lista de Pokémon
     */
    data class Success(val pokemons: List<Pokemon>) : PokemonUiState()
    
    /**
     * Estado Error (Error)
     * 
     * - Ocurrió un error al cargar los datos
     * - Contiene el mensaje de error para mostrar al usuario
     * - data class: Cada error puede tener un mensaje diferente
     * 
     * @param message Descripción del error para mostrar al usuario
     * 
     * Cuándo se usa:
     * - Cuando repository.readAll() lanza una excepción
     * - Cuando falla una llamada a API
     * - Cuando no hay conexión a internet
     * - La UI muestra un mensaje de error y opcionalmente un botón para reintentar
     */
    data class Error(val message: String) : PokemonUiState()
}
