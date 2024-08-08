// GlamAr.kt

package io.pixelbin.glamar

class GlamAr private constructor(val accessKey: String, val development: Boolean = true) {

    val api: GlamArApi = GlamArApi(accessKey, development)

    companion object {
        @Volatile
        private var instance: GlamAr? = null
        var BASE_URL = ""
        private const val DEV_URL = "https://api.pixelbinz0.de"
        private const val PROD_URL = "https://api.pixelbin.io"

        fun initialize(accessKey: String, development: Boolean = true): GlamAr {
            return instance ?: synchronized(this) {
                BASE_URL = if (development) DEV_URL else PROD_URL
                instance ?: GlamAr(accessKey, development = development).also { instance = it }
            }
        }

        fun getInstance(): GlamAr {
            return instance
                ?: throw Exception("GlamAR not initialized. Call initialize() first.")
        }
    }
}
