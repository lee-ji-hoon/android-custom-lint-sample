package com.superinit_lint_sample

import com.android.tools.lint.checks.infrastructure.LintDetectorTest
import com.android.tools.lint.checks.infrastructure.TestLintTask
import com.android.tools.lint.detector.api.Issue
import org.intellij.lang.annotations.Language

class CustomConstraintLayoutDetectorTest : LintDetectorTest() {

    override fun getDetector() = CustomConstraintLayoutDetector()

    override fun getIssues(): List<Issue> {
        return listOf(CustomConstraintLayoutDetector.ISSUE)
    }

    override fun lint(): TestLintTask {
        return super.lint()
            .allowMissingSdk()
    }

    fun testMissingConstraints() {
        @Language("XML")
        val xml = """
            <com.yourpackage.CustomConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
                xmlns:app="http://schemas.android.com/apk/res-auto"
                android:layout_width="match_parent"
                android:layout_height="match_parent">

                <Button
                    android:id="@+id/button1"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Button 1"
                    app:layout_constraintTop_toTopOf="parent"
                    app:layout_constraintStart_toStartOf="parent" />

                <!-- This button will cause an exception because it has no constraints -->
                <Button
                    android:id="@+id/button2"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Button 2" />

            </com.yourpackage.CustomConstraintLayout>
        """.trimIndent()

        lint()
            .files(xml("res/layout/test.xml", xml))
            .run()
            .expect(
                """
                res/layout/test.xml:15: Error: 이 뷰는 제약 조건이 없습니다. 제약 조건을 추가해주세요. [MissingConstraints]
                    <Button
                     ~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    fun testBarrierConstraints() {
        @Language("XML")
        val xml = """
            <com.yourpackage.CustomConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
                xmlns:app="http://schemas.android.com/apk/res-auto"
                android:layout_width="match_parent"
                android:layout_height="match_parent">

                <androidx.constraintlayout.widget.Barrier
                    android:id="@+id/barrier"
                    app:barrierDirection="end"
                    app:constraint_referenced_ids="button1,button2" />

                <Button
                    android:id="@+id/button1"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Button 1"
                    app:layout_constraintTop_toTopOf="parent"
                    app:layout_constraintStart_toStartOf="parent" />

                <Button
                    android:id="@+id/button2"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Button 2"
                    app:layout_constraintTop_toTopOf="parent"
                    app:layout_constraintStart_toStartOf="parent" />

            </com.yourpackage.CustomConstraintLayout>
        """.trimIndent()

        lint()
            .files(xml("res/layout/test.xml", xml))
            .run()
            .expectClean()
    }

    fun testFlowConstraints() {
        @Language("XML")
        val xml = """
            <com.yourpackage.CustomConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
                xmlns:app="http://schemas.android.com/apk/res-auto"
                android:layout_width="match_parent"
                android:layout_height="match_parent">

                <androidx.constraintlayout.widget.Flow
                    android:id="@+id/flow"
                    app:layout_constraintTop_toTopOf="parent"
                    app:layout_constraintStart_toStartOf="parent"
                    app:constraint_referenced_ids="button1,button2" />

                <Button
                    android:id="@+id/button1"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Button 1" />

                <Button
                    android:id="@+id/button2"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Button 2" />

            </com.yourpackage.CustomConstraintLayout>
        """.trimIndent()

        lint()
            .files(xml("res/layout/test.xml", xml))
            .run()
            .expectClean()
    }

    fun testHorizontallyUnconstrained() {
        @Language("XML")
        val xml = """
            <com.yourpackage.CustomConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
                xmlns:app="http://schemas.android.com/apk/res-auto"
                android:layout_width="match_parent"
                android:layout_height="match_parent">

                <Button
                    android:id="@+id/button1"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Button 1"
                    app:layout_constraintTop_toTopOf="parent" />

            </com.yourpackage.CustomConstraintLayout>
        """.trimIndent()

        lint()
            .files(xml("res/layout/test.xml", xml))
            .run()
            .expect(
                """
                res/layout/test.xml:6: Error: 이 뷰는 가로로 제약 조건이 없습니다. 가로 제약 조건을 추가해주세요. [MissingConstraints]
                    <Button
                     ~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }

    fun testVerticallyUnconstrained() {
        @Language("XML")
        val xml = """
            <com.yourpackage.CustomConstraintLayout xmlns:android="http://schemas.android.com/apk/res/android"
                xmlns:app="http://schemas.android.com/apk/res-auto"
                android:layout_width="match_parent"
                android:layout_height="match_parent">

                <Button
                    android:id="@+id/button1"
                    android:layout_width="wrap_content"
                    android:layout_height="wrap_content"
                    android:text="Button 1"
                    app:layout_constraintStart_toStartOf="parent" />

            </com.yourpackage.CustomConstraintLayout>
        """.trimIndent()

        lint()
            .files(xml("res/layout/test.xml", xml))
            .run()
            .expect(
                """
                res/layout/test.xml:6: Error: 이 뷰는 세로로 제약 조건이 없습니다. 세로 제약 조건을 추가해주세요. [MissingConstraints]
                    <Button
                     ~~~~~~
                1 errors, 0 warnings
                """.trimIndent()
            )
    }
}
