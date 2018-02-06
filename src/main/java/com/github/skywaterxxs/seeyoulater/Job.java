package com.github.skywaterxxs.seeyoulater;

/**
 * @author xuxiaoshuo 2018/1/31
 */
public interface Job {

    /** 获取Job执行器的key */
    String getJobExecutorKey();

    /** 获取Job执行器所需的data */
    Object getJobExecutorData();

    Trigger getTrigger();
}
