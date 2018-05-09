package views

import org.joda.time.DateTime

package object DateExtension {
  class Methods(tick: Long) {
    def toDate = new DateTime(tick)
    def toDateString : String = {
      val date = this.toDate
      return date.getDayOfMonth + "/" + date.getMonthOfYear + "/" + date.getYear + " " + date.getHourOfDay + ":" + date.getMinuteOfHour
    }
  }

  implicit def Methods(tick: Long) = new Methods(tick)
}