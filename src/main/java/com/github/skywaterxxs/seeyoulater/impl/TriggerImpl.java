package com.github.skywaterxxs.seeyoulater.impl;

import com.github.skywaterxxs.seeyoulater.Trigger;
import com.github.skywaterxxs.seeyoulater.TriggerExecutor;

import java.io.Serializable;

/**
 * @author xuxiaoshuo 2018/2/1
 */
public class TriggerImpl implements Trigger, Serializable {

    private String triggerExecutorKey;

    private Object triggerExecutorData;

    /**
     * 获取{@link TriggerExecutor}执行器的key
     */
    @Override
    public String getTriggerExecutorKey() {
        return triggerExecutorKey;
    }

    /**
     * 获取{@link TriggerExecutor}执行器所需的data
     */
    @Override
    public Object getTriggerExecutorData() {
        return triggerExecutorData;
    }

    public void setTriggerExecutorKey(String triggerExecutorKey) {
        this.triggerExecutorKey = triggerExecutorKey;
    }

    @Override
    public void setTriggerExecutorData(Object triggerExecutorData) {
        this.triggerExecutorData = triggerExecutorData;
    }
}
