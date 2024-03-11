package com.superinit_lint_sample

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestFile
import com.android.tools.lint.checks.infrastructure.TestLintResult
import com.android.tools.lint.checks.infrastructure.TestLintTask

import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue

/**
 * 4가지 경우의 테스트
 * - Int 값이 비어있는 경우
 * - String 값이 비어있는 경우
 * - Class 값이 비어있는 경우
 * - kotlinx.serialization이 아닌 경우
 */
class DtoDefaultValueDetectorTest : LintDetectorTest() {

    override fun getDetector(): Detector {
        return DtoDefaultValueDetector()
    }

    override fun getIssues(): MutableList<Issue> {
        return mutableListOf(DtoDefaultValueDetector.ISSUE)
    }

    val stubs: Array<TestFile> = arrayOf(
        kotlinSerializable,
    )

    /**
     * given
     *  - Int 타입의 빈값을 갖고 있는 파일 제공
     * when
     *  - DtoDefaultValueDetector lint run()
     * then
     *  - DtoDefaultValueDetector의 lint로 잡힌다.
     *  - expect 예상되는 메세지와 비교
     *  - Int쪽은 = 0 로 기본값 세팅
     */
    fun testSerializableWithOutDefaultValueInt() {
        val testFile = kotlin(
            """
            package com.superinit_lint_sample.data.dto.api
            
            import kotlinx.serialization.Serializable
            
            @Serializable
            data class TestDataClassWithoutDefault(
                val result: Int,
                val message: String = ""
            )
            """,
        ).indented()

        runLintTaskSource(testFile, stubs)
            .expect(
                expectedText = """
                src/com/superinit_lint_sample/data/dto/api/TestDataClassWithoutDefault.kt:7: Error: 해당 필드의 기본값을 할당해주세요. [DtoDefaultValueDetector]
                    val result: Int,
                    ~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent(),
            )
            .checkFix(
                fix = null,
                after = kotlin(
                    """
            package com.superinit_lint_sample.data.dto.api

            import kotlinx.serialization.Serializable

            @Serializable
            data class TestDataClassWithoutDefault(
                val result: Int = 0,
                val message: String = ""
            )
            """,
                ).indented(),
            )
    }

    fun runLintTaskSource(source: TestFile, stubs: Array<TestFile> = emptyArray()): TestLintResult =
        lint()
            .allowMissingSdk()
            .files(source, *stubs)
            .run()
}