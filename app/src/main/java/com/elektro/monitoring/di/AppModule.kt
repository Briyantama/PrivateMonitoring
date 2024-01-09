package com.elektro.monitoring.di

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import com.elektro.monitoring.data.repo.AuthRepository
import com.elektro.monitoring.data.repo.NotificationRepository
import com.elektro.monitoring.helper.sharedpref.SharedPrefData
import com.elektro.monitoring.viewmodel.DataViewModel
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.internal.managers.ApplicationComponentManager
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    fun provideContext(@ApplicationContext appContext: Context): Context {
        return appContext
    }

    @Provides
    fun provideSharedPreferences(application: Application): SharedPrefData {
        return SharedPrefData(application)
    }

    @Provides
    fun provideAuthRepository(sharedPreferences: SharedPrefData): AuthRepository {
        return AuthRepository(sharedPreferences)
    }

    @Provides
    fun provideNotificationRepository(): NotificationRepository {
        return NotificationRepository()
    }

    @Provides
    @Singleton
    fun provideDataViewModel(sharedPrefData: SharedPrefData): DataViewModel {
        return DataViewModel(sharedPrefData)
    }
}
