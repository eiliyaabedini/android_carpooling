package com.deftmove.authentication.firebase

import android.net.Uri
import com.deftmove.authentication.BuildConfig
import com.google.firebase.dynamiclinks.DynamicLink
import com.google.firebase.dynamiclinks.FirebaseDynamicLinks
import io.reactivex.Single
import io.reactivex.subjects.SingleSubject

class FirebaseLinkCreator {

    fun createLink(magicToken: String): Single<String> {

        val subject: SingleSubject<String> = SingleSubject.create()

        FirebaseDynamicLinks.getInstance().createDynamicLink()
              .setLink(Uri.parse("${BuildConfig.CONFIG_WEB_HOST}/auth/$magicToken"))
              .setDomainUriPrefix(BuildConfig.CONFIG_DYNAMIC_LINK_PREFIX)
              .setAndroidParameters(DynamicLink.AndroidParameters.Builder().build())
              .setIosParameters(
                    DynamicLink.IosParameters
                          .Builder(BuildConfig.CONFIG_IOS_BUNDLE_ID)
                          .setAppStoreId(BuildConfig.CONFIG_IOS_APP_STORE_ID)
                          .build()
              )
              .buildShortDynamicLink()
              .addOnSuccessListener { result ->
                  // Short link created
                  val shortLink = result.shortLink
                  val flowchartLink = result.previewLink

                  val availableLink =
                        if (shortLink.toString().isNotBlank()) shortLink.toString() else flowchartLink.toString()
                  subject.onSuccess(availableLink)

              }.addOnFailureListener {
                  it.printStackTrace()
                  subject.onError(it)
              }

        return subject
    }
}
