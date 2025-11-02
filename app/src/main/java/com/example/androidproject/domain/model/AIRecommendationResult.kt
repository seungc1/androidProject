package com.example.androidproject.domain.model

data class AIRecommendationResult(
    val recommendedExercises: List<Exercise>,
    val recommendedDiets: List<Diet>
)