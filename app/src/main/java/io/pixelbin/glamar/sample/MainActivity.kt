package io.pixelbin.glamar.sample

import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.pixelbin.galmar.sample.R
import io.pixelbin.glamar.GlamAr
import io.pixelbin.glamar.GlamArLogger
import io.pixelbin.glamar.GlamArPermissionHandler
import io.pixelbin.glamar.GlamArWebViewManager


class MainActivity : AppCompatActivity() {

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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

        GlamArWebViewManager.getPreparedWebView()?.let { webView ->
            val glamARView = findViewById<FrameLayout>(R.id.glamARView)
            glamARView.apply {
                addView(webView)
            }
        }

        val applyBtn = findViewById<Button>(R.id.apply_sku)
        val clearBtn = findViewById<Button>(R.id.clear)
        val download = findViewById<Button>(R.id.download)
        val glamArChangeCategory = findViewById<Button>(R.id.glamArChangeCategory)


        glamArChangeCategory.setOnClickListener {
            GlamAr.skinAnalysis("start")
        }

        GlamAr.addEventListener("sku-applied") {
            GlamArLogger.d("Glam_MainActivity", "sku-applied callback")
        }

        GlamAr.addEventListener("loaded") {
            GlamArLogger.d("Glam_MainActivity", "loaded callback")
        }

        applyBtn.setOnClickListener {
            GlamAr.applyByCategory("sunglasses")
        }

        clearBtn.setOnClickListener {
            GlamAr.close()
        }

        download.setOnClickListener {
            GlamAr.snapshot()
        }
    }
}
