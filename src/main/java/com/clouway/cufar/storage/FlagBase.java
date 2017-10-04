package com.clouway.cufar.storage;

import com.clouway.cufar.flag.ChangeFlag;

import java.util.Date;
import java.util.List;

/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 */
public interface FlagBase {

  /**
   * Store a ChangeFlag instance with the specified lastUpdateDate or update the lastUpdateDate of an already stored instance.
   * @param changeFlag the specified ChangeFlag instance.
   * @param referenceId the id of the object that relates to the flag.
   * @param lastUpdateDate the date when the last update was made.
   * @return the stored ChangeFlag.
   */
  <T, I> ChangeFlag storeOrUpdate(ChangeFlag<T, I> changeFlag, I referenceId, Date lastUpdateDate);

  /**
   * Set the seen date of the ChangeFlag instance for the specified attender.
   * @param changeFlag the specified ChangeFlag instance.
   * @param referenceIds the ids of the objects that relate to the flag.
   * @param attender the specified attender(who is going to see the flag).
   * @param seenDate the date when the flag was seen by that attender.
   */
  <T, I> void setAttenderSeenDate(ChangeFlag<T, I> changeFlag, List<I> referenceIds, String attender, Date seenDate);

  /**
   * Return the dates at which each of the specified ChangeFlag instances has been seen by the specified attender.
   * @param changeFlags the specified ChangeFlag instances.
   * @param attender the specified attender(who has seen the flags).
   * @param referenceIds the ids of the objects that relate to the flags.
   * @return a list of lists for each ChangeFlag instance. Each inner list contains the dates at which each flag of each referent object has been seen by the attender.
   */
  List<List<Date>> findSeenDatesByAttender(List<? extends ChangeFlag> changeFlags, String attender, List<?> referenceIds);

  /**
   * Return the dates at which each of the specified ChangeFlag instances has been lastly updated.
   * @param changeFlags the specified ChangeFlag instances.
   * @param referenceIds the ids of the objects that relate to the flags.
   * @return a list of lists for each ChangeFlag instance. Each inner list contains the dates at which each flag of each referent object has been lastly updated.
   */
  List<List<Date>> findUpdateDates(List<? extends ChangeFlag> changeFlags, List<?> referenceIds);
}
