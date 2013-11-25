package fiber;

import de.matthiasmann.continuations.Coroutine;
import de.matthiasmann.continuations.CoroutineProto;
import de.matthiasmann.continuations.SuspendExecution;

/**
 *
 * @author grom
 */
public abstract class Fiber implements Comparable<Fiber>, CoroutineProto {
    long awakeTime = 0;
    private boolean suspended;
    private boolean alive = true;
    final private Coroutine co = new Coroutine(this);

    final protected void sleep(long milliseconds) throws SuspendExecution {
        awakeTime = System.currentTimeMillis() + milliseconds;
        suspended = true;
        Coroutine.yield();
    }

    final protected boolean isSuspended() {
        return suspended;
    }

    final protected boolean isAlive() {
        return alive;
    }

    final protected void yield() throws SuspendExecution {
        Coroutine.yield();
    }

    final protected void waitOn(FiberCondition condition) throws SuspendExecution {
        waitOn(condition, Long.MAX_VALUE);
    }

    final protected void waitOn(FiberCondition condition, long waitDuration) throws SuspendExecution {
        condition.registerWait(this, waitDuration);
    }

    final protected void waitOn(FiberLatch latch) throws SuspendExecution {
        waitOn(latch, Long.MAX_VALUE);
    }

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

    final public void run() {
        suspended = false;
        co.run();
        if (co.getState() == Coroutine.State.FINISHED) {
            alive = false;
        }
    }
}
