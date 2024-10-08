package io.pixelbin.glamar.sample

import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.pixelbin.galmar.sample.R

import io.pixelbin.glamar.GlamAr
import io.pixelbin.glamar.GlamArView
import io.pixelbin.glamar.PreviewMode


class MainActivity : AppCompatActivity() {

    private lateinit var glamARView: GlamArView

    init {
        GlamAr.initialize(accessKey = "93009942-7912-462c-a688-90e6aa8b5b81", development = true)
        //GlamAr.initialize(accessKey = "ODkzZmU1ZGItZDg5ZS00Yzc3LTk2NjktYTM2OTZhZjRlNGVi", false)
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
        val applyBtn = findViewById<Button>(R.id.apply_sku)
        val initBtn = findViewById<Button>(R.id.init)
        val move = findViewById<Button>(R.id.move)
        val download = findViewById<Button>(R.id.download)

        glamARView.startPreview(
            previewMode = PreviewMode.Image(imageUrl = "https://cdn.pixelbin.io/v2/glamar-fynd-835885/original/glamar-custom-data/models/makeup/2.jpg"), isBeauty = true
        )

        applyBtn.setOnClickListener {
            glamARView.applySku(skuId = "666b311f-1b34-4082-99d1-c525451b44a1", category = "beauty")
        }

        initBtn.setOnClickListener {
            glamARView.clear()
        }
        move.setOnClickListener {
            glamARView.configChange("Opacity", 0.2)
        }
        download.setOnClickListener {
            glamARView.snapshot()
        }
    }
}
