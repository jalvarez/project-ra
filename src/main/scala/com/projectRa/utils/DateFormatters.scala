package com.projectRa.utils

import java.text.SimpleDateFormat

trait DateFormatters { // TODO Refactor to thread safety
  lazy val formatterTimestamp = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
  lazy val formatterDay = new SimpleDateFormat("yyyy-MM-dd")
  lazy val formatterTimestampUTC = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'UTC'")
}