package main.kotlin.graphs

import java.io.File

class GraphWiz(private val rootId: Int, path: String = ".") {

    companion object {
        val FILE_NAME = "graphwiz.dot"

        fun toDotFile(path: String = ".", content: () -> String) {
            val file = File("$path/$FILE_NAME")
            clear(file)
            start(file)
            append(file, content())
            stop(file)
        }

        fun clear(file: File) {
            file.writeText("")
        }

        fun append(file: File, content: String) {
            file.appendText(content)
        }

        fun start(file: File) {
            file.appendText("@startuml\n")
            file.appendText("graph G {\n")
        }

        fun stop(file: File) {
            file.appendText("}\n")
            file.appendText("@enduml\n")
        }
    }

    private val root = Node(rootId)

    private val nodes = mutableMapOf<Int, Node>()

    private val edges = mutableMapOf<Pair<Int, Int>, Int>()

    private val file = File("$path/$FILE_NAME")

    init {
        nodes.put(root.id, root)
        clear(file)
    }

    data class Node(val id: Int, val children: MutableSet<Int> = mutableSetOf())


    fun add(id: Int, asAChildOf: Int, edge: Int = 0) {
        val parent = nodes.getOrDefault(asAChildOf, Node(asAChildOf))
        val child = nodes.getOrDefault(id, Node(id))
        parent.children.add(id)
        nodes[asAChildOf] = parent
        nodes[id] = child
        edges[id to asAChildOf] = edge
        edges[asAChildOf to id] = edge
    }

    fun add(node: Node, asAChildOf: Int) {
        add(node.id, asAChildOf)
    }

    fun reset() {
        nodes.clear()
        nodes.put(rootId, Node(root.id))
    }


    fun toDotFile() {
        toDotFile { bfs(root) }
    }
    private fun bfs(root: Node): String {
        var output = ""
        root.children.forEach {
            output += "\"${root.id}\" -- \"$it\"[label=\" ${edges.getOrDefault(root.id to it, 0)}\"]\n"
            output += bfs(nodes[it]!!)
        }
        return output
    }
}