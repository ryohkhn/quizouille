@file:OptIn(ExperimentalMaterial3Api::class)

package com.univ.quizouille.ui

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHostState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.core.text.isDigitsOnly
import com.univ.quizouille.services.AppNotificationManager
import com.univ.quizouille.ui.components.TitleWithContentRow
import com.univ.quizouille.utilities.frequencyUnits
import com.univ.quizouille.viewmodel.SettingsViewModel
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.LaunchedEffect

@Composable
fun SettingsTextField(value: String, label: String, onDone: (String) -> Unit) {
    var text by remember(value) { mutableStateOf(value) }

    OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        label = { Text(label) },
        keyboardOptions = KeyboardOptions.Default.copy(
            keyboardType = KeyboardType.Number,
            imeAction = ImeAction.Done
        ),
        keyboardActions = KeyboardActions(
            onDone = {
                if (text.isNotEmpty() && text.isDigitsOnly() && text.toInt() > 0)
                    onDone(text)
            }
        ),
        modifier = Modifier.padding(start = 10.dp)
    )
}

/**
 * Composable affichant le menu général des paramètres.
 * Il permet d'activer/désactiver les notifications, le temps de réponse aux questions ainsi que la taille des épolices
 * @param permissionLauncher    Launcher permettant au Viewmodel de vérifier et demander la permission des notifications
 */
@Composable
fun SettingsScreen(
    settingsViewModel: SettingsViewModel,
    notificationManager: AppNotificationManager,
    permissionLauncher: ManagedActivityResultLauncher<String, Boolean>,
    snackbarHostState: SnackbarHostState
) {
    val questionDelay by settingsViewModel.questionDelayFlow.collectAsState(initial = 10)
    val policeSize by settingsViewModel.policeSizeFlow.collectAsState(initial = 16)
    val policeTitleSize by settingsViewModel.policeTitleSizeFlow.collectAsState(initial = 20)
    val notificationsMode by settingsViewModel.notificationsFlow.collectAsState(initial = false)

    Column {
        TitleWithContentRow(title = "Paramètres", fontSize = policeTitleSize, fontWeight = FontWeight.Bold)
        TitleWithContentRow(title = "Notifications", fontSize = policeSize) {
            Switch(
                checked = notificationsMode,
                onCheckedChange = {
                    settingsViewModel.setNotifications(
                        mode = it,
                        notificationManager = notificationManager,
                        permissionLauncher = permissionLauncher)
                })
        }
        if (notificationsMode) {
            NotificationSettingsSection(settingsViewModel = settingsViewModel)
        }
        TitleWithContentRow(title = "Temps de réponse aux questions", fontSize = policeSize) {
            SettingsTextField(
                value = questionDelay.toString(),
                label = "secondes",
                onDone = { settingsViewModel.setQuestionDelay(it.toInt()) }
            )
        }
        TitleWithContentRow(title = "Taille de la police", fontSize = policeSize) {
            SettingsTextField(
                value = policeSize.toString(),
                label = "",
                onDone = { settingsViewModel.setPoliceSize(it.toInt()) })
        }
    }

    // Détecte le changement de valeur de `settingsViewModel.snackBarMessage` et lance un SnackBar si non vide
    LaunchedEffect(settingsViewModel.snackBarMessage) {
        if (settingsViewModel.snackBarMessage.isNotEmpty()) {
            snackbarHostState.showSnackbar(settingsViewModel.snackBarMessage, duration = SnackbarDuration.Short)
            settingsViewModel.resetSnackbarMessage()
        }
    }
}

/**
 * Composable s'affichant lorsque l'utilisateur active les notifications.
 * Il permet de configurer la fréquence et l'unité de temps à la quelle les notifications vont arriver
 */
@Composable
fun NotificationSettingsSection(settingsViewModel: SettingsViewModel) {
    val notificationsFrequency by settingsViewModel.notificationsFreqFlow.collectAsState(initial = 1)
    val policeSize by settingsViewModel.policeSizeFlow.collectAsState(initial = 16)
    val unitString by settingsViewModel.frequencyUnitFlow.collectAsState(initial = "")

    TitleWithContentRow(title = "Fréquence des notifications", fontSize = policeSize) {
        SettingsTextField(
            value = notificationsFrequency.toString(),
            label = unitString,
            onDone = { settingsViewModel.setNotificationsFrequency(it.toInt()) })
    }

    var expanded by remember { mutableStateOf(false) }
    val settingsUnit by settingsViewModel.frequencyUnitFlow.collectAsState(initial = "heures")

    TitleWithContentRow(title = "Unité", fontSize = policeSize) {
        ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
            TextField(
                modifier = Modifier.menuAnchor(),
                readOnly = true,
                value = settingsUnit,
                onValueChange = {},
                label = { Text("Unité") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                colors = ExposedDropdownMenuDefaults.textFieldColors(),
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
            ) {
                for ((unit, _) in frequencyUnits) {
                    DropdownMenuItem(text = { Text(unit) }, onClick = {
                        settingsViewModel.setFrequencyTimeUnit(unit)
                        expanded = false
                    },
                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding)
                }
            }
        }

    }
}