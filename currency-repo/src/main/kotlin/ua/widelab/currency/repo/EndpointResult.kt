package ua.widelab.currency.repo

import com.github.kittinunf.result.Result
import com.github.kittinunf.result.getFailureOrNull
import com.github.kittinunf.result.isSuccess
import kotlinx.coroutines.flow.*
import ua.widelab.currency.api.CurrencyApiThrowable

data class EndpointResult<T>(
    val data: T?,
    val isLoading: Boolean,
    val error: Throwable?
)

sealed class RequestState {
    object Success : RequestState()
    data class Error(val throwable: Throwable) : RequestState()
    object Loading : RequestState()
}

enum class CacheMechanics {
    FORCE, //cache + new
    NETWORK_ONLY, //only new
    CACHE_ONLY, //only cache
    DEFAULT //cache + maybe new
}

//T - data from cache
//R - data from network
abstract class Endpoint<T, R> {

    abstract val cache: Flow<T>
    abstract val network: Flow<Result<R, Throwable>>

    abstract suspend fun store(data: R)

    abstract suspend fun shouldMakeRequest(cache: T): Boolean

    private fun load(): Flow<RequestState> {
        return network
            .onEach {
                if (it.isSuccess()) {
                    store(it.value)
                }
            }
            .map {
                if (it.isSuccess()) {
                    RequestState.Success
                } else {
                    RequestState.Error(it.getFailureOrNull()!!)
                }
            }
            .onStart { emit(RequestState.Loading) }
    }

    fun get(cacheMechanics: CacheMechanics = CacheMechanics.DEFAULT): Flow<EndpointResult<T>> {
        return when (cacheMechanics) {
            CacheMechanics.FORCE -> load()
                .flatMapLatest { networkState ->
                    cache.map { data ->
                        val throwable = (networkState as? RequestState.Error)?.throwable
                        EndpointResult(
                            data = data,
                            error = when (throwable) {
                                null -> null
                                is CurrencyApiThrowable -> throwable
                                else -> CurrencyApiThrowable(throwable)
                            },
                            isLoading = networkState is RequestState.Loading
                        )
                    }
                }

            CacheMechanics.NETWORK_ONLY -> load()
                .flatMapLatest {
                    when (it) {
                        is RequestState.Error -> throw it.throwable
                        RequestState.Loading -> emptyFlow()
                        RequestState.Success -> cache
                    }
                }
                .map {
                    EndpointResult(
                        data = it,
                        error = null,
                        isLoading = false
                    )
                }
                .catch {
                    emit(
                        EndpointResult(
                            data = null,
                            error = CurrencyApiThrowable(it),
                            isLoading = false
                        )
                    )
                }

            CacheMechanics.CACHE_ONLY -> cache.map {
                EndpointResult(
                    data = it,
                    error = null,
                    isLoading = false
                )
            }

            CacheMechanics.DEFAULT -> cache
                .take(1)
                .flatMapLatest {
                    if (shouldMakeRequest(it)) {
                        get(CacheMechanics.FORCE)
                    } else {
                        get(CacheMechanics.CACHE_ONLY)
                    }
                }
        }

    }
}

inline fun <T, R> Flow<EndpointResult<T>>.convert(crossinline f: suspend (T?) -> (R)): Flow<EndpointResult<R>> {
    return this.map {
        EndpointResult(
            data = f(it.data),
            isLoading = it.isLoading,
            error = it.error
        )
    }
}

inline fun <T, R> Flow<EndpointResult<T>>.convertMap(crossinline f: suspend (T?) -> Flow<R>): Flow<EndpointResult<R>> {
    return this.flatMapLatest { er ->
        f(er.data).map {
            EndpointResult(
                data = it,
                isLoading = er.isLoading,
                error = er.error
            )
        }
    }
}