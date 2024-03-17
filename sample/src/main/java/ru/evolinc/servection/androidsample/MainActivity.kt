package ru.evolinc.servection.androidsample

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import ru.evolinc.servection.R
import ru.evolinc.servection.di.DiContainer
import ru.evolinc.servection.di.RootContainer
import ru.evolinc.servection.di.retainContainer

class MainActivity : AppCompatActivity(R.layout.activity_main), DiContainer {

    override val container: RootContainer by retainContainer()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("testapp", "onCreate: $container")
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .add(android.R.id.content, MainFragment())
                .commitNow()
        }
    }
}