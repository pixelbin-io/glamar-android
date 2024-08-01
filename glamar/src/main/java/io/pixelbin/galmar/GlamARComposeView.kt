package io.pixelbin.galmar
//
//import android.content.Context
//import android.view.View
//import androidx.compose.runtime.Composable
//import androidx.compose.runtime.DisposableEffect
//import androidx.compose.runtime.remember
//import androidx.compose.ui.Modifier
//import androidx.compose.ui.viewinterop.AndroidView
//
///**
// * Composable function to wrap the GlamARView.
// *
// * @param modifier The modifier to be applied to the view.
// * @param accessKey The access key for the GlamAR SDK.
// * @param isBeauty Whether the beauty mode is enabled.
// */
//@Composable
//fun GlamARComposeView(
//    modifier: Modifier = Modifier,
//    accessKey: String,
//    isBeauty: Boolean
//) {
//    val glamARView = remember { GlamARView(ContextAmbient.current) }
//
//    DisposableEffect(Unit) {
//        glamARView.accessKey = accessKey
//        glamARView.isBeauty = isBeauty
//        onDispose { }
//    }
//
//    AndroidView(
//        modifier = modifier,
//        factory = { glamARView },
//        update = { view ->
//            (view as? GlamARView)?.apply {
//                this.accessKey = accessKey
//                this.isBeauty = isBeauty
//            }
//        }
//    )
//}
