package ru.evolinc.servection.di

data class DependencyFactory<T>(
    val factory: () -> T,
)

fun <T: Any> T.tryFactory(): T {
    return if (this is DependencyFactory<*>) {
        this.factory.invoke() as T
    } else {
        this
    }
}