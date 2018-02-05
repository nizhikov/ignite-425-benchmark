package ru.sbt.ignite425;

import javax.cache.configuration.FactoryBuilder;
import javax.cache.event.CacheEntryEvent;
import org.apache.ignite.cache.query.ContinuousQueryWithTransformer;
import org.apache.ignite.lang.IgniteClosure;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.infra.Blackhole;
import ru.sbt.ignite425.cq.CQWTListener;
import ru.sbt.ignite425.helpers.BenchContext;
import ru.sbt.ignite425.helpers.Value;
import ru.sbt.ignite425.helpers.WriteThread;

/**
 * @author NIzhikov
 */
public abstract class AbstractCQWTBenchmark<T> extends AbstractBenchmark {
    private CQWTListener<T> listener;

    @Override
    public void doSetup() {
        super.doSetup();

        listener = new CQWTListener<>();

        ContinuousQueryWithTransformer<Long, Value, T> cqwt = new ContinuousQueryWithTransformer<>();

        initCommonParams(cqwt);

        cqwt.setRemoteTransformerFactory(FactoryBuilder.factoryOf(transformer()));

        cqwt.setLocalListener(listener);

        testCache.query(cqwt);
    }

    @Benchmark @BenchmarkMode(Mode.Throughput)
    public void putBatch(BenchContext ctx, Blackhole blackhole) throws Exception {
        if (listener.ctx == null) {
            listener.ctx = ctx;

            listener.blackhole = blackhole;

            for (WriteThread writer : writers)
                writer.ctx = ctx;
        }

        barrier.await();
    }

    protected abstract IgniteClosure<CacheEntryEvent<? extends Long, ? extends Value>, T> transformer();
}
