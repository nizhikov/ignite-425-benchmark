package ru.sbt.ignite425;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;

public class AbstractBenchmark {
    List<Ignite> servers = new ArrayList<>();

    Ignite client;

    IgniteCache<Long, Long> testCache;

    AtomicLong cntr = new AtomicLong();

    CyclicBarrier barrier = new CyclicBarrier(WRITERS_COUNT + 1);

    List<WriteThread> writers = new ArrayList<>();

    public static final int BATCH_SIZE = 2000;

    public static final int WRITERS_COUNT = 8;

    public void doSetup() {
        IgniteConfiguration config = new IgniteConfiguration();

        for (int i = 0; i < 3; i++) {
            config.setIgniteInstanceName("ignite" + i);

            servers.add(Ignition.start(config));
        }

        config.setClientMode(true);

        config.setIgniteInstanceName("ignite-client");

        client = Ignition.start(config);

        testCache = client.createCache("testCache");

        for (int i=0; i<WRITERS_COUNT; i++) {
            WriteThread writer = new WriteThread(testCache, cntr, BATCH_SIZE, barrier);

            writer.start();

            writers.add(writer);
        }
    }

    public void doTearDown() {
        for (WriteThread writer : writers)
            writer.setStopped();

        try {
            barrier.await();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }

        client.close();

        for (Ignite ignite : servers)
            ignite.close();
    }

}
