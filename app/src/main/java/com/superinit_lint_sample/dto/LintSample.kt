package com.superinit_lint_sample.dto

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@Serializable
data class LintSample(
    val result: Int,
    val data: List<Streaming>
)

@Serializable
data class Streaming(
    val data: Int
)