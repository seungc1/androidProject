// app/src/main/java/com.example/androidproject/domain/model/AIAnalysisResult.kt

package com.dataDoctor.rehabai.domain.model

/**
 * AI (GPT) 모델로부터 받은 재활 진행 상황 분석 결과 및 피드백.
 * GPT의 추론 능력을 활용하여 사용자의 상태 변화를 해석하고 개선 방향을 제시합니다.
 */
data class AIAnalysisResult(
    val summary: String, // 분석 결과 요약
    val strengths: List<String>, // 잘하고 있는 점
    val areasForImprovement: List<String>, // 개선이 필요한 점
    val personalizedTips: List<String>, // 사용자 맞춤형 조언
    val nextStepsRecommendation: String, // 다음 단계에 대한 권장 사항
    val disclaimer: String = "본 분석은 AI에 의해 생성되었으며, 전문 의료인의 진단 및 조언을 대체할 수 없습니다."
)