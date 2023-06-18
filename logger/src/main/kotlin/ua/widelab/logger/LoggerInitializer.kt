package ua.widelab.logger

import android.app.Application
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import ua.widelab.android_initializable.Initializable
import javax.inject.Inject

internal class LoggerInitializer @Inject constructor() : Initializable {

    override fun init(application: Application) {
        Napier.base(DebugAntilog())
    }
}