package fiber;

import de.matthiasmann.continuations.SuspendExecution;
import java.util.ArrayList;
import java.util.List;

/**
 * Fiber version of java.util.concurrent.locks.Condition
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
    public synchronized void signalAll() {
        for (Fiber fiber : waitingFibers) {
            scheduler.wakeup(fiber);
        }
        waitingFibers.clear();
    }

    /**
     * Register fiber to wait on the condition
     */
    synchronized void registerWait(Fiber fiber, long waitDuration) throws SuspendExecution {
        waitingFibers.add(fiber);
        fiber.sleep(waitDuration);
    }
}
