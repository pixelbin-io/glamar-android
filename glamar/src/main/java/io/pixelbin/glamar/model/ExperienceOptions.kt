package io.pixelbin.glamar.model

sealed interface ExperienceOptions

data class VtoExperienceOptions(
    val category: String? = null,
    val subCategory: String? = null,
    val skuId: String? = null,
) : ExperienceOptions

data class SkinAnalysisExperienceOptions(
    val appId: String? = null,
) : ExperienceOptions
