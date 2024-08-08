package io.pixelbin.glamar.model

import com.google.gson.annotations.SerializedName

data class SkuListResponse(
    @SerializedName("page") val page: Page,
    @SerializedName("items") val items: List<Item>
)