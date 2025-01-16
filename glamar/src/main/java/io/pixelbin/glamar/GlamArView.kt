// GlamArView.kt

package io.pixelbin.glamar

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.util.AttributeSet
import android.util.Log
import android.view.ViewGroup
import android.webkit.PermissionRequest
import android.webkit.WebChromeClient
import android.widget.FrameLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class GlamArView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        if (GlamArWebViewManager.getPreparedWebView() == null) GlamArWebViewManager.prepareWebView(
            context, GlamAr.getInstance().debug
        )
        // Check if the WebView already has a parent
        (GlamArWebViewManager.getPreparedWebView()?.parent as? ViewGroup)?.removeView(
            GlamArWebViewManager.getPreparedWebView()
        )

        addView(GlamArWebViewManager.getPreparedWebView()?.apply {
            webChromeClient = object : WebChromeClient() {
                override fun onPermissionRequest(request: PermissionRequest) {
                    Log.e("WebView", "onPermissionRequest: $request")
                    handlePermissionRequest(request)
                }
            }
        })
    }

    fun setCallback(callback: GlamArCallback) {
        GlamArWebViewManager.setGlamArCallBack(callback)
    }

    fun changeFaceAnalysisCategory(category: String) {
        evaluateJavascript("window.parent.postMessage({ type: 'faceAnalysis' , payload: { options: 'changeCategory', value:'$category' }  }, '*');")
    }

    fun applySku(skuId: String) {
        evaluateJavascript("window.parent.postMessage({ type: 'applyBySku' , payload: { skuId: '${skuId}' } }, '*');")
    }

    fun clear() {
        evaluateJavascript("window.parent.postMessage({ type: 'clearSku'} , '*');")
    }

    fun configChange(options: String, value: Double? = null) {
        val script = if (value != null) {
            "window.parent.postMessage({ type: 'configChange', payload: { options: '$options', value: '$value' }}, '*');"
        } else {
            "window.parent.postMessage({ type: 'configChange', payload: { options: '$options' }}, '*');"
        }
        evaluateJavascript(script)
    }

    fun snapshot() {
        evaluateJavascript("window.parent.postMessage({ type: 'snapshot'} , '*');")
    }

    fun toggle(showOriginal: Boolean) {
        val script = if (showOriginal) {
            "window.parent.postMessage({type:'comparison', payload: {options: 'touch',value:'show'} }, '*');"
        } else {
            "window.parent.postMessage({type:'comparison', payload: {options: 'touch',value:'hide'} }, '*');"
        }
        evaluateJavascript(script)
    }

    private fun evaluateJavascript(script: String) {
        GlamArWebViewManager.evaluateJavascript(script)
    }

    private fun handlePermissionRequest(request: PermissionRequest) {
        val permissions = request.resources.mapNotNull {
            when (it) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> android.Manifest.permission.CAMERA
                PermissionRequest.RESOURCE_AUDIO_CAPTURE -> android.Manifest.permission.RECORD_AUDIO
                else -> null
            }
        }.toTypedArray()

        val missingPermissions = permissions.filter {
            ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missingPermissions.isEmpty()) {
            request.grant(request.resources)
        } else {
            ActivityCompat.requestPermissions(
                (context as Activity), missingPermissions.toTypedArray(), requestCodePermissions
            )
            pendingPermissionRequest = request
        }
    }

    private val requestCodePermissions = 1001
    private var pendingPermissionRequest: PermissionRequest? = null

    fun onRequestPermissionsResult(
        requestCode: Int, grantResults: IntArray
    ) {
        if (requestCode == requestCodePermissions) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                pendingPermissionRequest?.grant(pendingPermissionRequest?.resources)
                GlamArWebViewManager.initPreview()
            } else {
                pendingPermissionRequest?.deny()
            }
            pendingPermissionRequest = null
        }
    }
}


sealed class PreviewMode {
    data object None : PreviewMode()
    data class Image(val imageUrl: String) : PreviewMode()
    data object Camera : PreviewMode()
    data object FaceAnalysis : PreviewMode()
}
