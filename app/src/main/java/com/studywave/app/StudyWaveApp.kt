package com.studywave.app

import android.app.Application
import com.studywave.app.di.AppContainer

class StudyWaveApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
