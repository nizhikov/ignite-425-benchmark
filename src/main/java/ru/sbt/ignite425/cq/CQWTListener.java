package ru.sbt.ignite425.cq;

import org.apache.ignite.cache.query.ContinuousQueryWithTransformer.EventListener;
import org.openjdk.jmh.infra.Blackhole;
import ru.sbt.ignite425.helpers.BenchContext;

/**
 */
public class CQWTListener<T> implements EventListener<T> {
    public BenchContext ctx;

    public Blackhole blackhole;

    @Override public void onUpdated(Iterable<? extends T> events) {
        for (T event : events) {
            blackhole.consume(event);

            ctx.incEvtCnt();
        }
    }
}
