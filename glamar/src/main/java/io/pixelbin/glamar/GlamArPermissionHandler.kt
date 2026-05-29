package io.pixelbin.glamar

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.webkit.PermissionRequest
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object GlamArPermissionHandler {
    private const val requestCodePermissions = 1001
    private var pendingRequest: PermissionRequest? = null

    fun handlePermissionRequest(context: Context, request: PermissionRequest) {
        val permissions = request.resources.mapNotNull {
            when (it) {
                PermissionRequest.RESOURCE_VIDEO_CAPTURE -> android.Manifest.permission.CAMERA
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
            pendingRequest = request
        }
    }

    fun onRequestPermissionsResult(requestCode: Int, grantResults: IntArray) {
        if (requestCode == requestCodePermissions) {
            if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                pendingRequest?.grant(pendingRequest?.resources)
                GlamArLogger.d("GlamPermissionHandler", "permissions granted")
                GlamArWebViewManager.initPreview()
            } else {
                pendingRequest?.deny()
            }
            pendingRequest = null
        }
    }
}
