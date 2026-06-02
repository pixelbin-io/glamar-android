package io.pixelbin.glamar.sample

import android.app.Application
import android.os.Build
import android.webkit.WebView
import android.widget.FrameLayout.LayoutParams
import com.glamar.sdk.GlamAr
import com.glamar.sdk.GlamArLogger
import com.glamar.sdk.model.GlamAROverrides

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
            accessKey = "a9b90ac7-218e-4ee9-b0ba-acb6487f803b",
            overrides,
//            debug = BuildConfig.DEBUG,
        )
    }
}
