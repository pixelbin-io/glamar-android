package io.pixelbin.glamar.sample

import android.app.Application
import android.os.Build
import android.webkit.WebView
import android.webkit.WebChromeClient
import android.webkit.WebViewClient
import android.widget.FrameLayout.LayoutParams
import io.pixelbin.galmar.sample.BuildConfig
import io.pixelbin.glamar.GlamAr
import io.pixelbin.glamar.GlamArLogger
import io.pixelbin.glamar.model.ARConfig
import io.pixelbin.glamar.model.Configuration
import io.pixelbin.glamar.model.GlamAROverrides
import io.pixelbin.glamar.model.GlobalConfig
import io.pixelbin.glamar.model.LoaderConfig
import io.pixelbin.glamar.model.SkinAnalysisConfig
import io.pixelbin.glamar.model.UIConfig
import io.pixelbin.glamar.model.WatermarkConfig

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        GlamArLogger.d("Glam_myApp", "onCreate: ")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        val webView = WebView(this
        ).apply {
                layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
                settings.javaScriptEnabled = true
                settings.mediaPlaybackRequiresUserGesture = false
                val glamArHostUrl = "https://glamarz0.de/sdk/"
                loadUrl(glamArHostUrl)
        }

        val overrides = GlamAROverrides(
            category = "sunglasses",
//            configuration = Configuration(
//                global = GlobalConfig(
//                    openLiveOnInit = true,
//                    disableClose = true,
//                    disableBack = false
//                ),
//                skinAnalysis = SkinAnalysisConfig(
//                    version = "GlamGen",
//                    defaultFilter = true,
//                    startScreen = true
//                ),
//                ui = UIConfig(
//                    loader = LoaderConfig(
//                        disable = false,
//                        jsonData = "https://cdn.pixelbin.io/v2/glamar-fynd-835885/original/glamar_assets/loaders/loader_default.json",
//                        backgroundColor = "#000000"
//                    ),
//                    watermark = WatermarkConfig(
//                        text = "Water Mark Text",
//                        fontColor = "#00ff00",
//                        logo = "https://cdn.pixelbin.io/v2/glamar-fynd-835885/original/glamar_assets/icons/cxr_logo_w-yDFY_5Mio.svg"
//                    ),
                   // ar = ARConfig(disable3DUI = false)
//                )
//            )
        )

        // Initialise SDK
        GlamAr.init(
            context = this,
            accessKey = "ba843e66-4b93-48f6-a02e-ea48d920e52e",
            debug = BuildConfig.DEBUG,
            overrides = overrides,
//            webView
        )
    }
}

