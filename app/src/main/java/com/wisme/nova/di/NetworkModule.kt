package com.wisme.nova.di

import com.wisme.nova.BuildConfig
import com.wisme.nova.data.remote.AuthApiService
import com.wisme.nova.data.remote.PodcastApiService
import com.wisme.nova.data.remote.EpisodeApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * Dependency Injection module for network-related dependencies
 * 
 * This module provides singleton instances of:
 * - OkHttpClient with logging and timeouts
 * - Retrofit instance configured for your API
 * - API service interfaces
 * 
 * @author Wisme Labs
 * @since 1.0.0
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Provides OkHttpClient with logging and timeouts
     */
    @Provides
    @Singleton
    fun provideOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder().apply {
            // Add logging interceptor for debug builds
            if (BuildConfig.DEBUG) {
                val loggingInterceptor = HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                }
                addInterceptor(loggingInterceptor)
            }
            
            // Set timeouts
            connectTimeout(30, TimeUnit.SECONDS)
            readTimeout(30, TimeUnit.SECONDS)
            writeTimeout(30, TimeUnit.SECONDS)
            
            // Add auth interceptor (you'll implement this later)
            // addInterceptor(AuthInterceptor())
            
        }.build()
    }

    /**
     * Provides Retrofit instance
     */
    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    /**
     * Provides AuthApiService
     */
    @Provides
    @Singleton
    fun provideAuthApiService(retrofit: Retrofit): AuthApiService {
        return retrofit.create(AuthApiService::class.java)
    }

    /**
     * Provides PodcastApiService
     */
    @Provides
    @Singleton
    fun providePodcastApiService(retrofit: Retrofit): PodcastApiService {
        return retrofit.create(PodcastApiService::class.java)
    }

    /**
     * Provides EpisodeApiService
     */
    @Provides
    @Singleton
    fun provideEpisodeApiService(retrofit: Retrofit): EpisodeApiService {
        return retrofit.create(EpisodeApiService::class.java)
    }
}

/**
 * TODO: Repository module
 * 
 * When you implement repository classes, create this module:
 * 
 * @Module
 * @InstallIn(SingletonComponent::class)
 * abstract class RepositoryModule {
 * 
 *     @Binds
 *     abstract fun bindAuthRepository(
 *         authRepositoryImpl: AuthRepositoryImpl
 *     ): AuthRepository
 * 
 *     @Binds
 *     abstract fun bindPodcastRepository(
 *         podcastRepositoryImpl: PodcastRepositoryImpl
 *     ): PodcastRepository
 * }
 */