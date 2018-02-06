package com.github.skywaterxxs.seeyoulater;

/**
 * @author xuxiaoshuo 2018/1/31
 */
public interface Scheduler {

    void start();

    void addJob(Job job);

    void stop();
}
