package main.kotlin.hackerrank

import java.util.*
import kotlin.system.measureTimeMillis


/******* utility functions *************/

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
        Level.DEBUG -> if (isDevelopment) println("DEBUG: $message")
        Level.ACTUAL -> println(message)
    }
}

fun log(message: Any?) {
    println(message)
}

fun debugLog(message: Any?) {
    log(message, Level.DEBUG)
}

inline fun withTimeToExecution(block: () -> Unit) {
    val time = measureTimeMillis {
        block()
    }
    debugLog("Elapsed time: $time ms")
}

var isDevelopment = false

fun setDevelopmentFlag(args: Array<String>) {
    isDevelopment = args.contains("test")
}

/******* utility functions *************/
val possiblePaths = arrayOf(
    -2 to -1 to "UL",
    -2 to 1 to "UR",
    0 to 2 to "R",
    2 to 1 to "LR",
    2 to -1 to "LL",
    0 to -2 to "L"
)

var memoizedPaths = emptyMap<String, Pair<Int, List<String>>>().toMutableMap()

const val NOT_REACHABLE_MOVE = -1

fun get(point: Pair<Int, Int>): Pair<Int, List<String>>? {
    val (x, y) = point
    val r = memoizedPaths.get("$x-$y")
    return r
}

fun set(point: Pair<Int, Int>, value: Pair<Int, List<String>>) {
    debugLog("set cached value for $point | $value")
    val (x, y) = point
    memoizedPaths.set("$x-$y", value)
}


fun isAValidStep(currentPosition: Pair<Int, Int>, visited: Array<Array<Boolean>>, n: Int): Boolean {
    val (i, j) = currentPosition
    val v = i in 0 until n && j in 0 until n && !visited[i][j]
    return v
}


fun move(
    n: Int,
    currentPosition: Pair<Int, Int>,
    destination: Pair<Int, Int>,
    visited: Array<Array<Boolean>>
): Pair<Int, List<String>> {
    val (x, y) = currentPosition
    visited[x][y] = true
    if (currentPosition == destination) {
        debugLog("Reached destination")
        return Pair(0, listOf())
    }
    var minMovesAndPaths = Pair(Int.MAX_VALUE, listOf<String>())
    for ((deltas, currentPath) in possiblePaths) {
        val (xStep, yStep) = deltas
        val newPosition = Pair(x + xStep, y + yStep)
        if (isAValidStep(newPosition, visited, n)) {
            var cachedMovesAndPaths = get(newPosition)
            var movesAndPaths: Pair<Int, List<String>>? = cachedMovesAndPaths
            cachedMovesAndPaths.whenNull {
                debugLog("visiting $newPosition : $currentPath")
                movesAndPaths = move(n, newPosition, destination, visited)
            }
            movesAndPaths.whenNotNull { (newMoves, newPaths) ->
                if (newMoves != NOT_REACHABLE_MOVE && newMoves + 1 < minMovesAndPaths.first) {
                    debugLog("Found non null movesAndPaths:  $movesAndPaths")
                    minMovesAndPaths = Pair(newMoves + 1, newPaths + currentPath)

                }
            }

        }
    }
    var returnValue = minMovesAndPaths
    if (minMovesAndPaths.first == Int.MAX_VALUE) {
        returnValue = Pair(NOT_REACHABLE_MOVE, listOf())
    }
    set(currentPosition, returnValue)
    return returnValue
}


/**
 *
10
9 9 6 3

7
6 6 0 1

7
0 3 4 3

30
25 2 23 29

14
UR R R R R R R R R R R R R R
 */

// Complete the printShortestPath function below.
fun printShortestPath(
    n: Int,
    startX: Int,
    startY: Int,
    endX: Int,
    endY: Int
) {
    for (i in 0 until n) {
        for (j in 0 until n) {
            val visited = Array(n) { Array(n) { false } }
            move(n, Pair(i, j), Pair(endX, endY), visited)
        }
    }
    val (numberOfMoves, paths) = get(Pair(startX, startY)) ?: Pair(-1, listOf())
    if (numberOfMoves == NOT_REACHABLE_MOVE) log("Impossible") else log(
        "$numberOfMoves\n${paths.reversed().joinToString(
            " "
        )}"
    )
}

fun printShortestPathBfs(
    n: Int,
    startX: Int,
    startY: Int,
    endX: Int,
    endY: Int
) {
    val start = Pair(startX, startY)
    val end = Pair(endX, endY)
    val possible = bfs(start, n, end)
    val (numberOfMoves, paths) = get(end) ?: Pair(-1, listOf<String>())
    debugLog("$numberOfMoves|$paths")
    if (possible) log("$numberOfMoves\n${paths.joinToString(" ")}") else log("Impossible")
}

fun bfs(startingPoint: Pair<Int, Int>, n: Int, endingPoint: Pair<Int, Int>): Boolean {
    val queue = ArrayDeque<Pair<Pair<Int, Int>, List<String>>>()
    val visited = Array(n) { Array(n) { false } }
    val marker = Pair(Pair(-1, -1), listOf<String>())
    var distance = 0
    queue.push(Pair(startingPoint, listOf()))
    queue.addFirst(marker)
    var pendingMarkerPush = false
    while (!queue.isEmpty()) {
        val node = queue.removeLast()

        if (node == marker) {
            distance++
            pendingMarkerPush = true
            continue
        }
        val (point, pathTillNow) = node
        val (x, y) = point
        set(point, Pair(distance, pathTillNow))
        if (point == endingPoint) {
            return true
        }
        if (!visited[x][y]) {
            visited[x][y] = true
            val neighbors = neighbours(point, visited, n)
            if (pendingMarkerPush && neighbors.isNotEmpty()) {
                queue.addFirst(marker)
                pendingMarkerPush = false
            }
            neighbors.forEach { (point, path) -> queue.addFirst(Pair(point, pathTillNow + path)) }
        }
    }
    return false
}

fun neighbours(point: Pair<Int, Int>, visited: Array<Array<Boolean>>, n: Int): List<Pair<Pair<Int, Int>, String>> {
    return possiblePaths
        .map { (step, path) ->
            val (x, y) = point
            val (dx, dy) = step
            Pair(Pair(x + dx, y + dy), path)
        }.filter { (nextStep, _) ->
            isAValidStep(nextStep, visited, n)
        }
}


fun main(args: Array<String>) {
    setDevelopmentFlag(args)
    val scan = Scanner(System.`in`)
    val n = scan.nextLine().trim().toInt()
    val (startX, startY, endX, endY) = scan
        .nextLine()
        .split(" ")
        .map { it.trim().toInt() }
    withTimeToExecution {
        printShortestPathBfs(n, startX, startY, endX, endY)
    }
}

private fun printMemoizedPaths(n: Int) {
    val acc =
        memoizedPaths.entries.fold(List(n + 1) { List(n + 1) { -1 }.toMutableList() }.toMutableList()) { acc, (k, v) ->
            val (i, j) = k.split("-").map { it.toInt() }
            val list = acc[i]
            list[j] = v.first
            acc
        }

    print("\t")
    for (i in 0..n + 1) {
        print("$i|\t")
    }
    println()
    acc.forEachIndexed { index, mutableList ->
        println("$index|\t${mutableList.joinToString(",\t")}")
    }
}


