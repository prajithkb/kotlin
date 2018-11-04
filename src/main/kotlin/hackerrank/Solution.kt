package main.kotlin.hackerrank

import java.util.*

inline fun <T : Any> T?.whenNotNull(f: (it: T) -> Unit) {
    if (this != null) f(this)
}

fun steadyGene(gene: String, n: Int): Int {
    val characterCount = gene
        .fold(
            emptyMap<Char, Int>()
                .toMutableMap()
        ) { acc, c ->
            acc[c].whenNotNull {
                acc[c] = it + 1
            }
            acc
        }
    val charactersToBeRemoved = characterCount
        .filter { it.value > n / 4 }
        .mapValues { it.value - n / 4 }
        .toMutableMap()
    var left = 0
    var right = 0
    var minSubstringLength = Int.MAX_VALUE
    while (right < n) {
        val rightCharacter = gene[right++]
        charactersToBeRemoved[rightCharacter].whenNotNull {
            charactersToBeRemoved[rightCharacter] = it - 1
        }
        while (noMoreCharactersToBeRemoved(charactersToBeRemoved) && left <= right && left < n - 1) {
            minSubstringLength = minOf(minSubstringLength, right - left)
            val leftCharacter = gene[left++]
            charactersToBeRemoved[leftCharacter].whenNotNull {
                charactersToBeRemoved[leftCharacter] = it + 1
            }
        }
    }
    return minSubstringLength
}

fun noMoreCharactersToBeRemoved(charactersToBeRemoved: Map<Char, Int>): Boolean {
    return charactersToBeRemoved.values.all { it <= 0 }
}


fun main(args: Array<String>) {
    val scan = Scanner(System.`in`)
    val n = scan.nextLine().trim().toInt()
    val gene = scan.nextLine()
    val result = steadyGene(gene, n)
    println(result)
}
