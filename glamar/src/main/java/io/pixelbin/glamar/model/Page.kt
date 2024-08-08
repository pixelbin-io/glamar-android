package io.pixelbin.glamar.model

import com.google.gson.annotations.SerializedName

data class Page(
    @SerializedName("type") val type: String,
    @SerializedName("size") val size: Int,
    @SerializedName("current") val current: Int,
    @SerializedName("hasNext") val hasNext: Boolean,
    @SerializedName("itemTotal") val itemTotal: Int
)