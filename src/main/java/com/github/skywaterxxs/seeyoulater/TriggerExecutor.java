package com.github.skywaterxxs.seeyoulater;

import java.util.Date;

/**
 * @author xuxiaoshuo 2018/2/1
 */
public interface TriggerExecutor {

    String getExecutorKey();

    Date execute(Trigger trigger);
}
