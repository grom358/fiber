package fiber;

import java.util.ArrayDeque;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 *
 * @author grom
 */
public class FiberScheduler implements Runnable {
    /**
     * Fibers that are waiting (ie. sleep)
     */
    private PriorityQueue<Fiber> sleepingFibers;

    /**
     * Fibers that are active and need to be ran by the scheduler
     */
    private Queue<Fiber> activeFibers;

    /**
     * Fibers that recently added to scheduler. This is communication point
     * for other threads to add fibers to the scheduler
     */
    private BlockingQueue<Fiber> newFibers;

    private volatile boolean running;

    private volatile boolean joined;

    final private Thread schedulerThread;

    public FiberScheduler() {
        activeFibers = new ArrayDeque<Fiber>(100);
        sleepingFibers = new PriorityQueue<Fiber>(100);
        newFibers = new ArrayBlockingQueue<Fiber>(100);
        schedulerThread = new Thread(this);
    }

    public void start() {
        running = true;
        joined = false;
        schedulerThread.start();
    }

    public void join() throws InterruptedException {
        joined = true;
        synchronized(schedulerThread) {
            schedulerThread.notify();
        }
        schedulerThread.join();
    }

    public void shutdownNow() {
        running = false;
    }

    public void wakeup(Fiber fiber) {
        if (Thread.currentThread() == schedulerThread) {
            // Fiber waking up another fiber
            if (sleepingFibers.remove(fiber)) {
                activeFibers.add(fiber);
            }
        } else {
            // TODO Implement a message queue to notify scheduler of fibers to wakeup
            throw new UnsupportedOperationException("Only fibers on same scheduler can wakeup each other");
        }
    }

    public void submit(Fiber fiber) {
        if (running) {
            boolean doJob = true;
            while (doJob) {
                try {
                    newFibers.put(fiber);
                    // Wakeup the scheduler
                    schedulerThread.interrupt();
                    doJob = false;
                } catch (InterruptedException ex) {
                    // Ignore interrupt, and retry
                }
            }
        } else {
            activeFibers.add(fiber);
        }
    }

    @Override
    public void run() {
        while (running) {
            // Move newly scheduled fibers to active queue
            newFibers.drainTo(activeFibers);

            // If there are no fibers to process wait for fiber to be scheduled
            if (sleepingFibers.isEmpty() && activeFibers.isEmpty()) {
                if (joined) {
                    running = false;
                    return;
                }
                synchronized(schedulerThread) {
                    try {
                        schedulerThread.wait();
                    } catch (InterruptedException e) {
                    }
                }
                continue;
            }

            // Wake up all sleeping fibers with expired awakeTime
            if (!sleepingFibers.isEmpty()) {
                long now = System.currentTimeMillis();
                while (true) {
                    Fiber fiber = sleepingFibers.peek();
                    if (fiber != null && fiber.awakeTime <= now) {
                        // Wakeup fiber
                        activeFibers.add(sleepingFibers.poll());
                    } else {
                        break;
                    }
                }
            }
            if (activeFibers.isEmpty()) {
                // if there are no active threads, wait for next sleeping fiber to become active
                Fiber fiber = sleepingFibers.peek();
                if (fiber != null) {
                    long sleepTime = fiber.awakeTime - System.currentTimeMillis();
                    if (sleepTime > 0) {
                        try {
                            Thread.sleep(sleepTime);
                        } catch (InterruptedException e) {
                        }
                    }
                }
            } else {
                // Run active fibers
                int n = activeFibers.size();
                for (int i = 0; i < n; ++i) {
                    Fiber fiber = activeFibers.poll();
                    fiber.run();
                    if (fiber.isSuspended()) {
                        sleepingFibers.add(fiber);
                    } else if (fiber.isAlive()) {
                        activeFibers.add(fiber);
                    }
                }
            }
        }
    }
}
