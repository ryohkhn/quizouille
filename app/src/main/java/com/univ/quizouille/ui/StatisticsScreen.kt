package com.univ.quizouille.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.univ.quizouille.ui.components.TitleWithContentRow
import com.univ.quizouille.utilities.navigateToRouteNoPopUp
import com.univ.quizouille.utilities.stringToLocalDate
import com.univ.quizouille.viewmodel.GameViewModel
import com.univ.quizouille.viewmodel.SettingsViewModel
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/**
 * Composable affichant des Cards pour chaque jeu de question, et une pour tous les jeux de questions
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun StatisticsScreen(gameViewModel: GameViewModel, settingsViewModel: SettingsViewModel, navController: NavHostController) {
    val questionsSet by gameViewModel.allQuestionSetsFlow.collectAsState(listOf())
    val policeTitleSize by settingsViewModel.policeTitleSizeFlow.collectAsState(initial = 20)
    val policeSize by settingsViewModel.policeSizeFlow.collectAsState(initial = 16)
    var selectedSetId by remember { mutableStateOf<Int?>(null) }
    var showNextButton by remember { mutableStateOf<Boolean?>(null) }

    Column {
        TitleWithContentRow(title = "Statistiques", fontSize = policeTitleSize, fontWeight = FontWeight.Bold)
        if (questionsSet.isEmpty()) {
            TitleWithContentRow(title = "Aucun jeu de questions disponible actuellement", fontSize = policeSize)
        }
        else {
            Row {
                ECard(text = "Tous les jeux", fontSize = policeSize, modifier = Modifier.clickable {
                    showNextButton = true
                })
            }
            LazyColumn {
                items(questionsSet) { questionSet ->
                    ECard(text = questionSet.name, fontSize = policeSize, modifier = Modifier.clickable {
                        selectedSetId = questionSet.setId
                    })
                }
            }
        }
    }

    // Les LaunchedEffect détectent la séléction d'un jeu de question ou de tous les jeux de questions
    LaunchedEffect(selectedSetId) {
        selectedSetId?.let { setId ->
            navigateToRouteNoPopUp(route = "statistics/$setId", navController = navController)
        }
    }

    LaunchedEffect(showNextButton) {
        showNextButton?.let { _ ->
            navigateToRouteNoPopUp(route = "statistics/all", navController = navController)
        }
    }
}

@Composable
fun ShowStatisticsData(name: String, policeTitleSize: Int, policeSize: Int, correctCount: Int, totalAsked: Int) {
    TitleWithContentRow(title = "Statistiques: $name", fontSize = policeTitleSize, fontWeight = FontWeight.Bold)
    TitleWithContentRow(title = "Bonnes réponses: $correctCount", fontSize = policeSize)
    TitleWithContentRow(title = "Total répondu: $totalAsked", fontSize = policeSize)
}

/**
 * Composable qui affiche les statistiques d'un jeu de questions
 * @param setId Le jeu de question dont on affiche les statistiques
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShowStatisticsScreen(setId: Int, gameViewModel: GameViewModel, settingsViewModel: SettingsViewModel) {
    val policeTitleSize by settingsViewModel.policeTitleSizeFlow.collectAsState(initial = 20)
    val policeSize by settingsViewModel.policeSizeFlow.collectAsState(initial = 16)

    LaunchedEffect(setId) {
        gameViewModel.fetchSetStatisticsById(setId = setId)
        gameViewModel.fetchQuestionSet(setId = setId)
    }

    val questionSetStatistics by gameViewModel.setStatisticsFlow.collectAsState(initial = null)
    val questionSet by gameViewModel.questionSetFlow.collectAsState(initial = null)
    val currentDate = LocalDate.now()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        questionSetStatistics?.let { stats ->
            val setName = questionSet?.name
            if (setName != null) {
                ShowStatisticsData(name = setName,
                    policeTitleSize = policeTitleSize,
                    policeSize = policeSize,
                    correctCount = stats.correctCount,
                    totalAsked = stats.totalAsked
                )
            }
            if (stats.lastTrainedDate.isNotEmpty()) {
                val daysSinceLastShown = ChronoUnit.DAYS.between(stringToLocalDate(stats.lastTrainedDate), currentDate)
                TitleWithContentRow(title = "Jours depuis le dernier entrainement: $daysSinceLastShown", fontSize = policeSize)
            }
            else
                TitleWithContentRow(title = "Aucun entraînement effectué", fontSize = policeSize)
        }
    }
}

/**
 * Composable qui affiche les statistiques de l'ensemble des jeux
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun ShowAllStatisticsScreen(gameViewModel: GameViewModel, settingsViewModel: SettingsViewModel) {
    val policeTitleSize by settingsViewModel.policeTitleSizeFlow.collectAsState(initial = 20)
    val policeSize by settingsViewModel.policeSizeFlow.collectAsState(initial = 16)

    LaunchedEffect(true) {
        gameViewModel.fetchAllSetsStatistics()
    }

    val totalCorrectCount = gameViewModel.totalCorrectCount
    val totalAskedCount = gameViewModel.totalAskedCount
    val daysSinceTraining = gameViewModel.daysSinceTraining

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ShowStatisticsData(
            name = "Tous les jeux",
            policeTitleSize = policeTitleSize,
            policeSize = policeSize,
            correctCount = totalCorrectCount,
            totalAsked =totalAskedCount
        )
        if (daysSinceTraining >= 0)
            TitleWithContentRow(title = "Jours depuis le dernier entrainement: $daysSinceTraining", fontSize = policeSize)
        else
            TitleWithContentRow(title = "Aucun entraînement effectué", fontSize = policeSize)
    }
}
