package com.github.skywaterxxs.seeyoulater;

import com.github.skywaterxxs.seeyoulater.impl.TriggerImpl;

/**
 * <code>TriggerBuilder</code> is used to instantiate {@link Trigger}s.
 *
 * @see Job
 */
public class TriggerBuilder {

    private String triggerExecutorKey;

    private Object triggerExecutorData;

    protected TriggerBuilder() {
    }

    /**
     * Create a TriggerBuilder with which to define a <code>Trigger</code>.
     *
     * @return a new JobBuilder
     */
    public static TriggerBuilder newJob() {
        return new TriggerBuilder();
    }

    /**
     * Produce the <code>JobDetail</code> instance defined by this <code>JobBuilder</code>.
     *
     * @return the defined JobDetail.
     */
    public Trigger build() {

        TriggerImpl job = new TriggerImpl();

        job.setTriggerExecutorKey(triggerExecutorKey);
        job.setTriggerExecutorData(triggerExecutorData);

        return job;
    }

    public TriggerBuilder withTriggerExecutorKey(String triggerExecutorKey) {
        this.triggerExecutorKey = triggerExecutorKey;
        return this;
    }

    public TriggerBuilder withTriggerExecutorData(Object triggerExecutorData) {
        this.triggerExecutorData = triggerExecutorData;
        return this;
    }

}
