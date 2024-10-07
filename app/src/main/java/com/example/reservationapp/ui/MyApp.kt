package com.example.reservationapp.ui

import LoginScreen
import ProviderScheduleDetailScreen
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.reservationapp.data.model.ReserveBlockLength
import com.example.reservationapp.ui.feature.availableProviders.ProviderListScreen
import com.example.reservationapp.ui.feature.clientReservation.ClientReservationScreen
import com.example.reservationapp.ui.feature.clientReservation.ClientSideProviderScheduleScreen
import com.example.reservationapp.ui.feature.clientReservation.ProviderAvailableTimeSlotsScreen
import com.example.reservationapp.ui.feature.providerSchedule.ProviderSchedulingScreen
import kotlinx.serialization.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Serializable
object Login

@Serializable
data class ProviderScheduleDetail(val providerId: Int)

@Serializable
data class ProviderScheduling(val providerId: Int)

@Serializable
data class ProviderList(val clientId: Int)

@Serializable
data class ClientReservation(
    val clientId: Int,
    val providerId: Int,
    val reservationId: Int? = null,
    val createAt: String? = null,
)

@Serializable
data class ClientSideProviderSchedule(val providerId: Int)

@Serializable
data class ProviderAvailableTimeSlots(
    val providerId: Int,
    val date: String,
    val length: Int,
)

@Composable
fun MyApp() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Login) {
        composable<Login> {
            LoginScreen(
                onNavigateToProviderFlow = {
                    navController.navigate(ProviderScheduleDetail(it))
                },
                onNavigateToClientFlow = {
                    navController.navigate(ProviderList(it))
                })
        }

        composable<ProviderScheduleDetail> {
            ProviderScheduleDetailScreen(
                navigateToScheduling = {
                    navController.navigate(ProviderScheduling(it))
                }
            )
        }

        composable<ProviderScheduling> {
            ProviderSchedulingScreen(
                navigateToScheduleDetail = {
                    navController.navigate(ProviderScheduleDetail(it))
                }
            )
        }

        composable<ProviderList> {
            val providerList = it.toRoute<ProviderList>()
            ProviderListScreen(
                clientId = providerList.clientId,
                onNavigateToReservation = { clientId, providerId ->
                    navController.navigate(ClientReservation(clientId, providerId))
                })
        }

        composable<ClientReservation> {
            val clientReservation = it.toRoute<ClientReservation>()
            ClientReservationScreen(onNavigateBack = {
                navController.navigate(ProviderList(clientReservation.clientId)) {
                    popUpTo<ProviderList> {
                        inclusive = true
                    }
                }
            }, onNavigateToScheduleDetail = {
                navController.navigate(ClientSideProviderSchedule(it)) {
                    popUpTo<ClientReservation> {
                        inclusive = true
                    }
                }
            })
        }

        composable<ClientSideProviderSchedule> {
            ClientSideProviderScheduleScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToAvailableTimeSlots = { providerId, date, length ->
                    navController.navigate(
                        ProviderAvailableTimeSlots(
                            providerId = providerId,
                            date = date.toDateRouteArgument(),
                            length = length.toRouteArgument()
                        )
                    )
                }
            )
        }

        composable<ProviderAvailableTimeSlots> {
            ProviderAvailableTimeSlotsScreen(
                onNavigateBack = {
                    navController.popBackStack()
                },
                onNavigateToReservation = { reservationId, reservation ->
                    navController.navigate(
                        ClientReservation(
                            providerId = reservation.providerId,
                            clientId = reservation.userId,
                            reservationId = reservationId,
                            createAt = reservation.createdAt.convertTimeRouteArgument()
                        )
                    )
                }
            )
        }
    }
}

fun String.fromDateRouteArgument(): LocalDate {
    return LocalDate.parse(this, DateTimeFormatter.ISO_LOCAL_DATE)
}

fun LocalDate.toDateRouteArgument(): String {
    return this.format(DateTimeFormatter.ISO_LOCAL_DATE)
}

fun String.fromTimeRouteArgument(): Instant {
    return Instant.parse(this)
}

fun Instant.convertTimeRouteArgument(): String {
    return this.toString()
}

fun Int.fromBlockLengthRouteArgument(): ReserveBlockLength {
    return ReserveBlockLength(this)
}

fun ReserveBlockLength.toRouteArgument(): Int {
    return this.lengthInMinute
}



