package ru.evolinc.servection.di

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class DiComponentStoreViewModel(
    val container: RootContainer,
) : ViewModel()

fun ViewModelStoreOwner.retainContainer(
    modules: List<DiModule> = emptyList(),
    parentRootContainerRequest: () -> RootContainer? = { null },
    overrideViewModelStore: ViewModelStore? = null,
): Lazy<RootContainer> {
    return lazy {
        val viewModelProvider = ViewModelProvider(
            overrideViewModelStore ?: viewModelStore,
            object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DiComponentStoreViewModel(RootContainer(parentRootContainerRequest())) as T
                }
            }
        )
        val viewModel = viewModelProvider[DiComponentStoreViewModel::class.java]
        modules.forEach { module -> module.module.invoke(viewModel.container) }
        viewModel.container
    }
}

fun module(module: RootContainer.() -> Unit): DiModule {
    return DiModule(module)
}

data class DiModule(
    val module: RootContainer.() -> Unit,
)