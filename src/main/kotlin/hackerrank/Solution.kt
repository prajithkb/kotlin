package main.kotlin.hackerrank

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.File
import java.io.OutputStreamWriter
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


fun scanner(): Scan {
    if (devOverrides.readFromFile) {
        return Scan(File("/Users/kprajith/Desktop/REMOVE_THIS.txt").bufferedReader())
    } else {
        return Scan(System.`in`.bufferedReader())
    }
}

class Scan(val reader: BufferedReader) {
    fun nextLine(): String {
        return reader.readLine()
    }
}

val writer = BufferedWriter(OutputStreamWriter(System.out))
fun println(message: Any) {
    writer.write(message.toString())
    writer.newLine()
}

inline fun sandbox(crossinline block: () -> Any) {
    writer.use {
        completeWithin(5000) {
            withTimeToExecution("main") {
                block()
            }
        }
    }
}

data class Node(var id: Int, val children: MutableList<Int> = mutableListOf(), var reducedValue: Int = 0)

fun createTree(n: Int, scan: Scan): Array<Node> {
    val tree = Array(n + 1) { i -> Node(i) }
    for (treeRowItr in 0 until n - 1) {
        val (from, to) = scan.nextLine().split(" ").map { it.trim().toInt() }.toTypedArray()
        tree[from].children.add(tree[to].id)
        tree[to].children.add(tree[from].id)
    }
    return tree
}

fun bfs(root: Node, nodes: Array<Node>) {
    val visited = Array(nodes.size) { false }
    bfs(root, nodes, visited)
}

fun bfs(root: Node, nodes: Array<Node>, visited: Array<Boolean>): Int {
    var edges = 0
    visited[root.id] = true
    root.children.filter { !visited[it] }.forEach { id ->
        edges += bfs(nodes[id], nodes, visited) + 1
    }
    return edges
}



/******* utility functions *************/


fun main(args: Array<String>) {
    setDevelopmentFlag(args)
    val scan = scanner()
    sandbox {
        val t = scan.nextLine().trim().toInt()
        for (tItr in 1..t) {
            val n = scan.nextLine().trim().toInt()
            val tree = createTree(n, scan)
            val result = deforestation(n, tree)
            log(result)
        }
    }
}


fun reduce(root: Node, nodes: Array<Node>) {
    val visited = Array(nodes.size) { false }
    reduce(root, nodes, visited)
}

fun reduce(root: Node, nodes: Array<Node>, visited: Array<Boolean>): Int {
    visited[root.id] = true
    var reducedValue = 0
    root.children.filter { !visited[it] }.forEach { id ->
        val newRoot = nodes[id]
        val reduceValueForChild = 1 + reduce(newRoot, nodes, visited)
        reducedValue = reducedValue xor reduceValueForChild
    }
    root.reducedValue = reducedValue
    return reducedValue
}

fun deforestation(n: Int, tree: Array<Node>): String {
    reduce(tree[1], tree)
//    debugLog(tree.joinToString("\n"))
    val aliceWins = tree[1].reducedValue != 0
    return if (aliceWins) "Alice" else "Bob"
}

