# Library for use as a Service Locator and as Dependency Injection

[![](https://jitpack.io/v/nesterenko1pavel/servection.svg)](https://jitpack.io/#nesterenko1pavel/servection)

### Implementation

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
        maven { url = URI.create("https://jitpack.io") }
    }
}

dependencies {
  implementation("com.github.nesterenko1pavel:servection:VERSION")
}
```

### As shown in samples there are quite a lot abilities to work with dependencies such as:

- Providing dependencies into container using modules or straightforward providing:
```kotlin
class MainFragment : Fragment(R.layout.fragment_main), DiContainer {

    override val container: RootContainer by retainContainer(
        modules = listOf(mainFragmentModule),
    )

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container.provide(SomeOtherDep())
    }
}

val mainFragmentModule = module {
  provide(SomeDep("hello"))
}
```

- providing Factory dependency. It means that dependency will be instantiated every requested time:
```kotlin
val mainFragmentModule = module {
    factory { RepositoryImpl() }
}
```

- Binding dependencies to the same interface:
```kotlin
container.provide<IRepository>(RepositoryImpl())
```

- Marking dependencies by qualifier to separate dependencies of the same type without using interface:
```kotlin
@MyDependencyQualifier
data class MyDependency(val name: String)

val myDependency = container.getAnnotated<MyDependency, MyDependencyQualifier>()
```

- Auto creating dependency if it depends on already existing dependencies in current container ir in parent container:
```kotlin
data class MainViewModel @Inject(isSingleInstancePerRequest = true) constructor(
    private val repository: IRepository,
)
```

- Extensions for android to retain container while recreating Activity or Fragment

- Ability to request dependencies from another Container:
```kotlin
override val container: RootContainer by retainContainer(
    parentRootContainerRequest = { (requireActivity().application as MainApp).container },
)
```

### Important to note, that:
- If dependency was created using @Inject it means that life of dependency limited by
  - `isSingleInstancePerRequest`. If true - it means that dependency will be single instance during whole root container life. If false - it means that dependency will be instantiated every request from container
  - Limitation of parent container's life that holds current dependency
- If dependency was provided by calling `container.provide(...)` it means that dependency is single instance during whole root container life. The same behavior with qualified dependencies
- Currency library using `kotlin("reflect")` for correct operations with qualified dependencies 
