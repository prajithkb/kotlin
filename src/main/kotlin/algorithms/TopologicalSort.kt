package main.kotlin.algorithms

import java.lang.IllegalArgumentException
import java.util.*


val dependencies = mapOf(
        "WebView" to listOf("HTML", "MVC", "LDAP"),
        "HTML" to listOf("CSS"),
        "MVC" to listOf("HTML", "Logger"),
        "LDAP" to listOf("Logger")

)


fun main(args: Array<String>) {
    val pkg = "WebView"
    val buildOrder = getBuildOrder(pkg)
    println(buildOrder)
}

fun getBuildOrder(pkg: String): List<List<String>> {
    val builtDependencies = mutableSetOf<String>()
    val result: MutableList<MutableList<String>> = mutableListOf()
    val deque = ArrayDeque<String>()
    while (!isReadyToBuild(pkg, builtDependencies)) {
        // push to stack
        deque.addFirst(pkg)
        val visited = mutableSetOf<String>()
        val level = mutableListOf<String>()
        while (!deque.isEmpty()) {
            // pop from stack
            val currentPackage = deque.removeLast()
            if (visited.contains(currentPackage) || builtDependencies.contains(currentPackage)) {
                continue
            };
            visited.add(currentPackage)
            if (isReadyToBuild(currentPackage, builtDependencies)) {
                level.add(currentPackage)
            } else {
                dependencies[currentPackage]
                        ?.filter { !visited.contains(it) }
                        ?.forEach { deque.addFirst(it) }
            }
        }
        if (level.size == 0) {
            throw IllegalArgumentException("Cycle detected")
        }
        result.add(level)
        level.forEach { builtDependencies.add(it) }
    }
    result.add(mutableListOf(pkg));
    return result;
}

fun isReadyToBuild(pkg: String, builtDependencies: MutableSet<String>): Boolean {
    return dependencies[pkg]?.all { builtDependencies.contains(it) } ?: true
}
