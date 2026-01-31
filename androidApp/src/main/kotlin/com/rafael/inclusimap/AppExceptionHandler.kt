package com.rafael.inclusimap

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log

const val TAG = "AppExceptionHandler"

internal class AppExceptionHandler private constructor(
    private val applicationContext: Context,
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        Log.e(TAG, "Intercepted crash: ${throwable.message}", throwable)
        try {
            val intent = Intent(applicationContext, CrashActivity::class.java).apply {
                putExtras(
                    Bundle().apply {
                        putString("crashStack", throwable.stackTraceToString())
                        putString("crashMessage", throwable.message)
                    },
                )
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            applicationContext.startActivity(intent)
            Thread.sleep(800)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to launch CrashActivity", e)
            throw e
        }
    }

    companion object {
        private var isInitialized = false
        fun init(applicationContext: Context) {
            if (isInitialized) {
                Log.w(TAG, "Already initialized.")
                return
            }
            val defaultHandler = Thread.getDefaultUncaughtExceptionHandler()
            if (defaultHandler !is AppExceptionHandler) {
                Thread.setDefaultUncaughtExceptionHandler(
                    AppExceptionHandler(applicationContext.applicationContext),
                )
                Log.i(TAG, "Custom AppExceptionHandler initialized.")
                isInitialized = true
            } else {
                Log.i(TAG, "AppExceptionHandler was already the default handler.")
            }
        }
    }
}
