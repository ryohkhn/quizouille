package com.univ.quizouille.services

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment

class AppDownloadManager(private var context: Context) {
    private val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager

    /**
     * Lance le téléchargement du fichier sous le format `json`
     * @param url   un url internet
     * @return      l'ID unique de téléchargement
     */
    fun enqueueDownload(url: String): Long {
        val uri = Uri.parse(url)
        val request = DownloadManager.Request(uri)
            .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            .setDestinationInExternalFilesDir(context, Environment.DIRECTORY_DOWNLOADS, "data.json")
        return downloadManager.enqueue(request)
    }
}