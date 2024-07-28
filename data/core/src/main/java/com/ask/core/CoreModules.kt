package com.ask.core

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object CoreModules {

    @Provides
    @Singleton
    @Named(DISPATCHER_IO)
    fun provideDispatcherIO(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @Singleton
    @Named(DISPATCHER_DEFAULT)
    fun provideDispatcherDefault(): CoroutineDispatcher = Dispatchers.Default
}

