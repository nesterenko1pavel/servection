package ru.evolinc.servection.androidsample

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import kotlin.system.measureTimeMillis
import ru.evolinc.servection.R
import ru.evolinc.servection.androidsample.di.mainFragmentModule
import ru.evolinc.servection.androidsample.model.FirstInteractorImpl
import ru.evolinc.servection.annotations.MapDependency
import ru.evolinc.servection.di.DiContainer
import ru.evolinc.servection.di.RootContainer
import ru.evolinc.servection.di.retainContainer

class MainFragment : Fragment(R.layout.fragment_main), DiContainer {

    override val container: RootContainer by retainContainer(
        modules = listOf(mainFragmentModule),
        parentRootContainerRequest = { requestAppContainer() },
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mills = measureTimeMillis {
            repeat(10000) { container.get<FirstInteractorImpl>() }
        }
        Log.i("testapp", "mills: $mills")
        val viewModel = container.get<MainFragmentViewModel>()
        val booleanMap = container.get<Map<String, Boolean>>(MapDependency(String::class, Boolean::class))
    }
}