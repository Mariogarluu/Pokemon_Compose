package com.turingalan.pokemon

import com.turingalan.pokemon.data.model.Pokemon

sealed class PokemonUiState {
    object Idle : PokemonUiState()
    object Loading : PokemonUiState()
    data class Success(val pokemons: List<Pokemon>) : PokemonUiState()
    data class Error(val message: String) : PokemonUiState()
}
