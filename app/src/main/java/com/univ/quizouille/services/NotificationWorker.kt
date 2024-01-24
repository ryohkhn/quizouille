package com.univ.quizouille.services

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.univ.quizouille.utilities.frequencyUnits
import com.univ.quizouille.viewmodel.SettingsViewModel
import com.univ.quizouille.viewmodel.dataStore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.concurrent.TimeUnit

class NotificationWorker(context: Context, workerParameters: WorkerParameters) : CoroutineWorker(context, workerParameters) {
    override suspend fun doWork(): Result {
        val dataStore: DataStore<Preferences> = applicationContext.dataStore

        val shouldSendNotification = dataStore.data.map { preferences ->
            preferences[SettingsViewModel.PreferencesKeys.NOTIFICATIONS] ?: false
        }.first()

        val notificationFrequency = dataStore.data.map { preferences ->
            preferences[SettingsViewModel.PreferencesKeys.NOTIFICATIONS_FREQUENCY] ?: 24
        }.first()

        val unitFlow = dataStore.data.map { preferences ->
            preferences[SettingsViewModel.PreferencesKeys.FREQUENCY_UNIT] ?: "heures"
        }.first()

        if (shouldSendNotification) {
            val notificationManager = AppNotificationManager(applicationContext)
            notificationManager.createNotification()
        }

        val unit = frequencyUnits.getOrDefault(unitFlow, TimeUnit.HOURS)
        scheduleNextWork(context = applicationContext, unit, delay = notificationFrequency.toLong())

        return Result.success()
    }

    companion object {
        /**
         * Programme le prochain envoi de notifications.
         * À chaque appel lors de l'activation ou d'un changement de paramètres, l'ancien Worker est remplacé par le nouveau avec les valeurs correctes
         * @param timeUnit  Unité de fréquence des notifications [HOUR, MINUTES, SECONDS]
         * @param delay     Le délai entre chaque notification
         */
        fun scheduleNextWork(context: Context, timeUnit: TimeUnit, delay: Long) {
            val workRequest = OneTimeWorkRequestBuilder<NotificationWorker>()
                .setInitialDelay(delay, timeUnit)
                .build()
            WorkManager.getInstance(context).enqueueUniqueWork(
                "notification_work",
                ExistingWorkPolicy.REPLACE,
                workRequest
            )
        }
    }
}