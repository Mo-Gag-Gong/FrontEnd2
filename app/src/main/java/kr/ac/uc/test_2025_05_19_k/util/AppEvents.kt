package kr.ac.uc.test_2025_05_19_k.util

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 앱 전역에서 발생하는 이벤트를 정의합니다.
 */
sealed class RefreshEvent {
    data object LocationUpdated : RefreshEvent()
}

/**
 * SharedFlow를 사용해 앱의 여러 부분으로 이벤트를 전달하는 싱글턴 클래스입니다.
 */
@Singleton
class AppEvents @Inject constructor() {
    private val _events = MutableSharedFlow<RefreshEvent>()
    val events = _events.asSharedFlow()

    suspend fun emitEvent(event: RefreshEvent) {
        _events.emit(event)
    }
}