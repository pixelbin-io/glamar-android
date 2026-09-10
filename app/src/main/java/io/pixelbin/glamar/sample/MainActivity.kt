package io.pixelbin.glamar.sample

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.webkit.WebView
import android.widget.Button
import android.widget.FrameLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import io.pixelbin.glamar.GlamAr
import io.pixelbin.glamar.GlamArLogger
import io.pixelbin.glamar.GlamArPermissionHandler
import io.pixelbin.glamar.GlamArWebViewManager
import io.pixelbin.glamar.model.Configuration
import io.pixelbin.glamar.model.GlamAROverrides
import io.pixelbin.glamar.model.SkinAnalysisConfig
import io.pixelbin.glamar.model.SkinAnalysisExperienceOptions
import io.pixelbin.glamar.model.VtoExperienceOptions


class MainActivity : AppCompatActivity() {
    private var sdkWebView: WebView? = null

    companion object {
        private const val EXTRA_ACCESS_KEY = "access_key"
        private const val EXTRA_SKIN_ANALYSIS_APP_ID = "skin_analysis_app_id"

        fun createIntent(context: Context, accessKey: String, skinAnalysisAppId: String?): Intent {
            return Intent(context, MainActivity::class.java).apply {
                putExtra(EXTRA_ACCESS_KEY, accessKey)
                skinAnalysisAppId?.let { putExtra(EXTRA_SKIN_ANALYSIS_APP_ID, it) }
            }
        }
    }

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

        val accessKey = intent.getStringExtra(EXTRA_ACCESS_KEY)
        val appId = intent.getStringExtra(EXTRA_SKIN_ANALYSIS_APP_ID)
        if (accessKey.isNullOrBlank() || (appId != null && appId.isBlank())) {
            finish()
            return
        }
        val isSkinAnalysis = appId != null

        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val overrides = GlamAROverrides(
            category = if (isSkinAnalysis) "skinanalysis" else null,
            configuration = appId?.let {
                Configuration(skinAnalysis = SkinAnalysisConfig(appId = it))
            },
            meta = mapOf("sdkVersion" to "2.0.0")
        )
        GlamAr.init(
            context = this,
            accessKey = accessKey,
            overrides = overrides,
            debug = BuildConfig.DEBUG
        )
        GlamArWebViewManager.setUpActivityContext(this)
        sdkWebView = GlamArWebViewManager.getPreparedWebView()
        sdkWebView?.let { webView ->
            val glamARView = findViewById<FrameLayout>(R.id.glamARView)
            glamARView.apply {
                addView(webView)
            }
        }

        val applyBtn = findViewById<Button>(R.id.apply_sku)
        val clearBtn = findViewById<Button>(R.id.clear)
        val download = findViewById<Button>(R.id.download)
        val glamArChangeCategory = findViewById<Button>(R.id.glamArChangeCategory)

        applyBtn.visibility = if (isSkinAnalysis) View.GONE else View.VISIBLE
        findViewById<View>(R.id.move).visibility = if (isSkinAnalysis) View.GONE else View.VISIBLE
        glamArChangeCategory.visibility = if (isSkinAnalysis) View.VISIBLE else View.GONE
        glamArChangeCategory.setText(R.string.start_skin_analysis)

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
            GlamAr.setExperience(
                experience = "vto",
                options = VtoExperienceOptions(category = "makeup")
            )
        }


        clearBtn.setOnClickListener {
            GlamAr.close()
        }

        download.setOnClickListener {
            GlamAr.setExperience(
                experience = "skinAnalysis",
                options = SkinAnalysisExperienceOptions(appId = "0732f85d-5768-462e-a50a-174840438a69")
            )
        }
    }

    override fun onResume() {
        super.onResume()
        sdkWebView?.onResume()
    }

    override fun onPause() {
        sdkWebView?.onPause()
        super.onPause()
    }

    override fun onDestroy() {
        if (sdkWebView != null && GlamArWebViewManager.getPreparedWebView() === sdkWebView) {
            GlamArWebViewManager.releaseWebView()
        }
        sdkWebView = null
        super.onDestroy()
    }
}
