package com.github.skywaterxxs.seeyoulater.impl;

import com.github.skywaterxxs.seeyoulater.Job;
import com.github.skywaterxxs.seeyoulater.Trigger;

import java.io.Serializable;

/**
 * @author xuxiaoshuo 2018/1/31
 */
public class JobImpl implements Job, Serializable {

    private String jobExecutorKey;

    private Object jobExecutorData;

    private Trigger trigger;

    /**
     * 获取Job执行器的key
     */
    @Override
    public String getJobExecutorKey() {
        return jobExecutorKey;
    }

    /**
     * 获取Job执行器所需的data
     */
    @Override
    public Object getJobExecutorData() {
        return jobExecutorData;
    }

    @Override
    public Trigger getTrigger() {
        return trigger;
    }

    public void setJobExecutorKey(String jobExecutorKey) {
        this.jobExecutorKey = jobExecutorKey;
    }

    public void setJobExecutorData(Object jobExecutorData) {
        this.jobExecutorData = jobExecutorData;
    }

    public void setTrigger(Trigger trigger) {
        this.trigger = trigger;
    }
}
