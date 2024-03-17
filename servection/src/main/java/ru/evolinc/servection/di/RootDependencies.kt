package ru.evolinc.servection.di

import java.lang.reflect.Constructor
import java.lang.reflect.Parameter

class RootContainer {

    val dependencies: MutableMap<Class<out Any>, Any> = mutableMapOf()
    private val cachedDependencies: MutableMap<Class<out Any>, Any> = mutableMapOf()
    val qualifiedDependencies: MutableMap<Class<out Annotation>, Any> = mutableMapOf()

    inline fun <reified T : Any> provide(dependency: T) {
        val qualifiedAnnotation = findQualifiedAnnotation(dependency::class.java)
        if (qualifiedAnnotation != null) {
            qualifiedDependencies[qualifiedAnnotation.annotationClass.java] = dependency
        } else {
            dependencies[T::class.java] = dependency
        }
    }

    inline fun <reified T : Any> get(): T {
        return get(T::class.java, null)
    }

    inline fun <reified T : Any, reified R : Annotation> getAnnotated(): T {
        return get(T::class.java, R::class.java)
    }

    fun <T : Any> get(clazz: Class<T>, qualifierClazz: Class<out Annotation>? = null): T {
        return getQualifiedDependency(qualifierClazz)
            ?: dependencies[clazz] as? T
            ?: createDependency(clazz)
            ?: throw IllegalStateException("Missing [$clazz] dependency")
    }

    private fun <T> getQualifiedDependency(qualifierClazz: Class<out Annotation>?): T? {
        return qualifiedDependencies[qualifierClazz] as? T
    }

    private fun <T : Any> createDependency(clazz: Class<T>): T? {
        val constructor = clazz.constructors.firstOrNull() ?: return null
        val injectAnnotation = constructor.annotations.firstOrNull { it is Inject } as? Inject ?: return null

        return if (injectAnnotation.isSingleInstancePerRequest) {
            cachedDependencies[clazz] as? T
                ?: (create(constructor) as T).also {
                    cachedDependencies[clazz] = it
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
            val value = if (qualifiedAnnotation != null) {
                qualifiedDependencies[qualifiedAnnotation.annotationClass.java]
                    ?: throw IllegalStateException(
                        "In class [${constructor.name}] annotated parameter [$parameter] was not provided with qualifier"
                    )
            } else {
                get(parameter.type)
            }
            parametersList.add(value)
        }
        return constructor.newInstance(*parametersList.toTypedArray()) as T
    }

    fun findQualifiedAnnotation(clazz: Class<out Any>): Annotation? {
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