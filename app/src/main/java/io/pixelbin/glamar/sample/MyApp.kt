package io.pixelbin.glamar.sample

import android.app.Application
import android.webkit.WebView

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        WebView.setWebContentsDebuggingEnabled(BuildConfig.DEBUG)
    }
}
