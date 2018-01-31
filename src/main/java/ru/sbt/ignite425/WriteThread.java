package ru.sbt.ignite425;

import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.ignite.IgniteCache;

/**
 * @author NIzhikov
 */
public class WriteThread extends Thread {
    IgniteCache<Long, Long> testCache;

    AtomicLong cntr;

    long iterationSize;

    boolean isStopped = false;

    CyclicBarrier barrier;

    public WriteThread(IgniteCache<Long, Long> testCache, AtomicLong cntr, long iterationSize, CyclicBarrier barrier) {
        this.testCache = testCache;
        this.cntr = cntr;
        this.iterationSize = iterationSize;
        this.barrier = barrier;
    }

    @Override public void run() {
        while (true) {
            waitBarrier();

            if (isStopped)
                return;

            for (int i = 0; i < iterationSize; i++) {
                long val = cntr.incrementAndGet();

                testCache.put(val, val);
            }
        }
    }

    private void waitBarrier() {
        try {
            barrier.await();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void setStopped() {
        isStopped = true;
    }
}
