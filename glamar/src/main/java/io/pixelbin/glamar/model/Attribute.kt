package io.pixelbin.glamar.model

import com.google.gson.annotations.SerializedName

data class Attribute(
    @SerializedName("icons") val icons: List<String>,
    @SerializedName("colors") val colors: List<String>,
    @SerializedName("effectAssets") val effectAssets: List<String>
)