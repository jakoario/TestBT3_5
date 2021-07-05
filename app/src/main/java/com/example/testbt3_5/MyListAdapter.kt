package com.example.testbt3_5

import android.bluetooth.BluetoothDevice
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView

class MyListAdapter(private val context: Context, private val dataSource: ArrayList<BluetoothDevice>): BaseAdapter() {

    private val inflater: LayoutInflater = context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getCount(): Int {
        return dataSource.size // return size of data source
    }

    override fun getItem(position: Int): Any {
        return dataSource[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong() // the Id is the position of the Item
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val rowView = inflater.inflate(R.layout.custom_listview, parent, false)
        val nameTextView = rowView.findViewById(R.id.device_name) as TextView
        val addressTextView = rowView.findViewById(R.id.MAC_address) as TextView
        val bt = getItem(position) as BluetoothDevice
        nameTextView.text = bt.name
        addressTextView.text = bt.address
        return rowView
    }
}
