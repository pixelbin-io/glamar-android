package io.pixelbin.glamar.model

import com.google.gson.annotations.SerializedName

data class SkuItemResponse(
    @SerializedName("sku") val item: Item
)