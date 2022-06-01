package com.hazelcast.tpc.engine;


import org.junit.Test;

import java.util.concurrent.CountDownLatch;

import static com.hazelcast.tpc.engine.EventloopState.NEW;
import static com.hazelcast.tpc.engine.EventloopState.RUNNING;
import static com.hazelcast.tpc.engine.EventloopState.SHUTDOWN;
import static com.hazelcast.tpc.engine.EventloopState.TERMINATED;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public abstract class EventloopTest {

    public abstract Eventloop createEventloop();

    @Test(expected = IllegalStateException.class)
    public void test_start_whenAlreadyStarted() {
        Eventloop eventloop = createEventloop();
        eventloop.start();

        eventloop.start();
    }

    @Test(expected = IllegalStateException.class)
    public void test_start_whenAlreadyTerminated() throws InterruptedException {
        Eventloop eventloop = createEventloop();
        eventloop.start();
        eventloop.shutdown();
        eventloop.awaitTermination(5, SECONDS);

        eventloop.start();
    }

    @Test
    public void test_shutdown_whenNotStarted() {
        Eventloop eventloop = createEventloop();
        eventloop.shutdown();
        assertEquals(TERMINATED, eventloop.state());
    }

    @Test
    public void test_shutdown_whenRunning() throws InterruptedException {
        Eventloop eventloop = createEventloop();
        eventloop.start();

        eventloop.shutdown();

        assertTrue(eventloop.awaitTermination(1, SECONDS));
        assertEquals(TERMINATED, eventloop.state());
    }

    @Test
    public void test_shutdown_whenShuttingDown() throws InterruptedException {
        Eventloop eventloop = createEventloop();
        eventloop.start();

        CountDownLatch started = new CountDownLatch(1);
        eventloop.execute(() -> {
            started.countDown();
            Thread.sleep(1000);
        });

        started.await();
        eventloop.shutdown();

        eventloop.shutdown();
        assertTrue(eventloop.awaitTermination(1, SECONDS));
        assertEquals(TERMINATED, eventloop.state());
    }

    @Test
    public void test_shutdown_whenTerminated() {
        Eventloop eventloop = createEventloop();
        eventloop.shutdown();

        eventloop.shutdown();

        assertEquals(TERMINATED, eventloop.state());
    }


    @Test
    public void testLifecycle() throws InterruptedException {
        Eventloop eventloop = createEventloop();
        assertEquals(NEW, eventloop.state());

        eventloop.start();
        assertEquals(RUNNING, eventloop.state());

        CountDownLatch started = new CountDownLatch(1);
        eventloop.execute(() -> {
            started.countDown();
            Thread.sleep(2000);
        });

        started.countDown();
        eventloop.shutdown();
        assertEquals(SHUTDOWN, eventloop.state());

        assertTrue(eventloop.awaitTermination(5, SECONDS));
        assertEquals(TERMINATED, eventloop.state());
    }
}
