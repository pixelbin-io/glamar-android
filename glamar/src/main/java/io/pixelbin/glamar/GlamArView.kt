// GlamArView.kt

package io.pixelbin.glamar

import android.content.Context
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.FrameLayout

class GlamArView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    init {
        GlamArLogger.d("GlamARView", "Init GlamArView")
        if (GlamArWebViewManager.getPreparedWebView() == null){
            GlamArLogger.d("GlamArView","Prepare WebView")
            GlamArWebViewManager.prepareWebView(
                context, GlamAr.getInstance().debug
            )
        }

        // Check if the WebView already has a parent
        (GlamArWebViewManager.getPreparedWebView()?.parent as? ViewGroup)?.removeView(
            GlamArWebViewManager.getPreparedWebView()
        )

        addView(GlamArWebViewManager.getPreparedWebView())
    }
}
