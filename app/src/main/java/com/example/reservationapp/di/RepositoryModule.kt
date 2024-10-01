package com.example.reservationapp.di

import com.example.reservationapp.data.repository.ProviderRepository
import com.example.reservationapp.data.repository.ProviderRepositoryFakeImpl
import com.example.reservationapp.data.repository.UserRepository
import com.example.reservationapp.data.repository.UserRepositoryFakeImpl
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
    abstract fun bindProviderRepository(
        impl: ProviderRepositoryFakeImpl
    ): ProviderRepository

    @Binds
    @Singleton
    abstract fun bindUserRepository(
        impl: UserRepositoryFakeImpl
    ): UserRepository
}