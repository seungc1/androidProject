package com.example.androidproject.data.repository

import com.example.androidproject.domain.model.DietSession
import com.example.androidproject.domain.repository.DietSessionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map // Flow의 map 연산자를 사용하기 위해 import
import kotlinx.coroutines.flow.flowOf // flowOf를 사용하여 단일 값 Flow를 생성
import java.util.UUID // 고유 ID 생성을 위해 사용
import javax.inject.Inject
import javax.inject.Singleton

// DietSessionRepository 인터페이스의 실제 구현체
// @Singleton 어노테이션은 Hilt에게 이 클래스의 인스턴스를 앱 전체에서 하나만 생성하도록 지시합니다.
@Singleton
class DietSessionRepositoryImpl @Inject constructor() : DietSessionRepository {

    // 더미 데이터를 저장할 리스트 (초기 개발용)
    // MutableStateFlow를 사용하여 데이터 변경 시 이를 구독하는 곳에 알립니다.
    private val _dietSessions = MutableStateFlow<List<DietSession>>(emptyList())

    // _dietSessions의 읽기 전용 Flow를 노출합니다.
    // .asSharedFlow()는 외부에서 Flow를 구독할 수 있도록 하며, List 변경 시 자동으로 새 값을 방출합니다.
    private val dietSessions: Flow<List<DietSession>> = _dietSessions.asSharedFlow()

    // 새로운 식단 섭취 기록을 추가하는 함수 구현
    override suspend fun addDietSession(session: DietSession): Flow<Unit> {
        // 실제 구현에서는 로컬 DB 또는 서버 API에 기록을 추가합니다.
        // 현재는 더미 리스트에 추가합니다.

        // 전달받은 세션에 고유 ID를 부여 (ID가 이미 있다면 그대로 사용, 없다면 새로 생성)
        val newSession = session.copy(id = session.id.ifEmpty { UUID.randomUUID().toString() })

        // _dietSessions.value를 업데이트하여 새 세션을 추가하고, 구독자들에게 알립니다.
        _dietSessions.value = _dietSessions.value + newSession

        // 성공적으로 작업했음을 나타내는 Unit Flow를 반환합니다.
        return flowOf(Unit)
    }

    // 특정 사용자의 식단 섭취 기록 목록을 가져오는 함수 구현
    override suspend fun getDietHistory(userId: String): Flow<List<DietSession>> {
        // 실제 구현에서는 로컬 DB 또는 서버 API에서 특정 사용자의 기록을 가져옵니다.
        // 현재는 더미 리스트에서 해당 사용자의 기록만 필터링하여 반환합니다.

        // _dietSessions Flow에서 모든 기록을 가져온 후, userId로 필터링하여 새로운 Flow를 생성합니다.
        return _dietSessions.map { sessions -> sessions.filter { it.userId == userId } }
    }
}