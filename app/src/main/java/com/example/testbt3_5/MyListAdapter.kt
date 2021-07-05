package com.example.testbt3_5

import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView

class MyListAdapter(private val context: Activity, private val devicename: MutableList<String>, private val MACaddress: ArrayList<BluetoothDevice>) :
    ArrayAdapter<String>(context, R.layout.custom_listview) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.custom_listview, null, true)

        val NameText = rowView.findViewById(R.id.device_name) as TextView
        val addressText = rowView.findViewById(R.id.MAC_address) as TextView

        NameText.text = devicename[position]
        addressText.text = MACaddress[position].toString()


        return rowView
    }

}
