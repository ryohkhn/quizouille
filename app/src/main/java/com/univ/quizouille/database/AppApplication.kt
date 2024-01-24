package com.univ.quizouille.database

import android.app.Application

class AppApplication : Application() {
    val database: AppDatabase by lazy { AppDatabase.getDataBase(this) }
}