# see you later
scheduler a job just like see you later


### Use Delay

```java

 Scheduler scheduler = new HashedTimingWheelScheduler();

 scheduler.start();

 Trigger trigger = TriggerBuilder.newJob()
                .withTriggerExecutorKey("delay")
                .withTriggerExecutorData(3)
                .build();
 Job job = JobBuilder.newJob()
                .withJobExecutorKey("print")
                .withJobExecutorData("see you later")
                .withTrigger(trigger)
                .build();

 scheduler.addJob(job);
 
```

### Use Cron

```java
 Trigger trigger2 = TriggerBuilder.newJob()
                .withTriggerExecutorKey("cron")
                .withTriggerExecutorData("* * * * * ? *")
                .build();
 Job job2 = JobBuilder.newJob()
                .withJobExecutorKey("print")
                .withJobExecutorData("see you later one second")
                .withTrigger(trigger2)
                .build();
  scheduler.addJob(job2);

```