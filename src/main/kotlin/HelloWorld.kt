package main.kotlin

import kotlinx.coroutines.runBlocking

@Throws(ArrayIndexOutOfBoundsException::class)
fun main() = runBlocking {
    Boomerang.starting = true

    val x = object {
        val y = 2
    }
    x.y
}


object Boomerang {
    var starting: Boolean
        get() = starting
        set(_) {
            println("Called set")
        }

}
