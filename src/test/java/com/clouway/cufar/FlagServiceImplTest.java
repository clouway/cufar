package com.clouway.cufar;

import com.clouway.cufar.storage.FlagBase;

public class FlagServiceImplTest extends FlagServiceContractTest {

  @Override
  protected FlagService newFlagService(FlagBase flagBase) {
    return new FlagServiceImpl(flagBase);
  }
}