// app/src/main/java/kr/ac/uc/test_2025_05_19_k/di/SessionManagerEntryPoint.kt
package kr.ac.uc.test_2025_05_19_k.di

import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.ac.uc.test_2025_05_19_k.network.SessionManager

@EntryPoint
@InstallIn(SingletonComponent::class)
interface SessionManagerEntryPoint {
    fun sessionManager(): SessionManager
}
