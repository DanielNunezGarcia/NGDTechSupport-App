package com.example.ngdtechsupport.utils

import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.analytics.ktx.logEvent
import com.google.firebase.ktx.Firebase

object AnalyticsHelper {
    private val analytics: FirebaseAnalytics = Firebase.analytics

    fun screenView(screenName: String) {
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW) {
            param(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
        }
    }

    fun chatMessageSent(channelId: String) {
        analytics.logEvent("chat_message_sent") {
            param("channel_id", channelId)
        }
    }

    fun chatWelcomeDisplayed(channelId: String) {
        analytics.logEvent("chat_welcome_displayed") {
            param("channel_id", channelId)
        }
    }

    fun updateViewed(updateId: String) {
        analytics.logEvent("update_viewed") {
            param("update_id", updateId)
        }
    }

    fun privateChannelCreated(channelId: String) {
        analytics.logEvent("private_channel_created") {
            param("channel_id", channelId)
        }
    }

    fun loginSuccess(userRole: String) {
        analytics.logEvent("login_success") {
            param("user_role", userRole)
        }
    }

    fun errorOccurred(errorType: String) {
        analytics.logEvent("error_occurred") {
            param("error_type", errorType)
        }
    }
}