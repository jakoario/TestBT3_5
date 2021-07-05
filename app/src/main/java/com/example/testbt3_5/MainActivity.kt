package com.example.testbt3_5

import android.app.Activity
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.collections.ArrayList

class MainActivity : AppCompatActivity() {

    private val REQUEST_CODE_ENABLE_BT:Int = 1
    val registerFAR = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        onActivityResult(REQUEST_CODE_ENABLE_BT, result)
    }

    companion object {
        val EXTRA_ADDRESS: String = "Device_address"
    }
    //bluetooth adapter
    lateinit var bAdapter: BluetoothAdapter
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON) // desactive la mise en veille automatique
        setContentView(R.layout.activity_main)

        // init bluetooth adapter
        bAdapter = BluetoothAdapter.getDefaultAdapter()
        // check if bluetooth is available or not
        if(bAdapter==null) {
            bluetoothStatusTv.text = "Bluetooth is not available"
        }
        else {
            bluetoothStatusTv.text = "Bluetooth is available"
        }
        // set image according to BT status (on/off)
        if (bAdapter.isEnabled){
            // BT is on
            bluetoothIv.setImageResource(R.drawable.ic_bluetooth_on)
        }
        else {
            // BT is off
            bluetoothIv.setImageResource(R.drawable.ic_bluetooth_off)
        }

        // turn on BT
        turnOnBtn.setOnClickListener {
            if(bAdapter.isEnabled){
                // already enabled
                Toast.makeText(this, "Already on", Toast.LENGTH_LONG).show()
            }
            else {
                //turn on BT
                val intent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
                registerFAR.launch(intent)
            }
        }
        // turn off BT
        turnOffBtn.setOnClickListener {
            if(!bAdapter.isEnabled){
                // already enabled
                Toast.makeText(this, "Already off", Toast.LENGTH_LONG).show()
            }
            else {
                //turn off BT
                bAdapter.disable()
                bluetoothIv.setImageResource(R.drawable.ic_bluetooth_off)
                Toast.makeText(this, "Bluetooth turn off", Toast.LENGTH_LONG).show()
            }

        }

        // get list of paired devices
        pairedBtn.setOnClickListener {
            val devices = bAdapter.bondedDevices
            val deviceslist: ArrayList<BluetoothDevice> = ArrayList()
            val namelist = mutableListOf<String>()
            if (bAdapter.isEnabled) {

                pairedTv.text = "Paired Devices"
                // get list of paired devices

                for (bt: BluetoothDevice in devices) {
                    val deviceName = bt.name
                    pairedTv.append("\nDevice : $deviceName , $bt") // remplir la zone de texte
                    namelist.add(deviceName)
                    deviceslist.add(bt)
                    Log.i("device", ""+bt) // remplir la listview
                }
            } else {

                Toast.makeText(this, "Turn on bluetooth first", Toast.LENGTH_LONG).show()
            }
            //val adapter = ArrayAdapter(this, android.R.layout.simple_list_item_2, deviceslist)
            val myListAdapter = MyListAdapter(this,namelist,deviceslist)
            paired_list.adapter = myListAdapter
            paired_list.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
                val selectDeviceName = namelist[position]
                Toast.makeText(this, "Selected device : $selectDeviceName", Toast.LENGTH_SHORT)
                    .show()
                val device: BluetoothDevice = deviceslist[position]
                val address: String = device.address

                val i = Intent(this, BluetoothActivity::class.java)
                i.putExtra(EXTRA_ADDRESS, address)
                startActivity(i)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }


    private fun onActivityResult(requestCode: Int, result: ActivityResult) {
        val intent = result.data
        when(requestCode) {
            REQUEST_CODE_ENABLE_BT ->
                if(result.resultCode == Activity.RESULT_OK) {
                    bluetoothIv.setImageResource(R.drawable.ic_bluetooth_on)
                    Toast.makeText(this, "Bluetooth is on", Toast.LENGTH_SHORT).show()
                } else {
                    // user denied to turn on bluetooth from confirmation dialog
                    Toast.makeText(this, "Could not on Bluetooth", Toast.LENGTH_SHORT).show()
                }
        }
    }
}

