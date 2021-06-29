package com.example.testbt3_5


import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.Message
import android.text.method.ScrollingMovementMethod
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import java.math.BigDecimal
import java.math.RoundingMode


class BluetoothActivity : AppCompatActivity() {

    private lateinit var textviewData: TextView
    private lateinit var speedData: TextView
    private lateinit var distanceData: TextView
    private lateinit var address: String
    var distanceCumul: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // desactive la mise en veille automatique
        setContentView(R.layout.activity_bluetooth)


        address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS).toString()

        textviewData = findViewById(R.id.receive_text)
        textviewData.movementMethod = ScrollingMovementMethod() // make it scrollable
        speedData = findViewById(R.id.speed_text)
        speedData.movementMethod = ScrollingMovementMethod() // make it scrollable
        distanceData = findViewById(R.id.dist_text)

        val bt = BtInterface(handler, address)
        bt.connect()

    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val data = msg.data.getString("receivedData")
            if (data != null) {
                textviewData.append(data)

                // Calcul et envoie de la vitesse en km/h
                val speed: Double? = data.toDoubleOrNull()
                val periode = 0.5
                if (speed != null) {
                    val calculspeed: Double = speed * 1.44 / periode
                    if (calculspeed != 0.0) {
                        val roundspeed = BigDecimal(calculspeed).setScale(2, RoundingMode.HALF_EVEN) // arrondi à 2 décimales
                        speedData.append(roundspeed.toString() + "\n")
                    }
                } else {
                    speedData.append("error\n")
                }

                // Calcul et envoie de la distance cumulee
                if (speed != null) {
                    distanceCumul += speed * 0.4
                    val rounddist = BigDecimal(distanceCumul).setScale(2, RoundingMode.HALF_EVEN) // arrondi à 2 décimales
                    distanceData.text = rounddist.toString() + " m"
                }
            }
        }
    }

}