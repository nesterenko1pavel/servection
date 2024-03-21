package ru.evolinc.servection.androidsample

import android.util.Log
import ru.evolinc.servection.androidsample.model.SomeDep
import ru.evolinc.servection.annotations.Inject
import ru.evolinc.servection.annotations.MapDependency

class MainFragmentViewModel @Inject constructor(
    someDep: SomeDep,
    @InterAppQualifier interAppDependency: InterAppDependency,
    @MapDependency(String::class, String::class) stringMap: Map<String, String>,
    @MapDependency(String::class, Boolean::class) booleanMap: Map<String, Boolean>,
) {
    init {
        Log.i("testapp", "viewModel: $someDep, $interAppDependency $stringMap $booleanMap")
    }
}