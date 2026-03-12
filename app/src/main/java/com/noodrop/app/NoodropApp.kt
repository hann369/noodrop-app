package com.noodrop.app

import android.app.Application
import com.noodrop.app.util.SoundManager
import com.noodrop.app.util.createNotificationChannels
import com.noodrop.app.util.schedulePremiumHint
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class NoodropApp : Application() {

    override fun onCreate() {
        super.onCreate()

        // Notification channels (required before any notify() call)
        createNotificationChannels(this)

        // Sound effects (loaded once, reused across screens)
        SoundManager.init(this)

        // Schedule one-time Premium hint after 3 days (no-op if already scheduled)
        schedulePremiumHint(this)
    }
}
