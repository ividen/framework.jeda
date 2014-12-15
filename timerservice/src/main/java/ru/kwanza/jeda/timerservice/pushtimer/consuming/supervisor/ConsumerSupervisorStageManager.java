package ru.kwanza.jeda.timerservice.pushtimer.consuming.supervisor;

import org.springframework.stereotype.Service;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.consuming.NodeId;
import ru.kwanza.txn.api.Transactional;
import ru.kwanza.txn.api.TransactionalType;

import javax.annotation.Resource;

/**
 * Работает прозрачно с repair и не repair компонентами
 * @author Michael Yeskov
 */
@Service
public class ConsumerSupervisorStageManager {

    @Resource
    private ConsumerSupervisorDAO supervisorDAO;

    @Transactional(TransactionalType.REQUIRES_NEW)
    public long getFailoverLeftBorder(TimerClass timerClass, NodeId nodeId) {
        String className = timerClass.getTimerClassName();
        if (nodeId.getRepairedNodeId() == null) {
            Long currentLeftBorder = supervisorDAO.select(className, nodeId);
            if (currentLeftBorder == null) {
                currentLeftBorder = 1L;
                supervisorDAO.insert(className, nodeId, currentLeftBorder);
            }
            return currentLeftBorder;
        } else {
            Long maxCurrentBorder = supervisorDAO.selectMaxFailoverLeftBorder(className, nodeId.getEffectiveNodeId());
            if (maxCurrentBorder == null) {
                maxCurrentBorder = 1L;
            }
            Long currentLeftBorder = supervisorDAO.select(className, nodeId);
            if (currentLeftBorder == null) {
                supervisorDAO.insert(className, nodeId, maxCurrentBorder);
            } else {
                if (!currentLeftBorder.equals(maxCurrentBorder)) {
                    supervisorDAO.update(className, nodeId, maxCurrentBorder);
                }
            }
            return  maxCurrentBorder;
        }
    }

    @Transactional(TransactionalType.REQUIRES_NEW)
    public void updateFailoverLeftBorder(TimerClass timerClass, NodeId nodeId, long failoverLeftBorder) {
        String className = timerClass.getTimerClassName();
        Long currentLeftBorder = supervisorDAO.select(className, nodeId);
        if (currentLeftBorder == null) {
            supervisorDAO.insert(className, nodeId, failoverLeftBorder);
        } else {
            supervisorDAO.update(className, nodeId, failoverLeftBorder);
        }
    }
}
