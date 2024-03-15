package com.superinit_lint_sample

import com.android.tools.lint.client.api.IssueRegistry
import com.android.tools.lint.detector.api.Issue

class LintRegistry : IssueRegistry() {

    override val issues: List<Issue> = listOf(
        DtoDefaultValueDetector.ISSUE
    )
}