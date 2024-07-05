# android-custom-lint-sample

> super.init(version=5) 발표 자료 코드 샘플

## DtoDefaultValueDetector

> kotlinx.serialization dto 경우 기본값을 꼭 할당할 수 있게 하는 Lint

[코드](https://github.com/lee-ji-hoon/android-custom-lint-sample/blob/main/lint/src/main/kotlin/com/superinit_lint_sample/DtoDefaultValueDetector.kt)

<img width="715" alt="image" src="https://github.com/lee-ji-hoon/android-custom-lint-sample/assets/53300830/a704501c-6366-458b-bf1a-2064c099e14c">

<img width="658" alt="image" src="https://github.com/lee-ji-hoon/android-custom-lint-sample/assets/53300830/3df20201-7ee5-4e27-88a3-4d8604a8a301">

## CustomConstraintLayoutDetector

> ConstraintLayout을 상속 받은 View를 사용할 때도 자식이 Constraint를 걸었는지 확인하는 lint

[코드](https://github.com/lee-ji-hoon/android-custom-lint-sample/blob/main/lint/src/main/kotlin/com/superinit_lint_sample/CustomConstraintLayoutDetector.kt)

[Android-Code-Search-ConstraintLayoutDetector](https://cs.android.com/android-studio/platform/tools/base/+/mirror-goog-studio-main:lint/libs/lint-checks/src/main/java/com/android/tools/lint/checks/ConstraintLayoutDetector.kt)

Android 내부의 `ConstraintLayoutDetector` 인데 내부 코드를 보면 상속 받은 View 까지는 검사를 안하고 있어서 상속받은 `ConstraintLayout` 을 사용하면 자식 View에 제약을 실수로 놓치기도 하는 상황이 생겨서 만든 Lint

![image](https://github.com/lee-ji-hoon/android-custom-lint-sample/assets/53300830/ca27498b-db17-44d8-89ea-b5d06f797667)

<img width="670" alt="image" src="https://github.com/lee-ji-hoon/android-custom-lint-sample/assets/53300830/677cff57-7aab-4acc-b60a-47ba9354793f">



