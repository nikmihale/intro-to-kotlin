package search

import java.io.File

fun main(args: Array<String>) {
    val (_, fileName) = args
    val file = File(fileName)
    file.readLines().forEach { DataHouse.add(Person(it)) }
    while (true) {
        DataHouse.printMenu()
        when (readLine()!!.toInt()) {
            1 -> DataHouse.invertedSearch()
            2 -> DataHouse.printStorageContets()
            0 -> { println("\nBye!"); return}
            else -> println("\nIncorrect option! Try again.")
        }
    }
}

object DataHouse {
    private val storage = mutableListOf<Person>()
    private val invertedIndex = mutableMapOf<String, MutableSet<Int>>()
    enum class MatchingStrategy { ALL, ANY, NONE }

    private fun createInvertedIndex() {
        invertedIndex.clear()
        fun addToInvertedIndex(key: String, value: Int) = invertedIndex[key.toLowerCase()]?.add(value) ?: invertedIndex.set(key.toLowerCase(), mutableSetOf(value))
        storage.forEach {
            addToInvertedIndex(it.firstName, storage.indexOf(it))
            addToInvertedIndex(it.lastName, storage.indexOf(it))
            addToInvertedIndex(it.email, storage.indexOf(it))
        }
    }
    private fun invertedIndexOf(key: String) = invertedIndex[key.toLowerCase()].orEmpty()

    private fun searchAll(terms: List<String>): List<Person> = terms.map { invertedIndexOf(it) }.reduce { a, b -> a.intersect(b) }.map { storage[it] }
    private fun searchAny(terms: List<String>): List<Person> = terms.map { invertedIndexOf(it) }.reduce { a, b -> a.union(b) }.map { storage[it] }
    private fun searchNone(terms: List<String>): List<Person> = storage.filter { searchAny(terms).contains(it).not() }

    fun invertedSearch() {
        createInvertedIndex()
        println("Select a matching strategy: ALL, ANY, NONE")
        val search = when (MatchingStrategy.valueOf(readLine()!!)) {
            MatchingStrategy.ALL -> ::searchAll
            MatchingStrategy.ANY -> ::searchAny
            MatchingStrategy.NONE -> ::searchNone
        }
        println("\nEnter a name or email to search all suitable people.")
        val input = readLine()!!
        val searchTerms = input.split("""\s+""".toRegex())
        val searchResult = search(searchTerms)

        if (searchResult.isNotEmpty()){
            println("\n${searchResult.size} persons found:")
            searchResult.forEach(::println)
        } else {
            println("\nNo matching people found")
        }
    }

    fun add(person: Person) = storage.add(person)
    fun printMenu() = print("\n=== Menu ===\n" +
                                "1. Search information.\n" +
                                "2. Print all data.\n" +
                                "0. Exit.\n")
    fun printStorageContets() {
        println("\n=== List of people ===")
        storage.forEach(::println)
    }
}

data class Person(var firstName: String="", var lastName: String="", var email: String=""){
    constructor(string: String): this("", "", "") {
        val (_, firstName, lastName, email)
                = Regex("""^(\w+) (\w+) ?(.*)?$""")
                .find(string)!!.groups.map { it!!.value }.toTypedArray()
        this.firstName = firstName
        this.lastName = lastName
        this.email = email
    }
    fun contains(string: String) =  firstName.contains(string, ignoreCase = true)
                                        || lastName.contains(string, ignoreCase = true)
                                        || email.contains(string, ignoreCase = true)
    override fun toString() = "$firstName $lastName" + if (email.isEmpty()) "" else " $email"
}