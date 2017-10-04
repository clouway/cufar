package com.clouway.cufar;

import com.clouway.cufar.flag.ApplierForFlag;
import com.clouway.cufar.flag.ChangeFlag;
import com.clouway.cufar.flag.FlagApplier;
import com.clouway.cufar.flag.RequiredAnnotationException;
import com.clouway.cufar.storage.FlagBase;
import com.google.common.collect.Lists;
import org.jmock.Expectations;
import org.jmock.auto.Mock;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static com.clouway.cufar.DateHelper.january;
import static com.clouway.cufar.DateHelper.zeroDate;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 */
public abstract class FlagServiceContractTest {

  @ApplierForFlag(FakeTaskScheduleFlag.class)
  class FakeTaskScheduleFlagApplier implements FlagApplier<FakeTask, Long> {

    @Override
    public void apply(FakeTask fakeTask, boolean flagSeen) {
      fakeTask.setScheduleChangeSeen(flagSeen);
    }

    @Override
    public Long getReferenceId(FakeTask fakeTask) {
      return fakeTask.getId();
    }
  }

  @ApplierForFlag(FakeTaskCommentsFlag.class)
  class FakeTaskCommentsFlagApplier implements FlagApplier<FakeTask, Long> {

    @Override
    public void apply(FakeTask fakeTask, boolean flagSeen) {
      fakeTask.setCommentsChangeSeen(flagSeen);
    }

    @Override
    public Long getReferenceId(FakeTask fakeTask) {
      return fakeTask.getId();
    }
  }

  class UnannotatedFlagApplier implements FlagApplier<FakeTask, Long> {

    @Override
    public void apply(FakeTask fakeTask, boolean flagSeen) {

    }

    @Override
    public Long getReferenceId(FakeTask fakeTask) {
      return null;
    }
  }


  @Rule
  public JUnitRuleMockery context = new JUnitRuleMockery();

  @Mock
  protected FlagBase flagBase;

  protected FakeTaskScheduleFlag taskScheduleFlag;
  protected FakeTaskCommentsFlag taskCommentsFlag;

  protected FlagService flagService;
  protected FakeTask task;
  protected List<FakeTask> taskList;

  protected List<Long> taskIdList;
  protected final String attender = "fake_attender";

  protected final Date dateBeforeUpdate = january(2015, 15);
  protected final Date dateOfUpdate = january(2015, 17);
  protected final Date dateAfterUpdate = january(2015, 18);

  protected List<List<Date>> fakeAttenderSeenDates;
  protected List<List<Date>> fakeUpdateDates;

  protected abstract FlagService newFlagService(FlagBase flagBase);

  @Before
  public void setUp() throws Exception {
    task = new FakeTask();
    taskList = Lists.newArrayList(task);
    taskIdList = Lists.newArrayList(task.getId());

    flagService = newFlagService(flagBase);

    taskScheduleFlag = new FakeTaskScheduleFlag();
    taskCommentsFlag = new FakeTaskCommentsFlag();

    fakeAttenderSeenDates = Lists.newArrayList();
    fakeUpdateDates = Lists.newArrayList();
  }

  @Test
  public void neverMadeAnyChangesToFlag() throws Exception {

    fakeAttenderSeenDates.add(Lists.newArrayList(zeroDate()));
    fakeUpdateDates.add(Lists.newArrayList(zeroDate()));

    expectForApplyFlags(Lists.<ChangeFlag>newArrayList(taskScheduleFlag), fakeAttenderSeenDates, fakeUpdateDates);
    flagService.applyFlags(attender, taskList, new FakeTaskScheduleFlagApplier());

    assertThat(task.isScheduleChangeSeen(), is(true));
  }

  @Test
  public void neverSeenTheFlags() throws Exception {

    fakeUpdateDates.add(Lists.newArrayList(dateOfUpdate));
    fakeAttenderSeenDates.add(Lists.newArrayList(zeroDate()));

    expectForUpdateFlag(taskScheduleFlag);
    flagService.addFlagChange(taskScheduleFlag, dateOfUpdate, task.getId());

    expectForApplyFlags(Lists.<ChangeFlag>newArrayList(taskScheduleFlag), fakeAttenderSeenDates, fakeUpdateDates);
    flagService.applyFlags(attender, taskList, new FakeTaskScheduleFlagApplier());

    assertThat(task.isScheduleChangeSeen(), is(false));
  }

  @Test
  public void flagsNotSeenRecentlyEnough() throws Exception {

    fakeUpdateDates.add(Lists.newArrayList(dateOfUpdate));
    fakeAttenderSeenDates.add(Lists.newArrayList(dateBeforeUpdate));

    expectForUpdateFlag(taskScheduleFlag);
    flagService.addFlagChange(taskScheduleFlag, dateOfUpdate, task.getId());

    expectForSeeFlag(taskScheduleFlag, dateBeforeUpdate);
    flagService.seeFlag(taskScheduleFlag, taskList, attender, dateBeforeUpdate);

    expectForApplyFlags(Lists.<ChangeFlag>newArrayList(taskScheduleFlag), fakeAttenderSeenDates, fakeUpdateDates);
    flagService.applyFlags(attender, taskList, new FakeTaskScheduleFlagApplier());

    assertThat(task.isScheduleChangeSeen(), is(false));
  }

  @Test
  public void flagsSeenAfterLastChanges() throws Exception {

    fakeUpdateDates.add(Lists.newArrayList(dateOfUpdate));
    fakeAttenderSeenDates.add(Lists.newArrayList(dateAfterUpdate));

    expectForUpdateFlag(taskScheduleFlag);
    flagService.addFlagChange(taskScheduleFlag, dateOfUpdate, task.getId());

    expectForSeeFlag(taskScheduleFlag, dateAfterUpdate);
    flagService.seeFlag(taskScheduleFlag, taskList, attender, dateAfterUpdate);

    expectForApplyFlags(Lists.<ChangeFlag>newArrayList(taskScheduleFlag), fakeAttenderSeenDates, fakeUpdateDates);
    flagService.applyFlags(attender, taskList, new FakeTaskScheduleFlagApplier());

    assertThat(task.isScheduleChangeSeen(), is(true));
  }

  @Test
  public void multipleChanges() throws Exception {

    fakeUpdateDates.add(Lists.newArrayList(dateOfUpdate));
    fakeUpdateDates.add(Lists.newArrayList(dateOfUpdate));
    fakeAttenderSeenDates.add(Lists.newArrayList(dateBeforeUpdate));
    fakeAttenderSeenDates.add(Lists.newArrayList(dateAfterUpdate));

    expectForUpdateFlag(taskScheduleFlag);
    flagService.addFlagChange(taskScheduleFlag, dateOfUpdate, task.getId());

    expectForUpdateFlag(taskCommentsFlag);
    flagService.addFlagChange(taskCommentsFlag, dateOfUpdate, task.getId());

    expectForSeeFlag(taskScheduleFlag, dateBeforeUpdate);
    flagService.seeFlag(taskScheduleFlag, taskList, attender, dateBeforeUpdate);

    expectForSeeFlag(taskCommentsFlag, dateAfterUpdate);
    flagService.seeFlag(taskCommentsFlag, taskList, attender, dateAfterUpdate);

    expectForApplyFlags(Lists.<ChangeFlag>newArrayList(taskScheduleFlag, taskCommentsFlag), fakeAttenderSeenDates, fakeUpdateDates);
    flagService.applyFlags(attender, taskList, new FakeTaskScheduleFlagApplier(), new FakeTaskCommentsFlagApplier());

    assertThat(task.isScheduleChangeSeen(), is(false));
    assertThat(task.isCommentsChangeSeen(), is(true));
  }

  @Test
  public void flagsSeenByReferenceIdsInsteadOfInstances() throws Exception {
    expectForSeeFlag(taskCommentsFlag, dateAfterUpdate);
    flagService.seeFlagByIds(taskCommentsFlag, taskIdList, attender, dateAfterUpdate);
  }

  @Test(expected = RequiredAnnotationException.class)
  public void flagApplierDoesNotHaveAnnotation() throws Exception {
    flagService.applyFlags(attender, taskList, new UnannotatedFlagApplier());
  }


  // Helper methods for expectations
  private void expectForApplyFlags(final List<ChangeFlag> changeFlags, final List<List<Date>> seenDates, final List<List<Date>> updateDates) {
    context.checking(new Expectations() {{

      oneOf(flagBase).findSeenDatesByAttender(with(notNullValue(changeFlags.getClass())), with(equal(attender)), with(equal(taskIdList)));
      will(returnValue(seenDates));

      oneOf(flagBase).findUpdateDates(with(notNullValue(changeFlags.getClass())), with(equal(taskIdList)));
      will(returnValue(updateDates));
    }});
  }

  private void expectForUpdateFlag(final ChangeFlag changeFlag) {
    context.checking(new Expectations() {{
      oneOf(flagBase).storeOrUpdate(changeFlag, task.getId(), dateOfUpdate);
    }});
  }

  private void expectForSeeFlag(final ChangeFlag changeFlag, final Date seenDate) {
    context.checking(new Expectations() {{
      oneOf(flagBase).setAttenderSeenDate(changeFlag, taskIdList, attender, seenDate);
    }});
  }
}
