package ru.kwanza.jeda.persistentqueue;

import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.clusterservice.IClusterService;
import ru.kwanza.jeda.clusterservice.IClusteredModule;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.persistentqueue.old.EventWithKey;

import javax.transaction.Status;
import javax.transaction.Synchronization;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Guzanov Alexander
 */
public abstract class QueuePersistenceController implements IClusteredModule {
    private PersistentQueue queue;
    private int repairIterationItemCount = 100;
    private AtomicLong expectedRepairing = new AtomicLong(0l);
    protected IClusterService clusterService;
    private IJedaManager manager;

    protected QueuePersistenceController(IJedaManager manager, IClusterService clusterService) {
        this.manager = manager;
        this.clusterService = clusterService;
    }

    public abstract void delete(Collection<EventWithKey> result);

    public abstract Collection<EventWithKey> load(int count);

    public abstract void persist(Collection<EventWithKey> events);

    protected abstract int transfer(int count, Node currentNode, Node repairableNode);

    public int getRepairIterationItemCount() {
        return repairIterationItemCount;
    }

    public void setRepairIterationItemCount(int repairIterationItemCount) {
        this.repairIterationItemCount = repairIterationItemCount;
    }

    public String getName() {
        return "jeda.QueuePersistenceController." + getClusteredQueueName();
    }

    protected abstract String getClusteredQueueName();

    void init(PersistentQueue queue) {
        this.queue = queue;
        clusterService.registerModule(this);
    }

    public void handleStart() {
        queue.start();
    }

    public void handleStop() {
        queue.stop();
    }

    public boolean handleRepair(Node currentNode, Node reparableNode) {
        int count = transfer(getRepairIterationItemCount(), currentNode, currentNode);
        expectedRepairing.addAndGet(count);
        return count < getRepairIterationItemCount();
    }

    public Collection<EventWithKey> repair(final int count) {
        Collection<EventWithKey> result = load(count);
        try {
            manager.getTransactionManager().getTransaction().registerSynchronization(new ExpectedRepairingSync(result.size()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return result;
    }

    public int getExpectedRepairing() {
        return expectedRepairing.intValue();
    }

    private class ExpectedRepairingSync implements Synchronization {
        private final int size;

        public ExpectedRepairingSync(int size) {
            this.size = size;
        }

        public void beforeCompletion() {

        }

        public void afterCompletion(int i) {
            if (i == Status.STATUS_COMMITTED) {
                expectedRepairing.addAndGet(size);
            }
        }
    }
}
