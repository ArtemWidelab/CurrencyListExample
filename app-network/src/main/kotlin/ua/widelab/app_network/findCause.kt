package ua.widelab.app_network

inline fun <reified T : Throwable> findCause(throwable: Throwable): T? {
    var error: Throwable? = throwable
    while (error != null && error !is T) {
        error = error.cause
    }
    return error as? T
}