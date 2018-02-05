/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package ru.sbt.ignite425;

import java.util.concurrent.TimeUnit;
import javax.cache.event.CacheEntryEvent;
import org.apache.ignite.lang.IgniteClosure;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import ru.sbt.ignite425.helpers.Value;

import static ru.sbt.ignite425.AbstractBenchmark.BENCH_ITERATION;
import static ru.sbt.ignite425.AbstractBenchmark.BENCH_TIME;
import static ru.sbt.ignite425.AbstractBenchmark.JVM_ARGS;
import static ru.sbt.ignite425.AbstractBenchmark.WARMUP_ITERATION;
import static ru.sbt.ignite425.AbstractBenchmark.WARMUP_TIME;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = WARMUP_ITERATION, time = WARMUP_TIME)
@Measurement(iterations = BENCH_ITERATION, time = BENCH_TIME)
@Fork(value = 1, jvmArgsAppend = JVM_ARGS)
public class CQWTNullBenchmark extends AbstractCQWTBenchmark<Long> {
    @Override protected IgniteClosure<CacheEntryEvent<? extends Long, ? extends Value>, Long> transformer() {
        return new NullTransformer();
    }

    @Setup(Level.Iteration)
    @Override
    public void doSetup() {
        super.doSetup();
    }

    @TearDown(Level.Iteration)
    @Override
    public void doTearDown() {
        super.doTearDown();
    }

    public static class NullTransformer implements IgniteClosure<CacheEntryEvent<? extends Long, ? extends Value>, Long> {
        @Override public Long apply(CacheEntryEvent<? extends Long, ? extends Value> event) {
            return null;
        }
    }
}
