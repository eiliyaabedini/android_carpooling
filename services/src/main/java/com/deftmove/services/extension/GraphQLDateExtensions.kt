package com.deftmove.services.extension

import com.deftmove.heart.common.extension.getDateFormatUTC
import java.util.Date

fun Date.toGraphQLString(): String = getDateFormatUTC("yyyy-MM-dd'T'HH:mm:ssZ").format(this)

fun String.toDate(): Date = getDateFormatUTC("yyyy-MM-dd'T'HH:mm:ss").parse(this)
