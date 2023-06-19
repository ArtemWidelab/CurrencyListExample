package ua.widelab.app_network.hilt

import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl("https://api.exchangerate.host")
            .addConverterFactory(
                Json {
                    encodeDefaults = true
                    ignoreUnknownKeys = true
                    useAlternativeNames = false
                    explicitNulls = false
                }.asConverterFactory(MediaType.parse("application/json")!!)
            )
            .build()
    }

}