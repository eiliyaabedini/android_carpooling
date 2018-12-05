package com.deftmove.carpooling.interfaces

import com.deftmove.carpooling.interfaces.authentication.login.LoginWithMagicTokenModel
import com.deftmove.carpooling.interfaces.ride.create.model.CreateRideModel
import com.deftmove.carpooling.interfaces.ride.create.model.RepeatingRideModel
import com.deftmove.carpooling.interfaces.ride.model.RideForFeed
import com.deftmove.carpooling.interfaces.ride.model.RideRole
import com.deftmove.heart.interfaces.map.Location

data class OpenAuthenticationMagicTokenSentDialog(val emailAddress: String)
data class OpenProfilePublicScreen(val userId: String)
data class OpenLoginWithMagicTokenScreen(val model: LoginWithMagicTokenModel)
data class OpenCreateRideScreen(
    val role: RideRole,
    val rideForUpdate: RideForFeed? = null,
    val updateRepeatingRides: Boolean? = null
)

data class OpenWayPointsScreen(val model: CreateRideModel)
data class OpenCreateReviewScreen(val model: CreateRideModel)
data class OpenRideDetails(val rideId: String)
data class OpenRepeatingScreen(val repeatingRideModel: RepeatingRideModel)
object OpenSplashScreen
object OpenDebugScreen
object OpenDebugAccountSwitcherScreen
object OpenSignInScreen
object OpenRideFeedOrRegistrationScreen
object OpenProfileScreen
object OpenEditProfileScreen
object OpenNotificationsScreen

data class OpenAddressSearch(
    val showCurrentLocationRow: Boolean = true,
    val showSelectOnMapRow: Boolean = true,
    val defaultLocation: Location? = null,
    val addressTypeName: String? = ""
)
