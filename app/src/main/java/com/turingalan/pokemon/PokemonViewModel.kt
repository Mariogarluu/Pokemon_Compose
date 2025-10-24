package com.turingalan.pokemon

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.turingalan.pokemon.data.model.Pokemon
import com.turingalan.pokemon.data.repository.PokemonInMemoryRepository
import com.turingalan.pokemon.data.repository.PokemonRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel para gestionar el estado y la lógica de negocio de la aplicación Pokémon.
 * 
 * Responsabilidades:
 * - Gestionar el estado de la UI mediante StateFlow
 * - Coordinar la obtención de datos del repository
 * - Manejar la lógica de carga y errores
 * - Sobrevivir a cambios de configuración (rotación de pantalla)
 * 
 * @param repository Repositorio para acceder a los datos de Pokémon.
 *                   Por defecto usa PokemonInMemoryRepository para facilitar testing.
 */
class PokemonViewModel(
    private val repository: PokemonRepository = PokemonInMemoryRepository()
) : ViewModel() {

    /**
     * Estado privado mutable (MutableStateFlow)
     * - Solo el ViewModel puede modificar este estado
     * - Permite cambiar el valor mediante: _uiState.value = nuevoEstado
     * - El guion bajo (_) es una convención para indicar que es privado
     */
    private val _uiState = MutableStateFlow<PokemonUiState>(PokemonUiState.Idle)
    
    /**
     * Estado público inmutable (StateFlow)
     * - La UI solo puede observar este estado, no modificarlo
     * - Patrón "backing property": expone _uiState de forma read-only
     * - Se usa collectAsState() en Compose para observar cambios
     */
    val uiState: StateFlow<PokemonUiState> = _uiState

    /**
     * Carga la lista completa de Pokémon de forma asíncrona.
     * 
     * Flujo de estados:
     * 1. Idle/Success/Error → Loading (muestra indicador de carga)
     * 2. Loading → Success (muestra lista) o Error (muestra mensaje de error)
     * 
     * - Usa viewModelScope.launch para ejecutar código asíncrono
     * - viewModelScope se cancela automáticamente cuando el ViewModel se destruye
     * - El delay(1000) simula una llamada a red (en producción sería una API)
     * - try-catch captura errores y actualiza el estado apropiadamente
     */
    fun loadPokemons() {
        // Lanza una corrutina en el scope del ViewModel
        viewModelScope.launch {
            // Paso 1: Indicar que estamos cargando datos
            _uiState.value = PokemonUiState.Loading
            
            // Simula una operación de red (espera 1 segundo)
            // En una app real, esto sería una llamada a una API
            delay(1000)
            
            // Paso 2: Intentar obtener los datos
            try {
                // Obtiene todos los Pokémon del repositorio
                val pokemons = repository.readAll()
                
                // Si tiene éxito, actualiza el estado a Success con los datos
                _uiState.value = PokemonUiState.Success(pokemons)
            } catch (e: Exception) {
                // Si ocurre algún error, actualiza el estado a Error
                // Esto evita que la app crashee y permite mostrar un mensaje al usuario
                _uiState.value = PokemonUiState.Error("Error al cargar Pokémon")
            }
        }
    }

    /**
     * Obtiene un Pokémon específico por su ID.
     * 
     * @param id Identificador único del Pokémon
     * @return El Pokémon si existe, o null si no se encuentra
     * 
     * Nota: Esta función es síncrona (no usa corrutinas) porque el repositorio
     * en memoria devuelve datos instantáneamente. En una app con base de datos
     * o API, esta función también debería ser suspending o usar Flow.
     */
    fun getById(id: Long): Pokemon? = repository.readOne(id)
}
