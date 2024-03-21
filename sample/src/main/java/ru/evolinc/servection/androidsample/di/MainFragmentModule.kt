package ru.evolinc.servection.androidsample.di

import ru.evolinc.servection.androidsample.model.FirstInteractorImpl
import ru.evolinc.servection.androidsample.model.SecondInteractorImpl
import ru.evolinc.servection.androidsample.model.SomeDep
import ru.evolinc.servection.di.module

val mainFragmentModule = module {
    provide(SomeDep("hello"))

    factory { FirstInteractorImpl() }
    factory { SecondInteractorImpl() }

    intoMap(
        "one" to "two",
        "one1" to "two",
        "one2" to true,
        "one3" to false,
    )
}