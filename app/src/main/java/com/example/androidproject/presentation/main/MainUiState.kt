package com.example.androidproject.presentation.main

import com.example.androidproject.domain.model.Diet
import com.example.androidproject.domain.model.Exercise

/**
 * [새 파일 1/5]
 * '운동 to-do 리스트' 항목을 위한 UI용 래퍼 클래스입니다.
 * '필드값'인 Exercise와 '체크 상태'(isCompleted)를 '결합'합니다.
 */
data class TodayExercise(
    val exercise: Exercise,
    var isCompleted: Boolean = false
)

/**
 * '홈' 화면(대시보드)의 모든 UI 상태를 나타내는 데이터 클래스입니다.
 * RehabViewModel은 이 State 객체 하나만 StateFlow를 통해 노출합니다.
 *
 * (★ 수정 ★) 'ProfileFragment'에서 '환부(부위)'를 '표시'하기 위해
 * 'currentInjuryArea' 필드를 '추가'합니다.
 */
data class MainUiState(
    /** 데이터를 로드 중인지 여부 (로딩 스피너 표시용) */
    val isLoading: Boolean = true,

    /** 환영 메시지에 사용할 사용자 이름 */
    val userName: String = "",

    /** 현재 재활 중인 질환명 (예: 손목 염좌) */
    val currentInjuryName: String? = null,

    /** (★ 추가 ★) 현재 재활 중인 환부 (예: 손목) */
    val currentInjuryArea: String? = null,

    /** '오늘의 운동' Todo 리스트 */
    val todayExercises: List<TodayExercise> = emptyList(),

    /** '오늘의 식단' 추천 목록 */
    val recommendedDiets: List<Diet> = emptyList(),

    /** 에러 발생 시 메시지 (Toast/Snackbar 표시용) */
    val errorMessage: String? = null
)