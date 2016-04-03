package ru.kwanza.jeda.timerservice.pushtimer.tx;

import org.springframework.stereotype.Repository;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClassRepository;
import ru.kwanza.jeda.timerservice.pushtimer.internalapi.ITimerManagerInternal;

import javax.annotation.Resource;

/**
 * @author Michael Yeskov
 */
@Repository
public class PendingTxTimersStore {

    @Resource
    private TimerClassRepository repository;

    public Tx getCurrentTx(ITimerManagerInternal timerManager) {
        return Tx.getTx(timerManager, repository);
    }

}
