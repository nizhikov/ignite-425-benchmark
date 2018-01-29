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
@AuxCounters(AuxCounters.Type.OPERATIONS)
public class BenchContext {
    public long listenerExecuted;

    @Setup(Level.Iteration)
    public void clean() {
        listenerExecuted = 0;
    }
}
