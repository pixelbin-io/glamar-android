package io.pixelbin.glamar.sample

import android.app.Application
import android.os.Build
import android.util.Log
import android.webkit.WebView
import io.pixelbin.galmar.sample.BuildConfig
import io.pixelbin.glamar.GlamAr
import io.pixelbin.glamar.PreviewMode

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("TAG", "onCreate: ")

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
        // Initialise SDK
        GlamAr.initialize(
            context = this,
            accessKey = "62a16d1e", debug = BuildConfig.DEBUG, previewMode = PreviewMode.Image(imageUrl = "https://cdn.pixelbin.io/v2/glamar-fynd-835885/original/glamar-custom-data/models/makeup/2.jpg")
        )
    }
}
