package com.deftmove.carpooling.interfaces.ride.create.model

import com.deftmove.carpooling.interfaces.ride.model.RideRole
import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.map.Route
import com.deftmove.heart.interfaces.model.SearchAddressPrediction
import java.io.Serializable
import java.util.Calendar
import java.util.Date

data class CreateRideModel(
    var origin: SearchAddressPrediction = SearchAddressPrediction(Location.EMPTY, false),
    var destination: SearchAddressPrediction = SearchAddressPrediction(Location.EMPTY, false),
    var startTime: Date = Calendar.getInstance().apply {
        add(Calendar.HOUR_OF_DAY, 2)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
    }.time,
    var route: Route? = null,
    var repeat: RepeatingRideModel = RepeatingRideModel(),
    var updateRepeatingRides: Boolean? = null,
    var role: RideRole = RideRole.DRIVER,
    var isEditing: Boolean = false,
    var editRideId: String? = null
) : Serializable {

    fun fillItUp(launchModel: CreateRideLaunchModel) {

        launchModel.rideForUpdate?.origin?.let {
            origin = SearchAddressPrediction(
                  location = it,
                  isCurrentLocation = false
            )
        }

        launchModel.rideForUpdate?.destination?.let {
            destination = SearchAddressPrediction(
                  location = it,
                  isCurrentLocation = false
            )
        }

        launchModel.rideForUpdate?.time?.let {
            startTime = it
        }

        route = launchModel.rideForUpdate?.route

        launchModel.rideForUpdate?.repeat?.let {
            repeat = it
        }

        launchModel.updateRepeatingRides?.let {
            updateRepeatingRides = it
        }

        role = launchModel.role

        isEditing = launchModel.rideForUpdate?.origin != null

        launchModel.rideForUpdate?.id.let {
            editRideId = it
        }
    }
}
