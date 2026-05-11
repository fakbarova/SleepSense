package com.circadianx.sleepsense.di

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.circadianx.sleepsense.BuildConfig
import com.circadianx.sleepsense.data.db.SleepSenseDatabase
import com.circadianx.sleepsense.data.network.AuthHeaderInterceptor
import com.google.gson.Gson
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "settings")

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides @Singleton
    fun provideDatabase(@ApplicationContext ctx: Context): SleepSenseDatabase =
        Room.databaseBuilder(ctx, SleepSenseDatabase::class.java, "sleepsense.db").apply {
            if (BuildConfig.DEBUG) {
                fallbackToDestructiveMigration()
            }
        }.build()

    @Provides fun provideSleepSessionDao(db: SleepSenseDatabase) = db.sleepSessionDao()
    @Provides fun provideApneaEventDao(db: SleepSenseDatabase) = db.apneaEventDao()
    @Provides fun provideSleepRecordDao(db: SleepSenseDatabase) = db.sleepRecordDao()
    @Provides fun provideNightDisturbanceDao(db: SleepSenseDatabase) = db.nightDisturbanceDao()
    @Provides fun provideRoutineDao(db: SleepSenseDatabase) = db.routineDao()
    @Provides fun provideStepDao(db: SleepSenseDatabase) = db.stepDao()
    @Provides fun provideAppBlockOverrideDao(db: SleepSenseDatabase) = db.appBlockOverrideDao()
    @Provides fun provideChallengeDao(db: SleepSenseDatabase) = db.challengeDao()
    @Provides fun provideProgressPhotoDao(db: SleepSenseDatabase) = db.progressPhotoDao()
    @Provides fun provideSocialDao(db: SleepSenseDatabase) = db.socialDao()

    @Provides @Singleton
    fun provideDataStore(@ApplicationContext ctx: Context): DataStore<Preferences> = ctx.dataStore

    @Provides @Singleton
    fun provideBluetoothAdapter(@ApplicationContext ctx: Context): BluetoothAdapter? =
        (ctx.getSystemService(Context.BLUETOOTH_SERVICE) as? BluetoothManager)?.adapter

    @Provides @Singleton
    fun provideGson(): Gson = Gson()

    @Provides @Singleton
    fun provideOkHttpClient(authHeaderInterceptor: AuthHeaderInterceptor): OkHttpClient {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BASIC
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
        return OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(authHeaderInterceptor)
            .addInterceptor(logging)
            .build()
    }
}
