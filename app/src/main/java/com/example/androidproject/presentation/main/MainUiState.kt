package com.example.androidproject.presentation.main

import com.example.androidproject.domain.model.Diet
import com.example.androidproject.domain.model.Exercise

/**
 * '오늘의 운동' Todo 리스트 항목을 위한 UI용 래퍼 클래스.
 * Domain Model인 Exercise와 완료 상태(isCompleted)를 결합합니다.
 */
data class TodayExercise(
    val exercise: Exercise,
    var isCompleted: Boolean = false
)

/**
 * 메인 화면(대시보드)의 모든 UI 상태를 나타내는 데이터 클래스.
 * ViewModel은 이 State 객체 하나만 StateFlow를 통해 노출합니다.
 */
data class MainUiState(
    /** 데이터를 로드 중인지 여부 (로딩 스피너 표시용) */
    val isLoading: Boolean = true,

    /** 환영 메시지에 사용할 사용자 이름 */
    val userName: String = "",

    /** 현재 재활 중인 부상 이름 */
    val currentInjuryName: String? = null,

    /** '오늘의 운동' Todo 리스트 */
    val todayExercises: List<TodayExercise> = emptyList(),

    /** '오늘의 식단' 추천 목록 */
    val recommendedDiets: List<Diet> = emptyList(),

    /** 에러 발생 시 메시지 (Toast/Snackbar 표시용) */
    val errorMessage: String? = null
)
