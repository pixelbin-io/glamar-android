package com.glamar.sdk

import android.util.Log

object GlamArLogger {

    private var enabled: Boolean = false

    fun init(debug: Boolean) {
        enabled = debug
    }

    fun d(tag: String, message: String) {
        if (enabled) Log.d(tag, message)
    }

    fun e(tag: String, message: String, throwable: Throwable? = null) {
        if (enabled) {
            if (throwable != null) Log.e(tag, message, throwable)
            else Log.e(tag, message)
        }
    }

    fun i(tag: String, message: String) {
        if (enabled) Log.i(tag, message)
    }

    fun w(tag: String, message: String) {
        if (enabled) Log.w(tag, message)
    }
}
