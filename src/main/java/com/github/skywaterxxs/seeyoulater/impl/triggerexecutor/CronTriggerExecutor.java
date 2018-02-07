package com.github.skywaterxxs.seeyoulater.impl.triggerexecutor;

import com.github.skywaterxxs.seeyoulater.CronExpression;
import com.github.skywaterxxs.seeyoulater.Trigger;
import com.github.skywaterxxs.seeyoulater.TriggerExecutor;

import java.text.ParseException;
import java.util.Date;

/**
 * @author xuxiaoshuo 2018/2/7
 */
public class CronTriggerExecutor implements TriggerExecutor {
    public static final String EXECUTOR_KEY = "cron";

    @Override
    public String getExecutorKey() {
        return EXECUTOR_KEY;
    }

    @Override
    public Date execute(Trigger trigger) {

        CronExpression cronExpression = null;
        try {
            cronExpression = new CronExpression((String) trigger.getTriggerExecutorData());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        if (cronExpression == null) {
            return null;
        }

        return cronExpression.getNextValidTimeAfter(new Date());
    }
}
