package ru.evolinc.servection.androidsample.di

import ru.evolinc.servection.androidsample.model.SomeDep
import ru.evolinc.servection.di.module

val mainFragmentModule = module {
    provide(SomeDep("hello"))
}