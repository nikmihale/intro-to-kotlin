package converter

import java.util.*
import kotlin.collections.HashMap

fun main() {
    val scanner = Scanner(System.`in`)
    var input = scanner.prompt()
    while (input != "exit") {
        val match = """(degree(s?)\s+)?[a-zA-Z]+""".toRegex().find(input)!!
        val number = """[\-0-9\.]+""".toRegex().find(input)!!.value.toDouble()
        val leftHandSide = HandSide(number, match.value)
        if (leftHandSide.measure?.type in listOf(Type.DISTANCE, Type.WEIGHT)  && number < 0) {
            when (leftHandSide.measure?.type) {
                Type.WEIGHT -> println("Weight shouldn't be negative")
                Type.DISTANCE -> println("Length shouldn't be negative")
            }
        } else {
            val rightHandSide = HandSide(match.next()!!.next()!!.value)
            if (leftHandSide.isComparable(rightHandSide)) {
                rightHandSide.convertFrom(leftHandSide)
                println("$leftHandSide is $rightHandSide")
            } else {
                println("Conversion from ${leftHandSide.measure?.pluralName() ?: "???"} to ${rightHandSide.measure?.pluralName() ?: "???"} is impossible")
            }
        }
        input = scanner.prompt()
    }
}


class HandSide constructor(private var number: Double, val measure: Measures?) {
    constructor(string: String) : this(0.0, string)
    constructor(number: Double, string: String) : this(number, Measures.from(string))
    fun isComparable(other: HandSide): Boolean {
        return (this.measure != null && this.measure.type == other.measure?.type)
    }

    fun convertFrom(other: HandSide) {
        when (this.measure!!.type) {
            Type.DISTANCE, Type.WEIGHT -> this.number = other.number * other.measure!!.multiple / this.measure.multiple
            Type.TEMPERATURE -> this.number = convertFromTable(other.number, other.measure!!, this.measure)
        }
    }
    private fun convertFromTable(number: Double, source: Measures, destination: Measures): Double {
        val table = createTable()
        return table[Pair(source, destination)]!!.invoke(number)
    }

    private fun createTable(): HashMap<Pair<Measures, Measures>, (Double) -> Double> {
        val table = HashMap<Pair<Measures, Measures>, (x: Double) -> Double>()
        table[Pair(Measures.CELSIUS, Measures.CELSIUS)] = fun(x): Double {return x}
        table[Pair(Measures.FAHRENHEIT, Measures.FAHRENHEIT)] = fun(x): Double {return x}
        table[Pair(Measures.KELVIN, Measures.KELVIN)] = fun(x): Double {return x}
        table[Pair(Measures.FAHRENHEIT, Measures.CELSIUS)] = fun(x): Double {return (x - 32) * 5/9}
        table[Pair(Measures.CELSIUS, Measures.FAHRENHEIT)] = fun(x): Double {return x * 9/5 + 32}
        table[Pair(Measures.CELSIUS, Measures.KELVIN)] = fun(x): Double {return x + 273.15}
        table[Pair(Measures.KELVIN, Measures.CELSIUS)] = fun(x): Double {return x - 273.15}
        table[Pair(Measures.KELVIN, Measures.FAHRENHEIT)] = fun(x): Double {return x * 9/5 - 459.67}
        table[Pair(Measures.FAHRENHEIT, Measures.KELVIN)] = fun(x): Double {return (x + 459.67) * 5/9}
        return table
    }
    override fun toString(): String {
        return "$number " + when {
            measure == null -> "???"
            number != 1.0  -> measure.pluralName()
            else -> measure.singularName()
        }
    }
}

fun Scanner.prompt(): String {
    println("Enter what you want to convert (or exit):")
    return this.nextLine().toLowerCase()
}

enum class Type { DISTANCE, WEIGHT, TEMPERATURE}

enum class Measures (var multiple: Double, val type: Type) {
    METER(1.0, Type.DISTANCE),
    KILOMETER(1000.0, Type.DISTANCE),
    CENTIMETER(0.01, Type.DISTANCE),
    MILLIMETER(0.001, Type.DISTANCE),
    MILE(1609.35, Type.DISTANCE),
    YARD(0.9144, Type.DISTANCE),
    FOOT(0.3048, Type.DISTANCE),
    INCH(0.0254, Type.DISTANCE),
    GRAM(1.0, Type.WEIGHT),
    KILOGRAM(1000.0, Type.WEIGHT),
    MILLIGRAM(0.001, Type.WEIGHT),
    POUND(453.592, Type.WEIGHT),
    OUNCE(28.3495, Type.WEIGHT),
    KELVIN(0.0, Type.TEMPERATURE),
    CELSIUS(0.0, Type.TEMPERATURE),
    FAHRENHEIT(0.0, Type.TEMPERATURE);

    fun pluralName(): String {
        return when (this) {
            INCH -> "inches"
            FOOT -> "feet"
            CELSIUS -> "degrees Celsius"
            FAHRENHEIT -> "degrees Fahrenheit"
            else -> name.toLowerCase() + "s"
        }
    }

    fun singularName(): String {
        return when (this) {
            CELSIUS -> "degree Celsius"
            FAHRENHEIT -> "degree Fahrenheit"
            else -> name.toLowerCase()
        }
    }

    companion object {
        fun from(string: String): Measures? {
            return when (string.toLowerCase()) {
                "m", "meter", "meters" -> METER
                "km", "kilometer", "kilometers" -> KILOMETER
                "cm", "centimeter", "centimeters" -> CENTIMETER
                "mm", "millimeter", "millimeters" -> MILLIMETER
                "mi", "mile", "miles" -> MILE
                "yd", "yard", "yards" -> YARD
                "ft", "foot", "feet" -> FOOT
                "in", "inch", "inches" -> INCH
                "g", "gram", "grams" -> GRAM
                "kg", "kilogram", "kilograms" -> KILOGRAM
                "mg", "milligram", "milligrams" -> MILLIGRAM
                "lb", "pound", "pounds" -> POUND
                "oz", "ounce", "ounces" -> OUNCE
                "degree celsius", "degrees celsius", "celsius", "dc", "c" -> CELSIUS
                "degree fahrenheit", "degrees fahrenheit", "fahrenheit", "df", "f" -> FAHRENHEIT
                "kelvin", "kelvins", "k" -> KELVIN
                else -> null
            }
        }
    }
}