package ru.kwanza.jeda.clusterservice.impl.db;

import junit.framework.Assert;
import mockit.Deencapsulation;
import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author Alexander Guzanov
 */
public class WorkerControllerTest {

    private WorkerController test;

    @Before
    public void init() {
        test = new WorkerController(5, 20, 100);

        test.init();
    }

    @After
    public void destroy() throws InterruptedException {
        test.destroy();
    }

    @Test
    public void testParams() {
        Assert.assertEquals(20, test.getAttemptInterval());
        Assert.assertEquals(5, test.getThreadCount());
        Assert.assertEquals(100, test.getKeepAlive());
    }

    @Test
    public void testStart() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final MockUp<IClusteredComponent> mockUp = new MockUp<IClusteredComponent>() {
            @Mock
            private String getName() {
                return "component_1";
            }

            @Mock(invocations = 1)
            public void handleStart() {
                latch.countDown();
            }

        };

        test.startComponent("1_component_1", mockUp.getMockInstance());
        Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testStartAttempts() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(10);
        final MockUp<IClusteredComponent> mockUp = new MockUp<IClusteredComponent>() {
            @Mock
            private String getName() {
                return "component_1";
            }

            @Mock(invocations = 10)
            public void handleStart() {
                latch.countDown();
                if (latch.getCount() != 0) {
                    throw new RuntimeException();
                }
            }

        };

        test.startComponent("1_component_1", mockUp.getMockInstance());
        Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testStartManyAttempts() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(100);
        final MockUp<IClusteredComponent> mockUp = new MockUp<IClusteredComponent>() {
            @Mock
            private String getName() {
                return "component_1";
            }

            @Mock(invocations = 100)
            public void handleStart() {
                latch.countDown();
                if (latch.getCount() != 0) {
                    throw new RuntimeException();
                }
            }
        };

        test.startComponent("1_component_1", mockUp.getMockInstance());
        Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
    }


    @Test
    public void testStartManyAttempts_Interrupted() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final MockUp<IClusteredComponent> mockUp = new MockUp<IClusteredComponent>() {
            @Mock
            private String getName() {
                return "component_1";
            }

            @Mock(invocations = 1)
            public void handleStart() {
                latch.countDown();
                throw new RuntimeException();
            }
        };

        Deencapsulation.setField(test, "attemptInterval", 10000000);
        test.startComponent("1_component_1", mockUp.getMockInstance());
        Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
        test.destroy();
    }


    @Test
    public void testStopingAfterStarting() throws InterruptedException {
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch waitingStopLatch = new CountDownLatch(1);
        final CountDownLatch stopLatch = new CountDownLatch(1);
        final MockUp<IClusteredComponent> mockUp = new MockUp<IClusteredComponent>() {
            @Mock
            private String getName() {
                return "component_1";
            }

            @Mock(invocations = 1)
            public void handleStart() {
                startLatch.countDown();
                try {
                    waitingStopLatch.await(10000, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            @Mock(invocations = 1)
            public void handleStop() {
                stopLatch.countDown();
            }
        };

        Deencapsulation.setField(test, "attemptInterval", 10000000);
        test.startComponent("1_component_1", mockUp.getMockInstance());
        Assert.assertTrue(startLatch.await(10000, TimeUnit.MILLISECONDS));
        test.stopComponent("1_component_1", mockUp.getMockInstance());
        waitingStopLatch.countDown();
        Assert.assertTrue(stopLatch.await(10000, TimeUnit.MILLISECONDS));
        test.destroy();
    }


    @Test
    public void testStartingAfterStopping() throws InterruptedException {
        final CountDownLatch startLatch = new CountDownLatch(1);
        final CountDownLatch secondStarting = new CountDownLatch(1);
        final CountDownLatch waitingStopLatch = new CountDownLatch(1);
        final CountDownLatch stopLatch = new CountDownLatch(1);
        final AtomicInteger iteration = new AtomicInteger(0);
        final MockUp<IClusteredComponent> mockUp = new MockUp<IClusteredComponent>() {
            @Mock
            private String getName() {
                return "component_1";
            }

            @Mock(invocations = 2)
            public void handleStart() {
                if(iteration.compareAndSet(0,1)) {
                    startLatch.countDown();
                    try {
                        waitingStopLatch.await(10000, TimeUnit.MILLISECONDS);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                }else{
                    secondStarting.countDown();
                }
            }

            @Mock(invocations = 1)
            public void handleStop() {
                stopLatch.countDown();
            }
        };

        Deencapsulation.setField(test, "attemptInterval", 10000000);
        test.startComponent("1_component_1", mockUp.getMockInstance());
        Assert.assertTrue(startLatch.await(10000, TimeUnit.MILLISECONDS));
        test.stopComponent("1_component_1", mockUp.getMockInstance());
        waitingStopLatch.countDown();
        Assert.assertTrue(stopLatch.await(10000, TimeUnit.MILLISECONDS));
        test.startComponent("1_component_1", mockUp.getMockInstance());
        Assert.assertTrue(secondStarting.await(10000, TimeUnit.MILLISECONDS));
        test.destroy();
    }


    @Test
    public void testStop() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final MockUp<IClusteredComponent> mockUp = new MockUp<IClusteredComponent>() {
            @Mock
            private String getName() {
                return "component_1";
            }

            @Mock(invocations = 1)
            public void handleStop() {
                latch.countDown();
            }

        };

        test.stopComponent("1_component_1", mockUp.getMockInstance());
        Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testStopAttempts() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(10);
        final MockUp<IClusteredComponent> mockUp = new MockUp<IClusteredComponent>() {
            @Mock
            private String getName() {
                return "component_1";
            }

            @Mock(invocations = 10)
            public void handleStop() {
                latch.countDown();
                if (latch.getCount() != 0) {
                    throw new RuntimeException();
                }
            }

        };

        test.stopComponent("1_component_1", mockUp.getMockInstance());
        Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testStopManyAttempts() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(100);
        final MockUp<IClusteredComponent> mockUp = new MockUp<IClusteredComponent>() {
            @Mock
            private String getName() {
                return "component_1";
            }

            @Mock(invocations = 100)
            public void handleStop() {
                latch.countDown();
                if (latch.getCount() != 0) {
                    throw new RuntimeException();
                }
            }
        };

        test.stopComponent("1_component_1", mockUp.getMockInstance());
        Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
    }


    @Test
    public void testStopManyAttempts_Interrupted() throws InterruptedException {
        final CountDownLatch latch = new CountDownLatch(1);
        final MockUp<IClusteredComponent> mockUp = new MockUp<IClusteredComponent>() {
            @Mock
            private String getName() {
                return "component_1";
            }

            @Mock(invocations = 1)
            public void handleStop() {
                latch.countDown();
                throw new RuntimeException();
            }
        };

        Deencapsulation.setField(test, "attemptInterval", 10000000);
        test.stopComponent("1_component_1", mockUp.getMockInstance());
        Assert.assertTrue(latch.await(10000, TimeUnit.MILLISECONDS));
        test.destroy();
    }


    @Test
    public void testNotStartedNoStop() throws InterruptedException {
        final CountDownLatch latch1 = new CountDownLatch(1);
        final MockUp<IClusteredComponent> mockUp = new MockUp<IClusteredComponent>() {
            @Mock
            private String getName() {
                return "component_1";
            }

            @Mock
            public void handleStart() {
                latch1.countDown();
                throw new RuntimeException();
            }

            @Mock(invocations = 0)
            public void handleStop() {
            }

        };

        test.startComponent("1_component_1", mockUp.getMockInstance());
        Assert.assertTrue(latch1.await(10000, TimeUnit.MILLISECONDS));
        test.stopComponent("1_component_1", mockUp.getMockInstance());
    }


    @Test
    public void tesNoStartingIfNotStopped() throws InterruptedException {
        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(1);
        final CountDownLatch latch3 = new CountDownLatch(1);
        final CountDownLatch latch4 = new CountDownLatch(1);
        final MockUp<IClusteredComponent> mockUp = new MockUp<IClusteredComponent>() {
            @Mock
            private String getName() {
                return "component_1";
            }

            @Mock(invocations = 1)
            public void handleStart() {
                latch1.countDown();
            }

            @Mock(invocations = 1)
            public void handleStop() {
                if(latch2.getCount()>0) {
                    latch2.countDown();
                    try {
                        Assert.assertTrue(latch3.await(10000, TimeUnit.MILLISECONDS));
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    latch4.countDown();
                    throw new RuntimeException();
                }

            }

        };

        test.startComponent("1_component_1", mockUp.getMockInstance());
        Assert.assertTrue(latch1.await(10000, TimeUnit.MILLISECONDS));
        test.stopComponent("1_component_1", mockUp.getMockInstance());
        Assert.assertTrue(latch2.await(10000, TimeUnit.MILLISECONDS));
        latch3.countDown();
        test.startComponent("1_component_1", mockUp.getMockInstance());
        Assert.assertTrue(latch4.await(10000, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testStartNoStopStart() throws InterruptedException {
        final CountDownLatch latch1 = new CountDownLatch(100);
        final CountDownLatch latch2 = new CountDownLatch(1);
        final AtomicReference<Boolean> stopped = new AtomicReference<Boolean>(false);
        final MockUp<IClusteredComponent> mockUp = new MockUp<IClusteredComponent>() {
            @Mock
            private String getName() {
                return "component_1";
            }

            @Mock(invocations = 100)
            public void handleStart() {
                latch2.countDown();
                latch1.countDown();
                if (latch1.getCount() > 0) {
                    throw new RuntimeException();
                }
            }

            @Mock(invocations = 0)
            public void handleStop() {
                stopped.set(true);
            }

        };

        test.startComponent("1_component_1", mockUp.getMockInstance());
        Assert.assertTrue(latch2.await(1000000, TimeUnit.MILLISECONDS));
        test.stopComponent("1_component_1", mockUp.getMockInstance());
        test.startComponent("1_component_1", mockUp.getMockInstance());
        Assert.assertTrue(latch1.await(1000000, TimeUnit.MILLISECONDS));
        Assert.assertFalse(stopped.get());
    }


    @Test
    public void testStartRepair(@Mocked final Node node1, @Mocked final Node node2) throws InterruptedException {

        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(1);

        final MockUp<IClusteredComponent> mockUp1 = new MockUp<IClusteredComponent>() {
            @Mock
            private String getName() {
                return "component_1";
            }

            @Mock(invocations = 1)
            public void handleStartRepair(Node node) {
                latch1.countDown();
            }

        };

        final MockUp<IClusteredComponent> mockUp2 = new MockUp<IClusteredComponent>() {
            @Mock
            private String getName() {
                return "component_1";
            }

            @Mock(invocations = 1)
            public void handleStartRepair(Node node) {
                latch2.countDown();
            }

        };

        test.startRepair("1_component_1", mockUp1.getMockInstance(), node1);
        test.startRepair("2_component_1", mockUp2.getMockInstance(), node2);
        Assert.assertTrue(latch1.await(1000000, TimeUnit.MILLISECONDS));
        Assert.assertTrue(latch2.await(1000000, TimeUnit.MILLISECONDS));
    }

    @Test
    public void testStopRepair(@Mocked final Node node1, @Mocked final Node node2) throws InterruptedException {

        final CountDownLatch latch1 = new CountDownLatch(1);
        final CountDownLatch latch2 = new CountDownLatch(1);

        final MockUp<IClusteredComponent> mockUp1 = new MockUp<IClusteredComponent>() {
            @Mock
            private String getName() {
                return "component_1";
            }

            @Mock(invocations = 1)
            public void handleStopRepair(Node node) {
                latch1.countDown();
            }

        };

        final MockUp<IClusteredComponent> mockUp2 = new MockUp<IClusteredComponent>() {
            @Mock
            private String getName() {
                return "component_1";
            }

            @Mock(invocations = 1)
            public void handleStopRepair(Node node) {
                latch2.countDown();
            }

        };

        test.stopRepair("1_component_1", mockUp1.getMockInstance(), node1);
        test.stopRepair("2_component_1", mockUp2.getMockInstance(), node2);
        Assert.assertTrue(latch1.await(1000000, TimeUnit.MILLISECONDS));
        Assert.assertTrue(latch2.await(1000000, TimeUnit.MILLISECONDS));
    }


}
