package ru.kwanza.jeda.timerservice.entitytimer;

import org.springframework.stereotype.Repository;
import ru.kwanza.jeda.api.entitytimer.EntityTimer;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;
import ru.kwanza.toolbox.fieldhelper.Property;

import java.lang.reflect.AnnotatedElement;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Michael Yeskov
 */
@Repository
public class TimersRegistry implements ITimersRegistry {

    ConcurrentHashMap<Class, Map<String, EntityTimerMapping>> timerMappingCache = new ConcurrentHashMap<Class, Map<String, EntityTimerMapping>>();

    public List<EntityTimerMapping>  getTimerMappings(String timerName, Object... entityWithTimer) {
        List<EntityTimerMapping> result = new ArrayList<EntityTimerMapping>();
        for (Object current : entityWithTimer) {
            Map <String, EntityTimerMapping> classTimers = timerMappingCache.get(current.getClass());
            if (classTimers == null) {
                classTimers =  new HashMap<String, EntityTimerMapping>();
                Set<String> entityPropertySet = new HashSet<String>();
                fillClassTimers(current.getClass(), classTimers, entityPropertySet, current.getClass());
                Map<String, EntityTimerMapping> oldValue = timerMappingCache.putIfAbsent(current.getClass(), classTimers);
                if (oldValue != null) {
                    classTimers = oldValue;
                }
            }

            EntityTimerMapping timerMapping = classTimers.get(timerName);
            if (timerMapping == null) {
                throw new RuntimeException("@EntityTimer with name = '" + timerName + "' must be declared in " + current.getClass().getName());
            }
            result.add(timerMapping);
        }
        return result;
    }

    private void fillClassTimers(Class clazz, Map<String, EntityTimerMapping> classTimers, Set<String> entityPropertySet, Class startClass) {

        parseAnnotatedElements(clazz, clazz.getDeclaredFields(), classTimers, entityPropertySet, startClass);
        parseAnnotatedElements(clazz, clazz.getDeclaredMethods(), classTimers, entityPropertySet, startClass);

        if (clazz.getSuperclass() != null) {
            fillClassTimers(clazz.getSuperclass(), classTimers, entityPropertySet, startClass);
        }
    }

    private void parseAnnotatedElements(Class clazz, AnnotatedElement[] annotatedElements, Map<String, EntityTimerMapping> classTimers, Set<String> entityPropertySet, Class startClass) {

        for (AnnotatedElement current : annotatedElements){
            if (current.isAnnotationPresent(EntityTimer.class)) {
                String propertyName = EntityMappingHelper.getPropertyName(current);
                Property entityProperty = FieldHelper.constructProperty(clazz, propertyName);
                String timerName = current.getAnnotation(EntityTimer.class).name();
                if (entityPropertySet.contains(propertyName)) {
                    throw new RuntimeException("Property '" + propertyName +  "' has duplicate @EntityTimer annotation for class '" + startClass.getName() + "'");
                }
                if (classTimers.containsKey(timerName)) {
                    throw new RuntimeException("@EntityTimer with name '" + timerName +"' already defined for class '" + startClass.getName() + "'");
                }
                if (!entityProperty.getType().equals(Long.class)) {
                    throw new RuntimeException("Property '" + propertyName + "' must have java.lang.Long type to be used with @EntityTimer annotation");
                }
                entityPropertySet.add(propertyName);
                classTimers.put(timerName, new EntityTimerMapping(timerName, propertyName, entityProperty));
            }
        }
    }





}
