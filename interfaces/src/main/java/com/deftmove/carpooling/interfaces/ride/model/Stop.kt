package com.deftmove.carpooling.interfaces.ride.model

import com.deftmove.heart.interfaces.map.Location
import com.github.vivchar.rendererrecyclerviewadapter.ViewModel
import java.io.Serializable

data class Stop(
    val duration: Int,
    val location: Location
) : ViewModel, Serializable {

    override fun equals(other: Any?): Boolean {
        return other is Stop &&
              other.duration == this.duration &&
              other.location == this.location
    }

    override fun hashCode(): Int {
        return super.hashCode()
    }
}
