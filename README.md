# GlamAR SDK Documentation

## Overview

The GlamAR SDK provides tools to integrate augmented reality (AR) features into your Android application. The SDK is distributed as a local `.aar` file. This document covers the installation, initialization, and usage of the SDK, including details about `GlamArView` API, and `GlamAr` instance API.

## Installation

1. **Add the `.aar` file to your project:**
   - Place the `.aar` file in the `libs` directory of your project.
   - Open your project's `build.gradle` file and add the following:
   - Also add `okhttp` and `gson` dependencies

   ```groovy
   dependencies {
       implementation fileTree(dir: 'libs', include: ['*.jar', '*.aar'])
       implementation 'com.google.code.gson:gson:2.10.1'
       implementation 'com.squareup.okhttp3:okhttp:4.11.0'
       // Other dependencies
   }
   ```

2. **Sync your project with Gradle files.**

## Initialization

### Initialize SDK in Application Class

Initialize the SDK in your `Application` class to ensure it's set up when your app starts. By default it will be pointing to development make development parameter false for prod.

```kotlin
class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        GlamAr.initialize(accessKey = "YOUR_ACCESS_KEY", development = false)
    }
}
```

Don't forget to register your `Application` class in the `AndroidManifest.xml`:

```xml
<application
    android:name=".MyApplication"
    <!-- Other configurations -->
</application>
```

## GlamArView

### Setup

To use `GlamArView`, add it to your layout:

```xml
<com.example.glamar.GlamArView
    android:id="@+id/glamARView"
    android:layout_width="match_parent"
    android:layout_height="match_parent"/>
```

### Starting Preview

Start the preview in various modes using `startPreview`:

```kotlin
glamARView.startPreview(previewMode = PreviewMode.None)
glamARView.startPreview(previewMode = PreviewMode.Camera)
glamARView.startPreview(previewMode = PreviewMode.Image(imageUrl = "IMAGE_URL"))
```

### Applying SKUs

Apply a SKU to the `GlamArView`:

```kotlin
glamARView.applySku(skuId = "SKU_ID", category = "CATEGORY")
```

### Clearing View

Clear the `GlamArView`:

```kotlin
glamARView.clear()
```

### Taking Snapshot

Take a snapshot of the current view:

```kotlin
glamARView.snapshot()
```

## GlamAr Instance API

### Fetch SKU List

Fetch a list of SKUs:

```kotlin
GlamAr.getInstance().api.fetchSkuList(pageNo = 1, pageSize = 100) { result ->
    result.onSuccess { skuListResponse ->
        // Handle success
    }.onFailure { exception ->
        // Handle failure
    }
}
```

### Fetch Specific SKU

Fetch details of a specific SKU:

```kotlin
GlamAr.getInstance().api.fetchSku(id = "SKU_ID") { result ->
    result.onSuccess { item ->
        // Handle success
    }.onFailure { exception ->
        // Handle failure
    }
}
```

## Example Usage

Here's a complete example demonstrating the usage of `GlamAr` and `GlamArView`:

```kotlin
class MainActivity : AppCompatActivity() {

    private lateinit var glamARView: GlamArView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        glamARView = findViewById(R.id.glamARView)
        val applyBtn = findViewById<Button>(R.id.apply_sku)
        val initBtn = findViewById<Button>(R.id.init)
        val download = findViewById<Button>(R.id.download)

        glamARView.startPreview(
            previewMode = PreviewMode.Image(imageUrl = "IMAGE_URL")
        )

        applyBtn.setOnClickListener {
            glamARView.applySku(skuId = "SKU_ID", category = "style")
        }

        initBtn.setOnClickListener {
            glamARView.clear()
        }

        download.setOnClickListener {
            glamARView.snapshot()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        glamARView.onRequestPermissionsResult(requestCode, grantResults)
    }
}
```

## Permissions

Ensure you handle permissions appropriately, especially for camera access if using `PreviewMode.Camera`.

```kotlin
override fun onRequestPermissionsResult(
    requestCode: Int,
    permissions: Array<out String>,
    grantResults: IntArray
) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    glamARView.onRequestPermissionsResult(requestCode, grantResults)
}
```

## Conclusion

This document provides a comprehensive overview of the GlamAR SDK, detailing how to install, initialize, and use its various components. Use this as a reference to integrate AR features into your Android application effectively.
