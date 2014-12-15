package ru.kwanza.jeda.timerservice.pushtimer.monitoring.mbeans;

import org.springframework.stereotype.Component;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClassRepository;
import ru.kwanza.jeda.timerservice.pushtimer.monitoring.JMXRegistry;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.*;

/**
 * @author Michael Yeskov
 */
@Component
public class TimerClassRepositoryMonitoring implements TimerClassRepositoryMonitoringMBean{

    @Resource
    private TimerClassRepository patient;
    @Resource
    private JMXRegistry jmxRegistry;

    @PostConstruct
    public void init(){
        jmxRegistry.registerInTotal(TimerClassRepositoryMonitoring.class.getSimpleName(), this);
    }

    @Override
    public List<String> getTimerClassToTimerNames() {
        List<String> result = new ArrayList<String>();
        for (Map.Entry<String, TimerClass> entry : patient.getTimerNameToClass().entrySet()){
            result.add("class = " + entry.getValue().getTimerClassName() + " timer = " + entry.getKey());
        }
        Collections.sort(result);
        return result;
    }
}
