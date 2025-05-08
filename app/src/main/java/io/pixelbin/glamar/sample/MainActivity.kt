package io.pixelbin.glamar.sample

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.pixelbin.galmar.sample.R
import io.pixelbin.glamar.GlamAr
import io.pixelbin.glamar.GlamArLogger
import io.pixelbin.glamar.GlamArPermissionHandler
import io.pixelbin.glamar.GlamArView
import io.pixelbin.glamar.GlamArWebViewManager


class MainActivity : AppCompatActivity() {

    private lateinit var glamARView: GlamArView

    private lateinit var glamArChangeCategory: Button

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        GlamArPermissionHandler.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        GlamArWebViewManager.setUpActivityContext(this)

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        glamARView = findViewById(R.id.glamARView)
        val applyBtn = findViewById<Button>(R.id.apply_sku)
        val clearBtn = findViewById<Button>(R.id.clear)
        val download = findViewById<Button>(R.id.download)
        glamArChangeCategory = findViewById(R.id.glamArChangeCategory)

        glamArChangeCategory.setOnClickListener {
            GlamAr.skinAnalysis("changeCategory" , "wrinkle")
        }

        GlamAr.addEventListener("sku-applied"){
            GlamArLogger.d("Glam_MainActivity","sku-applied callback")
        }

        applyBtn.setOnClickListener {
            GlamAr.applySku( skuId = "48062362-cd9d-4a63-b755-3a9ed639f023")
        }

        clearBtn.setOnClickListener {
            GlamAr.close()
        }

        download.setOnClickListener {
            GlamAr.snapshot()
        }
    }
}
