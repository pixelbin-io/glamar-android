# GlamAR Android SDK

[![Maven Central](https://img.shields.io/maven-central/v/io.pixelbin.glamar/glamar.svg)](https://central.sonatype.com/artifact/io.pixelbin.glamar/glamar)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

## Overview

GlamAR is a powerful Augmented Reality SDK for Android that enables virtual try-on experiences for makeup, jewelry, and other beauty products. The SDK provides an easy-to-integrate solution with real-time AR capabilities, face detection, and product visualization features.

## Features

- Real-time virtual makeup try-on
- Multiple product category support
- Camera and image-based preview modes
- Real-time face tracking and analysis
- Easy integration with Android applications
- Snapshot functionality
- High-performance WebView-based rendering
- Original/Modified view comparison
- Configurable parameters

## Installation

### Maven Central

The GlamAR SDK is available on Maven Central. Add the following dependency to your app's `build.gradle`:

```groovy
dependencies {
    implementation 'io.pixelbin.glamar:glamar:2.0.7'
}
```

### Manual Installation

Alternatively, you can manually include the `.aar` file:

1. Place the `.aar` file in your project's `libs` directory
2. Add the following to your app's `build.gradle`:

```groovy
dependencies {
    implementation fileTree(dir: 'libs', include: ['*.jar', '*.aar'])
    implementation 'com.google.code.gson:gson:2.10.1'
    implementation 'com.squareup.okhttp3:okhttp:4.11.0'
}
```

Sync your project with Gradle files.

## Required Permissions

Add these permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" /> <!-- Required for camera preview mode -->
```

## Initialization

Initialize the SDK in your Application class to ensure it is set up when your app starts.

```kotlin
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        GlamAr.init(context = this, accessKey = "YOUR_ACCESS_KEY")
    }
}
```

Don't forget to register your `Application` class in the `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApplication"
    ... >
    <!-- Other configurations -->
</application>
```

This "init" will prompt our SDK to create a webview and open our SDK in it.

## AR View

### Setup

To use the webview we have created you will need to add it your layout. Paste the following code inside your layout xml file where you want to show the webview.

```xml
<FrameLayout
    android:id="@+id/glamARView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"
    app:layout_constraintBottom_toBottomOf="parent"
    app:layout_constraintEnd_toEndOf="parent"
    app:layout_constraintStart_toStartOf="parent"
    app:layout_constraintTop_toTopOf="parent" />
```

Inside the activity class add the following code:

```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // This passes the activity context to the webview
        GlamArWebViewManager.setUpActivityContext(this)

        // This adds the webview created by SDK in the layout
        GlamArWebViewManager.getPreparedWebView()?.let { webView ->
            val glamARView = findViewById<FrameLayout>(R.id.glamARView)
            glamARView.apply {
                addView(webView)
            }
        }
    }
}
```

You should be able to see GlamAR SDK page being loaded.

### Initialization options

Opens the SDK Home screen with relevant category module setup.

```kotline
import io.pixelbin.glamar.model.GlamAROverrides

val overrides = GlamAROverrides(
    category = "sunglasses",
)
GlamAr.init(context = this, accessKey = "YOUR_ACCESS_KEY", overrides)
```

Open the SDK with Live mode (web camera) straightaway. This bypasses the SDK home screen.

```kotline
import io.pixelbin.glamar.model.GlamAROverrides
import io.pixelbin.glamar.model.Configuration
import io.pixelbin.glamar.model.GlobalConfig

val overrides = GlamAROverrides(
    category = "sunglasses",
    configuration = Configuration(
        global = GlobalConfig(
            openLiveOnInit = true,
        ),
    )
)
GlamAr.init(context = this, accessKey = "YOUR_ACCESS_KEY", overrides)
```

Open SDK with disabled previous button and cross button.

```kotline
import io.pixelbin.glamar.model.GlamAROverrides
import io.pixelbin.glamar.model.Configuration
import io.pixelbin.glamar.model.GlobalConfig

val overrides = GlamAROverrides(
    category = "sunglasses",
    configuration = Configuration(
        global = GlobalConfig(
            disableClose = true,
            disableBack = false
        ),
    )
)
GlamAr.init(context = this, accessKey = "YOUR_ACCESS_KEY", overrides)
```

### Applying SKUs

Apply a SKU:

```kotlin
GlamAr.applyBySku(skuId = "SKU_ID")
```

### Applying catalog categories

Apply a catalog category or subcategory. Pass `ApplyCatalogOptions` when the
catalog should be resolved for a specific storefront:

```kotlin
import io.pixelbin.glamar.model.ApplyCatalogOptions

GlamAr.applyByCategory("eyewear")
GlamAr.applyByCategory(
    category = "eyewear",
    options = ApplyCatalogOptions(storeFront = "STORE_FRONT")
)

GlamAr.applyBySubCategory("sunglasses")
GlamAr.applyBySubCategory(
    subCategory = "sunglasses",
    options = ApplyCatalogOptions(storeFront = "STORE_FRONT")
)
```

### Taking Snapshot

Take a snapshot of the current view:

```kotlin
GlamAr.snapshot()
```

## Permissions

Ensure that you handle permissions appropriately, especially for camera access when using `openLiveOnInit`.

```kotlin
override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    GlamArPermissionHandler.onRequestPermissionsResult(requestCode, grantResults)
}
```

## Event Handling

Event listeners are essential soon after initialization is called to start listening to GlamAR SDK callback events.

### addEventListener

```kotlin
// Can be any event type sent from SDK
GlamAR.addEventListener("sku-applied"){
    GlamArLogger.d("TAG", "sku-applied callback")
}
```

### removeEventListener
Can also unregister from listening to events previously registered to but calling

```kotlin
GlamAR.removeEventListener("sku-applied")
```

## Best Practices

1. Always handle permissions appropriately:
```kotlin
override fun onRequestPermissionsResult(
  requestCode: Int,
  permissions: Array<out String>,
  grantResults: IntArray
) {
  super.onRequestPermissionsResult(requestCode, permissions, grantResults)
  GlamArPermissionHandler.onRequestPermissionsResult(requestCode, grantResults)
}
```
2. Initialize the SDK early in your application lifecycle
3. Handle callbacks for better user experience
4. Use appropriate preview modes based on your use case
5. Implement proper error handling

## Version History

- 1.0.2 (Latest)
  - New updated GlamAR structure
- 1.0.1
  - Maven Central release
  - Enhanced face tracking
  - Improved performance
  - Bug fixes and stability improvements

## Support

For support and bug reports, please create an issue in our GitHub repository or contact our support team at support@pixelbin.io.

## License

GlamAR SDK is available under the MIT license. See the LICENSE file for more info.
