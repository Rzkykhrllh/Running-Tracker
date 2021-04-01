package com.example.runningtracking.di

import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.room.Room
import com.example.runningtracking.db.RunningDatabase
import com.example.runningtracking.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.example.runningtracking.other.Constants.KEY_FOR_NAME
import com.example.runningtracking.other.Constants.KEY_FOR_WEIGHT
import com.example.runningtracking.other.Constants.RUNNING_DATABASE_NAME
import com.example.runningtracking.other.Constants.SHARED_PREFERENCES_NAME
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ApplicationComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

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

    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext app : Context) =
        app.getSharedPreferences(SHARED_PREFERENCES_NAME,  MODE_PRIVATE)


    @Singleton
    @Provides
    fun provideName(sharedPreferences: SharedPreferences) =
        sharedPreferences.getString(KEY_FOR_NAME, "default name") ?: "default name"

    @Singleton
    @Provides
    fun provideWeight(sharedPreferences: SharedPreferences) =
        sharedPreferences.getFloat(KEY_FOR_WEIGHT, 8F)

    @Singleton
    @Provides
    fun provideFirstTimeToggle(sharedPreferences: SharedPreferences) =
        sharedPreferences.getBoolean(KEY_FIRST_TIME_TOGGLE, true)
}
