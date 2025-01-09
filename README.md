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
    implementation 'io.pixelbin.glamar:glamar:1.0.1'
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

## Required Permissions

Add these permissions to your `AndroidManifest.xml`:

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.CAMERA" /> <!-- Required for camera preview mode -->
```

## Initialization

Initialize the SDK in your Application class:

```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        GlamAr.initialize(
            context = this,
            accessKey = "YOUR_ACCESS_KEY",
            debug = BuildConfig.DEBUG,  // Set to true for development environment
            previewMode = PreviewMode.Camera  // Or PreviewMode.Image for image-based preview
        )
    }
}
```

## Usage

### Basic Implementation

1. Add GlamArView to your layout:

```xml
<io.pixelbin.glamar.GlamArView
    android:id="@+id/glamARView"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

2. Setup callbacks and handle events:

```kotlin
glamARView.setCallback(object : GlamArCallback {
    override fun onError(error: String) {
        // Handle error
    }
    
    override fun onSuccess(message: String) {
        // Handle success
    }
    
    override fun onSnapshotTaken(base64Image: String) {
        // Handle snapshot image
    }
})
```

### Key Features

#### Preview Modes

```kotlin
// Camera preview mode
glamARView.startPreview(previewMode = PreviewMode.Camera)

// Image preview mode
glamARView.startPreview(previewMode = PreviewMode.Image(imageUrl = "IMAGE_URL"))

// No preview mode
glamARView.startPreview(previewMode = PreviewMode.None)
```

#### Product Application

```kotlin
// Apply a specific SKU
glamARView.applySku(skuId = "SKU_ID")

// Clear applied products
glamARView.clear()
```

#### Capture and Compare

```kotlin
// Take a snapshot
glamARView.snapshot()

// Toggle between original and modified view
glamARView.toggle(showOriginal = true)
```

#### Configuration

```kotlin
// Adjust various parameters
glamARView.configChange(options = "brightness", value = 1.2)
```

### API Integration

```kotlin
// Fetch SKU list
GlamAr.getInstance().api.fetchSkuList(pageNo = 1, pageSize = 20) { result ->
    result.onSuccess { response ->
        // Handle SKU list
    }.onFailure { exception ->
        // Handle error
    }
}

// Fetch specific SKU details
GlamAr.getInstance().api.fetchSku(id = "SKU_ID") { result ->
    result.onSuccess { item ->
        // Handle SKU details
    }.onFailure { exception ->
        // Handle error
    }
}
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
    glamARView.onRequestPermissionsResult(requestCode, grantResults)
}
```
2. Initialize the SDK early in your application lifecycle
3. Handle callbacks for better user experience
4. Use appropriate preview modes based on your use case
5. Implement proper error handling

## Version History

- 1.0.1 (Latest)
  - Maven Central release
  - Enhanced face tracking
  - Improved performance
  - Bug fixes and stability improvements

## Support

For support and bug reports, please create an issue in our GitHub repository or contact our support team at support@pixelbin.io.

## License

GlamAR SDK is available under the MIT license. See the LICENSE file for more info.
