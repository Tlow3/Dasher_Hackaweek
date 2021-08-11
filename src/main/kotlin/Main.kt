import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.observable.doOnBeforeError
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.onErrorComplete
import com.pison.core.client.bindToLocalServer
import com.pison.core.client.monitorConnectedDevices
import com.pison.core.client.newPisonSdkInstance
import com.pison.core.generated.ImuGesture
import com.pison.core.shared.DEFAULT_PISON_PORT
import java.awt.MouseInfo
import java.awt.Robot
import java.awt.event.InputEvent

fun main() {
    val sdk = newPisonSdkInstance()
    // disposableScope will bundle all disposables produced within it.
    val disposable = disposableScope {
        // connect to Android device running HubApp on the local network
        sdk.bindToServer("192.168.15.233", DEFAULT_PISON_PORT)
            // produces observable that only emits when a new Pison device is connected
            .monitorConnectedDevices(enableLogging = true)
            .subscribeScoped { device ->
                device.monitorGestures()
                    .doOnBeforeError { println("device disconnected") }
                    .onErrorComplete()
                    .filter { it == ImuGesture.INDEX_CLICK }
                    .subscribeScoped {
                        val click = 1
                        onClick(click)
                        println("Click happened")
                    }
                device.monitorEulerAngles()
                    .doOnBeforeError { println("device disconnected") }
                    .onErrorComplete()
                    .subscribeScoped { angles ->
                        onAnglesChange(angles.roll)
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
val robot = Robot();


fun onClick(click: Int) {

    if(click == 1){
        robot.mousePress(InputEvent.BUTTON1_MASK);
        robot.mouseRelease(InputEvent.BUTTON1_MASK)
        clickHappened = 1
    }
}


fun onAnglesChange(roll: Float) {

    if()

    val mousepointer = MouseInfo.getPointerInfo().getLocation();val robot = Robot();
    robot.mouseMove(960, 540);
    robot.mouseMove(mousepointer.x, mousepointer.y - roll.toInt());

}
