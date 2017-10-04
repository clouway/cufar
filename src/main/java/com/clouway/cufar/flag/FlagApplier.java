package com.clouway.cufar.flag;

/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 */
public interface FlagApplier<D, I> {

  /**
   * Apply the seen status value for the flag to the specified object instance.
   * @param d the specified instance.
   * @param flagSeen the value of the seen status.
   */
  void apply(D d, boolean flagSeen);

  /**
   * Return the id of the specified object instance.
   * @param d the specified instance.
   * @return the instance id.
   */
  I getReferenceId(D d);
}
