package com.superinit_lint_sample

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Issue

/**
 * 4가지 경우의 테스트
 * - Int 값이 비어있는 경우
 * - String 값이 비어있는 경우
 * - Class 값이 비어있는 경우
 * - kotlinx.serialization이 아닌 경우
 * - kotlinx.serialization과 아닌 경우가 섞여져 있는 경우
 */
class DtoDefaultValueDetectorTest : LintDetectorTest() {

    override fun getDetector(): Detector {
        return DtoDefaultValueDetector()
    }

    override fun getIssues(): MutableList<Issue> {
        return mutableListOf(DtoDefaultValueDetector.ISSUE)
    }

    override fun lint(): TestLintTask {
        return super.lint()
            .allowMissingSdk()
    }

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
            package com.afreecatv.data.dto.api
            
            import kotlinx.serialization.Serializable
            
            @Serializable
            data class TestDataClassWithoutDefault(
                val result: Int,
                val message: String = ""
            )
            """,
        ).indented()

        lint()
            .files(testFile, kotlinSerializable)
            .run()
            .expect(
                expectedText = """
                src/com/afreecatv/data/dto/api/TestDataClassWithoutDefault.kt:7: Error: 해당 필드의 기본값을 할당해주세요 [DtoDefaultValueDetector]
                    val result: Int,
                    ~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent(),
            )
            .checkFix(
                fix = null,
                after = kotlin(
                    """
            package com.afreecatv.data.dto.api

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

    /**
     * given
     *  - String 타입의 빈값을 갖고 있는 파일 제공
     * when
     *  - DtoDefaultValueDetector lint run()
     * then
     *  - DtoDefaultValueDetector의 lint로 잡힌다.
     *  - expect 예상되는 메세지와 비교
     *  - String값 = "" 로 기본값 세팅
     */
    fun testSerializableWithOutDefaultValueString() {
        val testFile = kotlin(
            """
            package com.afreecatv.data.dto.api
            
            import kotlinx.serialization.Serializable
            
            @Serializable
            data class TestDataClassWithoutDefault(
                val result: Int = -1,
                val message: String
            )
            """,
        ).indented()

        lint()
            .files(testFile, kotlinSerializable)
            .run()
            .expect(
                expectedText = """
                src/com/afreecatv/data/dto/api/TestDataClassWithoutDefault.kt:8: Error: 해당 필드의 기본값을 할당해주세요 [DtoDefaultValueDetector]
                    val message: String
                    ~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent(),
            )
            .checkFix(
                fix = null,
                after = kotlin(
                    """
            package com.afreecatv.data.dto.api

            import kotlinx.serialization.Serializable

            @Serializable
            data class TestDataClassWithoutDefault(
                val result: Int = -1,
                val message: String = ""
            )
            """,
                ).indented(),
            )
    }

    /**
     * given
     *  - Class 타입의 빈값을 갖고 있는 파일 제공
     * when
     *  - DtoDefaultValueDetector lint run()
     * then
     *  - DtoDefaultValueDetector의 lint로 잡힌다.
     *  - expect 예상되는 메세지와 비교
     *  - Class는 = Class() 로 기본값 세팅
     */
    fun testSerializableWithOutDefaultValueCustomClass() {
        val testFile = kotlin(
            """
            package com.afreecatv.data.dto.api
            
            import kotlinx.serialization.Serializable
            
            @Serializable
            data class TestDataClassWithoutDefault(
                val result: Int = -1,
                val message: String = "",
                val userFlag: UserFlag
            )
            """,
        ).indented()

        lint()
            .files(testFile, kotlinSerializable)
            .run()
            .expect(
                expectedText = """
                src/com/afreecatv/data/dto/api/TestDataClassWithoutDefault.kt:9: Error: 해당 필드의 기본값을 할당해주세요 [DtoDefaultValueDetector]
                    val userFlag: UserFlag
                    ~~~~~~~~~~~~~~~~~~~~~~
                1 errors, 0 warnings
                """.trimIndent(),
            )
            .checkFix(
                fix = null,
                after = kotlin(
                    """
            package com.afreecatv.data.dto.api

            import kotlinx.serialization.Serializable

            @Serializable
            data class TestDataClassWithoutDefault(
                val result: Int = -1,
                val message: String = "",
                val userFlag: UserFlag = UserFlag()
            )
            """,
                ).indented(),
            )
    }

    /**
     * given
     *  - Kotrlinx의 Serializable이 아닌 Dto면서 기본값 없는 상태
     * when
     *  - DtoDefaultValueDetector lint run()
     * then
     *  - DtoDefaultValueDetector의 lint로 잡히지 않는다.

     */
    fun testNotSerializableDto() {
        val testFile = kotlin(
            """
            package com.afreecatv.data.dto.api
            
            data class TestDataClassWithoutDefault(
                val result: Int,
                val message: String,
                val userFlag: UserFlag
            )
            """,
        ).indented()

        lint()
            .files(testFile, kotlinSerializable)
            .run().expectClean()
    }

    /**
     * given
     *  - Kotrlinx의 Serializable이 아닌 DTO면서 기본값 없는 상태
     *  - Kotrlinx의 Serializable인 DTO도 존재
     * when
     *  - DtoDefaultValueDetector lint run()
     * then
     *  - Kotrlinx의 Serializable인 DTO만 Lint로 잡혀야 한다.
     */
    fun testNotSerializableDtoAndSerializableDto() {
        val testFile = kotlin(
            """
            package com.afreecatv.data.dto.api
            
            import kotlinx.serialization.Serializable
            
            data class TestDataWithoutSerializableDto(
                val result: Int,
                val message: String,
                val userFlag: UserFlag
            )

            @Serializable
            data class TestDataClassWithoutDefault(
                val result: Int,
                val message: String = "",
                val userFlag: UserFlag = UserFlag()
            )
            """,
        ).indented()

        lint()
            .files(testFile, kotlinSerializable)
            .run()
            .checkFix(
                fix = null,
                after = kotlin(
                    """
            package com.afreecatv.data.dto.api

            import kotlinx.serialization.Serializable

            data class TestDataWithoutSerializableDto(
                val result: Int,
                val message: String,
                val userFlag: UserFlag
            )

            @Serializable
            data class TestDataClassWithoutDefault(
                val result: Int = 0,
                val message: String = "",
                val userFlag: UserFlag = UserFlag()
            )
            """,
                ).indented(),
            )
    }
}