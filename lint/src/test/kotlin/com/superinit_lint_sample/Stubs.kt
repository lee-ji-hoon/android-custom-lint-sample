package com.superinit_lint_sample

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFile

val kotlinSerializable: TestFile = LintDetectorTest.kotlin(
    """
    package kotlinx.serialization

    annotation class Serializable
    """.trimIndent(),
)
