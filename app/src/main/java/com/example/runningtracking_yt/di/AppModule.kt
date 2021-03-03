package com.example.runningtracking_yt.di

import android.content.Context
import androidx.room.Room
import com.example.runningtracking_yt.db.RunningDatabase
import com.example.runningtracking_yt.other.Constatns.RUNNING_DATABASE_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext

@Module
@InstallIn(ApplicationComponent::class) // Kapan objek dalam AppModule dibuat dan dihancurkan
object AppModule {

    @Provides
    fun provideRunningDatabase(
        @ApplicationContext app : Context //Dagger bakal ambil app dari Application
    ) = Room.databaseBuilder(
        app,
        RunningDatabase::class.java,
        RUNNING_DATABASE_NAME // nama database
    ).build()

    @Provides
    fun provideRunDao(db : RunningDatabase) = db.getRunDao()
}