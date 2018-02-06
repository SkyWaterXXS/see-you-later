package com.github.skywaterxxs.seeyoulater;

/**
 * @author xuxiaoshuo 2018/1/31
 */
public interface JobExecutor {

    String getExecutorKey();

    void execute(Job job);
}
