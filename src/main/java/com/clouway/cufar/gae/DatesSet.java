package com.clouway.cufar.gae;

import java.util.Date;
import java.util.Hashtable;
import java.util.Map;

/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 */
class DatesSet {

  private String id;
  private Map<String, Date> dates = new Hashtable<String, Date>();

  public DatesSet(String id) {
    this.id = id;
  }

  public String getId() {
    return id;
  }

  public Map<String, Date> getDates() {
    return dates;
  }

  public void add(String flagName, Date date) {
    dates.put(flagName, date);
  }
}
