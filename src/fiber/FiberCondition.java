package fiber;

import de.matthiasmann.continuations.SuspendExecution;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author grom
 */
public class FiberCondition {
    private FiberScheduler scheduler;

    private boolean condition = false;

    private List<Fiber> waitingFibers;

    protected FiberCondition(FiberScheduler scheduler) {
        this.scheduler = scheduler;
        this.waitingFibers = new ArrayList<Fiber>();
    }

    void registerWait(Fiber fiber, long waitDuration) throws SuspendExecution {
        waitingFibers.add(fiber);
        fiber.sleep(waitDuration);
    }

    public void signalAll() {
        // wakeup all the fibers waiting on this condition
        for (Fiber fiber : waitingFibers) {
            scheduler.wakeup(fiber);
        }
        waitingFibers.clear();
    }
}
