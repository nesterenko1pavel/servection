package ru.evolinc.servection.di

import java.lang.reflect.Constructor
import java.lang.reflect.Parameter
import kotlin.reflect.KClass
import kotlin.reflect.full.isSubclassOf
import ru.evolinc.servection.annotations.Inject
import ru.evolinc.servection.annotations.MapDependency
import ru.evolinc.servection.annotations.Qualifier

class RootContainer(
    private val parentRootContainer: RootContainer? = null,
) {

    private val dependencies: MutableMap<Class<out Any>, Any> = mutableMapOf()
    private val qualifiedDependencies: MutableMap<Class<out Annotation>, Any> = mutableMapOf()
    private val dependencyMaps: MutableMap<DependencyMapIdentifier, MutableMap<Any, Any>> = mutableMapOf()

    fun uploadModules(vararg diModules: DiModule) {
        diModules.forEach { uploadModule(it) }
    }

    private fun uploadModule(diModule: DiModule) {
        diModule.module.invoke(this)
    }

    inline fun <reified T : Any> factory(noinline factory: () -> T) {
        T::class.requireNotCollection()

        val dependencyFactory = DependencyFactory(factory)
        provide(T::class.java, dependencyFactory)
    }

    fun <T : Any> provide(dependency: T) {
        provide(dependency::class.java, dependency)
    }

    fun <T : Any> provide(clazz: Class<out T>, dependency: T) {
        dependency::class.requireNotCollection()

        val qualifiedAnnotation = findQualifiedAnnotation(clazz)
        if (qualifiedAnnotation != null) {
            qualifiedDependencies[qualifiedAnnotation.annotationClass.java] = dependency
        } else {
            dependencies[clazz] = dependency
        }
    }

    fun <T : Any> KClass<T>.requireNotCollection() {
        if (isSubclassOf(Iterable::class) || isSubclassOf(Map::class))
            throw IllegalStateException("Library is supporting work only with Map collection and only using [intoMap]")
    }

    fun <K : Any, V : Any> intoMap(vararg pairs: Pair<K, V>) {
        pairs.forEach { (key, value) -> intoMap(key, value) }
    }

    private fun <K : Any, V : Any> intoMap(key: K, value: V) {
        val mapIdentifier = DependencyMapIdentifier(key.javaClass, value.javaClass)
        val existedMap = dependencyMaps[mapIdentifier]
        if (existedMap != null) {
            existedMap[key] = value
        } else {
            val newMap: MutableMap<Any, Any> = mutableMapOf(key to value)
            dependencyMaps[mapIdentifier] = newMap
        }
    }

    inline fun <reified T : Any> get(mapDependencyAnnotation: MapDependency? = null): T {
        return get(T::class.java, null, mapDependencyAnnotation)
    }

    inline fun <reified T : Any, reified R : Annotation> getAnnotated(): T {
        return get(T::class.java, R::class.java)
    }

    fun <T : Any> get(
        clazz: Class<T>,
        qualifierClazz: Class<out Annotation>? = null,
        mapDependencyAnnotation: MapDependency? = null,
    ): T {
        return getInternal(clazz, qualifierClazz, mapDependencyAnnotation)
    }

    private fun <T : Any> getInternal(
        clazz: Class<T>,
        qualifierClazz: Class<out Annotation>? = null,
        mapDependencyAnnotation: MapDependency? = null,
    ): T {
        return getQualifiedDependency<T>(qualifierClazz)?.tryFactory()
            ?: (dependencies[clazz] as? T)?.tryFactory()
            ?: createDependency(clazz, qualifierClazz)
            ?: requestMapDependency(clazz, mapDependencyAnnotation)
            ?: parentRootContainer?.getInternal(clazz, qualifierClazz, mapDependencyAnnotation)
            ?: throw IllegalStateException(
                "Missing [$clazz] dependency with qualifier [$qualifierClazz]. If dependency should by auto-created check @Inject annotation on the constructor"
            )
    }

    private fun <T> getQualifiedDependency(qualifierClazz: Class<out Annotation>?): T? {
        return qualifiedDependencies[qualifierClazz] as? T
    }

    private fun <T : Any> createDependency(
        clazz: Class<T>,
        qualifierClazz: Class<out Annotation>? = null
    ): T? {
        val constructor = clazz.constructors.firstOrNull() ?: return null
        val injectAnnotation = constructor.annotations.firstOrNull { it is Inject } as? Inject ?: return null

        val isQualifiedDependency = findQualifiedAnnotation(clazz) != null
        return (create(constructor) as T).also { createdDependency ->
            if (injectAnnotation.isSingleInstancePerRequest) {
                if (isQualifiedDependency) {
                    if (qualifierClazz != null) {
                        qualifiedDependencies[qualifierClazz] = createdDependency
                    } else {
                        throw IllegalStateException(
                            "You're trying to request qualified dependency [$clazz] without providing qualifier"
                        )
                    }
                } else {
                    if (qualifierClazz == null) {
                        dependencies[clazz] = createdDependency
                    } else {
                        throw IllegalStateException(
                            "You're trying to request non qualified dependency [$clazz] with providing qualifier [$qualifierClazz]"
                        )
                    }
                }
            }
        }
    }

    private fun <T> create(constructor: Constructor<T>): T {
        val parameters = constructor.parameters
        val parametersList = mutableListOf<Any>()
        parameters.forEach { parameter ->
            val qualifiedAnnotation = findQualifiedAnnotation(parameter)
            val mapAnnotation = findDependencyMapAnnotation(parameter.annotations)
            val value = getInternal(
                parameter.type,
                qualifiedAnnotation?.annotationClass?.java,
                mapAnnotation
            )
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

    private fun findDependencyMapAnnotation(annotations: Array<Annotation>): MapDependency? {
        return annotations.firstOrNull { annotation -> annotation is MapDependency } as? MapDependency
    }

    private fun <T : Any> requestMapDependency(
        clazz: Class<out Any>,
        mapDependencyAnnotation: MapDependency? = null,
    ): T? {
        if (clazz != Map::class.java) return null
        if (mapDependencyAnnotation == null) throw IllegalStateException("For requesting [$clazz] you should provide MapDependency annotation")

        val dependencyMapIdentifier = DependencyMapIdentifier(
            keyClass = mapDependencyAnnotation.keyClass.javaObjectType,
            valueClass = mapDependencyAnnotation.valueClass.javaObjectType,
        )
        return dependencyMaps[dependencyMapIdentifier] as? T
            ?: throw IllegalStateException("Requested [$clazz] with parameters [$mapDependencyAnnotation] was not be added in container")
    }
}

private data class DependencyMapIdentifier(
    val keyClass: Class<out Any>,
    val valueClass: Class<out Any>,
)