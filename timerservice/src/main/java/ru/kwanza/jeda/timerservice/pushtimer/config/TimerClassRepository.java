package ru.kwanza.jeda.timerservice.pushtimer.config;

import ru.kwanza.jeda.api.timerservice.pushtimer.manager.TimerHandle;
import ru.kwanza.jeda.timerservice.pushtimer.dao.IDBTimerDAO;

import java.util.*;

/**
 * @author Michael Yeskov
 */
public class TimerClassRepository {
    private Set<TimerClass> registeredTimerClasses;
    private Map<String, TimerClass> timerNameToClass = new HashMap<String, TimerClass>();

    private Set<IDBTimerDAO> allDAO = new HashSet<IDBTimerDAO>(); //just to check for duplicate "by reference"

    public TimerClassRepository(Set<TimerClass> registeredTimerClasses) {
        for  (TimerClass currentClass : registeredTimerClasses) {
            if (allDAO.contains(currentClass.getDbTimerDAO())) {
                throw new RuntimeException("One DAO was registered in two TimerClasses. " + currentClass);
            }
            allDAO.add(currentClass.getDbTimerDAO());
            for (String timerName  : currentClass.getCompatibleTimerNames()){
                if (timerNameToClass.containsKey(timerName)) {
                    throw new RuntimeException("One TimerName was registered in two TimerDAO. " + currentClass);
                }
                timerNameToClass.put(timerName, currentClass);
            }
        }
        this.registeredTimerClasses = registeredTimerClasses;
    }

    public Map<String, TimerClass> getTimerNameToClass() {
        return timerNameToClass;
    }

    public Set<TimerClass> getRegisteredTimerClasses(){
        return registeredTimerClasses;
    }

    public void check(Collection<? extends TimerHandle> timers) {
        for (TimerHandle current : timers) {
            if (timerNameToClass.get(current.getTimerName()) == null) {
                throw new RuntimeException("Timer with name " + current.getTimerName() + " has no mapping to timer class");
            }
        }

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
}
