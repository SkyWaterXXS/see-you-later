package com.github.skywaterxxs.seeyoulater.impl;

import com.github.skywaterxxs.seeyoulater.Job;

import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

/**
 * @author xuxiaoshuo 2017/11/21
 */
public class HashedTimingWheelScheduler extends AbstractScheduler {

    private static final AtomicIntegerFieldUpdater<HashedTimingWheelScheduler> WORKER_STATE_UPDATER;

    static {
        WORKER_STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(HashedTimingWheelScheduler.class, "workerState");
    }

    public static final int WORKER_STATE_INIT = 0;
    public static final int WORKER_STATE_STARTED = 1;
    public static final int WORKER_STATE_SHUTDOWN = 2;

    // 0 - init, 1 - started, 2 - shut down
    @SuppressWarnings({"unused"})
    private volatile int workerState = WORKER_STATE_INIT;

    private final Worker worker = new Worker();
    private final Thread workerThread;

    private final long tickDuration;
    private final HashedWheelBucket[] wheel;
    private final int mask;
    private final CountDownLatch startTimeInitialized = new CountDownLatch(1);
    private final LinkedBlockingQueue<HashedWheelTimeout> timeouts = new LinkedBlockingQueue<>();
    private final LinkedBlockingQueue<Runnable> cancelledTimeouts = new LinkedBlockingQueue<>();

    private volatile long startTime;

    private final HashedTimingWheelScheduler hashedTimingWheelScheduler;

    /**
     * Creates a new timer with the default thread factory ({@link Executors#defaultThreadFactory()}), default tick
     * duration, and default number of ticks per wheel.
     */
    public HashedTimingWheelScheduler() {
        this(Executors.defaultThreadFactory());
    }

    /**
     * Creates a new timer with the default thread factory ({@link Executors#defaultThreadFactory()}) and default number
     * of ticks per wheel.
     *
     * @param tickDuration the duration between tick
     * @param unit         the time unit of the {@code tickDuration}
     * @throws NullPointerException     if {@code unit} is {@code null}
     * @throws IllegalArgumentException if {@code tickDuration} is <= 0
     */
    public HashedTimingWheelScheduler(long tickDuration, TimeUnit unit) {
        this(Executors.defaultThreadFactory(), tickDuration, unit);
    }

    /**
     * Creates a new timer with the default thread factory ({@link Executors#defaultThreadFactory()}).
     *
     * @param tickDuration  the duration between tick
     * @param unit          the time unit of the {@code tickDuration}
     * @param ticksPerWheel the size of the wheel
     * @throws NullPointerException     if {@code unit} is {@code null}
     * @throws IllegalArgumentException if either of {@code tickDuration} and {@code ticksPerWheel} is <= 0
     */
    public HashedTimingWheelScheduler(long tickDuration, TimeUnit unit, int ticksPerWheel) {
        this(Executors.defaultThreadFactory(), tickDuration, unit, ticksPerWheel);
    }

    /**
     * Creates a new timer with the default tick duration and default number of ticks per wheel.
     *
     * @param threadFactory a {@link ThreadFactory} that creates a background {@link Thread} which is dedicated to
     *                      {@link Runnable} execution.
     * @throws NullPointerException if {@code threadFactory} is {@code null}
     */
    public HashedTimingWheelScheduler(ThreadFactory threadFactory) {
        this(threadFactory, 10, TimeUnit.MILLISECONDS);
    }

    /**
     * Creates a new timer with the default number of ticks per wheel.
     *
     * @param threadFactory a {@link ThreadFactory} that creates a background {@link Thread} which is dedicated to
     *                      {@link Runnable} execution.
     * @param tickDuration  the duration between tick
     * @param unit          the time unit of the {@code tickDuration}
     * @throws NullPointerException     if either of {@code threadFactory} and {@code unit} is {@code null}
     * @throws IllegalArgumentException if {@code tickDuration} is <= 0
     */
    public HashedTimingWheelScheduler(ThreadFactory threadFactory, long tickDuration, TimeUnit unit) {
        this(threadFactory, tickDuration, unit, 512);
    }

    /**
     * Creates a new timer.
     *
     * @param threadFactory a {@link ThreadFactory} that creates a background {@link Thread} which is dedicated to
     *                      {@link HashedWheelTimeout} execution.
     * @param tickDuration  the duration between tick
     * @param unit          the time unit of the {@code tickDuration}
     * @param ticksPerWheel the size of the wheel
     * @throws NullPointerException     if either of {@code threadFactory} and {@code unit} is {@code null}
     * @throws IllegalArgumentException if either of {@code tickDuration} and {@code ticksPerWheel} is <= 0
     */
    public HashedTimingWheelScheduler(ThreadFactory threadFactory, long tickDuration, TimeUnit unit, int ticksPerWheel) {

        if (threadFactory == null) {
            throw new NullPointerException("threadFactory");
        }
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        if (tickDuration <= 0) {
            throw new IllegalArgumentException("tickDuration must be greater than 0: " + tickDuration);
        }
        if (ticksPerWheel <= 0) {
            throw new IllegalArgumentException("ticksPerWheel must be greater than 0: " + ticksPerWheel);
        }
        hashedTimingWheelScheduler = this;

        // Normalize ticksPerWheel to power of two and initialize the wheel.
        wheel = createWheel(ticksPerWheel);
        mask = wheel.length - 1;

        // Convert tickDuration to nanos.
        this.tickDuration = unit.toNanos(tickDuration);

        // Prevent overflow.
        if (this.tickDuration >= Long.MAX_VALUE / wheel.length) {
            throw new IllegalArgumentException(
                    String.format("tickDuration: %d (expected: 0 < tickDuration in nanos < %d", tickDuration,
                            Long.MAX_VALUE / wheel.length));
        }
        workerThread = threadFactory.newThread(worker);

        // leak = leakDetector.open(this);
    }

    private static HashedWheelBucket[] createWheel(int ticksPerWheel) {
        if (ticksPerWheel <= 0) {
            throw new IllegalArgumentException("ticksPerWheel must be greater than 0: " + ticksPerWheel);
        }
        if (ticksPerWheel > 1073741824) {
            throw new IllegalArgumentException("ticksPerWheel may not be greater than 2^30: " + ticksPerWheel);
        }

        ticksPerWheel = normalizeTicksPerWheel(ticksPerWheel);
        HashedWheelBucket[] wheel = new HashedWheelBucket[ticksPerWheel];
        for (int i = 0; i < wheel.length; i++) {
            wheel[i] = new HashedWheelBucket();
        }
        return wheel;
    }

    private static int normalizeTicksPerWheel(int ticksPerWheel) {
        int normalizedTicksPerWheel = 1;
        while (normalizedTicksPerWheel < ticksPerWheel) {
            normalizedTicksPerWheel <<= 1;
        }
        return normalizedTicksPerWheel;
    }

    /**
     * Starts the background thread explicitly. The background thread will start automatically on demand even if you did
     * not call this method.
     *
     * @throws IllegalStateException if this timer has been {@linkplain #stop() stopped} already
     */
    public void start() {


        switch (WORKER_STATE_UPDATER.get(this)) {

            case WORKER_STATE_INIT:
                if (WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_INIT, WORKER_STATE_STARTED)) {
                    workerThread.start();
                }
                break;
            case WORKER_STATE_STARTED:
                break;
            case WORKER_STATE_SHUTDOWN:
                throw new IllegalStateException("cannot be started once stopped");
            default:
                throw new Error("Invalid WorkerState");
        }

        // Wait until the startTime is initialized by the worker.
        while (startTime == 0) {
            try {
                startTimeInitialized.await();
            } catch (InterruptedException ignore) {
                // Ignore - it will be ready very soon.
            }
        }
    }

    public void stop() {
        if (Thread.currentThread() == workerThread) {
            throw new IllegalStateException(HashedTimingWheelScheduler.class.getSimpleName() + ".stop() cannot be called from "
                    + Runnable.class.getSimpleName());
        }

        if (!WORKER_STATE_UPDATER.compareAndSet(this, WORKER_STATE_STARTED, WORKER_STATE_SHUTDOWN)) {
            // workerState can be 0 or 2 at this moment - let it always be 2.
            WORKER_STATE_UPDATER.set(this, WORKER_STATE_SHUTDOWN);

            // if (leak != null) {
            // leak.close();
            // }

//            return Collections.emptySet();
        }

        boolean interrupted = false;
        while (workerThread.isAlive()) {
            workerThread.interrupt();
            try {
                workerThread.join(100);
            } catch (InterruptedException ignored) {
                interrupted = true;
            }
        }

        if (interrupted) {
            Thread.currentThread().interrupt();
        }

        // if (leak != null) {
        // leak.close();
        // }
//        return worker.unprocessedTimeouts();
    }


    @Override
    public void doAdd(Job job, Date executeData) {
        Date now = new Date();

        long delay = executeData.getTime() - now.getTime();

        hashedTimingWheelScheduler.newTimeout(job, delay, TimeUnit.MILLISECONDS);
    }


    public HashedWheelTimeout newTimeout(Job task, long delay, TimeUnit unit) {
        if (task == null) {
            throw new NullPointerException("task");
        }
        if (unit == null) {
            throw new NullPointerException("unit");
        }
        start();

        // Add the timeout to the timeout queue which will be processed on the next tick.
        // During processing all the queued HashedWheelTimeouts will be added to the correct HashedWheelBucket.
        long deadline = System.nanoTime() + unit.toNanos(delay) - startTime;
        HashedWheelTimeout timeout = new HashedWheelTimeout(this, task, deadline);
        try {
            timeouts.put(timeout);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return timeout;
    }

    private final class Worker implements Runnable {
        private final Set<HashedWheelTimeout> unprocessedTimeouts = new HashSet<>();

        private long tick;

        @Override
        public void run() {
            // Initialize the startTime.
            startTime = System.nanoTime();
            if (startTime == 0) {
                // We use 0 as an indicator for the uninitialized value here, so make sure it's not 0 when initialized.
                startTime = 1;
            }

            // Notify the other threads waiting for the initialization at start().
            startTimeInitialized.countDown();

            do {
                final long deadline = waitForNextTick();
                if (deadline > 0) {
                    int idx = (int) (tick & mask);
                    processCancelledTasks();
                    HashedWheelBucket bucket = wheel[idx];
                    transferTimeoutsToBuckets();
                    bucket.expireTimeouts(hashedTimingWheelScheduler, deadline);
                    tick++;
                }
            } while (WORKER_STATE_UPDATER.get(HashedTimingWheelScheduler.this) == WORKER_STATE_STARTED);

            // Fill the unprocessedTimeouts so we can return them from stop() method.
            for (HashedWheelBucket bucket : wheel) {
                bucket.clearTimeouts(unprocessedTimeouts);
            }
            for (; ; ) {
                HashedWheelTimeout timeout = timeouts.poll();
                if (timeout == null) {
                    break;
                }
                if (!timeout.isCancelled()) {
                    unprocessedTimeouts.add(timeout);
                }
            }
            processCancelledTasks();
        }

        private void transferTimeoutsToBuckets() {
            // transfer only max. 100000 timeouts per tick to prevent a thread to stale the workerThread when it just
            // adds new timeouts in a loop.
            for (int i = 0; i < 100000; i++) {

                HashedWheelTimeout timeout = timeouts.poll();
                if (timeout == null) {
                    // all processed
                    break;
                }
                if (timeout.state() == HashedWheelTimeout.ST_CANCELLED) {
                    // Was cancelled in the meantime.
                    continue;
                }

                long calculated = timeout.deadline / tickDuration;
                timeout.remainingRounds = (calculated - tick) / wheel.length;

                // Ensure we don't schedule for past.
                final long ticks = Math.max(calculated, tick);
                int stopIndex = (int) (ticks & mask);

                HashedWheelBucket bucket = wheel[stopIndex];
                bucket.addTimeout(timeout);
            }
        }

        private void processCancelledTasks() {
            for (; ; ) {
                Runnable task = cancelledTimeouts.poll();
                if (task == null) {
                    // all processed
                    break;
                }
                try {
                    new Thread(task).start();
                } catch (Throwable t) {
                    // if (logger.isWarnEnabled()) {
                    // logger.warn("An exception was thrown while process a cancellation task", t);
                    // }
                }
            }
        }

        /**
         * calculate goal nanoTime from startTime and current tick number, then wait until that goal has been reached.
         *
         * @return Long.MIN_VALUE if received a shutdown request, current time otherwise (with Long.MIN_VALUE changed by
         * +1)
         */
        private long waitForNextTick() {
            long deadline = tickDuration * (tick + 1);

            for (; ; ) {
                final long currentTime = System.nanoTime() - startTime;
                long sleepTimeMs = (deadline - currentTime + 999999) / 1000000;

                if (sleepTimeMs <= 0) {
                    if (currentTime == Long.MIN_VALUE) {
                        return -Long.MAX_VALUE;
                    } else {
                        return currentTime;
                    }
                }

                // Check if we run on windows, as if thats the case we will need
                // to round the sleepTime as workaround for a bug that only affect
                // the JVM if it runs on windows.
                //
                // See https://github.com/netty/netty/issues/356
                // if (PlatformDependent.isWindows()) {
                // sleepTimeMs = sleepTimeMs / 10 * 10;
                // }

                try {
                    Thread.sleep(sleepTimeMs);
                } catch (InterruptedException ignored) {
                    if (WORKER_STATE_UPDATER.get(HashedTimingWheelScheduler.this) == WORKER_STATE_SHUTDOWN) {
                        return Long.MIN_VALUE;
                    }
                }
            }
        }

        public Set<HashedWheelTimeout> unprocessedTimeouts() {
            return Collections.unmodifiableSet(unprocessedTimeouts);
        }
    }

    public void expire(HashedWheelTimeout hashedWheelTimeout) {
        if (!hashedWheelTimeout.expire()) {
            return;
        }

        executeJob(hashedWheelTimeout.job);
    }

    private static final class HashedWheelTimeout {

        private static final int ST_INIT = 0;
        private static final int ST_CANCELLED = 1;
        private static final int ST_EXPIRED = 2;
        private static final AtomicIntegerFieldUpdater<HashedWheelTimeout> STATE_UPDATER;

        static {
            STATE_UPDATER = AtomicIntegerFieldUpdater.newUpdater(HashedWheelTimeout.class, "state");
        }

        private final HashedTimingWheelScheduler timer;
        private final Job job;
        private final long deadline;

        @SuppressWarnings({"unused"})
        private volatile int state = ST_INIT;

        /**
         * remainingRounds will be calculated and set by Worker.transferTimeoutsToBuckets() before the
         * HashedWheelTimeout will be added to the correct HashedWheelBucket.
         */
        long remainingRounds;

        /**
         * This will be used to chain timeouts in HashedWheelTimerBucket via a double-linked-list. As only the
         * workerThread will act on it there is no need for synchronization / volatile.
         */
        HashedWheelTimeout next;
        HashedWheelTimeout prev;

        /**
         * The bucket to which the timeout was added
         */
        HashedWheelBucket bucket;

        HashedWheelTimeout(HashedTimingWheelScheduler timer, Job job, long deadline) {
            this.timer = timer;
            this.job = job;
            this.deadline = deadline;
        }

        public boolean expire() {
            return compareAndSetState(ST_INIT, ST_EXPIRED);
        }

        public HashedTimingWheelScheduler timer() {
            return timer;
        }

        public Job task() {
            return job;
        }

        public boolean cancel() {
            // only update the state it will be removed from HashedWheelBucket on next tick.
            if (!compareAndSetState(ST_INIT, ST_CANCELLED)) {
                return false;
            }
            // If a task should be canceled we create a new Runnable for this to another queue which will
            // be processed on each tick. So this means that we will have a GC latency of max. 1 tick duration
            // which is good enough. This way we can make again use of our MpscLinkedQueue and so minimize the
            // locking / overhead as much as possible.
            //
            // It is important that we not just add the HashedWheelTimeout itself again as it extends
            // MpscLinkedQueueNode and so may still be used as tombstone.
            timer.cancelledTimeouts.add(() -> {
                HashedWheelBucket bucket = HashedWheelTimeout.this.bucket;
                if (bucket != null) {
                    bucket.remove(HashedWheelTimeout.this);
                }
            });
            return true;
        }

        public boolean compareAndSetState(int expected, int state) {
            return STATE_UPDATER.compareAndSet(this, expected, state);
        }

        public int state() {
            return state;
        }

        public boolean isCancelled() {
            return state() == ST_CANCELLED;
        }

        public boolean isExpired() {
            return state() == ST_EXPIRED;
        }

        public HashedWheelTimeout value() {
            return this;
        }


        @Override
        public String toString() {
            final long currentTime = System.nanoTime();
            long remaining = deadline - currentTime + timer.startTime;

            StringBuilder buf = new StringBuilder(192);
            buf.append('(');

            buf.append("deadline: ");
            if (remaining > 0) {
                buf.append(remaining);
                buf.append(" ns later");
            } else if (remaining < 0) {
                buf.append(-remaining);
                buf.append(" ns ago");
            } else {
                buf.append("now");
            }

            if (isCancelled()) {
                buf.append(", cancelled");
            }

            buf.append(", task: ");
            buf.append(task());

            return buf.append(')').toString();
        }
    }

    /**
     * Bucket that stores HashedWheelTimeouts. These are stored in a linked-list like datastructure to allow easy
     * removal of HashedWheelTimeouts in the middle. Also the HashedWheelTimeout act as nodes themself and so no extra
     * object creation is needed.
     */
    private static final class HashedWheelBucket {
        /**
         * Used for the linked-list datastructure
         */
        private HashedWheelTimeout head;
        private HashedWheelTimeout tail;

        /**
         * Add {@link HashedWheelTimeout} to this bucket.
         */
        public void addTimeout(HashedWheelTimeout timeout) {
            assert timeout.bucket == null;
            timeout.bucket = this;
            if (head == null) {
                head = tail = timeout;
            } else {
                tail.next = timeout;
                timeout.prev = tail;
                tail = timeout;
            }
        }

        /**
         * Expire all {@link HashedWheelTimeout}s for the given {@code deadline}.
         */
        public void expireTimeouts(HashedTimingWheelScheduler hashedTimingWheelScheduler, long deadline) {
            HashedWheelTimeout timeout = head;

            // process all timeouts
            while (timeout != null) {
                boolean remove = false;
                if (timeout.remainingRounds <= 0) {
                    if (timeout.deadline <= deadline) {
                        hashedTimingWheelScheduler.expire(timeout);
                    } else {
                        // The timeout was placed into a wrong slot. This should never happen.
                        throw new IllegalStateException(
                                String.format("timeout.deadline (%d) > deadline (%d)", timeout.deadline, deadline));
                    }
                    remove = true;
                } else if (timeout.isCancelled()) {
                    remove = true;
                } else {
                    timeout.remainingRounds--;
                }
                // store reference to next as we may null out timeout.next in the remove block.
                HashedWheelTimeout next = timeout.next;
                if (remove) {
                    remove(timeout);
                }
                timeout = next;
            }
        }

        public void remove(HashedWheelTimeout timeout) {
            HashedWheelTimeout next = timeout.next;
            // remove timeout that was either processed or cancelled by updating the linked-list
            if (timeout.prev != null) {
                timeout.prev.next = next;
            }
            if (timeout.next != null) {
                timeout.next.prev = timeout.prev;
            }

            if (timeout == head) {
                // if timeout is also the tail we need to adjust the entry too
                if (timeout == tail) {
                    tail = null;
                    head = null;
                } else {
                    head = next;
                }
            } else if (timeout == tail) {
                // if the timeout is the tail modify the tail to be the prev node.
                tail = timeout.prev;
            }
            // null out prev, next and bucket to allow for GC.
            timeout.prev = null;
            timeout.next = null;
            timeout.bucket = null;
        }

        /**
         * Clear this bucket and return all not expired / cancelled {@link HashedWheelTimeout}s.
         */
        public void clearTimeouts(Set<HashedWheelTimeout> set) {
            for (; ; ) {
                HashedWheelTimeout timeout = pollTimeout();
                if (timeout == null) {
                    return;
                }
                if (timeout.isExpired() || timeout.isCancelled()) {
                    continue;
                }
                set.add(timeout);
            }
        }

        private HashedWheelTimeout pollTimeout() {
            HashedWheelTimeout head = this.head;
            if (head == null) {
                return null;
            }
            HashedWheelTimeout next = head.next;
            if (next == null) {
                tail = this.head = null;
            } else {
                this.head = next;
                next.prev = null;
            }

            // null out prev and next to allow for GC.
            head.next = null;
            head.prev = null;
            head.bucket = null;
            return head;
        }
    }
}
