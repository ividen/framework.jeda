package ru.kwanza.jeda.timerservice.pushtimer.tx;

import org.springframework.stereotype.Repository;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.internal.ITransactionManagerInternal;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClassRepository;
import ru.kwanza.jeda.timerservice.pushtimer.internalapi.ITimerManagerInternal;

import javax.annotation.Resource;
import javax.transaction.Transaction;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Michael Yeskov
 */
@Repository
public class PendingTxTimersStore {

    @Resource(name = "jeda.IJedaManager")
    private IJedaManager manager;

    @Resource
    private TimerClassRepository repository;


    protected ConcurrentMap<Transaction, Tx> transactions = new ConcurrentHashMap<Transaction, Tx>();

    private Transaction getJTATransaction() {
        ITransactionManagerInternal tm = manager.getTransactionManager();
        return tm == null ? null : tm.getTransaction();
    }


    public Tx getCurrentTx(ITimerManagerInternal timerManager) {
        Transaction transaction = getJTATransaction();
        if (transaction == null) {
            throw new IllegalStateException("TimerService must be accessed under transaction");
        }
        Tx tx = transactions.get(transaction);
        if (tx == null) {
            Tx newTx = new Tx(timerManager, this, transaction, repository);
            tx = transactions.putIfAbsent(transaction, newTx);
            if (tx == null) {
                tx = newTx;
                try {
                    transaction.registerSynchronization(tx);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return tx;
    }

    public void removeTx(Transaction jtaTrx) {
        transactions.remove(jtaTrx);
    }
}
