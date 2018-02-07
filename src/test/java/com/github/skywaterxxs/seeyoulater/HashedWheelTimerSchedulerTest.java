package com.github.skywaterxxs.seeyoulater;

import com.github.skywaterxxs.seeyoulater.impl.HashedTimingWheelScheduler;
import org.junit.Test;

/**
 * @author xuxiaoshuo 2018/2/1
 */
public class HashedWheelTimerSchedulerTest {

    @Test
    public void test1() {

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

        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
