package ru.sbt.ignite425;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;
import javax.cache.configuration.Factory;
import javax.cache.event.CacheEntryEvent;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.ContinuousQueryWithTransformer;
import org.apache.ignite.cache.query.TransformedEventListener;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.lang.IgniteClosure;
import org.openjdk.jmh.infra.Blackhole;

public class AbstractBenchmark {
    public static final String JVM_ARGS = "-Xmx3G";

    List<Ignite> servers = new ArrayList<>();

    Ignite client;

    IgniteCache<Long, Value> testCache;

    AtomicLong cntr = new AtomicLong();

    CyclicBarrier barrier = new CyclicBarrier(WRITERS_COUNT + 1);

    List<WriteThread> writers = new ArrayList<>();

    public static final int BATCH_SIZE = 1024;

    public static final int WRITERS_COUNT = 4;

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

    <T> void setupCQWT(
        TransformedEventListener<T> listener,
        Factory<? extends IgniteClosure<CacheEntryEvent<? extends Long, ? extends Value>, T>> factory
    ) {

        ContinuousQueryWithTransformer<Long, Value, T> cqwt = new ContinuousQueryWithTransformer<>();

        cqwt.setPageSize(BATCH_SIZE);
        cqwt.setTimeInterval(150);
        cqwt.setRemoteTransformerFactory(factory);

        cqwt.setLocalListener(listener);

        testCache.query(cqwt);
    }

    public static class MyListener<T> implements TransformedEventListener<T> {
        public BenchContext ctx;

        public Blackhole blackhole;

        @Override public void onUpdated(Iterable<? extends T> events) {
            long cnt = 0;

            for (T event : events) {
                blackhole.consume(event);
                cnt++;
            }

            synchronized (ctx) {
                ctx.evtCnt += cnt;
            }
        }
    }
}
