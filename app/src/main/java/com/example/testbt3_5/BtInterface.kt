package com.example.testbt3_5

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
import android.os.Bundle
import android.os.Handler
import java.io.IOException
import java.io.InputStream
import java.util.*


class BtInterface(var h: Handler, var address: String) {

    companion object {
        lateinit var socket: BluetoothSocket
    }

    var m_bluetoothAdapter: BluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
    val device = m_bluetoothAdapter.getRemoteDevice(address)
    val myUUID: UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
    val tmp: BluetoothSocket = device.createRfcommSocketToServiceRecord(myUUID)

    var receiverThread = ReceiverThread(h) // On crée le thread de réception des données avec
    // l'Handler venant de BluetoothActivity

    fun connect() {
        object : Thread() {
            override fun run() {
                socket = tmp
                try {
                    socket.connect()
                    receiverThread.start()
                } catch (connectException: IOException) {
                    connectException.printStackTrace()
                    try {
                        socket.close()
                    } catch (closeException: IOException) {
                        closeException.printStackTrace()

                    }
                }
            }
        }.start()
    }

    fun close() {
        try {
            socket.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    class ReceiverThread(var h: Handler) : Thread() {

        override fun run() {
            while (true) {
                try {       // On teste si des données sont disponibles
                    val receiveStream: InputStream = socket.inputStream
                    val buffer = ByteArray(1024) // buffer store for the stream
                    val k = receiveStream.read(buffer) // On lit les données, k représente le nombre de bytes lu
                    if (k > 0) {
                        // On convertit les données en String
                        val rawdata = ByteArray(k)
                        for (i in 0 until k) rawdata[i] = buffer[i]
                        val data = String(rawdata)

                        // On envoie les données vers l'UI avec l'Handler
                        val msg = h.obtainMessage()
                        val b = Bundle()
                        b.putString("receivedData", data)
                        msg.data = b
                        h.sendMessage(msg)
                    }

                } catch (e: IOException) {
                    e.printStackTrace()
                    break
                }
            }
        }
    }
}
