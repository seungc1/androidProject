package com.example.androidproject.presentation.main

import com.example.androidproject.domain.model.Diet
import com.example.androidproject.domain.model.Exercise
import com.example.androidproject.domain.model.ScheduledWorkout

data class TodayExercise(
    val exercise: Exercise,
    var isCompleted: Boolean = false
)

data class MainUiState(
    val isLoading: Boolean = true,
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