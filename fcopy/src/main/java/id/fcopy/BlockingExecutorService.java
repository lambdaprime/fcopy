/**
 * Copyright 2020 lambdaprime
 * 
 * Email: id.blackmesa@gmail.com 
 * Website: https://github.com/lambdaprime
 * 
 */
package id.fcopy;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.concurrent.AbstractExecutorService;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Stream;

import id.xfunction.XUtils;
import id.xfunction.function.Unchecked;

/**
 * <p>BlockingExecutorService keeps pool of worker threads which read tasks
 * from the blocking queue.</p>
 * 
 * <p>If blocking queue is bounded then thread submitting a new task to this
 * executor will block until new space in queue became available (with standard
 * ThreadPoolExecutor such task will be rejected).</p>
 * 
 */
public class BlockingExecutorService extends AbstractExecutorService {

    // end of queue
    private static final Runnable EOQ = () -> {};

    private Semaphore semaphore;
    private volatile boolean isShutdown;
    private BlockingQueue<Runnable> queue;
    private List<WorkerThread> workers;
    
    private class WorkerThread extends Thread  {
        @Override
        public void run() {
            try {
                semaphore.acquire();
                Runnable r;
                while ((r = queue.take()) != EOQ) {
//                    System.out.println("pick up new item from queue");
                    r.run();
                }
                // put it back for other workers
                queue.put(EOQ);
            } catch (Exception ex) {
                XUtils.printExceptions(ex);
            } finally {
                semaphore.release();
            }
        }
    }
    
    /**
     * Creates executor with bounded queue of given capacity.
     * 
     * @param maximumPoolSize number of worker threads which will be created and be waiting
     * for a new tasks
     * @param capacity size of the internal queue from which worker will pick up the tasks
     */
    public BlockingExecutorService(int maximumPoolSize, int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.semaphore = new Semaphore(maximumPoolSize);
        this.workers = Stream.generate(() -> new WorkerThread())
                .peek(Thread::start)
                .limit(maximumPoolSize)
                .collect(toList());
    }

    public BlockingExecutorService(int capacity) {
        this(ForkJoinPool.getCommonPoolParallelism(), capacity);
    }

    @Override
    public void shutdown() {
        isShutdown = true;
        Executors.defaultThreadFactory().newThread(() -> {
            Unchecked.run(() -> queue.put(EOQ));
        }).start();
    }

    @Override
    public List<Runnable> shutdownNow() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isShutdown() {
        return isShutdown;
    }

    @Override
    public boolean isTerminated() {
        return semaphore.availablePermits() == workers.size();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        boolean isTerminated = semaphore.tryAcquire(workers.size(), timeout, unit);
        if (isTerminated) return true;
        return workers.stream()
                .map(Thread::isAlive)
                .noneMatch(Predicate.isEqual(true));
    }

    @Override
    public void execute(Runnable command) {
        if (isShutdown) return;
        Unchecked.run(() -> queue.put(command));
    }

}
