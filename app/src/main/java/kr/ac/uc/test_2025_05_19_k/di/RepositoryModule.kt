package kr.ac.uc.test_2025_05_19_k.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kr.ac.uc.test_2025_05_19_k.network.ApiService
import kr.ac.uc.test_2025_05_19_k.network.api.GroupApi
import kr.ac.uc.test_2025_05_19_k.network.api.UserApi
import kr.ac.uc.test_2025_05_19_k.network.api.UserApiService
import kr.ac.uc.test_2025_05_19_k.repository.GroupRepository
import kr.ac.uc.test_2025_05_19_k.repository.ProfileRepository
import kr.ac.uc.test_2025_05_19_k.repository.UserRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideGroupRepository(api: GroupApi): GroupRepository {
        return GroupRepository(api)
    }

    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: ApiService,
        userApi: UserApi,
        userApiService: UserApiService
    ): UserRepository {
        return UserRepository(apiService, userApi, userApiService)
    }

    @Provides
    @Singleton
    fun provideProfileRepository(
        apiService: ApiService,
        userApi: UserApi,
        userApiService: UserApiService
    ): ProfileRepository {
        return ProfileRepository(apiService, userApi, userApiService)
    }




}
