package com.wisme.firstapp.di

import android.content.Context
import com.wisme.firstapp.data.local.AuthPreferences
import com.wisme.firstapp.data.api.AuraApiService
import com.wisme.firstapp.data.api.HealthApiService
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
import kotlinx.serialization.json.Json
import javax.inject.Singleton
import javax.inject.Named

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
            .baseUrl("https://aura-backend-ok92.onrender.com/api/v1/") // Production backend - verified working September 27, 2025
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
    @Named("health")
    fun provideHealthRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://aura-backend-ok92.onrender.com/") // Root level for health checks
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    @Provides
    @Singleton
    fun provideHealthApiService(@Named("health") retrofit: Retrofit): HealthApiService {
        return retrofit.create(HealthApiService::class.java)
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
        apiService: AuraApiService,
        healthApiService: HealthApiService
    ): ConnectivityRepository {
        return ConnectivityRepository(apiService, healthApiService)
    }
    
    @Provides
    @Singleton
    fun provideJourneyRepository(
        apiService: AuraApiService,
        connectivityRepository: ConnectivityRepository,
        authPrefs: AuthPreferences
    ): JourneyRepository {
        return JourneyRepository(apiService, connectivityRepository, authPrefs)
    }
    
    @Provides
    @Singleton
    fun provideUserRepository(
        apiService: AuraApiService,
        authPrefs: AuthPreferences
    ): com.wisme.firstapp.data.repository.UserRepository {
        return com.wisme.firstapp.data.repository.UserRepository(apiService, authPrefs)
    }
    
    @Provides
    @Singleton
    fun provideJson(): Json {
        return Json {
            ignoreUnknownKeys = true
            isLenient = true
            encodeDefaults = false
        }
    }
    
    @Provides
    @Singleton
    fun provideNovaDatabase(@ApplicationContext context: Context): com.wisme.firstapp.data.local.database.NovaDatabase {
        return com.wisme.firstapp.data.local.database.NovaDatabase.getInstance(context)
    }
    
    // Feedback system DAOs
    @Provides
    fun provideFeedbackQuestionDao(database: com.wisme.firstapp.data.local.database.NovaDatabase): com.wisme.firstapp.data.local.dao.FeedbackQuestionDao {
        return database.feedbackQuestionDao()
    }
    
    @Provides
    fun provideFeedbackSubmissionDao(database: com.wisme.firstapp.data.local.database.NovaDatabase): com.wisme.firstapp.data.local.dao.FeedbackSubmissionDao {
        return database.feedbackSubmissionDao()
    }
    
    // User management DAOs
    @Provides
    fun provideUserProfileDao(database: com.wisme.firstapp.data.local.database.NovaDatabase): com.wisme.firstapp.data.local.dao.UserProfileDao {
        return database.userProfileDao()
    }
    
    @Provides
    fun provideUserProgressDao(database: com.wisme.firstapp.data.local.database.NovaDatabase): com.wisme.firstapp.data.local.dao.UserProgressDao {
        return database.userProgressDao()
    }
    
    @Provides
    @Singleton
    fun provideNetworkConnectivityManager(@ApplicationContext context: Context): com.wisme.firstapp.data.network.NetworkConnectivityManager {
        return com.wisme.firstapp.data.network.NetworkConnectivityManager(context)
    }
    
    @Provides
    @Singleton
    fun provideFeedbackSyncManager(@ApplicationContext context: Context): com.wisme.firstapp.data.sync.FeedbackSyncManager {
        return com.wisme.firstapp.data.sync.FeedbackSyncManager(context)
    }
}
