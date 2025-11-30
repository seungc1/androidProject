package com.dataDoctor.rehabai.data.network.model

/**
 * GPT API 요청 DTO
 */
data class GptRequest(
    val model: String,
    val messages: List<GptMessage>,
    val response_format: ResponseFormat? = null, // JSON 모드 요청
    val max_tokens: Int? = null // 최대 토큰 수 제한
)

/**
 * GPT API 응답 DTO
 */
data class GptResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: GptMessage
)

data class GptMessage(
    val role: String, // "system", "user"
    val content: String
)

data class ResponseFormat(
    val type: String // "json_object"
)