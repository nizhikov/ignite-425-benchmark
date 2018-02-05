package ru.sbt.ignite425.cq;

import javax.cache.event.CacheEntryEvent;
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryUpdatedListener;
import org.openjdk.jmh.infra.Blackhole;
import ru.sbt.ignite425.helpers.BenchContext;
import ru.sbt.ignite425.helpers.Value;

/**
 * @author NIzhikov
 */
public class CQListener implements CacheEntryUpdatedListener<Long, Value> {
    public BenchContext ctx;

    public Blackhole blackhole;

    @Override public void onUpdated(
        Iterable<CacheEntryEvent<? extends Long, ? extends Value>> iterable) throws CacheEntryListenerException {
        for (Object event : iterable) {
            blackhole.consume(event);

            ctx.incEvtCnt();
        }
    }
}
