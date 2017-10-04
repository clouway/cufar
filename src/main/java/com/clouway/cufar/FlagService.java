package com.clouway.cufar;

import com.clouway.cufar.flag.ChangeFlag;
import com.clouway.cufar.flag.FlagApplier;

import java.util.Date;
import java.util.List;

/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 */
public interface FlagService {

  /**
   * Add a new flag or update an existing one.
   * @param changeFlag the changeFlag instance to be added or updated.
   * @param changedOn the date on which the flag was updated.
   * @param referenceId a list of the ids or the objects instances that refer to the specified flag.
   * @return
   */
  <T, I> ChangeFlag addFlagChange(ChangeFlag<T, I> changeFlag, Date changedOn, I referenceId);

  /**
   * Make the flag seen by the specified attender on the specified date.
   * @param changeFlag the changeFlag instance to be seen.
   * @param references a list of the objects instances that refer to the specified flag.
   * @param attender the attender to see the change.
   * @param seenOn the date when the flag is seen.
   */
  <T, I> void seeFlag(ChangeFlag<T, I> changeFlag, List<T> references, String attender, Date seenOn);

  /**
   * Make the flag seen by the specified attender on the specified date.
   * @param changeFlag the changeFlag instance to be seen.
   * @param referenceIds a list of the ids or the objects instances that refer to the specified flag.
   * @param attender the attender to see the change.
   * @param seenOn the date when the flag is seen.
   */
  <T, I> void seeFlagByIds(ChangeFlag<T, I> changeFlag, List<I> referenceIds, String attender, Date seenOn);

  /**
   * Apply changeFlags on the specified list of objects.
   * @param attender the attender who has seen or not seen the changes.
   * @param objects the list of objects on which the changeFlags will be applied.
   * @param flagAppliers flagApplier instances to use for applying flags.
   * @param <T> the type of the list of objects.
   * @param <I> the type of the objects' ids.
   */
  <T, I> void applyFlags(String attender, List<T> objects, FlagApplier<T, I>... flagAppliers);
}
