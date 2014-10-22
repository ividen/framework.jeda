package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.clusterservice.old.ClusterService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Guzanov Alexander
 */
class QueueEventsTransferRunnable implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(QueueEventsTransferRunnable.class);

    private PersistentQueue persistentQueue;
    private long lastActivity;
    private long nodeId;

    public QueueEventsTransferRunnable(PersistentQueue persistentQueue, long nodeId, long lastActivity) {
        this.persistentQueue = persistentQueue;
        this.nodeId = nodeId;
        this.lastActivity = lastActivity;
    }

    public void run() {
        while (true) {
            if (ClusterService.getLastNodeActivity(nodeId) > lastActivity) {
                logger.info("Node(nodeId={}) reactivated skip transfer", nodeId);
                break;
            }
            try {
                persistentQueue.waitForFreeSlots();
            } catch (SinkException.Closed e) {
                logger.warn("Queue is closed, skip transfer", e);
                break;
            }
            try {
                persistentQueue.getManager().getTransactionManager().begin();
                boolean transfer = persistentQueue.transfer(nodeId);
                persistentQueue.getManager().getTransactionManager().commit();
                if (transfer) {
                    break;
                }
            } catch (SinkException.Closed e) {
                logger.warn("Queue is closed, skip transfer", e);
                persistentQueue.getManager().getTransactionManager().rollback();
                break;
            } catch (Throwable e) {
                logger.error("Queue is closed, transfer failed!", e);
                persistentQueue.getManager().getTransactionManager().rollback();
            }
        }
    }
}
