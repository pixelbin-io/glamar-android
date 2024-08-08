package io.pixelbin.glamar.model

import com.google.gson.annotations.SerializedName


data class Meta(
    @SerializedName("material") val material: String,
    @SerializedName("dimension") val dimension: String
)