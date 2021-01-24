package components

import kotlinx.coroutines.*
import kotlinx.css.Color
import kotlinx.css.backgroundColor
import kotlinx.css.fontSize
import kotlinx.css.px
import org.fuho.sheefra.Boundary
import org.fuho.sheefra.Node
import org.fuho.sheefra.Position
import org.fuho.sheefra.SelfAvoidingPathGenerator
import react.*
import styled.css
import styled.styledDiv

external interface AppState : RState {
    var message: String
    var randomFill: String
    var width: Int
    var height: Int
    var length: Int
    var generator: SelfAvoidingPathGenerator?
    var solutions: MutableList<Node>
    var shownSolution: Node?
    var isCalculating: Boolean
    var calculationScope: CoroutineScope?
}

class App : RComponent<RProps, AppState>() {

    override fun AppState.init() {
        val start = Position(0, 0)
        val end = Position(7, 7)
        val boundary = Boundary(start, end)

        width = boundary.width
        height = boundary.height
        length = 27
        message = ""
        randomFill = "‧"
        solutions = mutableListOf()
        generator = SelfAvoidingPathGenerator(boundary = boundary, length = length)
        shownSolution = generator?.apply {
            solution.hasNext()
        }?.solution?.next()
        isCalculating = false
        calculationScope = null
    }

/*
    private suspend fun getSolutions(count: Int = 1): List<Node> = coroutineScope {
        (0..count).map { round ->
            println("getSolutions($count) round $round")
            async {
                getSolution()
            }
        }.awaitAll().filterNotNull()
    }
*/

    private suspend fun getSolution(): Node? = coroutineScope {
        withContext(Dispatchers.Main) {
            val nodeOrNull = async {
                if (state.generator?.solution?.hasNext() == true) {
                    state.generator?.solution?.next()
                } else {
                    null
                }
            }.await()
            nodeOrNull
        }
    }

    private fun createGenerator(width: Int, height: Int, length: Int): SelfAvoidingPathGenerator? {
        try {
            val start = Position(0, 0)
            val end = Position(width - 1, height - 1)
            val generator = SelfAvoidingPathGenerator(
                Boundary(start, end),
                length,
                start,
                end,
            )
            return generator
        } catch (e: IllegalArgumentException) {
            println(e.message)
            return null
        }
    }

    private fun handleNextClick() {
        println("Next clicked")
        setState {
            isCalculating = true
        }
        val scope = MainScope()
        val job = scope.async {
            val solutionOrNull = getSolution()
            setState {
                solutionOrNull?.let {
                    shownSolution = it
                }
                calculationScope = null
                isCalculating = false
            }
        }
        job.start()
//        job.await()
        setState {
            calculationScope = scope
        }
    }

    private fun handleStopClick() {
        setState {
            isCalculating = false
        }
        println("Stop clicked")
        state.calculationScope?.apply {
            cancel("Calculation cancelled by user.")
        }
    }

    private fun handleWidthChange(newWidth: Int) {
        println("Width changed to : $newWidth")
        setState {
            width = newWidth
            generator = createGenerator(newWidth, state.height, state.length)
        }
    }

    private fun handleHeightChange(newHeight: Int) {
        println("Height changed to : $newHeight")
        setState {
            height = newHeight
            generator = createGenerator(state.width, newHeight, state.length)
        }
    }

    private fun handleLengthChange(newLength: Int) {
        println("Length changed to : $newLength")
        setState {
            length = newLength
            generator = createGenerator(state.width, state.height, newLength)
        }
    }

    private fun handleMessageChange(message: String) {
        println("Message to encode changed to $message")
        setState { this.message = message }
    }

    private fun handleRandomFillChange(randomFill: String) {
        println("Random fill changed to $randomFill")
        setState { this.randomFill = randomFill }
    }

    override fun RBuilder.render() {
        styledDiv {
            puzzleToolbar {
                boardWidth = state.width
                minBoardWidth = 2
                maxBoardWidth = 64
                boardHeight = state.height
                minBoardHeight = 2
                maxBoardHeight = 64
                solutionLength = state.length
                message = state.message
                randomFill = state.randomFill
                onWidthChanged = { handleWidthChange(it) }
                onHeightChanged = { handleHeightChange(it) }
                onLengthChanged = { handleLengthChange(it) }
                onNextClick = { handleNextClick() }
                onStopClick = { handleStopClick() }
                onMessageChanged = { handleMessageChange(it) }
                onRandomFillChanged = { handleRandomFillChange(it) }
                isCalculating = state.isCalculating
            }
            if (state.generator == null) {
                styledDiv {
                    css {
                        backgroundColor = Color.red
                        fontSize = 32.px
                    }
                    +"No generator"
                }
            } else if (state.shownSolution == null) {
                styledDiv {
                    css {
                        backgroundColor = Color.yellow
                        fontSize = 32.px
                    }
                    +"No solution to show"
                }
            } else {
                mazeMessage {
                    boundary = state.generator!!.boundary
                    node = state.shownSolution!!
                    message = state.message
                    randomFill = state.randomFill
                }
            }
/*
            if (state.isCalculating) {
                styledDiv {
                    css {
                        width = 500.px
                        height = 500.px
                        position = kotlinx.css.Position.absolute
                        top = 0.px
                        right = 0.px
                        backgroundColor = Color.yellow.withAlpha(.5)
                        color = Color.purple
                    }
                    +"WRKNG"
                }
            }
*/

        }
    }
}