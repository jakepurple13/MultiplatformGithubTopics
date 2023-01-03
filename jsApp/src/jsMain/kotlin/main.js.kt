import androidx.compose.ui.window.Window
import org.jetbrains.skiko.wasm.onWasmReady
import com.programmersbox.common.MainApp

fun main() {
    onWasmReady {
        Window("GitHub Topics") {
            //MainApp()
        }
    }
}