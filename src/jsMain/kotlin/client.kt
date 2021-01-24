import components.App
import kotlinx.browser.document
import react.dom.render


fun main() {
    document.bgColor = "#111"
    document.fgColor = "#EEE"
    val root = document.getElementById("root")
    render(root) {
        child(App::class) {}
    }
}
