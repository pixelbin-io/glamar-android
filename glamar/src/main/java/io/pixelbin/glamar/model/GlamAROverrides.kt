package io.pixelbin.glamar.model

data class GlamAROverrides(
    val category: String? = null,
    val configuration: Configuration? = null,
    val meta: Map<String, Any>? = null,
)

data class Configuration(
    val global: GlobalConfig? = null,
    val skinAnalysis: SkinAnalysisConfig? = null,
    val ui: UIConfig? = null
)

data class GlobalConfig(
    val openLiveOnInit: Boolean? = null,
    val disableClose: Boolean? = null,
    val disableBack: Boolean? = null
)

data class SkinAnalysisConfig(
    val appId: String? = null,
)

data class UIConfig(
    val loader: LoaderConfig? = null,
    val watermark: WatermarkConfig? = null,
    val ar: ARConfig? = null
)

data class LoaderConfig(
    val disable: Boolean? = null,
    val jsonData: String? = null,
    val backgroundColor: String? = null
)

data class WatermarkConfig(
    val text: String? = null,
    val fontColor: String? = null,
    val logo: String? = null
)

data class ARConfig(
    val disable3DUI: Boolean? = null
)

data class ConfigData(
    val type: String,
    val value: Number
)
