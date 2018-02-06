# see you later
scheduler a job just like see you later

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