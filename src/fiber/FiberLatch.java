package fiber;

import de.matthiasmann.continuations.SuspendExecution;

/**
 * Fiber version of java.util.concurrent.CountDownLatch . The latch is not
 * thread safe and can only be used by fibers running under the same scheduler.
 *
 * @author grom
 */
public class FiberLatch {
    /**
     * Condition variable for latch closed/opened
     */
    private FiberCondition condition;

    /**
     * Count down counter for release of latch
     */
    private int counter;

    /**
     * Create instance of FiberLatch with parent scheduler and given latch count.
     */
    protected FiberLatch(FiberScheduler scheduler, int counter) {
        this.condition = new FiberCondition(scheduler);
        this.counter = counter;
    }

    /**
     * Decrements the count of the latch, releasing all waiting fibers if the count reaches zero.
     */
    public void countDown() {
        counter--;
        if (counter == 0) {
            condition.signalAll();
        }
    }

    /**
     * Register fiber to wait on the latch
     */
    void registerWait(Fiber fiber, long waitDuration) throws SuspendExecution {
        condition.registerWait(fiber, waitDuration);
    }
}
