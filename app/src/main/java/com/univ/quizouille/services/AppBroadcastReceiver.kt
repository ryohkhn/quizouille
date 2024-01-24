package com.univ.quizouille.services

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.RequiresApi
import com.univ.quizouille.viewmodel.GameViewModel

class AppBroadcastReceiver(private val gameViewModel: GameViewModel) : BroadcastReceiver() {
    /**
     * Traite la demande du DownloadManager et lance le parsing du fichier si le téléchargement n'a pas échoué
     */
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context?, intent: Intent?){
        val downloadId = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1) ?: -1
        if (downloadId == -1L) return
        gameViewModel.parseFile()
    }
}