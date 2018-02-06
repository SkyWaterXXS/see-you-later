package com.github.skywaterxxs.seeyoulater.impl.jobexecutor;

import com.github.skywaterxxs.seeyoulater.Job;
import com.github.skywaterxxs.seeyoulater.JobExecutor;

/**
 * @author xuxiaoshuo 2018/1/31
 */
public class PrintJobExecutor implements JobExecutor {

    public static final String EXECUTOR_KEY = "print";

    @Override
    public String getExecutorKey() {
        return EXECUTOR_KEY;
    }

    @Override
    public void execute(Job job) {
        Long addTime = (Long) job.getJobExecutorData();

        Long nowTime = System.currentTimeMillis();
        System.out.println(addTime + ":" + nowTime + ":" + (nowTime - addTime));
    }
}
