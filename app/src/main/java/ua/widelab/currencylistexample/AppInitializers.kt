package ua.widelab.currencylistexample

import android.app.Application
import ua.widelab.android_initializable.Initializable
import javax.inject.Inject

class AppInitializers @Inject constructor(
    private val initializers: Set<@JvmSuppressWildcards Initializable>
) {

    fun init(application: Application) {
        initializers.forEach { it.init(application) }
    }
}