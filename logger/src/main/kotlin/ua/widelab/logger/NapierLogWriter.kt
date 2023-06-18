package ua.widelab.logger

import io.github.aakira.napier.Napier

class NapierLogWriter : LogWriter {

    override fun v(message: String, throwable: Throwable?, tag: String?) {
        Napier.v(message, throwable, tag)
    }

    override fun i(message: String, throwable: Throwable?, tag: String?) {
        Napier.i(message, throwable, tag)
    }

    override fun d(message: String, throwable: Throwable?, tag: String?) {
        Napier.d(message, throwable, tag)
    }

    override fun w(message: String, throwable: Throwable?, tag: String?) {
        Napier.w(message, throwable, tag)
    }

    override fun e(message: String, throwable: Throwable?, tag: String?) {
        Napier.e(message, throwable, tag)
    }

}