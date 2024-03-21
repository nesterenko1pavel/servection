package ru.evolinc.servection.annotations

import kotlin.reflect.KClass

@Target(AnnotationTarget.VALUE_PARAMETER)
annotation class MapDependency(
    val keyClass: KClass<out Any>,
    val valueClass: KClass<out Any>,
)