package com.deftmove.services.profile

import com.apollographql.apollo.ApolloClient
import com.deftmove.carpooling.interfaces.authentication.model.UserDeviceType
import com.deftmove.carpooling.interfaces.authentication.model.UserGender
import com.deftmove.carpooling.interfaces.authentication.model.UserModel
import com.deftmove.carpooling.interfaces.profile.model.UserProfileModel
import com.deftmove.carpooling.interfaces.profile.service.ProfileService
import com.deftmove.carpooling.interfaces.service.rx.AsyncApollo
import com.deftmove.carpooling.interfaces.user.CurrentUserManager
import com.deftmove.carpooling.profile.FindUserQuery
import com.deftmove.carpooling.profile.UpdateUserMutation
import com.deftmove.carpooling.profile.UpdateUserNoResponseMutation
import com.deftmove.carpooling.type.FindUserInput
import com.deftmove.carpooling.type.UpdateUserInput
import com.deftmove.heart.errorhandler.GenericErrorHandler
import com.deftmove.heart.interfaces.ResponseResult
import com.deftmove.heart.interfaces.common.ReactiveTransformer
import com.deftmove.heart.interfaces.common.rx.doOnData
import com.deftmove.heart.interfaces.common.rx.doOnFailure
import com.deftmove.heart.interfaces.common.rx.flatMapData
import com.deftmove.heart.interfaces.common.rx.mapData
import com.deftmove.heart.interfaces.common.rx.toResult
import com.deftmove.services.extension.convert
import com.deftmove.services.extension.toGraphQLDeviceType
import com.deftmove.services.extension.toGraphQLGender
import io.reactivex.Observable
import io.reactivex.Single
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ProfileServiceImp(
    private val currentUserManager: CurrentUserManager,
    private val defaultApolloClient: ApolloClient,
    private val errorHandler: GenericErrorHandler,
    private val profileApi: ProfileApi,
    private val reactiveTransformer: ReactiveTransformer
) : ProfileService {

    override fun findUser(userId: String): Observable<ResponseResult<UserModel>> {
        val query = FindUserQuery.builder()
              .input(
                    FindUserInput.builder()
                          .id(userId)
                          .build()
              )
              .build()

        return AsyncApollo.from(defaultApolloClient.query(query))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData { it.findUser()!!.convert() }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }

    override fun getCurrentUser(): Single<ResponseResult<UserModel>> {
        val query = FindUserQuery.builder()
              .input(
                    FindUserInput.builder()
                          .id(null)
                          .build()
              )
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.query(query))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData { it.findUser()!!.convert() }
              .doOnData { userModel -> currentUserManager.setUser(userModel) }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }

    override fun updateUser(
        aboutMe: String?,
        carLicensePlate: String?,
        carModel: String?,
        deviceToken: String?,
        deviceType: UserDeviceType?,
        firstName: String?,
        gender: UserGender?,
        lastName: String?,
        phoneNumber: String?
    ): Single<ResponseResult<UserProfileModel>> {

        val input = UpdateUserInput.builder()
        aboutMe?.let { input.aboutMe(it) }
        carLicensePlate?.let { input.carLicensePlate(it) }
        carModel?.let { input.carModel(it) }
        deviceToken?.let { input.deviceToken(it) }
        deviceType?.let { input.deviceType(it.toGraphQLDeviceType()) }
        firstName?.let { input.firstname(it) }
        gender?.let { input.gender(it.toGraphQLGender()) }
        lastName?.let { input.lastname(it) }
        phoneNumber?.let { input.phoneNumber(it) }

        val mutation = UpdateUserMutation.builder()
              .input(input.build())
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.mutate(mutation))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .mapData { it.updateUser()!!.convert() }
              .doOnData { model -> currentUserManager.setUser(model.userModel) }
              .doOnFailure { errorHandler.handleThrowable(it) }
    }

    override fun updateUserNoResponse(
        aboutMe: String?,
        carLicensePlate: String?,
        carModel: String?,
        deviceToken: String?,
        deviceType: UserDeviceType?,
        firstName: String?,
        gender: UserGender?,
        lastName: String?,
        phoneNumber: String?
    ): Single<ResponseResult<Unit>> {

        val input = UpdateUserInput.builder()
        aboutMe?.let { input.aboutMe(it) }
        carLicensePlate?.let { input.carLicensePlate(it) }
        carModel?.let { input.carModel(it) }
        deviceToken?.let { input.deviceToken(it) }
        deviceType?.let { input.deviceType(it.toGraphQLDeviceType()) }
        firstName?.let { input.firstname(it) }
        gender?.let { input.gender(it.toGraphQLGender()) }
        lastName?.let { input.lastname(it) }
        phoneNumber?.let { input.phoneNumber(it) }

        val mutation = UpdateUserNoResponseMutation.builder()
              .input(input.build())
              .build()

        return AsyncApollo.fromSingle(defaultApolloClient.mutate(mutation))
              .subscribeOn(reactiveTransformer.ioScheduler())
              .doOnFailure { errorHandler.handleThrowable(it) }
              .mapData { Unit }
    }

    override fun uploadAvatarImage(imageFile: File): Single<ResponseResult<UserModel>> {
        val requestFile = imageFile.asRequestBody("multipart/form-data".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("file", imageFile.name, requestFile)
        return profileApi.uploadAvatar(body).toResult()
              .flatMapData {
                  getCurrentUser()
              }
              .subscribeOn(reactiveTransformer.ioScheduler())
              .observeOn(reactiveTransformer.mainThreadScheduler())
    }
}
