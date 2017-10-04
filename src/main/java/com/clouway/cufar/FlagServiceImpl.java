package com.clouway.cufar;

import com.clouway.cufar.flag.ApplierForFlag;
import com.clouway.cufar.flag.ChangeFlag;
import com.clouway.cufar.flag.FlagApplier;
import com.clouway.cufar.flag.RequiredAnnotationException;
import com.clouway.cufar.storage.FlagBase;
import com.google.common.collect.Lists;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.List;

/**
 * @author Stefan Dimitrov (stefan.dimitrov@clouway.com).
 */
class FlagServiceImpl implements FlagService {

  private FlagBase flagBase;

  public FlagServiceImpl(FlagBase flagBase) {

    this.flagBase = flagBase;
  }

  @Override
  public <T, I> ChangeFlag addFlagChange(ChangeFlag<T, I> changeFlag, Date changedOn, I referenceId) {

    return flagBase.storeOrUpdate(changeFlag, referenceId, changedOn);
  }

  @Override
  public <T, I> void seeFlag(ChangeFlag<T, I> changeFlag, List<T> references, String attender, Date seenOn) {
    List<I> referenceIds = getReferenceIds(changeFlag, references);

    seeFlagByIds(changeFlag, referenceIds, attender, seenOn);
  }

  @Override
  public <T, I> void seeFlagByIds(ChangeFlag<T, I> changeFlag, List<I> referenceIds, String attender, Date seenOn) {
    flagBase.setAttenderSeenDate(changeFlag, referenceIds, attender, seenOn);
  }

  @Override
  public <T, I> void applyFlags(String attender, List<T> objects, FlagApplier<T, I>... flagAppliers) {
    List<ChangeFlag> changeFlags = flagsFromAppliers(flagAppliers);
    List<I> referenceIds = referenceIdsFromApplier(objects, flagAppliers[0]);

    List<List<Date>> attenderSeenDates = flagBase.findSeenDatesByAttender(changeFlags, attender, referenceIds);
    List<List<Date>> lastUpdateDates = flagBase.findUpdateDates(changeFlags, referenceIds);

    int i = 0;
    for (FlagApplier<T, I> flagApplier : flagAppliers) {
      List<Date> flagUpdateDates = lastUpdateDates.get(i);
      List<Date> flagSeenDates = attenderSeenDates.get(i);

      applyOneFlag(objects, flagApplier, flagSeenDates, flagUpdateDates);

      i ++;
    }

  }

  private <T, I> void applyOneFlag(List<T> objects, FlagApplier<T, I> flagApplier, List<Date> flagSeenDates, List<Date> flagUpdateDates) {
    int j = 0;
    for (T object : objects) {
      boolean flagSeen = wasFlagSeen(flagSeenDates.get(j), flagUpdateDates.get(j));
      flagApplier.apply(object, flagSeen);
      j ++;
    }
  }

  private <T, I> List<I> getReferenceIds(ChangeFlag<T, I> changeFlag, List<T> references) {
    List<I> referenceIds = Lists.newArrayList();

    for (Object reference : references) {
      referenceIds.add(changeFlag.getReferenceId((T) reference));
    }

    return referenceIds;
  }

  private <T, I> List<ChangeFlag> flagsFromAppliers(FlagApplier<T, I>... flagAppliers) {
    List<String> flagNames = Lists.newArrayList();
    List<ChangeFlag> flagInstances = Lists.newArrayList();

    for (FlagApplier flagApplier: flagAppliers) {
      ChangeFlag flagInstance;

      try {
        ApplierForFlag applierForFlag = flagApplier.getClass().getAnnotation(ApplierForFlag.class);
        if (applierForFlag == null) {
          throw new RequiredAnnotationException(flagApplier.getClass(), ApplierForFlag.class);
        }

        Class<?> flagClass = applierForFlag.value();
        flagInstance = (ChangeFlag) flagClass.newInstance();

      } catch (InstantiationException e) {
        e.printStackTrace();
        return flagInstances;

      } catch (IllegalAccessException e) {
        e.printStackTrace();
        return flagInstances;
      }

      String flagClassName = flagInstance.getClass().getSimpleName();

      if (!flagNames.contains(flagClassName)) {
        flagNames.add(flagClassName);
        flagInstances.add(flagInstance);
      }
    }

    return flagInstances;
  }

  private <T, I> List<I> referenceIdsFromApplier(List<T> references, FlagApplier<T, I> flagApplier) {
    List<I> referenceIds = Lists.newArrayList();

    for (T reference : references) {
      referenceIds.add(flagApplier.getReferenceId(reference));
    }

    return referenceIds;
  }

  private boolean wasFlagSeen(Date attendanceDate, Date updateDate) {

    return (!attendanceDate.before(updateDate));
  }
}
