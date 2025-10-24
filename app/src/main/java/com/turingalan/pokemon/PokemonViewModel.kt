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

class PokemonViewModel(
    private val repository: PokemonRepository = PokemonInMemoryRepository()
) : ViewModel() {

    // Estado privado mutable
    private val _uiState = MutableStateFlow<PokemonUiState>(PokemonUiState.Idle)
    // Estado público inmutable para la UI
    val uiState: StateFlow<PokemonUiState> = _uiState

    fun loadPokemons() {
        viewModelScope.launch {
            _uiState.value = PokemonUiState.Loading
            delay(1000)
            try {
                val pokemons = repository.readAll()
                _uiState.value = PokemonUiState.Success(pokemons)
            } catch (e: Exception) {
                _uiState.value = PokemonUiState.Error("Error al cargar Pokémon")
            }
        }
    }

    fun getById(id: Long): Pokemon? = repository.readOne(id)
}
