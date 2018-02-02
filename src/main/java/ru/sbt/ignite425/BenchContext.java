package ru.sbt.ignite425;

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
    public long evtCnt;

    public long methodExecuted;

    @Setup(Level.Iteration)
    public void clean() {
        evtCnt = 0;
        methodExecuted = 0;
    }
}
