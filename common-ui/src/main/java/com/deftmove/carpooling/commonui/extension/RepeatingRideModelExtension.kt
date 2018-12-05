package com.deftmove.carpooling.commonui.extension

import androidx.annotation.StringRes
import com.deftmove.carpooling.commonui.R
import com.deftmove.carpooling.interfaces.ride.create.model.RepeatingRideModel
import com.deftmove.heart.interfaces.common.TextUtils
import java.text.DateFormatSymbols
import java.util.Calendar

fun RepeatingRideModel.toText(
    textUtils: TextUtils,
    @StringRes everyStringResource: Int = R.string.repeating_days_every
): String? {
    return if (!isActive()) {
        null
    } else {
        summariseWeekdays(textUtils)?.let { it } ?: textUtils.getString(everyStringResource, makeListOfShortNames())
    }
}

private fun RepeatingRideModel.makeListOfShortNames(): String {
    val stringBuilder: StringBuilder = StringBuilder()
    if (monday) stringBuilder.append(DateFormatSymbols().shortWeekdays[Calendar.MONDAY]).append(", ")
    if (tuesday) stringBuilder.append(DateFormatSymbols().shortWeekdays[Calendar.TUESDAY]).append(", ")
    if (wednesday) stringBuilder.append(DateFormatSymbols().shortWeekdays[Calendar.WEDNESDAY]).append(", ")
    if (thursday) stringBuilder.append(DateFormatSymbols().shortWeekdays[Calendar.THURSDAY]).append(", ")
    if (friday) stringBuilder.append(DateFormatSymbols().shortWeekdays[Calendar.FRIDAY]).append(", ")
    if (saturday) stringBuilder.append(DateFormatSymbols().shortWeekdays[Calendar.SATURDAY]).append(", ")
    if (sunday) stringBuilder.append(DateFormatSymbols().shortWeekdays[Calendar.SUNDAY]).append(", ")

    return stringBuilder.toString().trim().dropLast(1)
}

fun RepeatingRideModel.isActive(): Boolean {
    return monday || tuesday || wednesday ||
          thursday || friday || saturday || sunday
}

private fun RepeatingRideModel.count(): Int {
    var count = 0
    if (monday) count++
    if (tuesday) count++
    if (wednesday) count++
    if (thursday) count++
    if (friday) count++
    if (saturday) count++
    if (sunday) count++

    return count
}

private fun RepeatingRideModel.summariseWeekdays(textUtils: TextUtils): String? {
    return if (count() == 2 && saturday && sunday) {
        textUtils.getString(R.string.repeating_days_weekend)
    } else if (count() == WEEK_DAYS_COUNT) {
        textUtils.getString(R.string.repeating_days_everyday)
    } else {
        null
    }
}

private const val WEEK_DAYS_COUNT: Int = 7
