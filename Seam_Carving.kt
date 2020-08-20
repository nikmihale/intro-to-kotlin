package seamcarving

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

fun main(args: Array<String>) {
    val fileIn = File(args[args.indexOf("-in") + 1])
    val imageIn = ImageIO.read(fileIn)
    val verticalSeams = args[args.indexOf("-width") + 1].toInt()
    val HorizontalSeams = args[args.indexOf("-height") + 1].toInt()

    var rescaler = Rescaler(imageIn)
    repeat(verticalSeams) {
        rescaler = rescaler.removeVerticalSeam()
    }
    repeat(HorizontalSeams) {
        rescaler = rescaler.removeHorizontalSeam()
    }
    var imageOut = rescaler.image
    val fileOut = File(args[args.indexOf("-out") + 1])
    ImageIO.write(imageOut, "png", fileOut)
}

fun drawSeam(image: BufferedImage, seam: List<Pair<Int, Int>>): BufferedImage {
    val output = copyOf(image)
    seam.forEach { output.setRGB(it.first, it.second, Color(255,0,0).rgb) }
    return output
}
fun copyOf(image: BufferedImage): BufferedImage {
    val output = BufferedImage(image.width, image.height, image.type)
    val g = output.graphics
    g.drawImage(image, 0, 0, null)
    g.dispose()
    return output
}
fun transposeImage(image: BufferedImage): BufferedImage {
    val transposed = BufferedImage(image.height, image.width, image.type)
    for (y in 0 until image.height)
        for (x in 0 until image.width)
            transposed.setRGB(y, x, image.getRGB(x, y))
    return transposed
}
class Rescaler(var image: BufferedImage) {
    fun removeVerticalSeam(): Rescaler {
        val seam = findVerticalSeam()
        val output = BufferedImage(image.width - 1, image.height, image.type)
        val g = output.graphics

        seam.forEach {
            if ( it.first > 0 )
                g.drawImage(image.getSubimage(0, it.second, it.first, 1), 0, it.second, null )
            if (it. first + 1 < image.width)
                g.drawImage(image.getSubimage(it.first + 1, it.second, image.width - it.first - 1, 1), it.first , it.second, null)
        }
        return Rescaler(output)
    }
    fun removeHorizontalSeam() = Rescaler(transposeImage(Rescaler(transposeImage(image)).removeVerticalSeam().image))
    fun findHorizontalSeam() = Rescaler(transposeImage(image)).findVerticalSeam().map { Pair(it.second, it.first) }
    fun findVerticalSeam(): List<Pair<Int, Int>> = restoreSeam(propagateMapDownwards(createEnergyMap()))
    private fun createEnergyMap(): MutableMap<Pair<Int, Int>, Double> {
        val output = mutableMapOf<Pair<Int, Int>, Double>()
        for (y in 0 until image.height)
            for (x in 0 until image.width)
                output[Pair(x, y)] = energy(x, y)
        return output
    }
    private fun propagateMapDownwards(map: MutableMap<Pair<Int, Int>, Double>): MutableMap<Pair<Int, Int>, Double> {
        val propagatedMap = mutableMapOf<Pair<Int, Int>, Double>()
        propagatedMap.putAll(map)
        for (y in 1 until image.height)
            for (x in 0 until image.width)
                propagatedMap[Pair(x, y)] = propagatedMap[Pair(x, y)]!! +
                        IntRange(max(x-1,0), min(x+1, image.width-1)).mapNotNull { propagatedMap[Pair(it, y-1)] }.min()!!
        return propagatedMap
    }
    private fun restoreSeam(map: MutableMap<Pair<Int, Int>, Double>): List<Pair<Int, Int>> {
        val end = map.filterKeys { it.second == image.height - 1 }.minBy { it.value }!!.key
        val result = mutableListOf(end)
        for (y in image.height - 1 downTo 0) {
            val x = result.last().first
            IntRange(max(x-1, 0), min(x+1, image.width-1)).mapNotNull { Pair(it, y) }.minBy { map[it]!! }!!.let { result.add(it) }
        }
        return result
    }
    private fun energy(x: Int, y: Int): Double = sqrt((gradX(x, y) + gradY(x, y)).toDouble())
    private fun gradX(x: Int, y: Int): Int = when (x) {
        0 -> gradX(1, y)
        image.width-1 -> gradX(image.width-2, y)
        else -> distanceBetween(
                Color(image.getRGB(x - 1, y)),
                Color(image.getRGB(x + 1, y)))
    }
    private fun gradY(x: Int, y: Int): Int = when (y) {
        0 -> gradY(x, 1)
        image.height-1 -> gradY(x, image.height-2)
        else -> distanceBetween(
                Color(image.getRGB(x, y - 1)),
                Color(image.getRGB(x, y + 1)))
    }
    private fun distanceBetween(left: Color, right: Color): Int {
        return  (left.red - right.red)*(left.red - right.red) +
                (left.green - right.green)*(left.green - right.green) +
                (left.blue - right.blue)*(left.blue - right.blue)
    }
}