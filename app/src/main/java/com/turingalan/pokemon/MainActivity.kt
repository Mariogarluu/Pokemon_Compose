package com.turingalan.pokemon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.turingalan.pokemon.ui.detail.PokemonDetailScreen
import com.turingalan.pokemon.ui.list.PokemonListScreen
import com.turingalan.pokemon.ui.theme.PokemonTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PokemonTheme {
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "PokemonList",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("PokemonList") {
                            PokemonListScreen(navController = navController)
                        }
                        composable(
                            route = "Pokemon/{PokemonId}",
                            arguments = listOf(navArgument("PokemonId") { type = NavType.LongType })
                        ) { backStackEntry ->
                            val pokemonId = backStackEntry.arguments?.getLong("PokemonId")
                            if (pokemonId != null) {
                                PokemonDetailScreen(PokemonId = pokemonId, navController = navController)
                            }
                        }
                    }
                }
            }
        }
    }
}
