package io.pixelbin.galmar.sample

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.pixelbin.galmar.GlamAr
import io.pixelbin.galmar.GlamArView
import io.pixelbin.galmar.PreviewMode


class MainActivity : AppCompatActivity() {

    private lateinit var glamARView: GlamArView

    init {
        //        GlamAr.initialize(accessKey = "ZmY0MTQ2YzktMzg2YS00NjNkLTliN2QtNDE5MWJmYTM1Yzdm")
        GlamAr.initialize(accessKey = "ODkzZmU1ZGItZDg5ZS00Yzc3LTk2NjktYTM2OTZhZjRlNGVi")
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
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
        GlamAr.getInstance().fetchSkuList(pageNo = 1, pageSize = 100) { result ->
            result.onSuccess { skuListResponse ->
                Log.e("MainActivity", "Fetched SKU List: $skuListResponse")
            }.onFailure { exception ->
                Log.e("MainActivity", "Error fetching SKU List: ${exception.message}")
            }
        }

        // Fetch a specific SKU in a background thread
        GlamAr.getInstance().fetchSku(id = "0a1bf713-b596-44fb-a0f5-0bc5c2c57235") { result ->
            result.onSuccess { item ->
                Log.e("MainActivity", "Fetched SKU Item: $item")
            }.onFailure { exception ->
                Log.e("MainActivity", "Error fetching SKU Item: ${exception.message}")
            }
        }


        glamARView = findViewById(R.id.glamARView)
        val applyBtn = findViewById<Button>(R.id.apply_sku)
        val initBtn = findViewById<Button>(R.id.init)
        val move = findViewById<Button>(R.id.move)
        val download = findViewById<Button>(R.id.download)

//        glamARView.startPreview(
//            previewMode = PreviewMode.None,
//        )
//        glamARView.startPreview(
//            previewMode = PreviewMode.Camera,
//        )
        glamARView.startPreview(
            previewMode = PreviewMode.Image(imageUrl = "https://cdn.pixelbin.io/v2/glamar-fynd-835885/original/glamar-custom-data/models/makeup/2.jpg"),
        )

        applyBtn.setOnClickListener {
            glamARView.applySku(skuId = "68f2e7ad-f74e-4944-9910-e9a3186559f2", category = "style")
        }

        initBtn.setOnClickListener {
            glamARView.clear()
        }
        move.setOnClickListener {
            glamARView.configChange("down")
        }
        download.setOnClickListener {
            glamARView.snapshot()
        }
    }
}
