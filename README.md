# CuFAR
### Clouway User Flagged Attendance Reviewer

Seen/unseen flags with many users.

--------------------

### Usage

Here is an example of how the library is used.

Suppose we have a task that is scheduled for completion and we are making changes to its schedule. A general workflow will look like:

```java
/*
...
FlagService flagService = <new FlagService instance>
Task task = <new Task instance>
...
*/

// Create a flag instance
ChangeFlag taskScheduleFlag = new TaskScheduleFlag();

// Making changes to the schedule
flagService.addFlagChange(taskScheduleFlag, dateOfUpdate, task.getId());

// An attender(user or someone else) looks at the schedule
flagService.seeFlag(taskScheduleFlag, taskList, attender, dateBeforeUpdate);

// Apply the flag values to one or more tasks
flagService.applyFlags(attender, taskList, new TaskScheduleFlagApplier());
```

### Configuration

Your change flags should implement the *ChangeFlag* interface and be annotated with the *@FlagFor* annotation, like so:
```java
@FlagFor(target = "Task", flagName = "TaskSchedule")
class TaskScheduleFlag implements ChangeFlag<Task, Long> {
  @Override
  public Long getReferenceId(Task task) {
    return task.getId();
  }
}
```

Also you need a *FlagApplier* for each of your flags:
```java
@ApplierForFlag(TaskScheduleFlag.class)
class TaskScheduleFlagApplier implements FlagApplier<Task, Long> {

  @Override
  public void apply(Task task, boolean flagSeen) {
    /* implementation for applying the flagSeen value to the task instance */
  }

  @Override
    public Long getReferenceId(Task task) {
      return task.getId();
    }
}
```
