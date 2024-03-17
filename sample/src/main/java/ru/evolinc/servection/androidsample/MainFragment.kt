package ru.evolinc.servection.androidsample

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.fragment.app.Fragment
import ru.evolinc.servection.R
import ru.evolinc.servection.androidsample.di.mainFragmentModule
import ru.evolinc.servection.di.DiContainer
import ru.evolinc.servection.di.RootContainer
import ru.evolinc.servection.di.retainContainer

class MainFragment : Fragment(R.layout.fragment_main), DiContainer {

    override val container: RootContainer by retainContainer(mainFragmentModule)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i("testapp", "onViewCreated: $container")
        val viewModel = container.get<MainFragmentViewModel>()
    }
}