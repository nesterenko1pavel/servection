package ru.evolinc.servection.androidsample

import android.app.Application
import androidx.fragment.app.Fragment
import ru.evolinc.servection.di.DiContainer
import ru.evolinc.servection.di.DiContainerImpl
import ru.evolinc.servection.di.Qualifier
import ru.evolinc.servection.di.RootContainer

class MainApp : Application(), DiContainer by DiContainerImpl() {

    override fun onCreate() {
        super.onCreate()
        container.provide(InterAppDependency("InterAppDependency"))
    }
}

@Qualifier
annotation class InterAppQualifier

@InterAppQualifier
data class InterAppDependency(
    val name: String,
)

fun Fragment.requestAppContainer(): RootContainer {
    return (requireActivity().application as MainApp).container
}