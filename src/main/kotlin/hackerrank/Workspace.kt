package main.kotlin.hackerrank

import java.io.*
import java.math.*
import java.security.*
import java.text.*
import java.util.*
import java.util.concurrent.*
import java.util.function.*
import java.util.regex.*
import java.util.stream.*
import kotlin.collections.*
import kotlin.comparisons.*
import kotlin.io.*
import kotlin.jvm.*
import kotlin.jvm.functions.*
import kotlin.jvm.internal.*
import kotlin.ranges.*
import kotlin.sequences.*
import kotlin.text.*

inline fun <T : Any, R> whenNotNull(input: T?, callback: (T) -> R): R? {
    return input?.let(callback)
}

fun main(args: Array<String>) {
    val scan = Scanner(System.`in`)

    val numberOfDiscs = scan.nextLine().trim().toInt()

    val discs = scan.nextLine().split(" ").map { it.trim().toInt() }.toTypedArray()

    println(numberOfMoves(numberOfDiscs, discs));


}

val cachedNumberOfMoves: MutableMap<String, Int> = mutableMapOf()

fun getCachedMoves(discs: Array<Int>): Int? {
    return cachedNumberOfMoves[discs.joinToString { it.toString() }]
}

fun updateCachedMoves(discs: Array<Int>, numberOfMoves: Int): Int? {
    return cachedNumberOfMoves.put(discs.joinToString { it.toString() }, numberOfMoves)
}

fun numberOfMoves(numberOfDiscs: Int, discs: Array<Int>): Int {
return 0

}

fun minimumNumberOfMoves(discs: Array<Int>, numberOfMoves: Int, numberOfDiscs: Int): Int {
    val cachedMoves = getCachedMoves(discs)
    if (cachedMoves != null) {
        return cachedMoves!!
    }
    if(discs[0] == numberOfDiscs){
        updateCachedMoves(discs, numberOfMoves)
        return numberOfMoves
    } else {
        // TODO add the core algorithm
    }
    return 0
}
