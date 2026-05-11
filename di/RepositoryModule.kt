package com.circadianx.sleepsense.di

import com.circadianx.sleepsense.data.local.datastore.UserPreferencesRepositoryImpl
import com.circadianx.sleepsense.domain.repository.UserPreferencesRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindUserPreferencesRepository(
        impl: UserPreferencesRepositoryImpl
    ): UserPreferencesRepository
}

