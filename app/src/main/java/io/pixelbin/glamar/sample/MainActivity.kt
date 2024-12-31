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
import io.pixelbin.glamar.GlamArCallback
import io.pixelbin.glamar.GlamArView
import io.pixelbin.glamar.PreviewMode


class MainActivity : AppCompatActivity(), GlamArCallback {

    private lateinit var glamARView: GlamArView

    private lateinit var glamArChangeCategory: Button

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        glamARView.onRequestPermissionsResult(requestCode, grantResults)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        // Fetch SKU list in a background thread
        GlamAr.getInstance().api.fetchSkuList(pageNo = 1, pageSize = 100) { result ->
            result.onSuccess { skuListResponse ->
                Log.e("MainActivity", "Fetched SKU List: $skuListResponse")
            }.onFailure { exception ->
                Log.e("MainActivity", "Error fetching SKU List: ${exception.message}")
            }
        }

        // Fetch a specific SKU in a background thread
        GlamAr.getInstance().api.fetchSku(id = "0a1bf713-b596-44fb-a0f5-0bc5c2c57235") { result ->
            result.onSuccess { item ->
                Log.e("MainActivity", "Fetched SKU Item: $item")
            }.onFailure { exception ->
                Log.e("MainActivity", "Error fetching SKU Item: ${exception.message}")
            }
        }


        glamARView = findViewById(R.id.glamARView)
        glamARView.setCallback(this)
        val applyBtn = findViewById<Button>(R.id.apply_sku)
        val clearBtn = findViewById<Button>(R.id.clear)
        val move = findViewById<Button>(R.id.move)
        val download = findViewById<Button>(R.id.download)
        glamArChangeCategory = findViewById(R.id.glamArChangeCategory)

        glamArChangeCategory.setOnClickListener {
            glamARView.changeFaceAnalysisCategory("wrinkle")
        }

        applyBtn.setOnClickListener {
            glamARView.applySku(
                skuId = "0a1bf713-b596-44fb-a0f5-0bc5c2c57235"
            )
        }

        clearBtn.setOnClickListener {
            glamARView.clear()
        }
        move.setOnClickListener {
            glamARView.configChange("Opacity", 0.1)
        }
        download.setOnClickListener {
            glamARView.snapshot()
        }
    }

    override fun onInitComplete() {
        Log.d(MainActivity::class.java.name, "onInitComplete")
    }

    override fun onLoading() {
        Log.d(MainActivity::class.java.name, "onLoading")

    }

    override fun onSkuApplied() {
        Log.d(MainActivity::class.java.name, "onSkuApplied")

    }

    override fun onSkuFailed() {
        Log.d(MainActivity::class.java.name, "onSkuFailed")

    }

    override fun onPhotoLoaded(payload: Map<String, Any>) {
        Log.d(MainActivity::class.java.name, "onPhotoLoaded: $payload")

    }

    override fun onLoaded(previewMode: PreviewMode) {
        Log.d(MainActivity::class.java.name, "onLoaded: $previewMode")
        runOnUiThread {
            if (previewMode is PreviewMode.FaceAnalysis) {
                glamArChangeCategory.visibility = View.VISIBLE
            }
        }
    }

    override fun onOpened() {
        Log.d(MainActivity::class.java.name, "onOpened")

    }

    override fun onError(message: String) {
        Log.d(MainActivity::class.java.name, "onError")

    }

    override fun onFaceAnalysisCompleted(payload: Map<String, Any>) {
        Log.d(MainActivity::class.java.name, "onFaceAnalysisCompleted: $payload")
    }
}
