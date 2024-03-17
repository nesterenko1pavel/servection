package ru.evolinc.servection.androidsample

import android.util.Log
import ru.evolinc.servection.androidsample.model.SomeDep
import ru.evolinc.servection.di.Inject

class MainFragmentViewModel @Inject(isSingleInstancePerRequest = true) constructor(
    someDep: SomeDep,
){
    init {
        Log.i("testapp", "viewModel: $someDep")
    }
}