package ru.evolinc.servection.annotations

@Target(AnnotationTarget.CONSTRUCTOR)
annotation class Inject(val isSingleInstancePerRequest: Boolean = true)