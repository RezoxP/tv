package com.example

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import kotlinx.serialization.Serializable
import java.net.URLDecoder
import java.net.URLEncoder

@Serializable
object Home

@Serializable
data class Player(val url: String)

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Home) {
        composable<Home> {
            MainScreen(
                onNavigateToPlayer = { url -> 
                    val encodedUrl = URLEncoder.encode(url, "UTF-8")
                    navController.navigate(Player(encodedUrl))
                }
            )
        }
        
        composable<Player> { backStackEntry ->
            val navArg = backStackEntry.toRoute<Player>()
            val decodedUrl = URLDecoder.decode(navArg.url, "UTF-8")
            PlayerScreen(videoUrl = decodedUrl)
        }
    }
}
