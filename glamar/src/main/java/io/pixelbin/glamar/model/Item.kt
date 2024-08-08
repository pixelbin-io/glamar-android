package io.pixelbin.glamar.model

import com.google.gson.annotations.SerializedName
import java.util.Date

data class Item(
    @SerializedName("_id") val id: String,
    @SerializedName("orgId") val orgId: Int,
    @SerializedName("category") val category: String,
    @SerializedName("subCategory") val subCategory: String,
    @SerializedName("productName") val productName: String,
    @SerializedName("productImage") val productImage: String,
    @SerializedName("vendor") val vendor: String,
    @SerializedName("isActive") val isActive: Boolean,
    @SerializedName("itemCode") val itemCode: String,
    @SerializedName("styleVariant") val styleVariant: String?,
    @SerializedName("styleIcon") val styleIcon: String,
    @SerializedName("attributes") val attributes: List<Attribute>,
    @SerializedName("meta") val meta: Meta,
    @SerializedName("createdAt") val createdAt: Date,
    @SerializedName("updatedAt") val updatedAt: Date
)