package com.clouway.cufar.storage;


public class InMemoryFlagBaseTest extends FlagBaseContractTest {

  @Override
  protected FlagBase newFlagBase() {
    return new InMemoryFlagBase();
  }
}