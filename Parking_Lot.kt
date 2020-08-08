package parking

import java.lang.Exception

fun main() {
    loop@while( true ) {
        try {
            val input = readLine()!!.split("""\s+""".toRegex())
            when (Action.valueOf(input.first().toUpperCase())) {
                Action.PARK -> {
                    val (_, number, color) = input
                    ParkingLot.park(Car(number, color))
                }
                Action.LEAVE -> {
                    val (_, id) = input
                    ParkingLot.leave(id.toInt())
                }
                Action.CREATE -> {
                    val (_, capacity) = input
                    ParkingLot.create(capacity.toInt())
                }
                Action.STATUS -> {
                    ParkingLot.status()
                }
                Action.REG_BY_COLOR -> {
                    val (_, color) = input
                    ParkingLot.regByColor(color)
                }
                Action.SPOT_BY_COLOR -> {
                    val (_, color) = input
                    ParkingLot.spotByColor(color)
                }
                Action.SPOT_BY_REG -> {
                    val (_, reg) = input
                    ParkingLot.spotByReg(reg)
                }
                Action.EXIT -> break@loop
            }
        } catch (e: Exception) {
            println(e.message)
        }
    }
}

enum class Action { PARK, LEAVE, EXIT, CREATE, STATUS, REG_BY_COLOR, SPOT_BY_COLOR, SPOT_BY_REG }

object ParkingLot {
    private var lot: Array<Spot>? = null
    fun park(car: Car) {
        val spot = lot?.firstOrNull { !it.occupied } ?: throw Exception("Sorry, a parking lot has not been created.")
        spot.car = car
        spot.occupied = true
        println("${spot.car!!.color} car parked in spot ${spot.id}.")
    }
    fun leave(int: Int) {
        val spot = lot?.get(int - 1) ?: throw Exception("Sorry, a parking lot has not been created.")
        spot.car = null
        spot.occupied = false
        println("Spot ${spot.id} is free.")
    }
    fun create(capacity: Int) {
        lot = Array(capacity) { Spot(it + 1, false, null) }
        println("Created a parking lot with ${lot?.size} spots.")
    }
    fun status() {
        val occupiedSpots = lot?.filter { it.occupied } ?: throw Exception("Sorry, a parking lot has not been created.")
        if (occupiedSpots.isEmpty()) println("Parking lot is empty.") else occupiedSpots.forEach(::println)
    }
    fun regByColor(color: String) {
        val registrations = lot?.filter { it.occupied }
                            ?.map { it.car!! }
                            ?.filter { it.color.equals(color, ignoreCase = true) }
                            ?.map { it.registration }
                                ?: throw Exception("Sorry, a parking lot has not been created.")
        if (registrations.isEmpty()) println("No cars with color $color were found.") else println(registrations.joinToString())
    }
    fun spotByColor(color: String) {
        val spotIds = lot?.filter { it.occupied }
                            ?.filter { it.car!!.color.equals(color, ignoreCase = true) }
                            ?.map { it.id }
                                ?: throw Exception("Sorry, a parking lot has not been created.")
        if (spotIds.isEmpty()) println("No cars with color $color were found.") else println(spotIds.joinToString())
    }
    fun spotByReg(reg: String) {
        val spotIds = lot?.filter { it.occupied }
                ?.filter { it.car!!.registration.equals(reg, ignoreCase = true) }
                ?.map { it.id }
                    ?: throw Exception("Sorry, a parking lot has not been created.")
        if (spotIds.isEmpty()) println("No cars with registration number $reg were found.") else println(spotIds.joinToString())
    }
}

data class Spot(val id: Int, var occupied: Boolean, var car: Car?) {
    override fun toString(): String = "$id $car"
}
data class Car(val registration: String, val color: String){
    override fun toString(): String = "$registration $color"
}