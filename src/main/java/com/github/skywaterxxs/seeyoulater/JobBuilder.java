package com.github.skywaterxxs.seeyoulater;

import com.github.skywaterxxs.seeyoulater.impl.JobImpl;

/**
 * <code>JobBuilder</code> is used to instantiate {@link Job}s.
 * 
 * @see Job
 */
public class JobBuilder {

    private String jobExecutorKey;

    private Object jobExecutorData;

    private Trigger trigger;

    protected JobBuilder() {
    }

    /**
     * Create a JobBuilder with which to define a <code>JobDetail</code>.
     *
     * @return a new JobBuilder
     */
    public static JobBuilder newJob() {
        return new JobBuilder();
    }

    /**
     * Produce the <code>JobDetail</code> instance defined by this <code>JobBuilder</code>.
     *
     * @return the defined JobDetail.
     */
    public Job build() {

        JobImpl job = new JobImpl();

        job.setJobExecutorKey(jobExecutorKey);
        job.setJobExecutorData(jobExecutorData);
        job.setTrigger(trigger);

        return job;
    }

    public JobBuilder withJobExecutorKey(String jobExecutorKey) {
        this.jobExecutorKey = jobExecutorKey;
        return this;
    }

    public JobBuilder withJobExecutorData(Object jobExecutorData) {
        this.jobExecutorData = jobExecutorData;
        return this;
    }

    public JobBuilder withTrigger(Trigger trigger) {
        this.trigger = trigger;
        return this;
    }
}
