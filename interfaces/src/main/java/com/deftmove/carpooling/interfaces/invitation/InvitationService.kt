package com.deftmove.carpooling.interfaces.invitation

import com.deftmove.carpooling.interfaces.ride.model.Invitation
import com.deftmove.heart.interfaces.ResponseResult
import io.reactivex.Single

interface InvitationService {

    fun createInvitation(
        offerId: String,
        requestId: String
    ): Single<ResponseResult<Invitation>>

    fun confirmInvitation(
        invitationId: String
    ): Single<ResponseResult<Invitation>>

    fun cancelInvitation(
        invitationId: String
    ): Single<ResponseResult<String>>
}
