package com.example.ngdtechsupport.utils

import android.util.Log
import kotlinx.coroutines.delay
import kotlin.math.min
import kotlin.math.pow

object RetryUtil {

    private const val TAG = "RetryUtil"

    suspend fun <T> withRetry(
        maxRetries: Int = 3,
        initialDelayMs: Long = 1000,
        maxDelayMs: Long = 10000,
        block: suspend () -> T
    ): T {
        var currentDelay = initialDelayMs
        var attempt = 0

        while (true) {
            try {
                return block()
            } catch (e: Exception) {
                attempt++
                if (attempt > maxRetries) {
                    Log.e(TAG, "Retry failed after $maxRetries attempts", e)
                    throw e
                }
                Log.w(TAG, "Attempt $attempt failed, retrying in ${currentDelay}ms: ${e.message}")
                delay(currentDelay)
                currentDelay = min(currentDelay * 2, maxDelayMs)
            }
        }
    }
}
