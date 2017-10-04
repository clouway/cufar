package com.clouway.cufar.gae;

import com.clouway.cufar.storage.FlagBase;
import com.clouway.cufar.storage.FlagBaseContractTest;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import org.junit.After;


public class PersistenceFlagBaseTest extends FlagBaseContractTest {

  private final LocalServiceTestHelper helper = new LocalServiceTestHelper(new LocalDatastoreServiceTestConfig());
  private DatastoreService datastoreService = DatastoreServiceFactory.getDatastoreService();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    helper.setUp();
  }

  @After
  public void tearDown() throws Exception {
    helper.tearDown();
  }

  @Override
  protected FlagBase newFlagBase() {
    return new PersistenceFlagBase(datastoreService);
  }

}