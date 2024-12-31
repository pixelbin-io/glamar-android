package io.pixelbin.glamar.sample

import android.app.Application
import android.util.Log
import io.pixelbin.glamar.GlamAr

class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        Log.d("TAG", "onCreate: ")

        // Initialise SDK
        GlamAr.initialize(
            context = this,
            accessKey = "ff4146c9-386a-463d-9b7d-4191bfa35c7f",
            development = true,
            prepareWebView = true
        )
    }
}