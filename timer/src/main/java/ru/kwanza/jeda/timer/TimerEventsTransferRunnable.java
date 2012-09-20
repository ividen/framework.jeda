package ru.kwanza.jeda.timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Guzanov Alexander
 */
class TimerEventsTransferRunnable implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(TimerEventsTransferRunnable.class);

    private AbstractTimer timer;
    private long lastActivity;
    private long nodeId;

    public TimerEventsTransferRunnable(AbstractTimer timer, long nodeId, long lastActivity) {
        this.timer = timer;
        this.nodeId = nodeId;
        this.lastActivity = lastActivity;
    }

    public void run() {
//        while (true) {
//            if (ClusterService.getLastNodeActivity(nodeId) > lastActivity) {
//                logger.info("Node(nodeId={}) reactivated skip transfer", nodeId);
//                break;
//            }
//            timer.waitForFreeSlots();
//            try {
//                Manager.getTM().begin();
//                boolean transfer = timer.transfer(nodeId);
//                Manager.getTM().commit();
//                if (transfer) {
//                    break;
//                }
//            } catch (SinkException.Closed e) {
//                logger.warn("Queue is closed, skip transfer", e);
//                Manager.getTM().rollback();
//                break;
//            } catch (Throwable e) {
//                logger.error("Queue is closed, transfer failed!", e);
//                Manager.getTM().rollback();
//            }
//        }
    }
}
