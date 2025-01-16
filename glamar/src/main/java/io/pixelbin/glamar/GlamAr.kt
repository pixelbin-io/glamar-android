// GlamAr.kt
package io.pixelbin.glamar
import android.annotation.SuppressLint
import android.content.Context

class GlamAr private constructor(val accessKey: String, val debug: Boolean = true) {
    val api: GlamArApi = GlamArApi(accessKey, debug)
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
            debug: Boolean = true,
            previewMode: PreviewMode = PreviewMode.Camera
        ): GlamAr {
//                val previewMode =
//                    PreviewMode.Image(imageUrl = "https://cdn.pixelbin.io/v2/glamar-fynd-835885/original/glamar-custom-data/models/makeup/2.jpg")
            GlamArWebViewManager.prepareWebView(
                context, debug, previewMode = previewMode
            )
            
            return instance ?: synchronized(this) {
                BASE_URL = if (debug) DEV_URL else PROD_URL
                instance ?: GlamAr(accessKey, debug = debug).also { instance = it }
            }
        }
        fun getInstance(): GlamAr {
            return instance ?: throw Exception("GlamAR not initialized. Call initialize() first.")
        }
    }
}
