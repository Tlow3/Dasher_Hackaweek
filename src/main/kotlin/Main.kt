import com.badoo.reaktive.disposable.scope.disposableScope
import com.badoo.reaktive.observable.doOnBeforeError
import com.badoo.reaktive.observable.filter
import com.badoo.reaktive.observable.onErrorComplete
import com.badoo.reaktive.observable.throttle
import com.badoo.reaktive.scheduler.computationScheduler
import com.pison.core.client.monitorConnectedDevices
import com.pison.core.client.newPisonSdkInstance
import com.pison.core.generated.ImuGesture
import com.pison.core.shared.DEFAULT_PISON_PORT
import java.awt.Dimension
import java.awt.MouseInfo
import java.awt.Robot
import java.awt.Toolkit
import java.awt.event.InputEvent


fun main() {
    val sdk = newPisonSdkInstance()
    // disposableScope will bundle all disposables produced within it.
    val disposable = disposableScope {
        // connect to Android device running HubApp on the local network
        sdk.bindToServer("192.168.1.11", DEFAULT_PISON_PORT)
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
                device.monitorGestures()
                    .doOnBeforeError { println("device disconnected") }
                    .onErrorComplete()
                    .filter { it == ImuGesture.INDEX_HOLD }
                    .subscribeScoped {
                        val hold = 1
                        isHold(hold)
                        println("Hold happened")
                    }
                device.monitorEulerAngles()
                    .doOnBeforeError { println("device disconnected") }
                    .onErrorComplete()
                    .throttle(30)
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
var x = 0

fun positionMouse(){
    val d: Dimension = Toolkit.getDefaultToolkit().getScreenSize()
    println(d)
    robot.mouseMove(d.width / 2, d.height / 2)
}

fun mousePress(){
    //Press mouse
    robot.mousePress(InputEvent.BUTTON1_MASK);
    robot.mouseRelease(InputEvent.BUTTON1_MASK)
}

fun dasherSlow(){
    //set X value for mouse
    val d: Dimension = Toolkit.getDefaultToolkit().getScreenSize()
    val mousepointer = MouseInfo.getPointerInfo().getLocation();
    robot.mouseMove(d.width / 2 + d.width/40, mousepointer.y);
}

fun dasherFast(){
    //set X value for mouse
    val d: Dimension = Toolkit.getDefaultToolkit().getScreenSize()
    val mousepointer = MouseInfo.getPointerInfo().getLocation();
    robot.mouseMove(d.width / 2 + d.width/20, mousepointer.y);
}

fun dasherBack(){
    //set X value for mouse
    val d: Dimension = Toolkit.getDefaultToolkit().getScreenSize()
    val mousepointer = MouseInfo.getPointerInfo().getLocation();
    robot.mouseMove(d.width / 2 - d.width/40, mousepointer.y);
}

fun onClick(click: Int) {

    if(click == 1){
        when (x%3) {
            0 -> positionMouse()
            1 -> dasherSlow()
            2 -> dasherBack()
            //3 -> dasherBack()
        }
        x++
    }
}

fun isHold(hold: Int) {

    if(hold == 1){
        mousePress()
    }
}

fun onAnglesChange(roll: Float) {

    val mousepointer = MouseInfo.getPointerInfo().getLocation();//val robot = Robot();
    robot.mouseMove(mousepointer.x, mousepointer.y - roll.toInt());

}
