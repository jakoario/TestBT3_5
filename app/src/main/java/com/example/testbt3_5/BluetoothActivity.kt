package com.example.testbt3_5


import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.*
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileOutputStream
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import java.util.Date
import kotlin.math.round


class BluetoothActivity : AppCompatActivity(), LocationListener {

    private lateinit var speedData: TextView
    private lateinit var distanceData: TextView
    private lateinit var address: String
    private lateinit var speedGPSData: TextView
    private lateinit var distanceGPSData: TextView
    private lateinit var oldLocation: Location
    private lateinit var boutonStop: Button
    private lateinit var filename: String
    var distanceCumul: Double = 0.0
    var distanceCumulGPS: Double = 0.0


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // desactive la mise en veille automatique
        setContentView(R.layout.activity_bluetooth)

        address = intent.getStringExtra(MainActivity.EXTRA_ADDRESS).toString() // récupère l'adresse
        // du Bluetooth déterminée dans l'activité précédente (MainActivity)

        speedData = findViewById(R.id.speed_text)
        distanceData = findViewById(R.id.dist_text)
        speedGPSData = findViewById(R.id.tv_speedGPS)
        distanceGPSData = findViewById(R.id.dist_textGPS)
        boutonStop = findViewById(R.id.bouton_stop)

        // check for gps permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED)
            {
            requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 1000)
        } else {
            // start the program if the permission is granted
            doStuff()
        }

        val bt = BtInterface(handler, address)
        bt.connect()

        this.updateSpeed(null)

        // creer le nom du fichier de sauvegarde des donnees avec la date
        val formatter = SimpleDateFormat("yyyy_MM_dd_HH_mm_ss", Locale.FRANCE)
        val date = Date()
        filename = formatter.format(date) + "BT3_5.txt"

        boutonStop.setOnClickListener(View.OnClickListener {
            // sauvegarde des distances cumulees avant de fermer l activite
            saveData(filename, "\t\t" + distanceData.text.toString())
            saveData(filename, "\t\t" + distanceGPSData.text.toString())
            bt.close() // ferme la connexion Bluetooth
            finish() // ferme l activite
        })

    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // reactive la mise en veille automatique
    }

    val handler: Handler = object : Handler(Looper.getMainLooper()) {
        override fun handleMessage(msg: Message) {
            val data = msg.data.getString("receivedData")
            if (data != null) {

                // Calcul et envoie de la vitesse en km/h
                val speed: Double? = data.toDoubleOrNull()
                val periode = 0.5
                if (speed != null) {
                    val calculspeed: Double = speed * 1.44 / periode
                    if (calculspeed != 0.0) {
                        val roundspeed = BigDecimal(calculspeed).setScale(2, RoundingMode.HALF_EVEN) // arrondi à 2 décimales
                        val fmt = Formatter(StringBuilder())
                        fmt.format(Locale.FRANCE, "%5.1f", roundspeed) // mise en page
                        speedData.text = fmt.toString()

                        // sauvegarde de donnees
                        if (isExternalStorageReadable()) {
                            saveData(filename, "\n" + speedData.text)
                            saveData(filename, "\t\t" + speedGPSData.text)
                        }
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

    override fun onProviderDisabled(provider: String) {

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
        val strCurrentSpeed: String = fmt.toString()

        speedGPSData.text = strCurrentSpeed

        // distance cumulee
        if (::oldLocation.isInitialized) {  // oldLocation doit être initialisé pour pouvoir l'utiliser
            // OldLocation est initialisé après la 1ère detection de changement de location, dans onLocationChanged
            val results = FloatArray(1)
            if (location != null) {
                Location.distanceBetween(oldLocation.latitude, oldLocation.longitude, location.latitude, location.longitude, results)
            }
            val distance: Float = results[0]
            distanceCumulGPS += distance
            val distanceCumulArrondi = round(distanceCumulGPS)
            val strdistanceCumul: String = distanceCumulArrondi.toString()

            distanceGPSData.text = strdistanceCumul + " m"
        }

    }

    // Permission GPS
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

    // Vérifie si le stockage externe est possible
    private fun isExternalStorageReadable(): Boolean {
        return Environment.getExternalStorageState() in
                setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
    }

    fun saveData(dataName: String, data: String) {
        val file = File(getExternalFilesDir(null), dataName) // nom du fichier à changer
        var outputStream: FileOutputStream? = null
        try {
            file.createNewFile()
            //second argument of FileOutputStream constructor indicates whether
            //to append or create new file if one exists
            outputStream = FileOutputStream(file, true)
            outputStream.write(data.toByteArray())
            outputStream.flush() // inutile ?
            outputStream.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}