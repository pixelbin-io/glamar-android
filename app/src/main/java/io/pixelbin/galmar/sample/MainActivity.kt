package io.pixelbin.galmar.sample

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.pixelbin.galmar.GlamARView

class MainActivity : AppCompatActivity(), GlamARView.Callback {

    private lateinit var glamARView: GlamARView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        glamARView = findViewById(R.id.glamARView)
        val applyBtn = findViewById<Button>(R.id.apply_sku)
        val initBtn = findViewById<Button>(R.id.init)
        val move = findViewById<Button>(R.id.move)
        val download = findViewById<Button>(R.id.download)

        glamARView.setCallback(this)
        glamARView.initialize(
            accessKey = "ODkzZmU1ZGItZDg5ZS00Yzc3LTk2NjktYTM2OTZhZjRlNGVi",
            isBeauty = false,
            staging = true,
            previewMode = GlamARView.PreviewMode.CAMERA,
            imageUrl = "https://cdn.pixelbin.io/v2/glamar-fynd-835885/original/glamar-custom-data/models/makeup/2.jpg"
        )

        applyBtn.setOnClickListener {
            glamARView.applySku("68f2e7ad-f74e-4944-9910-e9a3186559f2")
        }

        initBtn.setOnClickListener {
            glamARView.clear()
        }
        move.setOnClickListener {
            glamARView.configChange("down" )
        }
        download.setOnClickListener {
            glamARView.snapshot()
        }
    }

    override fun onInitComplete() {
        Log.d("GlamAR", "SDK initialized successfully")
    }

    override fun onLoading() {
        Log.d("GlamAR", "Loading...")
    }

    override fun onSkuApplied() {
        Log.d("GlamAR", "SKU applied successfully")
    }

    override fun onSkuFailed() {
        Log.d("GlamAR", "Failed to apply SKU")
    }

    override fun onPhotoLoaded(payload: Map<String, Any>) {
        Log.d("GlamAR", "Photo loaded: $payload")
    }

    override fun onLoaded() {
        Log.d("GlamAR", "AR view loaded")
    }

    override fun onOpened() {
        Log.d("GlamAR", "AR view opened")
    }

    override fun onError(message: String) {
        Log.e("GlamAR", "Error: $message")
    }
}
