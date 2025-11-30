package com.dataDoctor.rehabai.presentation.main

import com.dataDoctor.rehabai.domain.model.Diet
import com.dataDoctor.rehabai.domain.model.Exercise
import com.dataDoctor.rehabai.domain.model.ScheduledWorkout

data class TodayExercise(
    val exercise: Exercise,
    var isCompleted: Boolean = false
)

data class MainUiState(
    val isLoading: Boolean = true,
    val isRoutineLoading: Boolean = true,      // 👈 [추가] 운동/식단 컨텐츠 로딩 상태 (핵심)
    val userName: String = "",
    val currentInjuryName: String? = null,
    val currentInjuryArea: String? = null,
    val fullRoutine: List<ScheduledWorkout> = emptyList(),
    val todayExercises: List<TodayExercise> = emptyList(),
    val recommendedDiets: List<Diet> = emptyList(),
    val errorMessage: String? = null,

    // (★추가★) 프로필 입력 완료 여부 (이 값이 false면 입력을 강제함)
    val isProfileComplete: Boolean = false
)