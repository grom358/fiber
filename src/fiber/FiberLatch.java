package fiber;

import de.matthiasmann.continuations.SuspendExecution;

/**
 *
 * @author grom
 */
public class FiberLatch {
    private FiberCondition condition;
    private int counter;

    protected FiberLatch(FiberScheduler scheduler, int counter) {
        this.condition = new FiberCondition(scheduler);
        this.counter = counter;
    }

    public void countDown() {
        counter--;
        if (counter == 0) {
            fireLatch();
        }
    }

    void registerWait(Fiber fiber, long waitDuration) throws SuspendExecution {
        condition.registerWait(fiber, waitDuration);
    }

    protected void fireLatch() {
        condition.signalAll();
    }
}
