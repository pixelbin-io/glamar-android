// GlamAr.kt

package io.pixelbin.glamar

import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import android.webkit.WebView
import android.widget.FrameLayout.LayoutParams

class GlamAr private constructor(val accessKey: String, val development: Boolean = true) {

    val api: GlamArApi = GlamArApi(accessKey, development)

    companion object {
        @SuppressLint("StaticFieldLeak")
        @Volatile
        private var instance: GlamAr? = null
        var BASE_URL = ""
        private const val DEV_URL = "https://api.pixelbinz0.de"
        private const val PROD_URL = "https://api.pixelbin.io"

        @SuppressLint("SetJavaScriptEnabled")
        fun initialize(
            context: Context,
            accessKey: String,
            development: Boolean = true,
            prepareWebView: Boolean = true
        ): GlamAr {

            if (prepareWebView) {
                val previewMode =
                    PreviewMode.Image(imageUrl = "https://cdn.pixelbin.io/v2/glamar-fynd-835885/original/glamar-custom-data/models/makeup/2.jpg")

                GlamArWebViewManager.prepareWebView(
                    context, development, previewMode = previewMode
                )
            }


            return instance ?: synchronized(this) {
                BASE_URL = if (development) DEV_URL else PROD_URL
                instance ?: GlamAr(accessKey, development = development).also { instance = it }
            }
        }

        fun getInstance(): GlamAr {
            return instance ?: throw Exception("GlamAR not initialized. Call initialize() first.")
        }
    }
}
