import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.runBlocking

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont
import org.openrndr.draw.loadImage
import org.openrndr.extra.noise.random
import org.openrndr.extra.noise.simplex
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


const val MAX_ITERATIONS = 600
const val PARTICLES_PER_ITERATION = 120

val BACKGROUND = ColorRGBa(200 / 255.0, 200 / 255.0, 200 / 255.0, 255 / 255.0)

fun main() = application {

    configure {
        height = 1080 / 3 * 2
        width = 1920 / 3 * 2
    }

    program {

        val image = loadImage("data/images/pm5544.png")
        val font = loadFont("data/fonts/default.otf", 64.0)

        fun initialParticles() = (1..PARTICLES_PER_ITERATION).map {
            val pos = Vector2(random(0.0, width.toDouble()), random(0.0, height.toDouble()))
            Particle(pos, Vector2.ZERO, random(1.1, 2.8))
        }

        val particles = mutableListOf(initialParticles())

        extend {

            drawer.clear(BACKGROUND)

            runBlocking {

                val nextIteration = particles.last()
                    .chunked(PARTICLES_PER_ITERATION / 2)
                    .map { async(Dispatchers.Default) { it.map { p -> p.move(0..width, 0..height, deltaTime) } } }
                    .awaitAll()
                    .flatten()
                particles.add(nextIteration)
            }
            if (particles.size > MAX_ITERATIONS) particles.removeAt(0)


            // draw particles
            drawer.stroke = null
            for (i in particles.indices) {
                val alpha = (i / MAX_ITERATIONS.toDouble()) * 0.5
                drawer.fill = ColorRGBa(10 / 255.0, 20 / 255.0, 30 / 255.0, alpha)
                drawer.circles(particles[i].map { Circle(it.pos, it.radius) })

            }
        }

    }

}


data class Particle(

    val pos: Vector2,
    val vel: Vector2,
    val radius: Double,

    private val generation: Int = 0,

    ) {

    private val speed = 2.5


    fun move(wRange: IntRange, hRange: IntRange, deltaTime: Double): Particle {

        fun wrap(value: Double, range: IntRange) = when {
            value < range.first -> range.last.toDouble()
            value > range.last -> range.first.toDouble()
            else -> value
        }

        val nextPosition = with(pos + vel) { Vector2(wrap(x, wRange), wrap(y, hRange)) }

        val timeFactor = 200.0
        val speedFactor = 100.0 * 2
        val generationFactor = 0.001

        val (x, y) = nextPosition / speedFactor
        val z = deltaTime * timeFactor + generation * generationFactor

        val noiseValue = simplex(0, x, y, z) * PI
        val velocity = Vector2(sin(noiseValue), cos(noiseValue)) * speed
        val a = (velocity + vel).normalized

        return Particle(nextPosition, a * speed, radius, generation + (0..3).random())
    }
}
