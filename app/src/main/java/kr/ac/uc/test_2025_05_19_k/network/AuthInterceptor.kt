// app/src/main/java/kr/ac/uc/test_2025_05_19_k/network/AuthInterceptor.kt
package kr.ac.uc.test_2025_05_19_k.network

import android.util.Log
import dagger.Lazy
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import kr.ac.uc.test_2025_05_19_k.model.RefreshTokenRequest
import kr.ac.uc.test_2025_05_19_k.repository.TokenManager
import kr.ac.uc.test_2025_05_19_k.network.SessionManager

/**
 * 모든 HTTP 요청에 Authorization 헤더 자동 추가 + 401 응답시 토큰 자동 갱신
 */
class AuthInterceptor(
    private val tokenManager: TokenManager,
    private val apiService: Lazy<ApiService>, // Lazy로 순환참조 방지
    private val sessionManager: SessionManager // ✅ ViewModel과 연결된 NavController 연동
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val accessToken = tokenManager.getAccessToken()

        // 1. accessToken이 있으면 Authorization 헤더에 추가
        if (!accessToken.isNullOrBlank()) {
            request = request.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
            Log.d("AuthInterceptor", "Authorization 헤더 추가됨. (AccessToken 앞 10글자: ${accessToken.take(10)}...)")
        } else {
            Log.w("AuthInterceptor", "AccessToken이 없습니다. 인증 없이 요청합니다.")
        }

        var response = chain.proceed(request)
        Log.d("AuthInterceptor", "응답 코드: ${response.code} for URL: ${response.request.url}")

        // 2. 401(Unauthorized) 응답이 오면 refresh 시도
        if (response.code == 401) {
            Log.w("AuthInterceptor", "401 Unauthorized 응답. 토큰 갱신 시도...")
            runBlocking {
                val refreshed = refreshTokenIfNeeded(tokenManager, apiService.get(), sessionManager)

                if (refreshed) {
                    val newAccessToken = tokenManager.getAccessToken()
                    val newRequest = request.newBuilder()
                        .removeHeader("Authorization")
                        .addHeader("Authorization", "Bearer $newAccessToken")
                        .build()
                    response.close()
                    Log.d("AuthInterceptor", "토큰 갱신 성공, 새로운 AccessToken으로 재요청.")
                    response = chain.proceed(newRequest)
                } else {
                    tokenManager.clearTokens()
                    Log.e("AuthInterceptor", "토큰 갱신 실패! 모든 토큰 삭제.")
                    sessionManager.notifyLogout() // ✅ 안전한 화면 전환 요청
                }
            }
        }
        return response
    }

    /**
     * 401 발생 시 리프레시 토큰으로 토큰 갱신 시도
     * @return true: 갱신 성공, false: 실패(로그아웃 필요)
     */
    suspend fun refreshTokenIfNeeded(
        tokenManager: TokenManager,
        apiService: ApiService,
        sessionManager: SessionManager // ✅ 추가된 인자
    ): Boolean {
        val refreshToken = tokenManager.getRefreshToken() ?: run {
            Log.w("AuthInterceptor", "리프레시 토큰이 없어 갱신할 수 없습니다.")
            return false
        }

        Log.d("AuthInterceptor", "리프레시 토큰으로 갱신 요청 중...")

        val response = apiService.refreshToken(RefreshTokenRequest(refreshToken))
        return if (response.isSuccessful && response.body() != null) {
            val tokenRes = response.body()!!
            tokenManager.saveTokens(tokenRes.accessToken, tokenRes.refreshToken, tokenRes.userId)
            Log.d("AuthInterceptor", "리프레시 토큰으로 AccessToken 갱신 성공.")
            true
        } else {
            tokenManager.clearTokens()
            Log.e("AuthInterceptor", "토큰 갱신 실패! 로그아웃 알림 전파")
            sessionManager.notifyLogout() // ✅ 로그아웃 이벤트 발생
            false
        }
    }
}

