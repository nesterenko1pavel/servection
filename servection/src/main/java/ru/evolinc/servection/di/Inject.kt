package ru.evolinc.servection.di

@Target(AnnotationTarget.CONSTRUCTOR)
annotation class Inject(val isSingleInstancePerRequest: Boolean = false)
