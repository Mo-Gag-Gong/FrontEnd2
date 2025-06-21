package kr.ac.uc.test_2025_05_19_k.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.flow.collect
import javax.inject.Singleton

@Singleton
class SessionManager @Inject constructor() {

    private val _logoutEvent = MutableSharedFlow<Unit>()

    // ✅ 외부에서 접근 가능하게 공유용 Flow 제공
    val logoutFlow: SharedFlow<Unit> = _logoutEvent

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())

    fun notifyLogout() {
        scope.launch {
            _logoutEvent.emit(Unit) // ✅ 내부에서는 _logoutEvent 사용
        }
    }
}
