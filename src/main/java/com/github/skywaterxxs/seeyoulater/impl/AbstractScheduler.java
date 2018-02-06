package com.github.skywaterxxs.seeyoulater.impl;

import com.github.skywaterxxs.seeyoulater.*;
import com.github.skywaterxxs.seeyoulater.impl.jobexecutor.PrintJobExecutor;
import com.github.skywaterxxs.seeyoulater.impl.triggerexecutor.DelayTriggerExecutor;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author xuxiaoshuo 2018/2/1
 */
public abstract class AbstractScheduler implements Scheduler {

    private Map<String, JobExecutor> jobExecutorMap = new HashMap<>();

    private Map<String, TriggerExecutor> triggerExecutorMap = new HashMap<>();


    protected ThreadFactory threadFactory = Executors.defaultThreadFactory();

    protected ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(2, 10, 77, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(), threadFactory);

    public AbstractScheduler() {

        PrintJobExecutor printJobExecutor = new PrintJobExecutor();
        jobExecutorMap.put(printJobExecutor.getExecutorKey(), printJobExecutor);


        DelayTriggerExecutor delayTriggerExecutor = new DelayTriggerExecutor();
        triggerExecutorMap.put(delayTriggerExecutor.getExecutorKey(), delayTriggerExecutor);
    }

    @Override
    public void start() {

    }

    @Override
    public void addJob(Job job) {

        Trigger trigger = job.getTrigger();

        TriggerExecutor triggerExecutor = triggerExecutorMap.get(trigger.getTriggerExecutorKey());

        Date executeData = triggerExecutor.execute(trigger);

        doAdd(job,executeData);
    }

    protected abstract void doAdd(Job job,Date executeData);


    protected void executeJob(Job job) {
        JobExecutor jobExecutor = jobExecutorMap.get(job.getJobExecutorKey());

        threadPoolExecutor.execute(() -> jobExecutor.execute(job));
    }

    @Override
    public void stop() {

    }

    public void addJobExecutor(JobExecutor jobExecutor) {
        jobExecutorMap.put(jobExecutor.getExecutorKey(), jobExecutor);
    }

    public void addTriggerExecutor(TriggerExecutor triggerExecutor) {
        triggerExecutorMap.put(triggerExecutor.getExecutorKey(), triggerExecutor);
    }
}
