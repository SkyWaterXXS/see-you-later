package com.github.skywaterxxs.seeyoulater.impl.triggerexecutor;

import com.github.skywaterxxs.seeyoulater.Trigger;
import com.github.skywaterxxs.seeyoulater.TriggerExecutor;

import java.util.Date;

/**
 * @author xuxiaoshuo 2018/2/6
 */
public class DelayTriggerExecutor implements TriggerExecutor {
    public static final String EXECUTOR_KEY = "delay";


    @Override
    public String getExecutorKey() {
        return EXECUTOR_KEY;
    }

    @Override
    public Date execute(Trigger trigger) {

        Integer delaySecs = (Integer) trigger.getTriggerExecutorData();

        return new Date(System.currentTimeMillis() + delaySecs * 1000L);
    }
}
