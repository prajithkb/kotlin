package main.kotlin.hackerrank

import java.io.File
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.system.measureTimeMillis

/******* utility functions *************/

data class DevOverrides(var isDebug: Boolean, var readFromFile: Boolean)


val devOverrides = DevOverrides(false, false)

inline fun <T : Any> T?.whenNotNull(f: (it: T) -> Unit) {
    if (this != null) f(this)
}

inline fun <T : Any> T?.whenNull(f: () -> Unit) {
    if (this == null) f()
}

enum class Level {
    ERROR,
    DEBUG,
    ACTUAL
}

fun log(message: Any?, level: Level) {
    when (level) {
        Level.DEBUG, Level.ERROR -> if (devOverrides.isDebug) println("$level: $message")
        Level.ACTUAL -> println(message)
    }
}

fun log(message: Any?) {
    println(message)
}

fun debugLog(message: Any) {
    debugLog(Level.DEBUG) { message.toString() }
}

fun debugLog(level: Level = Level.DEBUG, message: Any) {
    debugLog(level) { message.toString() }
}

inline fun debugLog(level: Level = Level.DEBUG, block: () -> Any) {
    if (devOverrides.isDebug) {
        log(block(), level)
    }

}

inline fun <T> withTimeToExecution(operationName: String = "Overall", block: () -> T): T {
    var value: T? = null
    val time = measureTimeMillis {
        value = block()
    }
    debugLog { "Elapsed time: $time ms for Operation: $operationName" }
    return value!!
}

fun <T> completeWithin(timeoutInMilliSecs: Long = 5000, block: () -> T): T? {
    try {
        return CompletableFuture
            .supplyAsync(block)
            .get(timeoutInMilliSecs, TimeUnit.MILLISECONDS)
    } catch (e: TimeoutException) {
        debugLog(Level.ERROR, "Failed to complete within $timeoutInMilliSecs ms")
        return null
    }
}

fun setDevelopmentFlag(args: Array<String>) {
    devOverrides.isDebug = args.contains("test")
    devOverrides.readFromFile = args.contains("readFromFile")
}

fun scanner(): Scanner {
    if (devOverrides.readFromFile) {
        return Scanner(File("/Users/kprajith/Desktop/REMOVE_THIS.txt"))
    } else {
        return Scanner(System.`in`)
    }
}


/******* utility functions *************/

data class Node(
    val id: Int,
    val children: MutableList<Int> = mutableListOf(),
    var towards: Int = 0,
    var away: Int = 0
)

//var graphWiz: GraphWiz = GraphWiz(GraphWiz.Node(1))


fun main(args: Array<String>) {
    setDevelopmentFlag(args)
    val scan = scanner()
    completeWithin(5000) {
        withTimeToExecution {
            //            graphWiz.start()
            val q = scan.nextLine().trim().toInt()
            for (qItr in 1..q) {
                val n = scan.nextLine().trim().toInt()
                val edges = Array(n - 1) { Array(2) { 0 } }
                for (edgesRowItr in 0 until n - 1) {
                    edges[edgesRowItr] = scan.nextLine().split(" ").map { it.trim().toInt() }.toTypedArray()
                }
                val gk = scan.nextLine().split(" ")
                val g = gk[0].trim().toInt()
                val k = gk[1].trim().toInt()
                val guesses = Array(g) { Array(2) { 0 } }
                for (guessesRowItr in 0 until g) {
                    guesses[guessesRowItr] = scan.nextLine().split(" ").map { it.trim().toInt() }.toTypedArray()
                }
                val result = storyOfATree(n, edges, k, guesses)
                println(result)
            }
//            graphWiz.stop()
        }

    }
}

fun storyOfATree(
    n: Int,
    edges: Array<Array<Int>>,
    k: Int,
    guesses: Array<Array<Int>>
): String {
    val visited = mutableMapOf<Int, Boolean>()
    val nodes = mutableMapOf<Int, Node>()
    populateNodes(edges, nodes)
    traverseAndPopulateRoot(nodes, visited, guesses)
    val possibleCount = nodes.values.filter { it.towards >= k }.count()
    return asFraction(possibleCount, n)
}

fun asFraction(numerator: Int, denominator: Int): String {
    if (numerator == denominator)
        return "1/1"
    if (numerator == 0)
        return "0/1"
    val gcd = gcd(numerator, denominator)
    return String.format("%d/%d", numerator / gcd, denominator / gcd)
}

fun gcd(a: Int, b: Int): Int {
    return if (b == 0) a else gcd(b, a % b)
}

fun traverseAndPopulateRoot(
    nodes: MutableMap<Int, Node>,
    visited: MutableMap<Int, Boolean>,
    guesses: Array<Array<Int>>
) {
    val guessesSet = guesses.map { (parent, child) -> key(parent, child) }.toSet()
//    debugLog {"Before \n" + nodes.values.joinToString("\n")}
    bfsPopulateRoot(1, nodes, visited, guessesSet)
//    graphWiz.appendToDotFile()
//    debugLog { guessesSet.toString() }
//    debugLog {"After bfsPopulateRoot  \n" + nodes.values.joinToString("\n")}
    visited.clear()
    bfsPopulateAllOtherNodes(1, nodes, visited, guessesSet)
//    debugLog { guessesSet.toString() }
//    debugLog {"After bfsPopulateAllOtherNodes \n" + nodes.values.joinToString("\n")}
}

private fun key(parent: Int, child: Int) = "$parent <- $child"


fun bfsPopulateAllOtherNodes(
    parent: Int,
    nodes: MutableMap<Int, Node>,
    visited: MutableMap<Int, Boolean>,
    guesses: Set<String>
) {
    visited[parent] = true
    val parentNode = nodes[parent]!!
    parentNode.children
        .filter { !visited.getOrDefault(it, false) }
        .map { nodes[it]!! }
        .forEach { child ->
            child.towards += (parentNode.towards - child.towards)
            child.away += (parentNode.away - child.away)
            if (guesses.contains(key(parent, child.id))) { // parent <- child
                child.towards--
                child.away++
            }
            if (guesses.contains(key(child.id, parent))) { // child <- parent
                child.away--
                child.towards++
            }
            bfsPopulateAllOtherNodes(child.id, nodes, visited, guesses)

        }
}

fun bfsPopulateRoot(
    parent: Int,
    nodes: MutableMap<Int, Node>,
    visited: MutableMap<Int, Boolean>,
    guesses: Set<String>
): Pair<Int, Int> {
    visited[parent] = true
    val parentNode = nodes[parent]!!
    parentNode.children
        .filter { !visited.getOrDefault(it, false) }
        .map { nodes[it]!! }
        .forEach { child ->
            //            graphWiz.add(GraphWiz.Node(child.id), parent)
            if (guesses.contains(key(parent, child.id))) { // parent <- child
                parentNode.towards++
                child.away++
            }
            if (guesses.contains(key(child.id, parent))) { // child <- parent
                parentNode.away++
                child.towards++
            }
            val (awayFromSingleChild, towardsFromSingleChild) = bfsPopulateRoot(child.id, nodes, visited, guesses)
            parentNode.away += awayFromSingleChild
            parentNode.towards += towardsFromSingleChild

            if (guesses.contains(key(parent, child.id))) { // remove the duplicate
                parentNode.away--
            }
            if (guesses.contains(key(child.id, parent))) { // remove the duplicate
                parentNode.towards--
            }
        }
    return Pair(parentNode.away, parentNode.towards)
}

private fun populateNodes(
    edges: Array<Array<Int>>,
    nodes: MutableMap<Int, Node>
) {
    edges.forEach { (from, to) ->
        val fromNode = nodes.getOrDefault(from, Node(from))
        val toNode = nodes.getOrDefault(to, Node(to))
        fromNode.children.add(to)
        toNode.children.add(from)
        nodes[from] = fromNode
        nodes[to] = toNode
    }
}



