package com.superinit_lint_sample

import com.android.tools.lint.client.api.UElementHandler
import com.android.tools.lint.detector.api.Detector
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.JavaContext
import com.android.tools.lint.detector.api.LintFix
import com.android.tools.lint.detector.api.Location
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.SourceCodeScanner
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtParameter
import org.jetbrains.uast.UClass
import org.jetbrains.uast.UElement

/**
 * kotlinx.serialization dto 경우 기본값을 꼭 할당할 수 있게 하는 Lint
 */
class DtoDefaultValueDetector : Detector(), SourceCodeScanner {

    override fun getApplicableUastTypes(): List<Class<out UElement?>> {
        return listOf(UClass::class.java)
    }

    override fun createUastHandler(context: JavaContext): UElementHandler {
        return object : UElementHandler() {
            override fun visitClass(node: UClass) {
                val ktClass = node.sourcePsi as? KtClass ?: return
                node.getAnnotation(KOTLINX_SERIALIZABLE) ?: return
                val contents = context.getContents() ?: return
                ktClass.primaryConstructorParameters
                    .filterNot { it.hasDefaultValue() }
                    .forEach { parameter ->
                        val (startOffset, endOffset) = adjustTextRange(
                            contents = contents,
                            parameter = parameter
                        )
                        val location = Location.create(context.file, context.getContents(), startOffset, endOffset)
                        context.report(
                            issue = ISSUE,
                            scope = parameter,
                            location = location,
                            message = "해당 필드의 기본값을 할당해주세요",
                            quickfixData = createDefaultValueFix(parameter, location, contents.substring(startOffset, endOffset))
                        )
                    }
            }
        }
    }

    fun adjustTextRange(contents: CharSequence, parameter: KtParameter): Pair<Int, Int> {
        var startOffset = parameter.textRange.startOffset + parameter.annotationEntries.sumOf { it.textLength }
        while (startOffset < contents.length && contents[startOffset].isWhitespace()) {
            startOffset++
        }
        val endOffset = parameter.textRange.endOffset

        return Pair(startOffset, endOffset)
    }

    private fun createDefaultValueFix(param: KtParameter, location: Location, target: String): LintFix {
        val typeName = param.typeReference?.text
        val defaultValue = getDefaultForType(typeName)
        val fixedSource = "$target = $defaultValue"
        return fix()
            .replace()
            .range(location)
            .text(target)
            .with(fixedSource)
            .build()
    }

    companion object {
        private const val KOTLINX_SERIALIZABLE = "kotlinx.serialization.Serializable"

        val ISSUE: Issue = Issue.create(
            id = "DtoDefaultValueDetector", // 고유 식별자
            briefDescription = "Dto의 data class에서 기본 값을 할당해주세요", // 간단한 설명
            explanation = "기본값이 존재하지 않을 경우 `MissingFieldException`이 발생할수도 있습니다.", // 자세한 설명
            priority = 1, // 우선순위
            severity = Severity.ERROR, // 문제가 발생했을 때 강도 (WARNING, ERROR 등)
            implementation = Implementation(DtoDefaultValueDetector::class.java, Scope.JAVA_FILE_SCOPE), //
        )
    }
}

internal fun getDefaultForType(typeName: String?): String {
    val cleanedTypeName = typeName?.replace(Regex("<.*?>"), "")
    return cleanedTypeName?.let {
        when (cleanedTypeName) {
            "Int" -> "0"
            "Long" -> "0L"
            "Double" -> "0.0"
            "Float" -> "0f"
            "Boolean" -> "false"
            "String" -> "\"\""
            "List" -> "emptyList()"
            "MutableList" -> "mutableListOf()"
            "Set" -> "emptySet()"
            "MutableSet" -> "mutableSetOf()"
            "Map" -> "emptyMap()"
            "MutableMap" -> "mutableMapOf()"
            "JsonElement" -> "JsonNull"
            else -> "$cleanedTypeName()"
        }
    } ?: "null"
}
