package ru.evolinc.servection.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class DiComponentStoreViewModel(
    val container: RootContainer,
) : ViewModel()

fun ViewModelStoreOwner.retainContainer(
    module: DiContainerModule? = null,
    overrideViewModelStore: ViewModelStore? = null,
): Lazy<RootContainer> {
    return lazy {
        val viewModelProvider = ViewModelProvider(
            overrideViewModelStore ?: viewModelStore,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DiComponentStoreViewModel(RootContainer()) as T
                }
            }
        )
        val viewModel = viewModelProvider[DiComponentStoreViewModel::class.java]
        module?.let { viewModel.container.it() }
        viewModel.container
    }
}