package com.wisme.firstapp

import android.app.Application
import com.wisme.firstapp.data.sync.FeedbackSyncManager
import dagger.hilt.android.HiltAndroidApp
import javax.inject.Inject

@HiltAndroidApp
class NovaApplication : Application() {
    
    @Inject
    lateinit var feedbackSyncManager: FeedbackSyncManager
    
    override fun onCreate() {
        super.onCreate()
        
        // Schedule periodic sync for feedback submissions
        feedbackSyncManager.schedulePeriodicSync()
    }
}
