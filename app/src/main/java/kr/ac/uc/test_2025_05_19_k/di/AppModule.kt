package kr.ac.uc.test_2025_05_19_k.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kr.ac.uc.test_2025_05_19_k.network.SessionManager
import javax.inject.Singleton
import kr.ac.uc.test_2025_05_19_k.repository.ProfileCacheManager

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Singleton
    @Provides
    fun provideProfileCacheManager(@ApplicationContext context: Context): ProfileCacheManager {
        return ProfileCacheManager(context)
    }

    @Provides
    @Singleton
    fun provideSessionManager(): SessionManager {
        return SessionManager()
    }
}
