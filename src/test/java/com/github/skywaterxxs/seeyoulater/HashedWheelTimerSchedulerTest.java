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


        Trigger trigger = TriggerBuilder.newJob().withTriggerExecutorKey("delay")
                .withTriggerExecutorData(3).build();
        Job job = JobBuilder.newJob().withJobExecutorKey("print").withJobExecutorData(System.currentTimeMillis())
                .withTrigger(trigger).build();

        scheduler.addJob(job);


        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

    }
}
