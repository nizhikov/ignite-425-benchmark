package ru.sbt.ignite425.helpers;

import java.util.Date;
import java.util.UUID;
import java.util.concurrent.CyclicBarrier;
import javax.cache.CacheException;
import org.apache.ignite.IgniteCache;

/**
 * @author NIzhikov
 */
public class WriteThread extends Thread {
    private IgniteCache<Long, Value> testCache;

    private volatile boolean isStopped = false;

    private CyclicBarrier barrier;

    public BenchContext ctx;

    private long batchSize;

    private long batchNumber;

    private long writersCnt;

    private long writerIdx;

    public WriteThread(IgniteCache<Long, Value> testCache, long batchSize, CyclicBarrier barrier, long writersCnt, long writerIdx) {
        this.testCache = testCache;
        this.batchSize = batchSize;
        this.barrier = barrier;
        this.writersCnt = writersCnt;
        this.writerIdx = writerIdx;
    }

    @Override public void run() {
        while (true) {
            if (isStopped)
                return;

            waitBarrier();

            if (isStopped)
                return;

            try {
                long base = writersCnt*batchNumber*batchSize + writerIdx*batchSize;

                long localID = 0;

                for (int i = 0; i < batchSize; i++) {
                    long id = base + localID++;

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

            batchNumber++;
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
