package main.kotlin.graphs

import java.io.File

class GraphWiz(private val root: Node, val path: String = ".") {

    companion object {
        val FILE_NAME = "graphwiz.dot"
    }

    private val nodes = mutableMapOf<Int, Node>()

    private val file = File("$path/$FILE_NAME")

    init {
        nodes.put(root.id, root)
        file.writeText("")
    }

    data class Node(val id: Int, val children: MutableSet<Int> = mutableSetOf())


    fun add(node: Node, asAChildOf: Int) {
        val parent = nodes.getOrDefault(asAChildOf, Node(asAChildOf))
        val child = nodes.getOrDefault(node.id, node)
        parent.children.add(node.id)
        nodes[asAChildOf] = parent
        nodes[node.id] = child
    }

    fun reset() {
        nodes.clear()
        nodes.put(root.id, root)
    }

    fun start() {
        file.appendText("@startuml\n")
    }

    fun stop() {
        file.appendText("@enduml\n")
    }

    fun toDotFile() {
        start()
        file.appendText("graph G {\n")
        file.appendText(bfs(root))
        file.appendText("}\n")
        stop()
    }
    private fun bfs(root: Node): String {
        var output = ""
        root.children.forEach {
            output += "\"${root.id}\" -- \"$it\"\n"
            output += bfs(nodes[it]!!)
        }
        return output
    }
}