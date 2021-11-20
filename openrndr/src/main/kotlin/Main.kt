import audio.waveform
import org.jtransforms.fft.DoubleFFT_1D
import org.jtransforms.fft.DoubleFFT_2D
import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.shape.Circle

fun main() {
    oscilloscope()
}

fun oscilloscope() = application {

    configure {
        width = 768
        height = 576
    }

    DoubleFFT_1D(1)

    oliveProgram {
        val n = 70
        val waveform = waveform(100_000, 100)
        val spread = width / n.toDouble()
        val rad = 3.0

        var i = 0

        extend {
            val circles = (0 until n).map { index ->
                val amp = waveform.getOrElse(i + index) { 0.0 }
                Circle(spread * index, 576 / 2 + amp, rad)
            }
            i += 2
            drawer.fill = ColorRGBa.RED
            drawer.circles(circles)
        }
    }
}


