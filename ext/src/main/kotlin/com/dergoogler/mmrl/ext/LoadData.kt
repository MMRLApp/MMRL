package com.dergoogler.mmrl.ext

sealed class LoadData<out V> {
    data object Pending : LoadData<Nothing>()

    data object Loading : LoadData<Nothing>()

    data class Success<out V>(
        val value: V,
    ) : LoadData<V>()

    data class Failure(
        val error: Throwable,
    ) : LoadData<Nothing>()

    val isLoading inline get() = this == Loading
    val isSuccess inline get() = this is Success
    val isFailure inline get() = this is Failure

    companion object Default {
        fun <V> Result<V>.asLoadData(): LoadData<V> =
            when {
                isSuccess -> Success(getOrThrow())
                else -> Failure(requireNotNull(exceptionOrNull()))
            }

        inline fun <V, R> Result<V>.asLoadData(transform: (V) -> R): LoadData<R> =
            when {
                isSuccess -> Success(transform(getOrThrow()))
                else -> Failure(requireNotNull(exceptionOrNull()))
            }

        fun <V> LoadData<V>.getOrThrow(): V =
            when (this) {
                Pending -> throw IllegalStateException("Pending")
                Loading -> throw IllegalStateException("Loading")
                is Success<V> -> value
                is Failure -> throw error
            }

        inline fun <V, R> LoadData<V>.getValue(
            fallback: R,
            transform: (V) -> R,
        ): R = (this as? Success)?.value?.let(transform) ?: fallback
    }
}
