package net.hearnsoft.gr3rd.compose.utils

import android.util.Log

import net.hearnsoft.gr3rd.compose.BuildConfig

object Logger {
    @JvmStatic
    fun info(tag: String, message: String) {
        Log.i(tag, message)
    }

    @JvmStatic
    fun info(tag: Any, message: String) {
        Log.i(tag::class.java.simpleName, message)
    }

    @JvmStatic
    fun debug(tag: String, message: String) {
        // Log debug messages
        if (BuildConfig.DEBUG) {
            Log.d(tag, message)
        }
    }

    @JvmStatic
    fun debug(tag: Any, message: String) {
        // Log debug messages
        if (BuildConfig.DEBUG) {
            Log.d(tag::class.java.simpleName, message)
        }
    }

    @JvmStatic
    fun warn(tag: String, message: String) {
        // Log warning messages
        Log.w(tag, message)
    }

    @JvmStatic
    fun warn(tag: Any, message: String) {
        // Log warning messages
        Log.w(tag::class.java.simpleName, message)
    }

    @JvmStatic
    fun err(tag: String, message: String, throwable: Throwable? = null) {
        // Log error messages
        if (BuildConfig.DEBUG) {
            Log.e(tag, message, throwable)
        } else {
            Log.e(tag, message)
            throwable?.let { Log.e(tag, "Error details:", it) }
        }
    }

    @JvmStatic
    fun err(tag: Any, message: String, throwable: Throwable? = null) {
        // Log error messages
        if (BuildConfig.DEBUG) {
            Log.e(tag::class.java.simpleName, message, throwable)
        } else {
            Log.e(tag::class.java.simpleName, message)
            throwable?.let { Log.e(tag::class.java.simpleName, "Error details:", it) }
        }
    }
}