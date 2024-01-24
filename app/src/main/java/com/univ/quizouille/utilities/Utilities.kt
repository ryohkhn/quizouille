package com.univ.quizouille.utilities

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.navigation.NavController
import java.lang.Exception
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

fun navigateToRoute(route: String, navController: NavController) {
    navController.navigate(route) {
        popUpTo(navController.graph.startDestinationId)
        launchSingleTop = true
    }
}

fun navigateToRouteNoPopUp(route: String, navController: NavController) {
    navController.navigate(route) {
        launchSingleTop = true
    }
}

@RequiresApi(Build.VERSION_CODES.O)
fun stringToLocalDate(dateString: String): LocalDate? {
    return try {
        LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
    } catch (e: Exception) {
        null
    }
}

val frequencyUnits = mapOf(
    "heures" to TimeUnit.HOURS,
    "minutes" to TimeUnit.MINUTES,
    "secondes" to TimeUnit.SECONDS)