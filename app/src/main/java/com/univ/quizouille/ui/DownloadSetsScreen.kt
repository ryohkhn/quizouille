package com.univ.quizouille.ui

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.univ.quizouille.ui.components.TitleWithContentRow
import com.univ.quizouille.viewmodel.GameViewModel
import com.univ.quizouille.viewmodel.SettingsViewModel

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DownloadSetsScreen(
    gameViewModel: GameViewModel,
    settingsViewModel: SettingsViewModel,
    snackbarHostState: SnackbarHostState
) {
    val policeSize by settingsViewModel.policeSizeFlow.collectAsState(initial = 16)
    val policeTitleSize by settingsViewModel.policeTitleSizeFlow.collectAsState(initial = 20)

    var url by remember { mutableStateOf("") }

    Column {
        TitleWithContentRow(title = "Téléchargement d'un jeu de questions", fontSize = policeTitleSize, fontWeight = FontWeight.Bold)
        Row {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("Lien") },
                modifier = Modifier.padding(start = 10.dp)
            )
        }
        Row {
            Button(onClick = {
                if (gameViewModel.downloadData(url) == -1L)
                    url = ""
                },
                modifier = Modifier.padding(10.dp)) {
                Text(text = "Importer", fontSize = policeSize.sp)
            }
        }
    }

    // Détecte le changement de valeur de `gameViewModel.snackBarMessage` et lance un SnackBar si non vide
    LaunchedEffect(gameViewModel.snackBarMessage) {
        if (gameViewModel.snackBarMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(gameViewModel.snackBarMessage, duration = SnackbarDuration.Short)
            gameViewModel.resetSnackbarMessage()
        }
    }
}
