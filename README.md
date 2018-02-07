# see you later
scheduler a job just like see you later

A Powerful Scheduler in Java. TRY IT NOW!

* Use Hashed Timing Wheel Algorithm
* Support CronTrigger and DelayTrigger
* Easy to Implement Trigger or Job

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

### JobExecutor Demo

```java

public class PrintJobExecutor implements JobExecutor {

    public static final String EXECUTOR_KEY = "print";

    @Override
    public String getExecutorKey() {
        return EXECUTOR_KEY;
    }

    @Override
    public void execute(Job job) {
        System.out.println(job.getJobExecutorData());
    }
}
    
```

##### if you have any problem,please contact me
 email:1160199984@qq.com