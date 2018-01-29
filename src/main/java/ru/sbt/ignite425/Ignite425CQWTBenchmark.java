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
import javax.cache.configuration.FactoryBuilder;
import javax.cache.event.CacheEntryEvent;
import org.apache.ignite.cache.query.ContinuousQueryWithTransformer;
import org.apache.ignite.cache.query.TransformedEventListener;
import org.apache.ignite.lang.IgniteClosure;
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

@State(Scope.Benchmark)
@OutputTimeUnit(TimeUnit.SECONDS)
@Warmup(iterations = 2, time = 10)
@Measurement(iterations = 10, time = 10)
@Fork(1)
public class Ignite425CQWTBenchmark extends AbstractBenchmark {
    private MyListener listener = new MyListener();

    @Setup(Level.Trial)
    @Override
    public void doSetup() {
        super.doSetup();

        ContinuousQueryWithTransformer<Long, Long, CacheEntryEvent> cqwt = new ContinuousQueryWithTransformer<>();

        cqwt.setRemoteTransformerFactory(FactoryBuilder.factoryOf(new MyTransformer()));

        cqwt.setLocalListener(listener);

        testCache.query(cqwt);
    }

    @TearDown(Level.Trial)
    public void doTearDown() {
        super.doTearDown();
    }

    @Benchmark @BenchmarkMode(Mode.Throughput)
    public void testMethod(BenchContext ctx) {
        if (listener.ctx == null)
            listener.ctx = ctx;

        long val = cntr.incrementAndGet();

        testCache.put(val, val);
    }

    public static class MyListener implements TransformedEventListener<CacheEntryEvent> {
        public BenchContext ctx;

        @Override public void onUpdated(Iterable<? extends CacheEntryEvent> events) {
            for (CacheEntryEvent event : events) {
                ctx.listenerExecuted++;
            }
        }
    }

    public static class MyTransformer implements IgniteClosure<CacheEntryEvent<? extends Long, ? extends Long>, CacheEntryEvent> {
        @Override public CacheEntryEvent apply(CacheEntryEvent<? extends Long, ? extends Long> event) {
            return event;
        }
    }
}
