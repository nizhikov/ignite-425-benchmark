package ru.sbt.ignite425.helpers;

import java.util.concurrent.atomic.LongAdder;
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
    private LongAdder evtCnt = new LongAdder();

    private LongAdder putCnt = new LongAdder();

    @Setup(Level.Iteration)
    public void clean() {
        evtCnt.reset();
        putCnt.reset();
    }

    public void incEvtCnt() {
        this.evtCnt.add(1);
    }

    public void incPutCnt() {
        this.putCnt.add(1);
    }

    public long listenerReceiveEvt() {
        return evtCnt.longValue();
    }

    public long putCnt() {
        return putCnt.longValue();
    }
}
