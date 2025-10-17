package com.turingalan.pokemon.data.repository

import com.turingalan.pokemon.R
import com.turingalan.pokemon.data.model.Pokemon

class PokemonInMemoryRepository(): PokemonRepository {

    val pokemonList: List<Pokemon> = listOf(
        Pokemon(
            id = 1,
            name = "Bulbasaur",
            spriteId = R.drawable.sprite_1,
            artworkId = R.drawable.artwork_1
        ),
        Pokemon(
            id = 4,
            name = "Charmander",
            spriteId = R.drawable.sprite_4,
            artworkId = R.drawable.artwork_4
            ),
        Pokemon(
            id = 7,
            name = "Squirtle",
            spriteId = R.drawable.sprite_7,
            artworkId = R.drawable.artwork_7
            ),
        Pokemon(
            id = 10,
            name = "Caterpie",
            spriteId = R.drawable.sprite_10,
            artworkId = R.drawable.artwork_10
        ),
        Pokemon(
            id = 25,
            name = "Pikachu",
            spriteId = R.drawable.sprite_25,
            artworkId = R.drawable.artwork_25
        ),
        Pokemon(
            id = 39,
            name = "JigglyPuff",
            spriteId = R.drawable.sprite_39,
            artworkId = R.drawable.artwork_39
            ),
        Pokemon(
            id = 133,
            name = "Eeve",
            spriteId = R.drawable.sprite_133,
            artworkId = R.drawable.artwork_133
        )
        ,
        Pokemon(
            id = 143,
            name = "Snorlax",
            spriteId = R.drawable.sprite_143,
            artworkId = R.drawable.artwork_143
        )

    )

    override fun readOne(id: Long): Pokemon? {
        val pokemon = pokemonList.firstOrNull() {
            p -> p.id == id
        }
        return pokemon
    }

    override fun readAll(): List<Pokemon> = this.pokemonList
}