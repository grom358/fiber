package fiber;

import java.util.concurrent.atomic.AtomicInteger;
import de.matthiasmann.continuations.SuspendExecution;

/**
 * Fiber version of java.util.concurrent.CountDownLatch
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
    private AtomicInteger counter;

    /**
     * Create instance of FiberLatch with parent scheduler and given latch count.
     */
    protected FiberLatch(FiberScheduler scheduler, int counter) {
        this.condition = new FiberCondition(scheduler);
        this.counter = new AtomicInteger(counter);
    }

    /**
     * Decrements the count of the latch, releasing all waiting fibers if the count reaches zero.
     */
    public void countDown() {
        int c = counter.decrementAndGet();
        if (c == 0) {
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
