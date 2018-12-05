package com.deftmove.services.rx

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import io.reactivex.Maybe
import io.reactivex.schedulers.Schedulers
import java.io.ByteArrayOutputStream

class Base64ImageProcessor(
    private val context: Context
) {

    fun convertToBase64(imageUri: Uri, targetQuality: Int = 90, maxLargestSize: Int = 1080): Maybe<String> {
        return uriToScaledBitmap(context, imageUri, maxLargestSize, maxLargestSize)
              .map { bitmap ->
                  ByteArrayOutputStream().apply {
                      bitmap.compress(Bitmap.CompressFormat.JPEG, targetQuality, this)
                  }.toByteArray()
              }
              .map { bytes ->
                  BASE64_JPEG_PREFIX + Base64.encodeToString(bytes, Base64.NO_WRAP)
              }
    }

    private fun uriToScaledBitmap(context: Context, uri: Uri, maxWidth: Int, maxHeight: Int): Maybe<Bitmap> {
        return Maybe.create<Bitmap> { emitter ->
            try {
                val bounds = getBounds(context, uri)
                val sampleSize = calculateInSampleSize(bounds, maxWidth, maxHeight)
                val bitmap = getScaledBitmap(context, sampleSize, uri)
                bitmap?.let { emitter.onSuccess(it) }
                emitter.onComplete()
            } catch (e: Exception) {
                emitter.onError(e)
            }
        }.subscribeOn(Schedulers.newThread())
    }

    private fun getBounds(context: Context, uri: Uri): BitmapFactory.Options {
        val stream = context.contentResolver.openInputStream(uri)
        val options = BitmapFactory.Options().apply { inJustDecodeBounds = true }

        BitmapFactory.decodeStream(stream, null, options)

        return options
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, maxWidth: Int, maxHeight: Int): Int {
        val height = options.outHeight
        val width = options.outWidth

        var inSampleSize = 1

        if (height > maxHeight || width > maxWidth) {
            // Calculate the first inSampleSize value that is a power of 2 and where either width or
            // height are smaller than the requested sizes for the first time
            do {
                inSampleSize *= 2
            } while (height / inSampleSize >= maxHeight && width / inSampleSize >= maxWidth)
        }

        return inSampleSize
    }

    private fun getScaledBitmap(context: Context, sampleSize: Int, uri: Uri): Bitmap? {
        val stream = context.contentResolver.openInputStream(uri)
        val options = BitmapFactory.Options().apply { inSampleSize = sampleSize }

        return BitmapFactory.decodeStream(stream, null, options)
    }

    companion object {
        const val BASE64_JPEG_PREFIX = "data:image/jpeg;base64,"
    }
}
