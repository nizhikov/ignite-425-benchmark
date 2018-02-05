package ru.sbt.ignite425.helpers;

import java.util.concurrent.atomic.AtomicLong;
import org.openjdk.jmh.annotations.AuxCounters;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

/**
 * @author NIzhikov
 */
@State(Scope.Thread)
@AuxCounters(AuxCounters.Type.EVENTS)
public class BenchContext {
    private AtomicLong evtCnt = new AtomicLong();

    private AtomicLong putCnt = new AtomicLong();

    @Setup(Level.Iteration)
    public void clean() {
        evtCnt.set(0);
        putCnt.set(0);
    }

    public void incEvtCnt() {
        this.evtCnt.incrementAndGet();
    }

    public void incPutCnt() {
        this.putCnt.incrementAndGet();
    }

    public long listenerReceiveEvt() {
        return evtCnt.get();
    }

    public long putCnt() {
        return putCnt.get();
    }
}
