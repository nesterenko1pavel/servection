package ru.evolinc.servection.di

interface DiContainer {

    val container: RootContainer
}

class DiContainerImpl : DiContainer {

    override val container: RootContainer = RootContainer()
}

inline fun <reified T : Any> DiContainer.inject(): Lazy<T> {
    return lazy { container.get(T::class.java) }
}

inline fun <reified T : Any, reified R : T, reified A : Annotation> DiContainer.injectAnnotated(): Lazy<R> {
    return lazy { container.get(T::class.java, A::class.java) as R }
}