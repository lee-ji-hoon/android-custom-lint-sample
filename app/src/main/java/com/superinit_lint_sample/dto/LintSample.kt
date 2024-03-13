package com.superinit_lint_sample.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class LintSample(
    @SerialName("streaming_list")
    val streamingList: List<Streaming>,
    val message: String
)

@Serializable
data class Streaming(
    val data: Int
)