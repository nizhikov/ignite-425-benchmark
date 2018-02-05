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
import org.apache.ignite.cache.query.ContinuousQuery;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.annotations.Warmup;
import org.openjdk.jmh.infra.Blackhole;
import ru.sbt.ignite425.helpers.BenchContext;
import ru.sbt.ignite425.cq.CQListener;
import ru.sbt.ignite425.helpers.Value;
import ru.sbt.ignite425.helpers.WriteThread;

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
public class CQBenchmark extends AbstractBenchmark {
    private CQListener listener;

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

    @Setup(Level.Iteration)
    public void setup() {
        super.doSetup();

        listener = new CQListener();

        ContinuousQuery<Long, Value> cq = new ContinuousQuery<>();

        initCommonParams(cq);

        cq.setLocalListener(listener);

        testCache.query(cq);
    }

    @TearDown(Level.Iteration)
    public void tearDown() {
        super.doTearDown();
    }
}
