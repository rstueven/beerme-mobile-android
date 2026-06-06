package com.beerme.data

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore
import androidx.room.Room
import com.beerme.data.local.AppDatabase
import com.beerme.data.remote.BreweryApiService
import com.beerme.data.remote.FlexibleDoubleAdapter
import com.beerme.data.repository.BreweryRepository
import com.beerme.data.repository.UserPreferencesRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

private const val USER_PREFERENCES_NAME = "beer_me_preferences"
private val Context.dataStore by preferencesDataStore(
    name = USER_PREFERENCES_NAME
)

class AppContainer(context: Context) {
    private val database: AppDatabase by lazy {
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "beer_me_db"
        ).fallbackToDestructiveMigration()
            .build()
    }

    private val moshi = Moshi.Builder()
        .add(FlexibleDoubleAdapter())
        .add(KotlinJsonAdapterFactory())
        .build()

    private val okHttpClient = OkHttpClient.Builder()
        // The initial full sync downloads multi-megabyte JSON payloads;
        // OkHttp's 10s default read timeout is not enough on slow links.
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(120, TimeUnit.SECONDS)
        .build()

    private val apiService: BreweryApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://beerme.com/")
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(BreweryApiService::class.java)
    }

    val userPreferencesRepository: UserPreferencesRepository by lazy {
        UserPreferencesRepository(context.dataStore)
    }

    val repository: BreweryRepository by lazy {
        BreweryRepository(
            database.breweryDao(),
            database.beerDao(),
            database.tastingNoteDao(),
            apiService,
            userPreferencesRepository
        )
    }
}
