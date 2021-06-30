package com.example.testbt3_5


import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.text.method.ScrollingMovementMethod
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.*
import kotlin.math.round


class BluetoothActivity : AppCompatActivity(), LocationListener {

    private lateinit var textviewData: TextView
    private lateinit var speedData: TextView
    private lateinit var distanceData: TextView
    private lateinit var address: String
    private lateinit var speedGPSData: TextView
    private lateinit var distanceGPSData: TextView
    private lateinit var oldLocation: Location
    var distanceCumul: Double = 0.0
    var distanceCumulGPS: Double = 0.0


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
        speedGPSData = findViewById(R.id.tv_speedGPS)
        distanceGPSData = findViewById(R.id.dist_textGPS)

        // check for gps permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1000)
        } else {
            // start the program if the permission is granted
            doStuff()
        }

        val bt = BtInterface(handler, address)
        bt.connect()

        this.updateSpeed(null)

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

    // Location and GPS methods

    override fun onLocationChanged(location: Location) {
        if (location != null) run {
            val myLocation = CLocation(location)
            this.updateSpeed(myLocation)
            oldLocation = myLocation
        }
    }

    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {

    }

    private fun doStuff() {
        val locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager
        if(locationManager !=null) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0.0F, this)
        }
        Toast.makeText(this, "Waiting for GPS connection", Toast.LENGTH_SHORT).show()
    }

    private fun updateSpeed(location: CLocation?) {
        var nCurrentSpeed: Float = 0F

        if (location != null) {
            nCurrentSpeed = location.speed
        }
        val fmt = Formatter(StringBuilder())
        fmt.format(Locale.FRANCE, "%5.1f", nCurrentSpeed)
        var strCurrentSpeed: String = fmt.toString()
        strCurrentSpeed = strCurrentSpeed.replace(" ", "0")

        speedGPSData.text = strCurrentSpeed + " km/h"

        // distance cumulee
        if (::oldLocation.isInitialized) {  // oldLocation doit être initialisé pour pouvoir l'utiliser
            // OldLocation est initialisé après la 1ère detection de changement de location, dans onLocationChanged
            val distance: Float = oldLocation.distanceTo(location)
            distanceCumulGPS += distance
            val distanceCumulArrondi = round(distanceCumulGPS)
            val strdistanceCumul: String = distanceCumulArrondi.toString()

            distanceGPSData.text = strdistanceCumul + " m"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == 1000) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doStuff()
            } else {
                finish()
            }
        }
    }

}