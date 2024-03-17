package ru.evolinc.servection.androidsample.di

import ru.evolinc.servection.androidsample.model.SomeDep
import ru.evolinc.servection.di.DiContainerModule

val mainFragmentModule: DiContainerModule = {
    provide(SomeDep("hello"))
}