package ua.widelab.logger

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import ua.widelab.android_initializable.Initializable

@Module
@InstallIn(SingletonComponent::class)
abstract class LoggerModule {

    @Binds
    @IntoSet
    internal abstract fun bindLogger(loggerInitializer: LoggerInitializer): Initializable
}