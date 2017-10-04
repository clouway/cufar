package com.clouway.cufar;

import com.clouway.cufar.flag.ChangeFlag;
import com.clouway.cufar.flag.FlagFor;

/**
* @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
*/
@FlagFor(target = "FakeTask", flagName = "FakeTaskSchedule")
public class FakeTaskScheduleFlag implements ChangeFlag<FakeTask, Long> {

  @Override
  public Long getReferenceId(FakeTask fakeTask) {
    return fakeTask.getId();
  }
}
