package com.clouway.cufar.storage;

import com.clouway.cufar.flag.ChangeFlag;
import com.clouway.cufar.flag.FlagFor;
import com.clouway.cufar.flag.RequiredAnnotationException;
import com.google.common.collect.Lists;

import java.util.Date;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 */
class InMemoryFlagBase implements FlagBase {

  private class FlagEntity {
    private String id;
    private Date lastUpdateDate;

    private FlagEntity(String id, Date lastUpdateDate) {
      this.id = id;
      this.lastUpdateDate = lastUpdateDate;
    }

    public String getId() {
      return id;
    }

    public Date getLastUpdateDate() {
      return lastUpdateDate;
    }
  }

  private class AttenderEntity {
    private Date lastSeenDate;

    public AttenderEntity(Date lastSeenDate) {
      this.lastSeenDate = lastSeenDate;
    }

    public Date getLastSeenDate() {
      return lastSeenDate;
    }
  }

  private Map<String, Object> dataMap = new Hashtable<String, Object>();


  @Override
  public ChangeFlag storeOrUpdate(ChangeFlag changeFlag, Object referenceId, Date lastUpdateDate) {

    String key = generateChangeFlagId(changeFlag, referenceId);
    dataMap.put(key, new FlagEntity(key, lastUpdateDate));

    return changeFlag;
  }

  @Override
  public <T, I> void setAttenderSeenDate(ChangeFlag<T, I> changeFlag, List<I> referenceIds, String attender, Date seenDate) {
    for (Object referenceId : referenceIds) {

      String key = generateAttenderEntityKey(attender, changeFlag, referenceId);

      dataMap.put(key, new AttenderEntity(seenDate));
    }
  }

  @Override
  public List<List<Date>> findSeenDatesByAttender(List<? extends ChangeFlag> changeFlags, String attender, List<?> referenceIds) {
    List<List<Date>> seenDates = Lists.newArrayList();

    for (ChangeFlag changeFlag : changeFlags) {

      List<Date> dateList = Lists.newArrayList();
      for (Object referenceId : referenceIds) {
        String attenderEntityKey = generateAttenderEntityKey(attender, changeFlag, referenceId);

        if (dataMap.containsKey(attenderEntityKey)) {
          AttenderEntity attenderEntity = (AttenderEntity) dataMap.get(attenderEntityKey);
          dateList.add(attenderEntity.getLastSeenDate());

        } else {
          dateList.add(new Date(0l));
        }
      }

      seenDates.add(dateList);
    }

    return seenDates;
  }

  @Override
  public List<List<Date>> findUpdateDates(List<? extends ChangeFlag> changeFlagList, List<?> referenceIds) {
    List<List<Date>> updateDates = Lists.newArrayList();

    for (ChangeFlag changeFlag : changeFlagList) {

      List<Date> dateList = Lists.newArrayList();
      for (Object referenceId : referenceIds) {
        String flagEntityKey = generateChangeFlagId(changeFlag, referenceId);

        if (dataMap.containsKey(flagEntityKey)) {
          FlagEntity flagEntity = (FlagEntity) dataMap.get(flagEntityKey);
          dateList.add(flagEntity.getLastUpdateDate());

        } else {
          dateList.add(new Date(0l));
        }
      }

      updateDates.add(dateList);

    }
    return updateDates;
  }

  private String generateChangeFlagId(ChangeFlag changeFlag, Object referenceId) {

    FlagFor flagForAnnotation = changeFlag.getClass().getAnnotation(FlagFor.class);

    if (flagForAnnotation == null) {
      throw new RequiredAnnotationException(changeFlag.getClass(), FlagFor.class);
    }

    String refName = flagForAnnotation.target();
    String flagName = flagForAnnotation.flagName();

    return String.format("%s%s%s", referenceId, refName, flagName);
  }

  private String generateAttenderEntityKey(String attender, ChangeFlag changeFlag, Object referenceId) {

    return String.format("%s%s", attender, generateChangeFlagId(changeFlag, referenceId));
  }
}
