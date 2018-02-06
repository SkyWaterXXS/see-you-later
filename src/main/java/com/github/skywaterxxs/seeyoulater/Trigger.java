package com.github.skywaterxxs.seeyoulater;

/**
 * @author xuxiaoshuo 2018/1/31
 */
public interface Trigger {
    /** 获取{@link TriggerExecutor}执行器的key */
    String getTriggerExecutorKey();

    /** 获取{@link TriggerExecutor}执行器所需的data */
    Object getTriggerExecutorData();
}
