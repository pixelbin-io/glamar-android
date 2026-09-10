package io.pixelbin.glamar.model

/** The response from one version API attempt. Request credentials are not included. */
data class VersionApiResponse(
    val url: String,
    val statusCode: Int?,
    val body: String?,
    val error: String?
)
