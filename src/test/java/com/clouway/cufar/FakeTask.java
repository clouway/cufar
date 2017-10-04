package com.clouway.cufar;

/**
* @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
*/
public class FakeTask {
  private Long id = 12345l;

  private boolean commentsChangeSeen;
  private boolean scheduleChangeSeen;

  public Long getId() {
    return id;
  }

  public void setCommentsChangeSeen(boolean commentsChangeSeen) {
    this.commentsChangeSeen = commentsChangeSeen;
  }

  public void setScheduleChangeSeen(boolean scheduleChangeSeen) {
    this.scheduleChangeSeen = scheduleChangeSeen;
  }

  public boolean isCommentsChangeSeen() {
    return commentsChangeSeen;
  }

  public boolean isScheduleChangeSeen() {
    return scheduleChangeSeen;
  }
}
