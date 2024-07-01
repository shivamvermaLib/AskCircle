package com.ask.app.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Named

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModules {

    @Provides
    @Named("IO")
    fun provideDispatcherIO(): CoroutineDispatcher = Dispatchers.IO
}