package com.clouway.cufar;

import com.clouway.cufar.storage.FlagBase;

/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 */
public final class FlagServiceFactory {

  public static FlagService getFlagService(FlagBase flagBase) {
    return new FlagServiceImpl(flagBase);
  }
}
