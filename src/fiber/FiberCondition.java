package fiber;

import de.matthiasmann.continuations.SuspendExecution;
import java.util.ArrayList;
import java.util.List;

/**
 * Fiber version of java.util.concurrent.locks.Condition . The condition is not
 * thread safe and can only be used by fibers unning under the same scheduler.
 *
 * If other threads wish to signal the condition then can queue signal of
 * condition via the signal methods of FiberScheduler
 *
 * @author grom
 */
public class FiberCondition {
    /**
     * Scheduler that owns the condition
     */
    private FiberScheduler scheduler;

    /**
     * Fibers waiting on condition
     */
    private List<Fiber> waitingFibers;

    /**
     * Create instance of FiberLatch with parent scheduler.
     */
    protected FiberCondition(FiberScheduler scheduler) {
        this.scheduler = scheduler;
        this.waitingFibers = new ArrayList<Fiber>();
    }

    /**
     * Wakes up all waiting fibers.
     */
    public void signalAll() {
        for (Fiber fiber : waitingFibers) {
            scheduler.wakeup(fiber);
        }
        waitingFibers.clear();
    }

    /**
     * Register fiber to wait on the condition
     */
    void registerWait(Fiber fiber, long waitDuration) throws SuspendExecution {
        waitingFibers.add(fiber);
        fiber.sleep(waitDuration);
    }
}
