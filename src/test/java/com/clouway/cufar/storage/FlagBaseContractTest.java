package com.clouway.cufar.storage;

import com.clouway.cufar.flag.ChangeFlag;
import com.clouway.cufar.flag.FlagFor;
import com.clouway.cufar.flag.RequiredAnnotationException;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;
import java.util.List;

import static com.clouway.cufar.DateHelper.january;
import static com.clouway.cufar.DateHelper.zeroDate;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

public abstract class FlagBaseContractTest {

  protected class ChatRoom {
    private Long id;

    public ChatRoom(Long id) {
      this.id = id;
    }

    public Long getId() {
      return id;
    }
  }

  @FlagFor(target = "ChatRoom", flagName = "ChatRoomComments")
  private class CommentsFlag implements ChangeFlag<ChatRoom, Long> {

    @Override
    public Long getReferenceId(ChatRoom chatRoom) {
      return chatRoom.getId();
    }
  }

  @FlagFor(target = "ChatRoom", flagName = "ChatRoomDescription")
  private class DescriptionFlag implements ChangeFlag<ChatRoom, Long> {
    @Override
    public Long getReferenceId(ChatRoom chatRoom) {
      return chatRoom.getId();
    }
  }

  private class UnannotatedFlag implements ChangeFlag<ChatRoom, Long> {

    @Override
    public Long getReferenceId(ChatRoom chatRoom) {
      return null;
    }
  }

  protected FlagBase flagBase;
  protected ChatRoom chatRoom;
  protected List<Long> chatRoomIdList;

  protected final Long chatRoomId = 35l;
  protected final Date updateDate = january(2015, 20);
  protected final Date secondUpdateDate = january(2015, 20);
  protected final Date seenDate = january(2015, 27);
  protected final String fakeAttender = "fake_attender";
  protected ChangeFlag commentsFlag = new CommentsFlag();
  protected ChangeFlag descriptionFlag = new DescriptionFlag();

  protected abstract FlagBase newFlagBase();

  @Before
  public void setUp() throws Exception {
    chatRoom = new ChatRoom(chatRoomId);
    flagBase = newFlagBase();

    chatRoomIdList = Lists.newArrayList(chatRoomId);
  }

  @Test
  public void storeAChangeFlagAndFindItsUpdateDate() throws Exception {

    commentsFlag = flagBase.storeOrUpdate(commentsFlag, chatRoomId, updateDate);

    List<List<Date>> updateDates = flagBase.findUpdateDates(Lists.newArrayList(commentsFlag), chatRoomIdList);
    assertThat(updateDates, is(notNullValue(List.class)));
    assertThat(updateDates.size(), is(1));
    assertThat(updateDates.get(0).size(), is(1));
    assertThat(updateDates.get(0).get(0), is(updateDate));
  }

  @Test
  public void updateAStoredChangeFlag() throws Exception {

    commentsFlag = flagBase.storeOrUpdate(commentsFlag, chatRoomId, updateDate);
    flagBase.storeOrUpdate(commentsFlag, chatRoomId, secondUpdateDate);

    List<List<Date>> updateDates = flagBase.findUpdateDates(Lists.newArrayList(commentsFlag), chatRoomIdList);
    assertThat(updateDates, is(notNullValue(List.class)));
    assertThat(updateDates.size(), is(1));
    assertThat(updateDates.get(0).size(), is(1));
    assertThat(updateDates.get(0).get(0), is(secondUpdateDate));
  }

  @Test
  public void findUpdateDatesOfChangeFlagsWhenSomeOfThemAreNotStored() throws Exception {

    flagBase.storeOrUpdate(descriptionFlag, chatRoomId, updateDate);
    /*** commentsFlag is not stored ***/

    List<List<Date>> updateDates = flagBase.findUpdateDates(Lists.newArrayList(commentsFlag, descriptionFlag), chatRoomIdList);
    assertThat(updateDates, is(notNullValue(List.class)));
    assertThat(updateDates.size(), is(2));
    assertThat(updateDates.get(0).size(), is(1));
    assertThat(updateDates.get(0).get(0), is(zeroDate()));
    assertThat(updateDates.get(1).get(0), is(updateDate));
  }

  @Test
  public void seeChangeFlagAfterStoreOrUpdate() throws Exception {

    commentsFlag = flagBase.storeOrUpdate(commentsFlag, chatRoomId, updateDate);
    flagBase.setAttenderSeenDate(commentsFlag, chatRoomIdList, fakeAttender, seenDate);

    List<List<Date>> seenDates = flagBase.findSeenDatesByAttender(Lists.newArrayList(commentsFlag), fakeAttender, chatRoomIdList);

    assertThat(seenDates, is(notNullValue(List.class)));
    assertThat(seenDates.size(), is(1));
    assertThat(seenDates.get(0).size(), is(1));
    assertThat(seenDates.get(0).get(0), is(seenDate));
  }

  @Test
  public void seeChangeFlagThatIsNotStored() throws Exception {

    flagBase.setAttenderSeenDate(commentsFlag, chatRoomIdList, fakeAttender, seenDate);

    List<List<Date>> seenDates = flagBase.findSeenDatesByAttender(Lists.newArrayList(commentsFlag), fakeAttender, chatRoomIdList);

    assertThat(seenDates, is(notNullValue(List.class)));
    assertThat(seenDates.size(), is(1));
    assertThat(seenDates.get(0).size(), is(1));
    assertThat(seenDates.get(0).get(0), is(seenDate));
  }

  @Test
  public void findSeenDateWhenChangeFlagIsNotSeen() throws Exception {

    commentsFlag = flagBase.storeOrUpdate(commentsFlag, chatRoomId, updateDate);

    List<List<Date>> seenDates = flagBase.findSeenDatesByAttender(Lists.newArrayList(commentsFlag), fakeAttender, chatRoomIdList);

    assertThat(seenDates, is(notNullValue(List.class)));
    assertThat(seenDates.size(), is(1));
    assertThat(seenDates.get(0).size(), is(1));
    assertThat(seenDates.get(0).get(0), is(zeroDate()));
  }

  @Test(expected = RequiredAnnotationException.class)
  public void changeFlagClassIsNotAnnotated() throws Exception {
    flagBase.storeOrUpdate(new UnannotatedFlag(), chatRoomId, updateDate);
  }
}