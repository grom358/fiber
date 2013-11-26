package fiber;

import de.matthiasmann.continuations.Coroutine;
import de.matthiasmann.continuations.CoroutineProto;
import de.matthiasmann.continuations.SuspendExecution;

/**
 * A fiber is lightweight cooperative thread.
 *
 * @author grom
 */
public abstract class Fiber implements Comparable<Fiber>, CoroutineProto {
    /**
     * Time to wake up the fiber
     */
    long awakeTime = 0;

    /**
     * True if the fiber is suspended
     */
    private boolean suspended;

    /**
     * True if the fiber is alive
     */
    private boolean alive = true;

    /**
     * The continuation of this fiber
     */
    final private Coroutine co = new Coroutine(this);

    /**
     * Causes the fiber to sleep (temporarily cease execution) for the specified
     * number of milliseconds, subject to the precision and accuracy of system
     * timers and the execution of other fibers on the same FiberScheduler.
     */
    final protected void sleep(long milliseconds) throws SuspendExecution {
        if (milliseconds == -1) {
            awakeTime = Long.MAX_VALUE;
        } else {
            awakeTime = System.currentTimeMillis() + milliseconds;
        }
        suspended = true;
        Coroutine.yield();
    }

    /**
     * Tests if this thread is suspended.
     */
    final protected boolean isSuspended() {
        return suspended;
    }

    /**
     * Tests if this fiber is alive.
     */
    final protected boolean isAlive() {
        return alive;
    }

    /**
     * Yield execution to allow other fibers to use the scheduler.
     */
    final protected void yield() throws SuspendExecution {
        Coroutine.yield();
    }

    /**
     * Causes the fiber to wait on a FiberCondition.
     */
    final protected void waitOn(FiberCondition condition) throws SuspendExecution {
        waitOn(condition, -1);
    }

    /**
     * Causes the fiber to wait on a FiberCondition or until the specified
     * amount of milliseconds has elapsed.
     */
    final protected void waitOn(FiberCondition condition, long waitDuration) throws SuspendExecution {
        condition.registerWait(this, waitDuration);
    }

    /**
     * Causes the fiber to wait on a FiberLatch.
     */
    final protected void waitOn(FiberLatch latch) throws SuspendExecution {
        waitOn(latch, -1);
    }

    /**
     * Causes the fiber to wait on a FiberLatch or until the specified
     * amount of milliseconds has elapsed.
     */
    final protected void waitOn(FiberLatch latch, long waitDuration) throws SuspendExecution {
        latch.registerWait(this, waitDuration);
    }

    @Override
    final public int compareTo(Fiber other) {
        if (this == other) {
            return 0;
        }
        return (int) (this.awakeTime - other.awakeTime);
    }

    /**
     * Run the fiber
     */
    final protected void run() {
        suspended = false;
        co.run();
        if (co.getState() == Coroutine.State.FINISHED) {
            alive = false;
        }
    }
}
