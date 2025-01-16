package io.pixelbin.glamar

// Callback interface for WebView events
interface GlamArCallback {
    fun onInitComplete()
    fun onLoading()
    fun onSkuApplied()
    fun onSkuFailed()
    fun onPhotoLoaded(payload: Map<String, Any>)
    fun onLoaded(previewMode: PreviewMode)
    fun onOpened()
    fun onError(message: String)
    fun onFaceAnalysisCompleted(payload: Map<String, Any>)
}