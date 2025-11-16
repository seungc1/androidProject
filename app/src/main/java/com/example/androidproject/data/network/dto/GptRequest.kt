// data/network/dto/GptRequest.kt
data class GptRequest(
    val model: String,
    val messages: List<Message>,
    val response_format: ResponseFormat? = null // JSON 모드를 위한 설정
)

data class Message(
    val role: String, // "system", "user"
    val content: String
)

data class ResponseFormat(
    val type: String // "json_object"
)

// data/network/dto/GptResponse.kt
data class GptResponse(
    val choices: List<Choice>
)

data class Choice(
    val message: Message
    // message.content가 AI가 생성한 JSON 문자열이 됩니다.
)