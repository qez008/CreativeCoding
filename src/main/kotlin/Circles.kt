import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.compositor.compose
import org.openrndr.extra.compositor.draw
import org.openrndr.extra.compositor.layer
import org.openrndr.extra.compositor.post
import org.openrndr.extra.fx.color.LumaOpacity
import org.openrndr.extra.olive.oliveProgram
import org.openrndr.shape.Circle

fun main() {
    application {


        configure {
            height = 800
            width = 720
        }

        oliveProgram {
            val c = compose {

                layer {
                    draw {
                        drawer.fill = ColorRGBa.WHITE
                        drawer.stroke = null

                        drawer.circle(Circle(300.0, 200.0, 80.0))
                        drawer.circle(Circle(310.0, 250.0, 70.0))
                    }
                    post(LumaOpacity().apply { foregroundOpacity = 0.5 })
                }


            }
            extend {
                c.draw(drawer)
            }
        }

    }
}