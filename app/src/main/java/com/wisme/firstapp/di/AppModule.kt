package com.wisme.firstapp.di

import android.content.Context
import com.wisme.firstapp.data.local.AuthPreferences
import com.wisme.firstapp.data.api.AuraApiService
import com.wisme.firstapp.data.repository.AuthRepository
import com.wisme.firstapp.data.repository.ConnectivityRepository
import com.wisme.firstapp.data.repository.JourneyRepository
import com.wisme.firstapp.util.HttpLoggingInterceptor
import com.google.firebase.auth.FirebaseAuth
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    
    @Provides
    @Singleton
    fun provideAuthPreferences(@ApplicationContext context: Context): AuthPreferences {
        return AuthPreferences(context)
    }
    
    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth {
        return FirebaseAuth.getInstance()
    }
    
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        val customLoggingInterceptor = HttpLoggingInterceptor()
        
        return OkHttpClient.Builder()
            .addInterceptor(customLoggingInterceptor)
            .build()
    }
    
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://8eb80c8f633d.ngrok-free.app/api/v1/") // Live backend from team - updated with new ngrok URL
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideAuraApiService(retrofit: Retrofit): AuraApiService {
        return retrofit.create(AuraApiService::class.java)
    }
    
    @Provides
    @Singleton
    fun provideAuthRepository(
        apiService: AuraApiService,
        authPrefs: AuthPreferences
    ): AuthRepository {
        return AuthRepository(apiService, authPrefs)
    }
    
    @Provides
    @Singleton
    fun provideConnectivityRepository(
        apiService: AuraApiService
    ): ConnectivityRepository {
        return ConnectivityRepository(apiService)
    }
    
    @Provides
    @Singleton
    fun provideJourneyRepository(
        apiService: AuraApiService,
        connectivityRepository: ConnectivityRepository
    ): JourneyRepository {
        return JourneyRepository(apiService, connectivityRepository)
    }
}
