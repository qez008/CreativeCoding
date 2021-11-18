import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.draw.loadFont
import org.openrndr.draw.loadImage
import org.openrndr.extra.compositor.*
import org.openrndr.extra.fx.blend.Multiply
import org.openrndr.extra.fx.color.LumaOpacity
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.math.Vector2
import org.openrndr.math.transforms.transform
import org.openrndr.shape.Circle

fun main() = application {
    configure {
        width = 768
        height = 576
    }

    oliveProgram {

        val TRS = TransRotScale()
        val image = loadImage("data/images/mediterranean-white.png")
        val font = loadFont("data/fonts/default.otf", 32.0)
        val ps = listOf(
            Vector2(650.0, 350.0),
            Vector2(450.0, 300.0),
            Vector2(400.0, 100.0)
        )

        val ports = compose {
            layer {
                draw {

                    val cs = ps.map { (x, y) -> Circle(x, y, 18.0) }
                    drawer.fill = ColorRGBa.BLACK
                    drawer.stroke = null
                    drawer.circles(cs)
                }
                post(LumaOpacity().apply { this.backgroundOpacity = 0.5 })
                blend(Multiply())
            }
            layer {
                draw {
                    drawer.fontMap = font
                    var i = 1
                    for ((x, y) in ps) {
                        val str = "${9 * i++}"
                        drawer.text(str, x - (7 * str.length), y + 8)
                    }
                }
            }
        }

        extend(TRS)

        extend {
            val k = 0.35
            drawer.image(image, 0.0, 0.0, 3000.0 * k, 1811.0 * k)
            ports.draw(drawer)
        }
    }
}