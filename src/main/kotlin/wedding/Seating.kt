package com.wedding

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File


data class Guest(val id: Int, val name: String, val relationships: Relationships)

data class Relationships(
    val plusOnes: List<Int>,
    val family: List<Int>,
    val closeFriends: List<Int>,
    val friends: List<Int>,
    val dislikes: List<Int>
)


class GuestPicker(private val guestLookup: MutableMap<Int, Guest>) {
    fun pick(guests: List<Int>): Guest? {
        return guests.sorted().map { guestLookup[it] }.firstOrNull()
    }

    fun pick(guests: List<Int>, from: List<Int>): Guest? {
        return from.sorted().filter { guests.contains(it) }.map { guestLookup[it] }.firstOrNull()
    }

}

interface SeatingStrategy {
    fun assignGuests(table: Table, guests: MutableList<Guest>)
}

class StandardSeatingStrategy(private val guestPicker: GuestPicker) : SeatingStrategy {

    override fun assignGuests(table: Table, guests: MutableList<Guest>) {
        var currentGuest = guestPicker.pick(guests.map { it.id })
        while (currentGuest != null && table.hasSeatsAvailable()) {
            val result = table.assign(currentGuest)
//            println("Assigned ${currentGuest.name} to ${table.number} with result $result")
            guests.remove(currentGuest)
            currentGuest = getNextEligibleGuest(currentGuest, guests)
        }
    }

    private fun getNextEligibleGuest(currentGuest: Guest, guests: MutableList<Guest>): Guest? {
        val (plusOnes, family, closeFriends, friends, dislikes) = currentGuest.relationships
        val orderedRelationships = listOf(
            plusOnes,
            family,
            closeFriends,
            friends
        ) // This is the order in which we pick the next eligible guest
        for (relationShip in orderedRelationships) {
            val nextGuest = guestPicker.pick(guests.map { it.id }, relationShip)
            if (nextGuest != null) {
                return nextGuest
            }
        }
        return null
    }

}

data class DiningRoom(val tables: List<Table>) {
    fun getNextTable(): Table? {
        return tables.firstOrNull { it.hasSeatsAvailable() }
    }
}

data class Table(val seats: List<Seat>, val number: Int) {

    fun assign(guest: Guest): Boolean {
        val seat = getNextAvailableSeat()
        if (seat != null) {
            seat.guest = guest
            return true
        }
        return false
    }

    fun getNextAvailableSeat(): Seat? {
        return seats.find { it.isEmpty() }
    }

    fun hasSeatsAvailable(): Boolean {
        return seats.filter { it.isEmpty() }.isNotEmpty()
    }
}


data class Seat(val number: Int, var guest: Guest? = null) {
    fun isEmpty() = guest == null
}

const val NUMBER_OF_TABLES = 8

const val NUMBER_SEATS_PER_TABLE = 10

fun main(args: Array<String>) {
//    println("Fetching latest data from Google sheets.....")
//    Runtime.getRuntime()
//        .exec("node /Users/kprajith/workspace/wedding-seating > /Users/kprajith/workspace/wedding-seating/seating-arrangement.json")
    val input = File("/Users/kprajith/workspace/wedding-seating/seating-arrangement.json").readText()
//    println("Data available locally: $input")
    val listType = object : TypeToken<List<Guest>>() {}.type
    val guests = Gson().fromJson<List<Guest>>(input, listType)
    val guestsLookup = guests.associate { it.id to it }.toMutableMap()
    println("Converting data into Kotlin objects:\n $guests")
    println("Creating Dining room with $NUMBER_OF_TABLES tables & $NUMBER_SEATS_PER_TABLE seats/table")
    val diningRoom = DiningRoom(List(NUMBER_OF_TABLES) { i -> Table(List(NUMBER_SEATS_PER_TABLE) { j -> Seat(j) }, i) })
    var table = diningRoom.getNextTable()
    val guestPicker = GuestPicker(guestsLookup)
    val seatingStrategy = StandardSeatingStrategy(guestPicker)
    val mutableGuests = guests.sortedBy { it.id }.toMutableList()
    while (table != null) {
        seatingStrategy.assignGuests(table, mutableGuests)
        table = diningRoom.getNextTable()
    }
    println("------------ Output ------------------------")
    for (table in diningRoom.tables) {
        println("Table : ${table.number + 1}")
        println("\tSeats: ")
        println(table.seats.joinToString("\n") { "\t\t ${it.number + 1}. ${it.guest?.name}" })
    }
}