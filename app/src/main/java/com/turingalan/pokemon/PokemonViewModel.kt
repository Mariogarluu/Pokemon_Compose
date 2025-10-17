package com.turingalan.pokemon

import androidx.lifecycle.ViewModel
import com.turingalan.pokemon.data.model.Pokemon
import com.turingalan.pokemon.data.repository.PokemonInMemoryRepository
import com.turingalan.pokemon.data.repository.PokemonRepository

class PokemonViewModel : ViewModel() {
    private val repository: PokemonRepository = PokemonInMemoryRepository()

    fun getAll(): List<Pokemon> = repository.readAll()

    fun getById(id: Long): Pokemon? = repository.readOne(id)
}
