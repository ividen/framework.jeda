package ru.kwanza.jeda.clusterservice.db;

import ru.kwanza.jeda.clusterservice.ClusterService;
import ru.kwanza.jeda.clusterservice.INodeListener;
import junit.framework.TestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * Date: 26.01.12-16:33
 *
 * @author dkostochko
 */
public class NodeMonitorTest extends TestCase {
    private static final Logger logger = LoggerFactory.getLogger(NodeMonitorTest.class);

    public void testClusterMonitor() throws Exception {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml", NodeMonitorTest.class);
        //NodeMonitor monitor = new NodeMonitor();
        ClusterService clusterService = ctx.getBean(ClusterService.class);
        ClusterService.subscribe(new NodeMonitor());
        assertNotNull(clusterService);
        Thread.sleep(250000);
    }

    private class NodeMonitor implements INodeListener {
        public void onNodeLost(Long nodeId, long lastNodeTs) {
            logger.warn("Node lost {}", nodeId);
        }

        public void onNodeActivate(Long nodeId, long lastNodeTs) {
            logger.warn("Node activate {}", nodeId);
        }

        public void onCurrentNodeActivate() {
            logger.warn("Current node activate");
        }

        public void onCurrentNodeLost() {
            logger.warn("Current node lost");
        }
    }
}
