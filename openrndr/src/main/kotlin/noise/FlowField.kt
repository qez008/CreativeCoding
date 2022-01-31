package noise

import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.noclear.NoClear
import org.openrndr.extra.noise.simplex
import org.openrndr.extra.noise.uniform
import org.openrndr.math.Vector2
import org.openrndr.shape.Circle
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin


fun main() = application {

    var step = 0.0

    configure {
        width = 400
        height = 600
    }

    program {

        val points = (0..height step 2).map { Vector2(0.0, it.toDouble()) }.toMutableList()
        val offsets = points.map { Vector2.uniform(min = -1.0) * 0.0 }

        extend(NoClear())

        extend {
            drawer.fill = ColorRGBa(0.0, 0.0, 0.0, 10 / 256.0)
            drawer.rectangle(0.0, 0.0, width.toDouble(), height.toDouble())
//            drawer.fill = ColorRGBa(1.0 - dt / 2, (dt / 1.2 - 1.0).coerceAtLeast(0.0), 0.0 + dt, 0.2)
            drawer.fill = ColorRGBa(0.9, 0.2, 0.4, 0.2)
            drawer.stroke = null
            val cs = (points zip offsets).map { (x, y) -> Circle(x + y, 2.0) }
//            drawer.circles(cs)
            for (c in cs) {
                val x = c.center.x / width
                val y = c.center.y / height
                val z = x * y - y
                drawer.fill = ColorRGBa(x, z, y, 0.4)
                drawer.circle(c)
            }

            flow(points, 0, step, width, height)
            step++
        }
    }
}


fun flow(points: MutableList<Vector2>, seed: Int, t: Double, maxW: Int? = null, maxH: Int? = null) {
    val k = 0.02
    val speed = cos(t / 100) * 5
    for (i in points.indices) {
        val point = points[i]
        val noise = simplex(seed, point.x * k, point.y * k, t / 100) * PI
        val v = Vector2(cos(noise), sin(noise)) * speed
        points[i] += v + Vector2.uniform(min = -0.5, max = 0.5)

        // wrap x
        maxW?.let { w ->
            var x = points[i].x
            if (x < 0) x = w.toDouble()
            else if (x > w) x = 0.0
            points[i] = Vector2(x, points[i].y)
        }
        // wrap y
        maxH?.let { h ->
            var y = points[i].y
            if (y < 0) y = h.toDouble()
            else if (y > h) y = 0.0
            points[i] = Vector2(points[i].x, y)
        }
    }
}

