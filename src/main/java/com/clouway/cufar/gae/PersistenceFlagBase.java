package com.clouway.cufar.gae;

import com.clouway.cufar.flag.ChangeFlag;
import com.clouway.cufar.flag.FlagFor;
import com.clouway.cufar.flag.RequiredAnnotationException;
import com.clouway.cufar.storage.FlagBase;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 */
public class PersistenceFlagBase implements FlagBase {

  private final String ATTENDER_SET_KIND = "ChangeAttenderSet";
  private final String CHANGE_FLAG_SET_KIND = "ChangeFlagSet";

  private final String FLAG_NAMES_PROPERTY = "flags.names";
  private final String UPDATE_DATES_PROPERTY = "flags.updateDates";
  private final String SEEN_DATES_PROPERTY = "attender.seenDates";

  private DatastoreService datastoreService;

  public PersistenceFlagBase(DatastoreService datastoreService) {
    this.datastoreService = datastoreService;
  }

  @Override
  public <T, I> ChangeFlag storeOrUpdate(ChangeFlag<T, I> changeFlag, I referenceId, Date lastUpdateDate) {

    Key changeFlagSetKey = KeyFactory.createKey(CHANGE_FLAG_SET_KIND, generateChangeFlagSetId(changeFlag, referenceId));
    Entity changeSetEntity;
    List<String> flagNames = null;
    List<Date> updateDates = null;

    try {
      changeSetEntity = datastoreService.get(changeFlagSetKey);
      flagNames = (List<String>) changeSetEntity.getProperty(FLAG_NAMES_PROPERTY);
      updateDates = (List<Date>) changeSetEntity.getProperty(UPDATE_DATES_PROPERTY);

    } catch (EntityNotFoundException e) {
      changeSetEntity = new Entity(changeFlagSetKey);
    }

    if (flagNames == null || updateDates == null) {
      flagNames = Lists.newArrayList();
      updateDates = Lists.newArrayList();
    }

    String changeFlagId = generateChangeFlagId(changeFlag);

    if (flagNames.contains(changeFlagId)) {
      updateDates.set(flagNames.indexOf(changeFlagId), lastUpdateDate);

    } else {
      flagNames.add(changeFlagId);
      updateDates.add(lastUpdateDate);
    }

    changeSetEntity.setUnindexedProperty(FLAG_NAMES_PROPERTY, flagNames);
    changeSetEntity.setUnindexedProperty(UPDATE_DATES_PROPERTY, updateDates);

    datastoreService.put(changeSetEntity);

    return changeFlag;
  }

  @Override
  public <T, I> void setAttenderSeenDate(ChangeFlag<T, I> changeFlag, List<I> referenceIds, String attender, Date seenDate) {

    for (Object referenceId : referenceIds) {
      Key changeAttendersSetKey = KeyFactory.createKey(ATTENDER_SET_KIND, generateAttenderSetId(attender, changeFlag, referenceId));

      Entity attenderSetEntity;
      List<String> flagNames = null;
      List<Date> seenDates = null;

      try {
        attenderSetEntity = datastoreService.get(changeAttendersSetKey);
        flagNames = (List<String>) attenderSetEntity.getProperty(FLAG_NAMES_PROPERTY);
        seenDates = (List<Date>) attenderSetEntity.getProperty(SEEN_DATES_PROPERTY);

      } catch (EntityNotFoundException e) {
        attenderSetEntity = new Entity(changeAttendersSetKey);
      }

      if (flagNames == null || seenDates == null) {
        flagNames = Lists.newArrayList();
        seenDates = Lists.newArrayList();
      }

      String changeFlagId = generateChangeFlagId(changeFlag);

      if (flagNames.contains(changeFlagId)) {
        seenDates.set(flagNames.indexOf(changeFlagId), seenDate);

      } else {
        flagNames.add(changeFlagId);
        seenDates.add(seenDate);
      }

      attenderSetEntity.setUnindexedProperty(FLAG_NAMES_PROPERTY, flagNames);
      attenderSetEntity.setUnindexedProperty(SEEN_DATES_PROPERTY, seenDates);

      datastoreService.put(attenderSetEntity);
    }

  }

  @Override
  public List<List<Date>> findSeenDatesByAttender(List<? extends ChangeFlag> changeFlags, String attender, List<?> referenceIds) {
    List<List<Date>> seenDates = Lists.newArrayList();

    Map<String, DatesSet> changeAttendersSetMap = createChangeAttendersSetMap(attender, changeFlags, referenceIds);
    fillDatesSetMap(changeAttendersSetMap, ATTENDER_SET_KIND, SEEN_DATES_PROPERTY);

    // Fill seenDates list
    for (ChangeFlag changeFlag : changeFlags) {

      List<Date> dateList = Lists.newArrayList();
      for (Object referenceId : referenceIds) {
        DatesSet changeAttendersSet = changeAttendersSetMap.get(generateAttenderSetId(attender, changeFlag, referenceId));
        Map<String, Date> changeAttenders = changeAttendersSet.getDates();

        Date seenDate = changeAttenders.get(generateChangeFlagId(changeFlag));

        if (seenDate != null) {
          dateList.add(seenDate);

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

    Map<String, DatesSet> changeFlagsSetMap = createChangeFlagsSetMap(changeFlagList, referenceIds);

    fillDatesSetMap(changeFlagsSetMap, CHANGE_FLAG_SET_KIND, UPDATE_DATES_PROPERTY);

    // Fill updateDates list
    for (ChangeFlag changeFlag : changeFlagList) {

      List<Date> dateList = Lists.newArrayList();
      for (Object referenceId : referenceIds) {
        DatesSet changeFlagsSet = changeFlagsSetMap.get(generateChangeFlagSetId(changeFlag, referenceId));
        Map<String, Date> flagsUpdateDates = changeFlagsSet.getDates();

        Date updateDate = flagsUpdateDates.get(generateChangeFlagId(changeFlag));

        if (updateDate != null) {
          dateList.add(updateDate);

        } else {
          dateList.add(new Date(0l));
        }
      }

      updateDates.add(dateList);
    }

    return updateDates;
  }

  private void fillDatesSetMap(Map<String, DatesSet> datesSetMap, String datesSetKind, String datePropertyName) {
    Map<String, Entity> datesSetEntities = fetchEntities(Lists.newArrayList(datesSetMap.values()), datesSetKind);

    for (DatesSet datesSet : datesSetMap.values()) {
      Entity datesSetEntity = datesSetEntities.get(datesSet.getId());

      if (datesSetEntity == null) {
        continue;
      }

      List<String> flagNames = (List<String>) datesSetEntity.getProperty(FLAG_NAMES_PROPERTY);
      List<Date> dates = (List<Date>) datesSetEntity.getProperty(datePropertyName);

      int i = 0;
      for (String flagName : flagNames) {
        datesSet.add(flagName, dates.get(i));
        i++;
      }
    }
  }

  private String generateChangeFlagId(ChangeFlag changeFlag) {
    String flagClassName = getFlagForAnnotation(changeFlag).flagName();

    return flagClassName;
  }

  private String generateChangeFlagSetId(ChangeFlag changeFlag, Object referenceId) {
    String referenceName = getFlagForAnnotation(changeFlag).target();

    return String.format("%s%s", referenceId, referenceName);
  }

  private String generateAttenderSetId(String attender, ChangeFlag changeFlag, Object referenceId) {
    String referenceName = getFlagForAnnotation(changeFlag).target();

    return String.format("%s%s%s", referenceId, attender, referenceName);
  }

  private FlagFor getFlagForAnnotation(ChangeFlag changeFlag) {
    FlagFor flagForAnnotation = changeFlag.getClass().getAnnotation(FlagFor.class);

    if (flagForAnnotation == null) {
      throw new RequiredAnnotationException(changeFlag.getClass(), FlagFor.class);
    }

    return flagForAnnotation;
  }

  private Map<String, Entity> fetchEntities(List<DatesSet> datesSets, String datesSetKind) {
    Map<String, Entity> entitiesMap = Maps.newHashMap();

    for (DatesSet datesSet : datesSets) {
      Key datesSetKey = KeyFactory.createKey(datesSetKind, datesSet.getId());

      try {
        Entity datesSetEntity = datastoreService.get(datesSetKey);
        entitiesMap.put(datesSet.getId(), datesSetEntity);

      } catch (EntityNotFoundException e) {

      }
    }

    return entitiesMap;
  }

  private Map<String, DatesSet> createChangeFlagsSetMap(List<? extends ChangeFlag> changeFlagList, List<?> referenceIds) {
    Map<String, DatesSet> changeFlagsSetMap = Maps.newHashMap();

    for (ChangeFlag changeFlag : changeFlagList) {
      for (Object referenceId : referenceIds) {
        String changeFlagSetId = generateChangeFlagSetId(changeFlag, referenceId);

        if (!changeFlagsSetMap.containsKey(changeFlagSetId)) {
          DatesSet changeFlagsSet = new DatesSet(changeFlagSetId);
          changeFlagsSetMap.put(changeFlagSetId, changeFlagsSet);
        }
      }
    }

    return changeFlagsSetMap;
  }

  private Map<String, DatesSet> createChangeAttendersSetMap(String attender, List<? extends ChangeFlag> changeFlagList, List<?> referenceIds) {
    Map<String, DatesSet> changeAttendersSetMap = Maps.newHashMap();

    for (ChangeFlag changeFlag : changeFlagList) {
      for (Object referenceId : referenceIds) {
        String changeAttendersSetId = generateAttenderSetId(attender, changeFlag, referenceId);

        if (!changeAttendersSetMap.containsKey(changeAttendersSetId)) {
          DatesSet changeAttendersSet = new DatesSet(changeAttendersSetId);
          changeAttendersSetMap.put(changeAttendersSetId, changeAttendersSet);
        }
      }
    }
    return changeAttendersSetMap;
  }
}
