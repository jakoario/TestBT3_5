package com.example.testbt3_5

import android.location.Location

class CLocation : Location {

    constructor(location: Location): super(location)

    override fun getSpeed(): Float {

        return super.getSpeed() * 3.6F
    }

}