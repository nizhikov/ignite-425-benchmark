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
import javax.cache.event.CacheEntryListenerException;
import javax.cache.event.CacheEntryUpdatedListener;
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

import static ru.sbt.ignite425.AbstractBenchmark.JVM_ARGS;

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 3, time = 20)
@Measurement(iterations = 5, time = 30)
@Fork(value = 1, jvmArgsAppend = JVM_ARGS)
public class CQBenchmark extends AbstractBenchmark {
    private MyListener listener =new MyListener();

    @Setup(Level.Trial)
    @Override
    public void doSetup() {
        super.doSetup();

        ContinuousQuery<Long, Value> cq = new ContinuousQuery<>();

        cq.setLocalListener(listener);
        cq.setPageSize(1024);
        cq.setTimeInterval(150);

        testCache.query(cq);
    }

    @TearDown(Level.Trial)
    public void doTearDown() {
        super.doTearDown();
    }

    @Benchmark @BenchmarkMode(Mode.Throughput)
    public void putBatch(BenchContext ctx, Blackhole blackhole) throws Exception {
        ctx.methodExecuted++;

        if (listener.ctx == null) {
            listener.ctx = ctx;
            listener.blackhole = blackhole;
        }

        barrier.await();
    }

    public static class MyListener implements CacheEntryUpdatedListener<Long, Value> {
        public BenchContext ctx;

         public Blackhole blackhole;

        @Override public void onUpdated(
            Iterable<CacheEntryEvent<? extends Long, ? extends Value>> iterable) throws CacheEntryListenerException {

            long cnt = 0;

            for (Object event : iterable) {
                blackhole.consume(event);
                cnt++;
            }

            synchronized (ctx) {
                ctx.evtCnt += cnt;
            }
        }
    }
}
