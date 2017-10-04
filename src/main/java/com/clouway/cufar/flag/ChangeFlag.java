package com.clouway.cufar.flag;


/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 */
public interface ChangeFlag<T, I> {

  /**
   * Return the ID of the specified object instance.
   * @param t the specified instance.
   * @return the instance ID.
   */
  I getReferenceId(T t);
}
