package components

import kotlinx.css.*
import kotlinx.html.InputType
import kotlinx.html.js.onChangeFunction
import kotlinx.html.js.onClickFunction
import org.w3c.dom.HTMLInputElement
import org.w3c.dom.HTMLTextAreaElement
import react.*
import react.dom.br
import react.dom.div
import react.dom.textArea
import react.dom.value
import styled.*

external interface PuzzleToolbarProps : RProps {
    var boardWidth: Int?
    var maxBoardWidth: Int?
    var minBoardWidth: Int?
    var boardHeight: Int?
    var minBoardHeight: Int?
    var maxBoardHeight: Int?
    var solutionLength: Int?
    var message: String?
    var randomFill: String?
    var onNextClick: (() -> Unit)?
    var onStopClick: (() -> Unit)?
    var onWidthChanged: ((Int) -> Unit)?
    var onHeightChanged: ((Int) -> Unit)?
    var onLengthChanged: ((Int) -> Unit)?
    var onMessageChanged: ((String) -> Unit)?
    var onRandomFillChanged: ((String) -> Unit)?
    var isCalculating: Boolean
}

class PuzzleToolbar : RComponent<PuzzleToolbarProps, RState>() {

    override fun RBuilder.render() {
        styledDiv {
            css {
                display = Display.flex
            }
            styledDiv {
                styledDiv {
                    css {
                        alignItems = Align.end
                    }
                    numberInput {
                        attrs {
                            label = "WIDTH "
                            value = props.boardWidth
                            min = props.minBoardWidth
                            max = props.maxBoardWidth
                            onValueChange = {
                                props.onWidthChanged?.invoke(it)
                            }
                        }
                    }

                }
                styledDiv {
                    css {
                        alignItems = Align.end
                    }
                    numberInput {
                        attrs {
                            label = "HEIGHT "
                            value = props.boardHeight
                            min = props.minBoardHeight
                            max = props.maxBoardHeight
                            onValueChange = {
                                props.onHeightChanged?.invoke(it)
                            }
                        }
                    }
                }
                styledDiv {
                    css {
                        alignItems = Align.end
                    }
                    numberInput {
                        attrs {
                            label = "LENGTH "
                            value = props.solutionLength
                            onValueChange = {
                                props.onLengthChanged?.invoke(it)
                            }
                        }
                    }
                }
                styledButton {
                    css {
                        if (props.isCalculating) {
                            backgroundColor = Color.darkRed
                        }
                    }
                    attrs {
                        onClickFunction = {
                            props.onNextClick?.invoke()
                        }
                        disabled = props.isCalculating
                    }
                    +if (props.isCalculating) "Calculating.." else "Get Next Solution"
                }
                if (props.isCalculating) {
                    styledButton {
                        css {
                            backgroundColor = Color.black
                            color = Color.white
                        }
                        attrs {
                            onClickFunction = {
                                props.onStopClick?.invoke()
                            }
                        }
                        +"Stop!"
                    }
                }

            }
            styledDiv {
                css {
                }
                div {
                    styledLabel {
                        +"Message:"
                        br {}
                        textArea {
                            attrs {
                                name = "message"
                                placeholder = "Message to hide along the path"
                                value = props.message ?: ""
                                onChangeFunction = {
                                    val target = it.target as HTMLTextAreaElement
                                    props.onMessageChanged?.invoke(target.value)
                                }
                            }
                        }
                    }
                }
                div {
                    styledLabel {
                        +"Random Fill:"
                        br {}
                        textArea {
                            attrs {
                                name = "randomFill"
                                placeholder = "Text to use for random fill"
                                value = props.randomFill ?: ""
                                onChangeFunction = {
                                    val target = it.target as HTMLTextAreaElement
                                    props.onRandomFillChanged?.invoke(target.value)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun RBuilder.puzzleToolbar(handler: PuzzleToolbarProps.() -> Unit): ReactElement {
    return child(PuzzleToolbar::class) {
        this.attrs(handler)
    }
}

external interface NumberInputProps : RProps {
    var value: Int?
    var label: String?
    var min: Int?
    var max: Int?
    var onValueChange: ((Int) -> Unit)?
}

class NumberInput : RComponent<NumberInputProps, RState>() {
    override fun RBuilder.render() {
        styledLabel {
            css {
            }
            props.label?.let {
                +it
            }
            styledInput(InputType.number, name = "width") {
                css {
                    width = 50.px
                }
                attrs {
                    value = props.value.toString()
                    onChangeFunction = {
                        val target = it.target as HTMLInputElement
                        props.onValueChange?.invoke(target.value.toInt())
                    }
                    props.min?.let { min = it.toString() }
                    props.max?.let { max = it.toString() }
                }
            }
        }

    }
}

fun RBuilder.numberInput(handler: NumberInputProps.() -> Unit): ReactElement {
    return child(NumberInput::class) {
        this.attrs(handler)
    }
}
