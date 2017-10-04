package com.clouway.cufar;

import java.util.Calendar;
import java.util.Date;

/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 */
public class DateHelper {

  public static Date january(int year, int day) {
    return newDate(year, 1, day);
  }

  public static Date zeroDate() {
    return new Date(0l);
  }

  private static Date newDate(int year, int month, int day) {
    Calendar calendar = Calendar.getInstance();
    calendar.set(Calendar.YEAR, year);
    calendar.set(Calendar.MONTH, month);
    calendar.set(Calendar.DAY_OF_MONTH, day);
    calendar.set(Calendar.HOUR, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 1);

    return calendar.getTime();
  }
}
