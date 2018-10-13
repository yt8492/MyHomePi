package com.yt8492.myhomesensor

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.android.things.contrib.driver.ht16k33.Ht16k33
import com.google.android.things.contrib.driver.rainbowhat.RainbowHat
import com.google.android.things.pio.Gpio
import com.google.android.things.pio.GpioCallback
import com.google.android.things.pio.PeripheralManager
import com.google.firebase.FirebaseApp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import kotlinx.android.synthetic.main.activity_main.*
import java.util.concurrent.TimeUnit

/**
 * Skeleton of an Android Things activity.
 *
 * Android Things peripheral APIs are accessible through the class
 * PeripheralManagerService. For example, the snippet below will open a GPIO pin and
 * set it to HIGH:
 *
 * <pre>{@code
 * val service = PeripheralManagerService()
 * val mLedGpio = service.openGpio("BCM6")
 * mLedGpio.setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
 * mLedGpio.value = true
 * }</pre>
 * <p>
 * For more complex peripherals, look for an existing user-space driver, or implement one if none
 * is available.
 *
 * @see <a href="https://github.com/androidthings/contrib-drivers#readme">https://github.com/androidthings/contrib-drivers#readme</a>
 *
 */
class MainActivity : Activity(){

    private val segment by lazy {
        RainbowHat.openDisplay().apply {
            setBrightness(Ht16k33.HT16K33_BRIGHTNESS_MAX)
            setEnabled(true)
        }
    }
    private val led by lazy {
        RainbowHat.openLedBlue().apply {
            setDirection(Gpio.DIRECTION_OUT_INITIALLY_LOW)
        }
    }
    private val db by lazy {
        FirebaseFirestore.getInstance()
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        button.setOnClickListener {
            getStatus()
        }
        getStatus()
    }

    override fun onDestroy() {
        super.onDestroy()
        segment.close()
        led.close()
    }

    private fun getStatus() {
        db.collection("status")
                .get()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        log("success")
                        task.result?.let { results ->
                            val data = results.last().data
                            led.value = data["led"] as Boolean
                            segment.display((data["segment"] as? Long)?.toInt() ?: 0)
                        }
                    } else {
                        log("failed")
                    }
                }
    }
}

fun log(msg: String) {
    Log.d("MyApp", msg)
}