package ru.sbt.ignite425;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.query.AbstractContinuousQuery;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.internal.util.typedef.G;
import ru.sbt.ignite425.helpers.Value;
import ru.sbt.ignite425.helpers.WriteThread;

public class AbstractBenchmark {
    public static final int WARMUP_ITERATION = 5;

    public static final int WARMUP_TIME = 30;

    public static final int BENCH_ITERATION = 5;

    public static final int BENCH_TIME = 120;

    public static final int BATCH_SIZE = 1024*10;

    public static final int WRITERS_COUNT = 6;

    public static final String JVM_ARGS = "-Xmx3G";

    private List<Ignite> servers;

    private Ignite client;

    IgniteCache<Long, Value> testCache;

    List<WriteThread> writers;

    CyclicBarrier barrier;

    public void doSetup() {
        IgniteConfiguration config = new IgniteConfiguration();

        servers = new ArrayList<>();

        for (int i = 0; i < 3; i++) {
            config.setIgniteInstanceName("ignite" + i);

            servers.add(Ignition.start(config));
        }

        config.setClientMode(true);

        config.setIgniteInstanceName("ignite-client");

        client = Ignition.start(config);

        testCache = client.createCache("testCache");

        AtomicLong idGenerator = new AtomicLong();

        writers = new ArrayList<>();

        barrier = new CyclicBarrier(WRITERS_COUNT + 1);

        for (int i=0; i<WRITERS_COUNT; i++) {
            WriteThread writer = new WriteThread(testCache, idGenerator, BATCH_SIZE, barrier);

            writer.start();

            writers.add(writer);
        }
    }

    public void doTearDown() {
        for (WriteThread writer : writers)
            writer.setStopped();

        barrier.reset();

        G.stop(client.name(), true);

        for (Ignite ignite : servers)
            G.stop(ignite.name(), true);
    }

    protected <K, V> void initCommonParams(AbstractContinuousQuery<K, V> cqwt) {
        cqwt.setPageSize(BATCH_SIZE);

        cqwt.setTimeInterval(150);
    }

}
