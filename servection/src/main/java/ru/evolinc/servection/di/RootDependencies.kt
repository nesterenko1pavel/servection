package ru.evolinc.servection.di

import java.lang.reflect.Constructor
import java.lang.reflect.Parameter

class RootContainer(
    private val parentRootContainer: RootContainer? = null,
) {

    private val dependencies: MutableMap<Class<out Any>, Any> = mutableMapOf()
    private val qualifiedDependencies: MutableMap<Class<out Annotation>, Any> = mutableMapOf()

    inline fun <reified T : Any> factory(noinline factory: () -> T) {
        val dependencyFactory = DependencyFactory(factory)
        provide(T::class.java, dependencyFactory)
    }

    fun <T : Any> provide(dependency: T) {
        provide(dependency::class.java, dependency)
    }

    fun <T : Any> provide(clazz: Class<out T>, dependency: T) {
        val qualifiedAnnotation = findQualifiedAnnotation(clazz)
        if (qualifiedAnnotation != null) {
            qualifiedDependencies[qualifiedAnnotation.annotationClass.java] = dependency
        } else {
            dependencies[clazz] = dependency
        }
    }

    inline fun <reified T : Any> get(): T {
        return get(T::class.java, null)
    }

    inline fun <reified T : Any, reified R : Annotation> getAnnotated(): T {
        return get(T::class.java, R::class.java)
    }

    fun <T : Any> get(clazz: Class<T>, qualifierClazz: Class<out Annotation>? = null): T {
        return getQualifiedDependency<T>(qualifierClazz)?.tryFactory()
            ?: (dependencies[clazz] as? T)?.tryFactory()
            ?: createDependency(clazz)
            ?: parentRootContainer?.get(clazz, qualifierClazz)
            ?: throw IllegalStateException("Missing [$clazz] dependency with qualifier [$qualifierClazz]")
    }

    private fun <T> getQualifiedDependency(qualifierClazz: Class<out Annotation>?): T? {
        return qualifiedDependencies[qualifierClazz] as? T
    }

    private fun <T : Any> createDependency(clazz: Class<T>): T? {
        val constructor = clazz.constructors.firstOrNull() ?: return null
        val injectAnnotation = constructor.annotations.firstOrNull { it is Inject } as? Inject ?: return null

        return if (injectAnnotation.isSingleInstancePerRequest) {
            (create(constructor) as T).also {
                dependencies[clazz] = it
            }
        } else {
            create(constructor) as T
        }
    }

    private fun <T> create(constructor: Constructor<T>): T {
        val parameters = constructor.parameters
        val parametersList = mutableListOf<Any>()
        parameters.forEach { parameter ->
            val qualifiedAnnotation = findQualifiedAnnotation(parameter)
            val value = get(parameter.type, qualifiedAnnotation?.annotationClass?.java)
            parametersList.add(value)
        }
        return constructor.newInstance(*parametersList.toTypedArray()) as T
    }

    private fun findQualifiedAnnotation(clazz: Class<out Any>): Annotation? {
        return findQualifiedAnnotation(clazz.annotations)
    }

    private fun findQualifiedAnnotation(parameter: Parameter): Annotation? {
        return findQualifiedAnnotation(parameter.annotations)
    }

    private fun findQualifiedAnnotation(annotations: Array<Annotation>): Annotation? {
        return annotations.firstOrNull { annotation ->
            annotation.annotationClass.annotations.firstOrNull { it is Qualifier } != null
        }
    }
}