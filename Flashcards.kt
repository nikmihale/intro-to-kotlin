package flashcards

import java.io.File
import java.io.FileNotFoundException
import java.util.*
import kotlin.collections.HashMap

fun main(args: Array<String>) {
    val parsedArgs = parseArgs(args)
    val scanner = Scanner(System.`in`)
    val cards = CardIndex()
    if (parsedArgs.containsKey("-import")) {
        cards.import(parsedArgs["-import"]!!)
    }
    do {

        flashcards.println("Input the action (add, remove, import, export, ask, exit, log, hardest card, reset stats):")
        val action = Actions.valueOf(scanner.nextLineWithLog().toUpperCase().replace(' ', '_'))
        when (action) {
            Actions.ADD -> cards.add(scanner)
            Actions.REMOVE -> cards.remove(scanner)
            Actions.ASK -> cards.ask(scanner)
            Actions.EXPORT -> { flashcards.println("File name:"); cards.export(scanner.nextLineWithLog()) }
            Actions.IMPORT -> { flashcards.println("File name:"); cards.import(scanner.nextLineWithLog()) }
            Actions.LOG -> cards.log(scanner)
            Actions.HARDEST_CARD -> cards.hardest()
            Actions.RESET_STATS -> cards.reset()
            Actions.EXIT -> flashcards.println("Bye bye!")
        }
    } while (action != Actions.EXIT)
    if (parsedArgs.containsKey("-export")) {
        cards.export(parsedArgs["-export"]!!)
    }
}

fun parseArgs(args: Array<String>): HashMap<String, String> {
    var map = HashMap<String, String>()
    if (args.isNotEmpty()) {
        for (i in 0 until args.lastIndex step 2) {
            if (args[i].startsWith('-')) map[args[i]] = args[i + 1]
        }
    }
    return map
}

enum class Actions { ADD, REMOVE, IMPORT, EXPORT, ASK, EXIT, LOG, HARDEST_CARD, RESET_STATS }

object Log {
    var contents = mutableListOf<String>()
    fun writeToLog(string: String): Unit { this.contents.add(string) }
    fun saveToFile(string: String): Unit {
        File(string).writeText(contents.stream().reduce("", String::plus).toString())
        flashcards.println("The log has been saved.")
    }
}

fun println(string: String): Unit {
    Log.writeToLog(string)
    kotlin.io.println(string)
}


fun Scanner.nextLineWithLog(): String {
    val line = this.nextLine()
    Log.writeToLog(line)
    return line
}

class CardIndex() {
    val index: MutableList<Card> = mutableListOf()

    fun add(scanner: Scanner) {
        flashcards.println("The card:")
        val term = scanner.nextLineWithLog()
        if (termInArray(term)) {
            flashcards.println("The card \"$term\" already exists.")
            return
        }
        flashcards.println("The definition of the card:")
        val definition = scanner.nextLineWithLog()
        if (definitionInArray(definition)) {
            flashcards.println("The definition \"$definition\" already exists.")
            return
        }
        index.add(Card(term, definition, 0))
        flashcards.println("The pair (\"$term\":\"$definition\") has been added.")
    }

    fun remove(scanner: Scanner) {
        flashcards.println("Which card:")
        val term = scanner.nextLineWithLog()
        if (termInArray(term)) {
            index.removeIf { it.term == term }
            flashcards.println("The card has been removed.")
        } else flashcards.println("Can't remove \"$term\": there is no such card.")
    }

    fun export(string: String) {
        val file = File(string)
        file.writeText("")
        for (card in index) {
            file.appendText("${card.term}:${card.definition}:${card.errorCount}\n")
        }
        flashcards.println("${file.readLines().size} cards have been saved.")
    }

    fun import(string: String) {
        try {
            val file = File(string)
            for (line in file.readLines()) {
                val card = line.split(":".toRegex())
                val term = card.first()
                val definition = card.get(1)
                val errorCount = card.last().toInt()
                when (termInArray(term)) {
                    true -> index.find { it.term == term }!!.definition = definition
                    else -> index.add(Card(term, definition, errorCount))
                }

            }
            flashcards.println("${file.readLines().size} cards have been loaded.")
        } catch (e: FileNotFoundException) {
            flashcards.println("File not found.")
        }
    }

    fun ask(scanner: Scanner) {
        fun checkCard(card: Card, scanner: Scanner) {
            fun correct() = flashcards.println("Correct answer.")
            fun incorrect(correctDefinition: String) = flashcards.println("Wrong answer. The correct one is \"${correctDefinition}\"")
            fun incorrect(givenDefinition: String, correctDefinition: String ) = flashcards.println("Wrong answer. The correct one is \"${correctDefinition}\", " +
                        "you've just written the definition of \"${index.first { it.definition == givenDefinition }.term}\".")
                flashcards.println("Print the definition of \"${card.term}\":")
                val definition = scanner.nextLineWithLog()
                val correctDefinition = card.definition
                when {
                    definitionInArray(definition) && definition == correctDefinition -> correct()
                    definitionInArray(definition) && definition != correctDefinition -> { incorrect(definition, correctDefinition); card.errorCount++ }
                    else -> { incorrect(correctDefinition); card.errorCount++ }
                }
        }
        flashcards.println("How many times to ask?")
        val quantity = scanner.nextLineWithLog().toInt()
        val random = kotlin.random.Random
        repeat(quantity) {
            checkCard(index.get(random.nextInt(index.size)), scanner)
        }
    }

    fun log(scanner: Scanner) {
        flashcards.println("File name:")
        Log.saveToFile(scanner.nextLineWithLog())
    }

    fun hardest() {
        val highestCount = index.map { it.errorCount }.max()
        val hardestCards = index.filter { it.errorCount!= 0 }.filter { it.errorCount == highestCount }
        val appendTerm = {s: String, c: Card -> "$s\"${c.term}\" "}
        when (hardestCards.isEmpty()) {
            true -> flashcards.println("There are no cards with errors.")
            else -> flashcards.println("The hardest card is ${hardestCards.fold("", appendTerm)}. You have ${highestCount} errors answering it.")
        }
    }

    fun reset() {
        index.forEach { it.errorCount = 0 }
        flashcards.println("Card statistics have been reset.")
    }

    fun termInArray(term: String): Boolean {
        return index.map { card ->  card.term }.contains(term)
    }

    fun definitionInArray(definition: String): Boolean {
        return index.map { card ->  card.definition }.contains(definition)
    }
}

data class Card(var term: String, var definition: String, var errorCount: Int)