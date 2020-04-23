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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Predicate;
import java.util.stream.Stream;

import id.xfunction.XUtils;
import id.xfunction.function.Unchecked;

public class BlockingExecutorService extends AbstractExecutorService {

    // end of queue
    private static final Runnable EOQ = () -> {};

    private Semaphore semaphore;
    private AtomicBoolean isTerminated = new AtomicBoolean(false);
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
                System.out.println("worker terminated");
            }
        }
    }
    
    public BlockingExecutorService(int numOfThreads, int capacity) {
        this.queue = new ArrayBlockingQueue<>(capacity);
        this.semaphore = new Semaphore(numOfThreads);
        this.workers = Stream.generate(() -> new WorkerThread())
                .peek(Thread::start)
                .limit(numOfThreads)
                .collect(toList());
    }

    public BlockingExecutorService(int capacity) {
        this(ForkJoinPool.getCommonPoolParallelism(), capacity);
    }

    @Override
    public void shutdown() {
        isTerminated.set(true);
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
        return isTerminated.get();
    }

    @Override
    public boolean isTerminated() {
        return semaphore.availablePermits() == workers.size();
    }

    @Override
    public boolean awaitTermination(long timeout, TimeUnit unit)
            throws InterruptedException {
        boolean isTerminated = semaphore.tryAcquire(workers.size(), timeout, unit);
        if (isTerminated) return isTerminated;
        return workers.stream()
                .map(Thread::isAlive)
                .noneMatch(Predicate.isEqual(true));
    }

    @Override
    public void execute(Runnable command) {
        if (isTerminated.get()) return;
        Unchecked.run(() -> queue.put(command));
    }

}
