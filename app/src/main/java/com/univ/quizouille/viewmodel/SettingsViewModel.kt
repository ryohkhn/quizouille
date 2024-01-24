package com.univ.quizouille.viewmodel

import android.app.Application
import android.content.Context
import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.WorkManager
import com.univ.quizouille.services.AppNotificationManager
import com.univ.quizouille.services.NotificationWorker
import com.univ.quizouille.utilities.frequencyUnits
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name ="settings")

class SettingsViewModel (application: Application) : AndroidViewModel(application) {
    private val dataStore: DataStore<Preferences> = application.applicationContext.dataStore;

    var snackBarMessage by mutableStateOf("")
    fun resetSnackbarMessage() {
        snackBarMessage = ""
    }

    val questionDelayFlow = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.QUESTION_DELAY] ?: 15
    }

    val policeSizeFlow = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.POLICE_SIZE] ?: 16
    }

    val policeTitleSizeFlow = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.POLICE_TITLE_SIZE] ?: 20
    }

    val notificationsFlow = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS] ?: false
    }

    val notificationsFreqFlow = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.NOTIFICATIONS_FREQUENCY] ?: 24
    }

    val frequencyUnitFlow = dataStore.data.map { preferences ->
        preferences[PreferencesKeys.FREQUENCY_UNIT] ?: "heures"
    }

    fun setQuestionDelay(delay: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.QUESTION_DELAY] = delay
            }
        }
    }

    fun setPoliceSize(size: Int) {
        val sizeMin = 8
        val sizeMax = 20
        if (size < sizeMin || size > sizeMax) {
            snackBarMessage = "La taille de la police doit être comprise entre $sizeMin et $sizeMax"
            return
        }
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.POLICE_SIZE] = size
                preferences[PreferencesKeys.POLICE_TITLE_SIZE] = (size * 1.25).toInt()
            }
        }
    }

    private suspend fun enableNotifications() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS] = true
        }
        rescheduleNotificationWorker()
    }

    private suspend fun disableNotifications() {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.NOTIFICATIONS] = false
        }
        // Pour désactiver les notifications on supprime le Worker en cours
        WorkManager.getInstance(getApplication()).cancelUniqueWork("notification_work")
    }

    fun setNotifications(
        mode: Boolean,
        notificationManager: AppNotificationManager,
        permissionLauncher: ManagedActivityResultLauncher<String, Boolean>
    ) {
        viewModelScope.launch {
            if (mode) {
                if(notificationManager.hasNotificationPermission())
                    enableNotifications()
                else
                    notificationManager.requestNotificationPermission(permissionLauncher)
            }
            else {
                disableNotifications()
            }
        }
    }

    fun setNotificationsFrequency(freq: Int) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.NOTIFICATIONS_FREQUENCY] = freq
            }
            rescheduleNotificationWorker()
        }
    }

    /**
     * Modifie l'unité de fréquence des notifications
     * @param stringUnit    L'unité de fréquence: [heures, minutes, secondes]
     */
    fun setFrequencyTimeUnit(stringUnit: String) {
        viewModelScope.launch {
            dataStore.edit { preferences ->
                preferences[PreferencesKeys.FREQUENCY_UNIT] = stringUnit
            }
            rescheduleNotificationWorker()
        }
    }

    /**
     * Reprogramme le Worker des notifications avec les valeurs du Datastore
     */
    private suspend fun rescheduleNotificationWorker() {
        val frequency = notificationsFreqFlow.first().toLong()
        val unit = frequencyUnits[frequencyUnitFlow.first()] ?: TimeUnit.HOURS

        NotificationWorker.scheduleNextWork(
            context = getApplication(),
            timeUnit = unit,
            delay = frequency
        )
    }

    object PreferencesKeys {
        val QUESTION_DELAY = intPreferencesKey("question_delay")
        val POLICE_SIZE = intPreferencesKey("police_size")
        val POLICE_TITLE_SIZE = intPreferencesKey("police_title_size")
        val NOTIFICATIONS = booleanPreferencesKey("notifications")
        val NOTIFICATIONS_FREQUENCY = intPreferencesKey("notifications_freq")
        val FREQUENCY_UNIT = stringPreferencesKey("frequency_unit")
    }
}