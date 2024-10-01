package com.example.reservationapp.ui

import LoginScreen
import ProviderScheduleScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.reservationapp.ui.feature.availableProviders.ProviderListScreen
import com.example.reservationapp.ui.feature.clientReservation.ClientReservationScreen
import kotlinx.serialization.Serializable

@Serializable
object Login

@Serializable
data class ProviderSchedule(val providerId: Int)

@Serializable
data class ProviderList(val clientId: Int)

@Serializable
data class ClientReservation(val clientId: Int, val providerId: Int)

@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Login) {
        composable<Login> {
            LoginScreen(
                onNavigateToProviderFlow = {
                    navController.navigate(ProviderSchedule(it))
                },
                onNavigateToClientFlow = {
                    navController.navigate(ProviderList(it))
                })
        }

        composable<ProviderSchedule> {
            ProviderScheduleScreen()
        }

        composable<ProviderList> {
            val providerList = it.toRoute<ProviderList>()
            ProviderListScreen(onProviderSelected = {
                navController.navigate(ClientReservation(providerList.clientId, it))
            })
        }

        composable<ClientReservation> {
            ClientReservationScreen(onNavigateBack = {
                navController.popBackStack()
            })
        }
    }
}