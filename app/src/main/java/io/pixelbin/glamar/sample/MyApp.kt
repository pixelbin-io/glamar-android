package io.pixelbin.glamar.sample

import android.app.Application
import android.os.Build
import android.webkit.WebView
import android.widget.FrameLayout.LayoutParams
import io.pixelbin.glamar.GlamAr
import io.pixelbin.glamar.GlamArLogger
import io.pixelbin.glamar.model.GlamAROverrides

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        GlamArLogger.d("Glam_myApp", "onCreate: ")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        val webView = WebView(
            this
        ).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            settings.javaScriptEnabled = true
            settings.mediaPlaybackRequiresUserGesture = false
            val glamArHostUrl = "https://cdn.glamar.io/sdk/"
            loadUrl(glamArHostUrl)
        }

        val overrides = GlamAROverrides(
            // category = "skinanalysis",
            meta = mapOf(
                "sdkVersion" to "2.0.0"
            ),
//            configuration = Configuration(
//                skinAnalysis = SkinAnalysisConfig(
//                    appId = "YOUR_APP_ID"
//                ),
//            )
        )

        // Initialise SDK
        GlamAr.init(
            context = this,
            accessKey = "YOUR_ACCESS_KEY",
            overrides,
//            webView,
//            debug = BuildConfig.DEBUG,

        )
    }
}

