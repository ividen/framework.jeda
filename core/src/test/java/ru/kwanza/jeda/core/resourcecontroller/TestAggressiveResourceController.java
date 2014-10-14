package ru.kwanza.jeda.core.resourcecontroller;

/**
 * @author Guzanov Alexander
 */
public class TestAggressiveResourceController/* extends TestCase*/ {
//    public void testLineEncrease() throws InterruptedException {
//        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml",
//                TestSmoothResourceController.class);
//        DefaultSystemManager systemManager = ctx.getBean("ru.kwanza.jeda.api.ISystemManager",
//                DefaultSystemManager.class);
//
//        TestStage stage = new TestStage();
//        stage.setEstiatedCount(1000);
//
//        AggressiveResourceController rc = new AggressiveResourceController(1000);
//        rc.initStage(stage);
//        rc.setMaxBatchSize(1000000);
//
//        int batchSize = 0;
//        for (int i = 0; i < 3; i++) {
//            rc.input(500 + (int) (500 * i * 0.05d));
//            rc.input(500 + (int) (500 * i * 0.05d));
//            Thread.sleep(1000);
//            rc.throughput(rc.getBatchSize(),batchSize,1000, true);
//        }
//
//        Thread.sleep(1000);
//        int batchSize1 = rc.getBatchSize();
//        rc.throughput(batchSize1, batchSize1,1000, true);
//        Thread.sleep(1000);
//        rc.throughput(rc.getBatchSize(),batchSize1, 1000, true );
//
//        assertTrue(rc.getRate() > rc.getRate());
//        assertTrue(batchSize1 < rc.getBatchSize());
//    }
//
//
//    public void testLineEncrease_throuputlimit() throws InterruptedException {
//        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml",
//                TestSmoothResourceController.class);
//        DefaultSystemManager systemManager = ctx.getBean("ru.kwanza.jeda.api.ISystemManager",
//                DefaultSystemManager.class);
//
//        TestStage stage = new TestStage();
//        stage.setEstiatedCount(1000);
//
//        AggressiveResourceController rc = new AggressiveResourceController(1000);
//        rc.setMaxBatchSize(1000000);
//        rc.initStage(stage);
//
//        for (int i = 0; i < 5; i++) {
//            rc.input(500 + (int) (500 * i * 0.05d));
//            rc.input(500 + (int) (500 * i * 0.05d));
//            rc.input(500 + (int) (500 * i * 0.05d));
//            Thread.sleep(1000);
//            if (i % 2 != 0) {
//                rc.throughput(rc.getBatchSize(),rc.getBatchSize(), 1000, true);
//            }
//        }
//
//        assertTrue(rc.getRate() < rc.getRate());
//    }
//
//    public void testThrouhgPut_NotSucess() throws InterruptedException {
//        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml",
//                TestSmoothResourceController.class);
//        DefaultSystemManager systemManager = ctx.getBean("ru.kwanza.jeda.api.ISystemManager",
//                DefaultSystemManager.class);
//
//        TestStage stage = new TestStage();
//        stage.setEstiatedCount(1000);
//
//        AggressiveResourceController rc = new AggressiveResourceController(1000);
//        rc.setMaxBatchSize(1000000);
//        rc.initStage(stage);
//
//        for (int i = 0; i < 2; i++) {
//            rc.input(500 + (int) (500 * i * 0.05d));
//            rc.input(500 + (int) (500 * i * 0.05d));
//            rc.input(500 + (int) (500 * i * 0.05d));
//            Thread.sleep(1000);
//
//            rc.throughput(rc.getBatchSize(), rc.getBatchSize(),1000, false);
//        }
//
//        assertTrue(rc.getRate() < rc.getRate());
//        assertTrue(rc.getRate() < 0.0000001d);
//    }
//
//
//    public void testLineEncrease_throughputtime_limit() throws InterruptedException {
//        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml",
//                TestSmoothResourceController.class);
//        DefaultSystemManager systemManager = ctx.getBean("ru.kwanza.jeda.api.ISystemManager",
//                DefaultSystemManager.class);
//
//        TestStage stage = new TestStage();
//        stage.setEstiatedCount(1000);
//
//        AggressiveResourceController rc = new AggressiveResourceController(1000);
//        rc.setMaxBatchSize(1000000);
//        rc.initStage(stage);
//        rc.setBatchProcessingTimeThreshold(2000);
//
//        for (int i = 0; i < 5; i++) {
//            rc.input(500 + (int) (500 * i * 0.05d));
//            rc.input(500 + (int) (500 * i * 0.05d));
//            rc.input(500 + (int) (500 * i * 0.05d));
//            rc.input(500 + (int) (500 * i * 0.05d));
//            Thread.sleep(1000);
//            rc.throughput(rc.getBatchSize(),rc.getBatchSize(), rc.getBatchSize() > 1300 ? 3000 : 1000, true);
//        }
//
//        assertTrue(rc.getRate() < rc.getRate());
//    }
//
//    public void testLineEncrease_maxbatchSize_limit() throws InterruptedException {
//        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml",
//                TestSmoothResourceController.class);
//        DefaultSystemManager systemManager = ctx.getBean("ru.kwanza.jeda.api.ISystemManager",
//                DefaultSystemManager.class);
//
//        TestStage stage = new TestStage();
//        stage.setEstiatedCount(1000);
//
//        AggressiveResourceController rc = new AggressiveResourceController(1000);
//        rc.initStage(stage);
//        rc.setMaxBatchSize(3500);
//        rc.setBatchProcessingTimeThreshold(2000);
//
//        for (int i = 0; i < 5; i++) {
//            rc.input(500 + (int) (500 * i * 0.05d));
//            rc.input(500 + (int) (500 * i * 0.05d));
//            rc.input(500 + (int) (500 * i * 0.05d));
//            rc.input(500 + (int) (500 * i * 0.05d));
//            rc.input(500 + (int) (500 * i * 0.05d));
//            rc.input(500 + (int) (500 * i * 0.05d));
//            rc.input(500 + (int) (500 * i * 0.05d));
//            Thread.sleep(1000);
//            rc.throughput(rc.getBatchSize(),rc.getBatchSize(), 1000, true);
//        }
//
//        assertTrue(rc.getRate() < rc.getRate());
//    }
//
//    public void testDefaultConstructor() {
//        IResourceController rc = new AggressiveResourceController();
//        assertEquals(Util.DEFAULT_START_BATCH_SIZE, rc.getBatchSize());
//        assertEquals(Util.DEFAULT_MAX_BATCH_PROCESSING_THRESHOLD, ((AggressiveResourceController) rc).getBatchProcessingTimeThreshold());
//    }
//
//    public void testLineEncrease_throuputlimit_slowinc_dec() throws InterruptedException {
//        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml",
//                TestSmoothResourceController.class);
//        DefaultSystemManager systemManager = ctx.getBean("ru.kwanza.jeda.api.ISystemManager",
//                DefaultSystemManager.class);
//
//        TestStage stage = new TestStage();
//        stage.setEstiatedCount(1000);
//
//        AggressiveResourceController rc = new AggressiveResourceController(1);
//        rc.initStage(stage);
//        rc.setMaxBatchSize(1000000);
//        rc.setBatchProcessingTimeThreshold(2000);
//
//
//        for (int i = 0; i < 6; i++) {
//            rc.input((i + 1) * 1);
//            Thread.sleep(1000);
//            if (i > 2) {
//                rc.throughput(rc.getBatchSize(), rc.getBatchSize(),3000, true);
//            } else {
//                rc.throughput(rc.getBatchSize(),rc.getBatchSize(), 1000, true);
//            }
//
//        }
//        assertTrue(rc.getRate() < rc.getRate());
//        assertTrue(rc.getBatchSize() == 1);
//    }
//
//
//    public void testThreadCountAdjust() throws InterruptedException {
//        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml",
//                TestSmoothResourceController.class);
//        DefaultSystemManager systemManager = ctx.getBean("ru.kwanza.jeda.api.ISystemManager",
//                DefaultSystemManager.class);
//
//        TestStage stage = new TestStage();
//        stage.setEstiatedCount(10000);
//
//        AggressiveResourceController rc = new AggressiveResourceController(1000);
//        rc.initStage(stage);
//        rc.setMaxBatchSize(100000);
//        rc.setMaxThreadCount(5);
//        assertEquals(rc.getMaxThreadCount(), 5);
//
//
//        rc.input(1000);
//        assertEquals(5, rc.getThreadCount());
//        rc.throughput(1000, rc.getBatchSize(),1000, true);
//        stage.setEstiatedCount(stage.getEstimatedCount() - 1000);
//        assertEquals(5, rc.getThreadCount());
//        Thread.sleep(1000);
//        rc.throughput(5000,rc.getBatchSize(), 1000, true);
//        stage.setEstiatedCount(stage.getEstimatedCount() - 5000);
//        assertEquals(5, rc.getThreadCount());
//
//        Thread.sleep(1000);
//        rc.throughput(1000,rc.getBatchSize(), 1000, true);
//        stage.setEstiatedCount(stage.getEstimatedCount() - 1000);
//        assertEquals(3, rc.getThreadCount());
//
//        Thread.sleep(1000);
//        rc.throughput(1000,rc.getBatchSize(), 1000, true);
//        stage.setEstiatedCount(stage.getEstimatedCount() - 1000);
//        assertEquals(3, rc.getThreadCount());
//
//
//        Thread.sleep(1000);
//        rc.throughput(1000, rc.getBatchSize(),1000, true);
//        stage.setEstiatedCount(stage.getEstimatedCount() - 1000);
//        assertEquals(2, rc.getThreadCount());
//
//        Thread.sleep(1000);
//        rc.throughput(1000,rc.getBatchSize(), 1000, true);
//        stage.setEstiatedCount(stage.getEstimatedCount() - 1000);
//        assertEquals(1, rc.getThreadCount());
//    }
}
