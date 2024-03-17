package ru.evolinc.servection.kotlinsample

import ru.evolinc.servection.di.DiContainer
import ru.evolinc.servection.di.DiContainerImpl
import ru.evolinc.servection.di.Inject
import ru.evolinc.servection.di.inject
import ru.evolinc.servection.di.injectAnnotated

fun main() {
    val carMuseum = CarMuseum()
    val dodgeChanger = DodgeChanger("red")
    val dodgeChallenger = DodgeChallenger("yellow")
    carMuseum.provideChanger(dodgeChanger)
    carMuseum.provideChallenger(dodgeChallenger)
    carMuseum.open()
}

class CarMuseum : DiContainer by DiContainerImpl() {

    private val challenger by injectAnnotated<Dodge, DodgeChallenger, ChallengerQualifier>()
    private val foreignMuseum by inject<ForeignMuseum>()

    fun provideChanger(dodgeChanger: DodgeChanger) {
        container.provide<Dodge>(dodgeChanger)
    }

    fun provideChallenger(dodgeChallenger: DodgeChallenger) {
        container.provide<Dodge>(dodgeChallenger)
    }

    fun open() {
        println(this.challenger)
        val dodgeChanger = container.getAnnotated<Dodge, ChangerQualifier>()
        val dodgeChallenger = container.get(Dodge::class.java, ChallengerQualifier::class.java)
        println(dodgeChanger)
        println(dodgeChallenger)

        val carSharer1 = container.get(CarSharer::class.java)
        val carSharer2 = container.get(CarSharer::class.java)
        println(carSharer1)
        println(carSharer2)

        println(foreignMuseum)
    }
}

data class CarSharer @Inject(isSingleInstancePerRequest = true) constructor(
    @ChangerQualifier val dodgeChanger: Dodge,
    @ChallengerQualifier val dodgeChallenger: Dodge,
    val foreignMuseum: ForeignMuseum,
)

data class ForeignMuseum @Inject constructor(
    @ChangerQualifier val dodgeChanger: Dodge,
    @ChallengerQualifier val dodgeChallenger: Dodge,
)

interface Dodge

@ChangerQualifier
data class DodgeChanger(val color: String) : Dodge

@ChallengerQualifier
data class DodgeChallenger(val color: String) : Dodge
