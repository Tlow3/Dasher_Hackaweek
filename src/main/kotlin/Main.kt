import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.observable.doOnBeforeError
import com.badoo.reaktive.observable.onErrorComplete
import com.pison.core.client.bindToLocalServer
import com.pison.core.client.monitorConnectedDevices
import com.pison.core.client.newPisonSdkInstance
import com.pison.core.shared.DEFAULT_PISON_PORT

fun main() {
    val sdk = newPisonSdkInstance()

    // disposableScope will bundle all disposables produced within it.
    val disposable = disposableScope {
        // connect to Android device running HubApp on the local network
        sdk.bindToServer("192.168.1.11", DEFAULT_PISON_PORT)
            // produces observable that only emits when a new Pison device is connected
            .monitorConnectedDevices(enableLogging = true)
            .subscribeScoped { device ->
                device.monitorEulerAngles()
                    .doOnBeforeError { println("device disconnected") }
                    .onErrorComplete()
                    .subscribeScoped { angles ->
                        println("got euler angles: ${angles.roll}, ${angles.pitch}, ${angles.yaw}")
                    }
            }
    }

    while (true) {
        if (readLine() == "exit") {
            break
        }
    }

    // shut down any remaining connection to server
    disposable.dispose()
}