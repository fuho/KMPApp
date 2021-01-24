package components

import kotlinx.css.*
import kotlinx.css.properties.LineHeight
import kotlinx.css.properties.Transforms
import kotlinx.css.properties.deg
import kotlinx.css.properties.rotate
import org.fuho.sheefra.Boundary
import org.fuho.sheefra.CardinalDirection
import org.fuho.sheefra.Node
import react.*
import styled.css
import styled.styledDiv
import styled.styledSpan
import kotlin.Float


external interface MazeMessageProps : RProps {
    var boundary: Boundary
    var node: Node
    var message: String
    var randomFill: String
}

class MazeMessageComponent : RComponent<MazeMessageProps, AppState>() {

    override fun RBuilder.render() {
        styledDiv {
            for (y in props.boundary.a.y..props.boundary.b.y) {
                styledDiv {
                    for (x in props.boundary.a.x..props.boundary.b.x) {
                        props.node.path.firstOrNull { it.position.x == x && it.position.y == y }?.let {
                            mazeMessageCell {
                                text =
                                    if (props.message.isEmpty()) "➜" else {
                                        props.message.get((props.message.length + it.length - 1) % props.message.length)
                                            .toString()
                                    }
                                direction = when (it.direction) {
                                    CardinalDirection.NORTH -> 270f
                                    CardinalDirection.EAST -> 0f
                                    CardinalDirection.SOUTH -> 90f
                                    CardinalDirection.WEST -> 180f
                                }
                            }
                        } ?: run {
                            mazeMessageCell {
                                text = if (props.randomFill.isEmpty()) "" else props.randomFill.random().toString()
                                direction = listOf(270f, 0f, 90f, 180f).random()
                            }
                        }
                    }
                }
            }
        }
    }
}

fun RBuilder.mazeMessage(handler: MazeMessageProps.() -> Unit): ReactElement {
    return child(MazeMessageComponent::class) {
        this.attrs(handler)
    }
}

external interface MazeMessageCellProps : RProps {
    var text: String
    var direction: Float
}

class MazeMessageCellComponent : RComponent<MazeMessageCellProps, RState>() {

    override fun RBuilder.render() {
        styledSpan {
            css {
                display = Display.inlineFlex
                justifyContent = JustifyContent.center
                alignItems = Align.center
                width = 32.px
                height = 32.px
                fontSize = 32.px
                lineHeight = LineHeight("1")
                fontFamily = "'LimeLight'"
                transform = Transforms().apply { rotate(props.direction.deg) }
            }
            +props.text
        }
    }
}

fun RBuilder.mazeMessageCell(handler: MazeMessageCellProps.() -> Unit): ReactElement {
    return child(MazeMessageCellComponent::class) {
        this.attrs(handler)
    }
}

