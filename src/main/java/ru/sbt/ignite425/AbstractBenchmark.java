package ru.sbt.ignite425;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;

public class AbstractBenchmark {
    List<Ignite> servers = new ArrayList<>();

    Ignite client;

    IgniteCache<Long, Long> testCache;

    AtomicLong cntr = new AtomicLong();

    public void doSetup() {
        IgniteConfiguration config = new IgniteConfiguration();

        for (int i = 0; i < 3; i++) {
            config.setIgniteInstanceName("ignite" + i);

            servers.add(Ignition.start(config));
        }

        config.setClientMode(true);

        config.setIgniteInstanceName("ignite-client");

        client = Ignition.start(config);

        testCache = client.createCache("testCache");
    }

    public void doTearDown() {
        client.close();

        for (Ignite ignite : servers)
            ignite.close();
    }
}
