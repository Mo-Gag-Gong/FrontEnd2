// app/src/main/java/kr/ac/uc/test_2025_05_19_k/di/NetworkModule.kt
package kr.ac.uc.test_2025_05_19_k.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import dagger.Lazy
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.ac.uc.test_2025_05_19_k.network.ApiService
import kr.ac.uc.test_2025_05_19_k.network.AuthInterceptor
import kr.ac.uc.test_2025_05_19_k.network.SessionManager
import kr.ac.uc.test_2025_05_19_k.network.api.GoalApiService
import kr.ac.uc.test_2025_05_19_k.network.api.GroupApi
import kr.ac.uc.test_2025_05_19_k.network.api.UserApi
import kr.ac.uc.test_2025_05_19_k.network.api.UserApiService
import kr.ac.uc.test_2025_05_19_k.repository.TokenManager
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 네트워크 및 API DI 모듈
 * 싱글톤 객체로 Gson, OkHttp, Retrofit, ApiService, TokenManager 등 제공
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {


    private const val BASE_URL =
        "http://springboot-developer-env.eba-mikwqecm.ap-northeast-2.elasticbeanstalk.com/"

    /**
     * Gson 객체 제공 (JSON 파싱에 사용)
     */
    @Provides
    @Singleton
    fun provideGson(): Gson = GsonBuilder().setLenient().create()

    /**
     * TokenManager 싱글톤 제공 (앱 전체 인증 토큰 관리)
     */
    @Provides
    @Singleton
    fun provideTokenManager(@ApplicationContext context: Context): TokenManager =
        TokenManager(context)

    /**
     * AuthInterceptor 싱글톤 제공 (모든 HTTP 요청에 토큰 자동 추가)
     */
    @Provides
    @Singleton
    fun provideAuthInterceptor(
        tokenManager: TokenManager,
        apiService: Lazy<ApiService>,
        sessionManager: SessionManager // ✅ 추가
    ): AuthInterceptor {
        return AuthInterceptor(tokenManager, apiService, sessionManager)
    }



    /**
     * OkHttpClient 싱글톤 제공 (인터셉터 포함, 타임아웃 30초)
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(
        authInterceptor: AuthInterceptor
    ): OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    /**
     * Retrofit 싱글톤 제공 (기본 URL, Gson 컨버터, OkHttp 클라이언트 사용)
     */
    @Provides
    @Singleton
    fun provideRetrofit(
        client: OkHttpClient,
        gson: Gson
    ): Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    /**
     * ApiService 싱글톤 제공 (통합 서버 API)
     */
    @Provides
    @Singleton
    fun provideApiService(retrofit: Retrofit): ApiService =
        retrofit.create(ApiService::class.java)

    /**
     * UserApi 싱글톤 제공 (유저 전용 API)
     */
    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi =
        retrofit.create(UserApi::class.java)

    /**
     * GroupApi 싱글톤 제공 (그룹 전용 API)
     */
    @Provides
    @Singleton
    fun provideGroupApi(retrofit: Retrofit): GroupApi =
        retrofit.create(GroupApi::class.java)

    @Provides
    @Singleton
    fun provideGoalApiService(retrofit: Retrofit): GoalApiService =
        retrofit.create(GoalApiService::class.java)

    @Provides
    @Singleton
    fun provideUserApiService(retrofit: Retrofit): UserApiService {
        return retrofit.create(UserApiService::class.java)
    }

}
