package ru.kwanza.jeda.timerservice.pushtimer.config;

import ru.kwanza.jeda.api.pushtimer.manager.TimerHandle;
import ru.kwanza.jeda.timerservice.pushtimer.dao.IDBTimerDAO;

import java.util.*;

/**
 * @author Michael Yeskov
 */
public class TimerClassRepository{
    private Set<TimerClass> registeredTimerClasses = new HashSet<TimerClass>();
    private Map<String, TimerClass> timerNameToClass = new HashMap<String, TimerClass>();

    private Set<IDBTimerDAO> allDAO = new HashSet<IDBTimerDAO>(); //just to check for duplicate "by reference"

    public Map<String, TimerClass> getTimerNameToClass() {
        return timerNameToClass;
    }

    public Set<TimerClass> getRegisteredTimerClasses(){
        return registeredTimerClasses;
    }

    public TimerClass getClassByTimerName(String timerName) {
        TimerClass timerClass = timerNameToClass.get(timerName);
        if (timerClass == null) {
            throw new RuntimeException("TimerClass is not configured for timerName = " + timerName);
        }
        return timerClass;
    }

    public Map<TimerClass, Set<? extends TimerHandle>> splitByClass(Collection<? extends TimerHandle> timers) {
        Map<TimerClass, Set<TimerHandle>> result = new HashMap<TimerClass, Set<TimerHandle>>();

        for (TimerHandle current : timers) {
            TimerClass currentClass =  timerNameToClass.get(current.getTimerName());
            if (currentClass == null) {
               throw new RuntimeException("Timer with name " + current.getTimerName() + " has no mapping to timer class");
            }
            Set<TimerHandle> currentSet = result.get(currentClass);
            if (currentSet == null) {
                currentSet = new HashSet<TimerHandle>();
                result.put(currentClass, currentSet);
            }
            currentSet.add(current);
        }

        return (Map)result;
    }

    public void registerNameToClassBinding(String timerName, TimerClass timerClass) {
        if (!registeredTimerClasses.contains(timerClass)) {
            if (allDAO.contains(timerClass.getDbTimerDAO())) {
                throw new RuntimeException("One DAO was registered in two TimerClasses. TimerClass = " + timerClass);
            }
            allDAO.add(timerClass.getDbTimerDAO());
            registeredTimerClasses.add(timerClass);
        }

        if (timerNameToClass.containsKey(timerName)) {
            throw new RuntimeException("Timer with name =   "+ timerName + " already registered");
        }

        timerClass.registerCompatibleTimer(timerName);
        timerNameToClass.put(timerName, timerClass);
    }
}
