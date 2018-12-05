package com.deftmove.services.extension

import com.deftmove.carpooling.authentication.GetCurrentCustomerQuery
import com.deftmove.carpooling.authentication.LoginWithMagicTokenMutation
import com.deftmove.carpooling.fragment.InvitationForRideDetailsFragment
import com.deftmove.carpooling.fragment.LocationFragment
import com.deftmove.carpooling.fragment.MoneyFragment
import com.deftmove.carpooling.fragment.NotificationDataFragment
import com.deftmove.carpooling.fragment.NotificationFragment
import com.deftmove.carpooling.fragment.RecommendationForRideDetailsFragment
import com.deftmove.carpooling.fragment.RepeatingRideFragment
import com.deftmove.carpooling.fragment.RideForDetailsFragment
import com.deftmove.carpooling.fragment.RideForFeedFragment
import com.deftmove.carpooling.fragment.RideFragment
import com.deftmove.carpooling.fragment.StopWithTimeFragment
import com.deftmove.carpooling.fragment.UserFragment
import com.deftmove.carpooling.interfaces.authentication.model.CurrentUserModel
import com.deftmove.carpooling.interfaces.authentication.model.CustomerModel
import com.deftmove.carpooling.interfaces.authentication.model.UserDeviceType
import com.deftmove.carpooling.interfaces.authentication.model.UserGender
import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.carpooling.interfaces.notifications.model.NotificationDataModel
import com.deftmove.carpooling.interfaces.notifications.model.NotificationModel
import com.deftmove.carpooling.interfaces.profile.model.UserProfileModel
import com.deftmove.carpooling.interfaces.ride.create.model.RepeatingRideModel
import com.deftmove.carpooling.interfaces.ride.details.model.RideForDetails
import com.deftmove.carpooling.interfaces.ride.model.Invitation
import com.deftmove.carpooling.interfaces.ride.model.Recommendation
import com.deftmove.carpooling.interfaces.ride.model.RideForFeed
import com.deftmove.carpooling.interfaces.ride.model.RideRole
import com.deftmove.carpooling.interfaces.ride.model.Stop
import com.deftmove.carpooling.onboarding.OnBoardUserMutation
import com.deftmove.carpooling.profile.FindUserQuery
import com.deftmove.carpooling.profile.UpdateUserMutation
import com.deftmove.carpooling.ride.FindRoutesQuery
import com.deftmove.carpooling.type.DeviceType
import com.deftmove.carpooling.type.Gender
import com.deftmove.carpooling.type.LocationInput
import com.deftmove.carpooling.type.RepeatInput
import com.deftmove.carpooling.type.Role
import com.deftmove.carpooling.type.StopInput
import com.deftmove.heart.interfaces.common.model.Money
import com.deftmove.heart.interfaces.map.Coordinate
import com.deftmove.heart.interfaces.map.Location
import com.deftmove.heart.interfaces.map.LocationWithTime
import com.deftmove.heart.interfaces.map.Route

fun UserGender.toGraphQLGender(): Gender = when (this) {
    UserGender.FEMALE -> Gender.FEMALE
    UserGender.MALE -> Gender.MALE
    UserGender.UNKNOWN -> Gender.OTHER
}

fun UserDeviceType.toGraphQLDeviceType(): DeviceType = when (this) {
    UserDeviceType.ANDROID -> DeviceType.ANDROID
}

fun Gender.toUserGender(): UserGender = when (this) {
    Gender.MALE -> UserGender.MALE
    Gender.FEMALE -> UserGender.FEMALE
    else -> UserGender.UNKNOWN
}

fun Role.toRideRole(): RideRole = when (this) {
    Role.DRIVER -> RideRole.DRIVER
    Role.PASSENGER -> RideRole.PASSENGER
    else -> error("unknown role $this")
}

fun LoginWithMagicTokenMutation.LoginWithMagicToken.convert(): CurrentUserModel {
    return CurrentUserModel(
          apiToken = apiToken(),
          user = user().fragments().userFragment().convert()
    )
}

fun GetCurrentCustomerQuery.CurrentCustomer.convert(): CustomerModel {
    return CustomerModel(
          currencyIsoCode = customer().currencyIsoCode(),
          farePerKm = customer().farePerKm().fragments().moneyFragment().convert(),
          name = customer().name()
    )
}

fun OnBoardUserMutation.OnboardUser.convert(): UserProfileModel {
    return UserProfileModel(
          userModel = user().fragments().userFragment().convert()
    )
}

fun UpdateUserMutation.UpdateUser.convert(): UserProfileModel {
    return UserProfileModel(
          userModel = user().fragments().userFragment().convert()
    )
}

fun FindRoutesQuery.FindRoutes.convert(): List<Route> {
    return routes()?.map { route ->
        Route(
              distance = route.distance(),
              duration = route.duration(),
              polyline = route.polyline(),
              stopOrder = route.stopOrder(),
              stops = route.stops()?.map { stop ->
                  stop.location().fragments().locationFragment().convert(stop.duration().toDouble())
              } ?: emptyList()
        )
    } ?: emptyList()
}

fun FindUserQuery.FindUser.convert(): UserModel {
    return user().fragments().userFragment().convert()
}

fun UserFragment.convert(): UserModel {
    return UserModel(
          id = id(),
          firstName = firstname(),
          lastName = lastname(),
          email = email(),
          gender = gender()?.toUserGender(),
          avatarUrl = avatarUrl(),
          aboutMe = aboutMe(),
          phoneNumber = phoneNumber(),
          carLicensePlate = carLicensePlate(),
          carModel = carModel(),
          memberSince = memberSince(),
          numberOfRidesAsDriver = numberOfRidesAsDriver(),
          numberOfRidesAsPassenger = numberOfRidesAsPassenger()
    )
}

fun RideFragment.convert(): RideForFeed {
    val offerCount: Int =
          invitations()?.filter {
              it.cancelledAt() == null && it.confirmedAt() == null && it.senderId() == it.driverId()
          }?.count() ?: 0

    val requestCount: Int =
          invitations()?.filter {
              it.cancelledAt() == null && it.confirmedAt() == null && it.senderId() == it.passengerId()
          }?.count() ?: 0

    val confirmedCount: Int =
          invitations()?.filter { it.cancelledAt() == null && it.confirmedAt() != null }?.count()
                ?: 0

    val cancelledCount: Int = invitations()?.filter { it.cancelledAt() != null && it.confirmedAt() == null }?.count()
          ?: 0

    val declinedCount: Int = invitations()?.filter { it.cancelledAt() != null && it.confirmedAt() != null }?.count()
          ?: 0

    val sumConfirmedPrice: Money =
          invitations()?.filter { it.cancelledAt() == null && it.confirmedAt() != null }
                ?.fold(Money.EMPTY) { total, next ->
                    total + (next.grossPrice()?.fragments()?.moneyFragment()?.convert()
                          ?: Money.EMPTY)
                } ?: Money.EMPTY

    return RideForFeed(
          id = id(),
          origin = origin().fragments().locationFragment().convert(),
          destination = destination().fragments().locationFragment().convert(),
          route = if (distance() != null || duration() != null ||
              polyline() != null) {
              Route(
                    distance = distance() ?: 0.0,
                    duration = duration() ?: 0.0,
                    polyline = polyline() ?: "",
                    stops = emptyList(),
                    stopOrder = null
              )
          } else null,
          role = role().toRideRole(),
          time = time(),
          recommendationsCount = recommendationsCount() ?: 0,
          offeredCount = offerCount,
          requestedCount = requestCount,
          confirmedCount = confirmedCount,
          cancelledCount = cancelledCount,
          declinedCount = declinedCount,
          sumConfirmedPrice = sumConfirmedPrice,
          driver = null,
          repeat = RepeatingRideModel()
    )
}

fun RideForFeedFragment.convert(): RideForFeed {
    val offerCount: Int =
          invitations()?.filter {
              it.cancelledAt() == null && it.confirmedAt() == null && it.senderId() == it.driverId()
          }?.count() ?: 0

    val requestCount: Int =
          invitations()?.filter {
              it.cancelledAt() == null && it.confirmedAt() == null && it.senderId() == it.passengerId()
          }?.count() ?: 0

    val confirmedCount: Int =
          invitations()?.filter { it.cancelledAt() == null && it.confirmedAt() != null }?.count()
                ?: 0

    val cancelledCount: Int = invitations()?.filter { it.cancelledAt() != null && it.confirmedAt() == null }?.count()
          ?: 0

    val declinedCount: Int = invitations()?.filter { it.cancelledAt() != null && it.confirmedAt() != null }?.count()
          ?: 0

    val sumConfirmedPrice: Money =
          invitations()?.filter { it.cancelledAt() == null && it.confirmedAt() != null }
                ?.fold(Money.EMPTY) { total, next ->
                    total + (next.grossPrice()?.fragments()?.moneyFragment()?.convert()
                          ?: Money.EMPTY)
                } ?: Money.EMPTY

    val driver: UserModel? = when (role()) {
        Role.PASSENGER -> invitations()
              ?.filter {
                  it.cancelledAt() == null && it.confirmedAt() != null
              }
              ?.take(1)
              ?.map { it.driver().fragments().userFragment().convert() }?.firstOrNull()
        else -> null
    }

    return RideForFeed(
          id = id(),
          origin = origin().fragments().locationFragment().convert(),
          destination = destination().fragments().locationFragment().convert(),
          route = if (distance() != null || duration() != null ||
              polyline() != null) {
              Route(
                    distance = distance() ?: 0.0,
                    duration = duration() ?: 0.0,
                    polyline = polyline() ?: "",
                    stops = stops()?.map { it.convert().location } ?: emptyList(),
                    stopOrder = null
              )
          } else null,
          role = role().toRideRole(),
          time = time(),
          recommendationsCount = recommendationsCount() ?: 0,
          offeredCount = offerCount,
          requestedCount = requestCount,
          confirmedCount = confirmedCount,
          cancelledCount = cancelledCount,
          declinedCount = declinedCount,
          sumConfirmedPrice = sumConfirmedPrice,
          driver = driver,
          repeat = repeatingRide()?.fragments()?.repeatingRideFragment()?.convert()
                ?: RepeatingRideModel()
    )
}

fun RideForDetailsFragment.convert(): RideForDetails {
    return RideForDetails(
          id = id(),
          origin = origin().fragments().locationFragment().convert(),
          destination = destination().fragments().locationFragment().convert(),
          polyline = polyline(),
          recommendationsCount = recommendationsCount(),
          role = role().toRideRole(),
          time = time(),
          invitations = invitations()?.map { it.fragments().invitationForRideDetailsFragment().convert() }
                ?: emptyList(),
          recommendations = recommendations()?.map { it.fragments().recommendationForRideDetailsFragment().convert() }
                ?: emptyList()
    )
}

fun InvitationForRideDetailsFragment.convert(): Invitation {
    return Invitation(
          id = id(),
          cancelledAt = cancelledAt(),
          confirmedAt = confirmedAt(),
          offerId = offerId(),
          requestId = requestId(),
          senderId = senderId(),
          passengerId = passengerId(),
          driverId = driverId(),
          sharedRoute = sharedRoute(),
          pickup = pickup()?.fragments()?.stopWithTimeFragment()?.convert(),
          dropoff = dropoff()?.fragments()?.stopWithTimeFragment()?.convert(),
          grossPrice = grossPrice()?.fragments()?.moneyFragment()?.convert(),
          driver = driver().fragments().userFragment().convert(),
          passenger = passenger().fragments().userFragment().convert()
    )
}

fun RecommendationForRideDetailsFragment.convert(): Recommendation {
    return Recommendation(
          id = id(),
          origin = origin().fragments().locationFragment().convert(),
          destination = destination().fragments().locationFragment().convert(),
          pickup = pickup()?.fragments()?.stopWithTimeFragment()?.convert(),
          dropoff = dropoff()?.fragments()?.stopWithTimeFragment()?.convert(),
          grossPrice = grossPrice()?.fragments()?.moneyFragment()?.convert(),
          role = role().toRideRole(),
          sharedRoute = sharedRoute(),
          time = time(),
          user = user().fragments().userFragment().convert()
    )
}

fun StopWithTimeFragment.convert(): LocationWithTime {
    return LocationWithTime(
          time = time(),
          name = location()?.name(),
          address = location()?.address() ?: "",
          coordinate = Coordinate(
                latitude = location()!!.latitude(),
                longitude = location()!!.longitude()
          )
    )
}

fun LocationFragment.convert(duration: Double? = null): Location {
    return Location(
          duration = duration ?: 0.0,
          name = name(),
          address = address() ?: "",
          coordinate = Coordinate(
                latitude = latitude(),
                longitude = longitude()
          )
    )
}

fun Location.toLocationInput(): LocationInput = LocationInput.builder()
      .name(name)
      .address(address)
      .latitude(coordinate.latitude)
      .longitude(coordinate.longitude)
      .build()

fun Location.toStopInput(): StopInput {
    return StopInput.builder()
          .duration(this.duration.toInt())
          .location(
                LocationInput.builder()
                      .latitude(this.coordinate.latitude)
                      .longitude(this.coordinate.longitude)
                      .address(this.address)
                      .name(this.name)
                      .build()
          ).build()
}

fun MoneyFragment.convert(): Money {
    return Money(
          amount = amount(),
          amountCents = amountCents(),
          currency = currency(),
          currencySymbol = currencySymbol()
    )
}

fun NotificationFragment.convert(): NotificationModel {
    return NotificationModel(
          id = id(),
          message = message(),
          title = title(),
          insertedAt = insertedAt(),
          seen = seen(),
          data = data()?.map { data -> data.fragments().notificationDataFragment().convert() }
                ?: emptyList()
    )
}

fun NotificationDataFragment.convert(): NotificationDataModel {
    return NotificationDataModel(
          key = key(),
          value = value()
    )
}

fun RepeatingRideModel.convert(): RepeatInput {
    return RepeatInput.builder()
          .monday(monday)
          .tuesday(tuesday)
          .wednesday(wednesday)
          .thursday(thursday)
          .friday(friday)
          .saturday(saturday)
          .sunday(sunday)
          .build()
}

fun RepeatingRideFragment.convert(): RepeatingRideModel {
    return RepeatingRideModel(
          monday = monday(),
          tuesday = tuesday(),
          wednesday = wednesday(),
          thursday = thursday(),
          friday = friday(),
          saturday = saturday(),
          sunday = sunday()
    )
}

fun RideForFeedFragment.Stop.convert(): Stop {
    return Stop(
          duration = duration(),
          location = location().fragments().locationFragment().convert()
    )
}
