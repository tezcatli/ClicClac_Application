package com.tezcatli.clicclac.di

import android.content.ContentResolver
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.tezcatli.clicclac.dataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.Executor


@InstallIn(SingletonComponent::class)
@Module
class ContextModule {
    @Provides
    fun provideDataStore(@ApplicationContext appContext: Context) : DataStore<Preferences> {
        return appContext.dataStore

    }

    @Provides
    fun provideContentResolver(@ApplicationContext appContext: Context) : ContentResolver {
        return appContext.contentResolver
    }

    @Provides
    fun provideExecutor(@ApplicationContext appContext: Context) : Executor {
        return appContext.mainExecutor
    }
}


/*
object DatabaseModule {
    @Provides
    fun provideLogDao(database: AppDatabase): LogDao {
        return database.logDao()
    }


    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): AppDatabase {
        return Room.databaseBuilder(
            appContext,
            AppDatabase::class.java,
            "logging.db"
        ).build()
    }

}
*/
