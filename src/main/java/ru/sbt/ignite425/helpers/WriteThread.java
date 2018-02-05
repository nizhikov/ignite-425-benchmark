package ru.sbt.ignite425.helpers;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import javax.cache.CacheException;
import org.apache.ignite.IgniteCache;

/**
 * @author NIzhikov
 */
public class WriteThread extends Thread {
    private IgniteCache<Long, Value> testCache;

    private AtomicLong cntr;

    private long iterationSize;

    private volatile boolean isStopped = false;

    private CyclicBarrier barrier;

    public BenchContext ctx;

    public WriteThread(IgniteCache<Long, Value> testCache, AtomicLong cntr, long iterationSize, CyclicBarrier barrier) {
        this.testCache = testCache;
        this.cntr = cntr;
        this.iterationSize = iterationSize;
        this.barrier = barrier;
    }

    @Override public void run() {
        while (true) {
            if (isStopped)
                return;

            waitBarrier();

            if (isStopped)
                return;

            try {
                for (int i = 0; i < iterationSize; i++) {
                    long id = cntr.incrementAndGet();

                    Value val = new Value();

                    val.id = id;
                    val.str = String.valueOf(id);
                    val.str = val.str + val.str + val.str;
                    val.uuid = UUID.randomUUID();
                    val.date = new Date();

                    testCache.put(id, val);

                    ctx.incPutCnt();
                }
            }
            catch (CacheException e) {
                /* no-op */
            }

        }
    }

    private void waitBarrier() {
        try {
            barrier.await();
        }
        catch (Exception e) {
            /* no-op */
        }
    }

    public void setStopped() {
        isStopped = true;
    }
}
