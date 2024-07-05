package com.superinit_lint_sample

import com.android.ide.common.rendering.api.AndroidConstants.AUTO_URI
import com.android.tools.lint.detector.api.Category
import com.android.tools.lint.detector.api.Implementation
import com.android.tools.lint.detector.api.Issue
import com.android.tools.lint.detector.api.LayoutDetector
import com.android.tools.lint.detector.api.Scope
import com.android.tools.lint.detector.api.Severity
import com.android.tools.lint.detector.api.XmlContext
import com.android.tools.lint.detector.api.isLayoutMarkerTag
import org.w3c.dom.Element
import org.w3c.dom.Node

/**
 * Custom ConstraintLayout에서 제약 조건이 설정되지 않은 경우 Lint 경고를 발생시키는 Detector.
 */
class CustomConstraintLayoutDetector : LayoutDetector() {

    override fun getApplicableElements(): Collection<String> {
        return setOf("com.superinit_lint_sample.CustomConstraintLayout")
    }

    override fun visitElement(context: XmlContext, element: Element) {
        if (element.hasAttributeNS(AUTO_URI, "layoutDescription")) {
            return
        }

        val flowList = getFlowList(element)

        // 모든 자식 뷰가 가로 및 세로로 제약 조건이 설정되었는지 확인
        var child = element.firstChild
        while (child != null) {
            if (child.nodeType != Node.ELEMENT_NODE) {
                child = child.nextSibling
                continue
            }
            val layout = child as Element
            if (shouldSkipElement(layout)) {
                child = child.nextSibling
                continue
            }
            if (isElementConstrained(layout, flowList).not()) {
                val message: String = when {
                    isConstrainedVertically(layout) -> "이 뷰는 가로로 제약 조건이 없습니다. 가로 제약 조건을 추가해주세요."
                    isConstrainedHorizontally(layout) -> "이 뷰는 세로로 제약 조건이 없습니다. 세로 제약 조건을 추가해주세요."
                    else -> "이 뷰는 제약 조건이 없습니다. 제약 조건을 추가해주세요."
                }
                context.report(ISSUE, layout, context.getNameLocation(layout), message)
            }
            child = child.nextSibling
        }
    }

    /**
     * Flow로 제약된 뷰의 ID 목록을 가져옵니다.
     * @param element ConstraintLayout 요소
     * @return Flow로 제약된 뷰의 ID 목록
     */
    private fun getFlowList(element: Element): List<String> {
        val flowList = mutableListOf<String>()
        var child = element.firstChild
        while (child != null) {
            if (child.nodeType == Node.ELEMENT_NODE) {
                val childElement = child as Element
                if (childElement.tagName == "androidx.constraintlayout.widget.Flow") {
                    val attributes = childElement.attributes
                    for (i in 0 until attributes.length) {
                        val attribute = attributes.item(i)
                        val name = attribute.localName ?: continue
                        if (name.contains("constraint_referenced_ids")) {
                            flowList.addAll(attribute.nodeValue.split(","))
                        }
                    }
                }
            }
            child = child.nextSibling
        }
        return flowList
    }

    /**
     * 주어진 요소가 가로 및 세로로 제약 조건이 설정되었는지 확인합니다.
     * @param element 검사할 요소
     * @param flowList Flow로 제약된 뷰의 ID 목록
     * @return 가로 및 세로로 제약 조건이 설정된 경우 true, 그렇지 않으면 false
     */
    private fun isElementConstrained(element: Element, flowList: List<String>): Boolean {
        var isConstrainedHorizontally = false
        var isConstrainedVertically = false
        val attributes = element.attributes
        for (i in 0 until attributes.length) {
            val attribute = attributes.item(i)
            val name = attribute.localName ?: continue

            if (name == "id" && flowList.contains(attribute.nodeValue.split("/").last())) {
                isConstrainedHorizontally = true
                isConstrainedVertically = true
                break
            }

            if (!name.startsWith("layout_constraint") || name.endsWith("_creator")) {
                continue
            }

            if (isHorizontallyConstrained(name, attribute.nodeValue)) {
                isConstrainedHorizontally = true
            }
            if (isVerticallyConstrained(name, attribute.nodeValue)) {
                isConstrainedVertically = true
            }
            if (isConstrainedHorizontally && isConstrainedVertically) {
                break
            }
        }
        return isConstrainedHorizontally && isConstrainedVertically
    }

    /**
     * 주어진 요소가 스킵해야 하는 요소인지 확인합니다.
     * @param element 검사할 요소
     * @return 스킵해야 하는 경우 true, 그렇지 않으면 false
     */
    private fun shouldSkipElement(element: Element): Boolean {
        val tagName = element.tagName
        return tagName == "androidx.constraintlayout.widget.Guideline" ||
                tagName == "androidx.constraintlayout.widget.Group" ||
                tagName == "include" ||
                isLayoutMarkerTag(tagName) ||
                (tagName == "androidx.constraintlayout.widget.Barrier" && scanForBarrierConstraint(element))
    }

    /**
     * 가로 제약 조건이 설정되었는지 확인합니다.
     * @param name 속성 이름
     * @param value 속성 값
     * @return 가로 제약 조건이 설정된 경우 true, 그렇지 않으면 false
     */
    private fun isHorizontallyConstrained(name: String, value: String): Boolean {
        return name == "layout_width" && value == "match_parent" ||
                name.endsWith("toLeftOf") ||
                name.endsWith("toRightOf") ||
                name.endsWith("toStartOf") ||
                name.endsWith("toEndOf") ||
                name.endsWith("toCenterX")
    }

    /**
     * 세로 제약 조건이 설정되었는지 확인합니다.
     * @param name 속성 이름
     * @param value 속성 값
     * @return 세로 제약 조건이 설정된 경우 true, 그렇지 않으면 false
     */
    private fun isVerticallyConstrained(name: String, value: String): Boolean {
        return name == "layout_height" && value == "match_parent" ||
                name.endsWith("toTopOf") ||
                name.endsWith("toBottomOf") ||
                name.endsWith("toCenterY") ||
                name.endsWith("toBaselineOf")
    }

    /**
     * 주어진 요소에 대해 가로 제약 조건이 설정되었는지 확인합니다.
     * @param element 검사할 요소
     * @return 가로 제약 조건이 설정된 경우 true, 그렇지 않으면 false
     */
    private fun isConstrainedHorizontally(element: Element): Boolean {
        val attributes = element.attributes
        for (i in 0 until attributes.length) {
            val attribute = attributes.item(i)
            val name = attribute.localName ?: continue
            if (isHorizontallyConstrained(name, attribute.nodeValue)) {
                return true
            }
        }
        return false
    }

    /**
     * 주어진 요소에 대해 세로 제약 조건이 설정되었는지 확인합니다.
     * @param element 검사할 요소
     * @return 세로 제약 조건이 설정된 경우 true, 그렇지 않으면 false
     */
    private fun isConstrainedVertically(element: Element): Boolean {
        val attributes = element.attributes
        for (i in 0 until attributes.length) {
            val attribute = attributes.item(i)
            val name = attribute.localName ?: continue
            if (isVerticallyConstrained(name, attribute.nodeValue)) {
                return true
            }
        }
        return false
    }

    companion object {
        @JvmField
        val ISSUE = Issue.create(
            id = "MissingConstraints",
            briefDescription = "ConstraintLayout에 제약 조건이 누락되었습니다",
            explanation = """
                가로 및 세로 제약 조건이 모두 있는지 확인해주세요..
            """,
            category = Category.CORRECTNESS,
            priority = 6,
            severity = Severity.ERROR,
            implementation = Implementation(
                CustomConstraintLayoutDetector::class.java,
                Scope.RESOURCE_FILE_SCOPE
            ),
            androidSpecific = true
        )

        /**
         * 주어진 요소에 barrierDirection 속성이 설정되었는지 확인합니다.
         * @param element 검사할 요소
         * @return barrierDirection 속성이 설정된 경우 true, 그렇지 않으면 false
         */
        private fun scanForBarrierConstraint(element: Element): Boolean {
            val attributes = element.attributes
            for (i in 0 until attributes.length) {
                val attribute = attributes.item(i)
                val name = attribute.localName ?: continue
                if (name.endsWith("barrierDirection")) {
                    return true
                }
            }
            return false
        }
    }
}