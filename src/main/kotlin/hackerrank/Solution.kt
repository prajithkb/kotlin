package main.kotlin.hackerrank

import java.io.File
import java.util.*
import kotlin.system.measureTimeMillis

/******* utility functions *************/


data class Ref<T>(var value: T)

val isDevelopment = Ref(false)

inline fun <T : Any> T?.whenNotNull(f: (it: T) -> Unit) {
    if (this != null) f(this)
}

inline fun <T : Any> T?.whenNull(f: () -> Unit) {
    if (this == null) f()
}

enum class Level {
    DEBUG,
    ACTUAL
}

fun log(message: Any?, level: Level) {
    when (level) {
        Level.DEBUG -> if (isDevelopment.value) println("DEBUG: $message")
        Level.ACTUAL -> println(message)
    }
}

fun log(message: Any?) {
    println(message)
}

fun debugLog(message: Any?) {
    log(message, Level.DEBUG)
}

inline fun <T> withTimeToExecution(operationName: String = "Default", block: () -> T): T {
    var value: T? = null
    val time = measureTimeMillis {
        value = block()
    }
    debugLog("Elapsed time: $time ms for Operation: $operationName")
    return value!!
}


fun setDevelopmentFlag(args: Array<String>) {
    isDevelopment.value = args.contains("test")
}

/******* utility functions *************/

enum class Type {
    StartCloud,
    EndCloud,
    Town
}

data class Position(val coordinate: Long, val type: Type, val value: Long, val id: Int)

fun main(args: Array<String>) {
    setDevelopmentFlag(args)
    //90394853496857 - expected
    val scan = Scanner(File("/Users/kprajith/Desktop/REMOVE_THIS.txt"))
//    val scan = Scanner(System.`in`)
    val n = scan.nextLine().trim().toInt()
    val p = scan.nextLine().split(" ").map { it.trim().toLong() }.toTypedArray()
    val x = scan.nextLine().split(" ").map { it.trim().toLong() }.toTypedArray()
    val m = scan.nextLine().trim().toInt()
    val y = scan.nextLine().split(" ").map { it.trim().toLong() }.toTypedArray()
    val r = scan.nextLine().split(" ").map { it.trim().toLong() }.toTypedArray()
    val result = withTimeToExecution { maximumPeople(p, x, y, r, n, m) }
    println(result)
}

fun maximumPeople(
    people: Array<Long>,
    towns: Array<Long>,
    clouds: Array<Long>,
    ranges: Array<Long>,
    n: Int,
    m: Int
): Long {
    val positions = getSortedPositions(n, towns, people, m, clouds, ranges)
    var peopleInASunnyTown = 0L
    val potentialSingleCloudCandidates = mutableListOf<Pair<Int, Long>>()
    var cloudsAboveTown = mutableSetOf<Int>()
    val positionMap = mutableMapOf<Long, Long>()
    positions.forEach { (position, type, value, id) ->
        //        debugLog("Position(pos: $position, type: $type, value: $value, id: $id) - overlapping count: ${cloudsAboveTown.size}" )
        when (type) {
            Type.StartCloud -> {
                positionMap.merge(position, 1, Long::plus)
                cloudsAboveTown.add(id)
            }
            Type.EndCloud -> {
                positionMap.merge(position, 1, Long::plus)
                cloudsAboveTown.remove(id)
            }
            Type.Town -> {
                val count = positionMap.getOrDefault(position, 0)

                when {
                    //Sunny
                    cloudsAboveTown.isEmpty() && count == 0L -> peopleInASunnyTown += value
                    //Just one cloud
                    cloudsAboveTown.size == 1 && count == 0L -> potentialSingleCloudCandidates.add(
                        Pair(
                            cloudsAboveTown.first(),
                            value
                        )
                    )
                    //Ending of a cloud
                    count <= 1 && cloudsAboveTown.isEmpty() -> potentialSingleCloudCandidates.add(Pair(id, value))
                }
            }
        }
    }
//    debugLog("peopleInASunnyTown: $peopleInASunnyTown")
//    debugLog("potential single candidates: $potentialSingleCloudCandidates")
    val singleCloudGrouping = potentialSingleCloudCandidates.groupBy({ it.first }, { it.second }).map { it.value.sum() }
//    debugLog("singleCloudGrouping: $singleCloudGrouping")
    val maxContribution = singleCloudGrouping.max() ?: 0L
    return maxContribution + peopleInASunnyTown

}

private fun getSortedPositions(
    n: Int,
    towns: Array<Long>,
    people: Array<Long>,
    m: Int,
    clouds: Array<Long>,
    ranges: Array<Long>
): List<Position> {
    val positions = mutableListOf<Position>()
    for (i in 0 until n) {
        positions.add(Position(towns[i], Type.Town, people[i], i))
    }
    for (i in 0 until m) {
        positions.add(Position(clouds[i] - ranges[i], Type.StartCloud, 0, i))
        positions.add(Position(clouds[i] + ranges[i], Type.EndCloud, 0, i))
    }
    return positions.sortedWith(compareBy({ it.coordinate }, { it.type }))
}



