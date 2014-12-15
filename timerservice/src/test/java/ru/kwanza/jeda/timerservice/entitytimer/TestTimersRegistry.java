package ru.kwanza.jeda.timerservice.entitytimer;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import ru.kwanza.jeda.api.timerservice.entitytimer.IEntityTimerManager;
import ru.kwanza.jeda.timerservice.entitytimer.entity.*;
import ru.kwanza.toolbox.fieldhelper.Property;

import java.util.List;

/**
 * @author Michael Yeskov
 */

public class TestTimersRegistry extends Assert {

    private TimersRegistry timersRegistry;

    @Before
    public void before() {
        timersRegistry = new TimersRegistry();
    }

    private void internalSingleTimerTest(Object object) {
        List<TimerMapping> result = timersRegistry.getTimerMappings(IEntityTimerManager.DEFAULT_TIMER, object);

        assertEquals(1, result.size());
        assertEquals(IEntityTimerManager.DEFAULT_TIMER, result.get(0).getTimerName());
        assertEquals("timer", result.get(0).getPropertyName());

        Property entityProperty = result.get(0).getEntityProperty();

        assertEquals(null, entityProperty.value(object));
        entityProperty.set(object, 445L);
        assertEquals(1, timersRegistry.timerMappingCache.get(object.getClass()).size());
    }

    @Test
    public void testEntityWithTimer1() throws Exception {
        EntityWithTimer1 object = new EntityWithTimer1();
        internalSingleTimerTest(object);
        assertEquals(Long.valueOf(445), object.timer);
    }


    @Test
    public void testEntityWithTimer2() throws Exception {
        EntityWithTimer2 object = new EntityWithTimer2();
        internalSingleTimerTest(object);
        assertEquals(Long.valueOf(445), object.internalGetTimer());
    }
    @Test
    public void testEntityWithTimer3() throws Exception {
        EntityWithTimer3Child entityWithTimer3Child = new EntityWithTimer3Child();
        internalSingleTimerTest(entityWithTimer3Child);
        assertEquals(Long.valueOf(445), entityWithTimer3Child.internalGetTimer());

        EntityWithTimer4Child entityWithTimer4Child = new EntityWithTimer4Child();

        try {
            List<TimerMapping> result = timersRegistry.getTimerMappings(IEntityTimerManager.DEFAULT_TIMER, entityWithTimer4Child);
            fail("Default timer in EntityWithTimer4Child must be undefined");
        } catch (RuntimeException e) {
            if (!e.getMessage().contains("must be declared in")) {
                fail();
            }
        }

        List<TimerMapping> result = null;

        result = timersRegistry.getTimerMappings("timer1", entityWithTimer4Child);
        assertEquals("timer1", result.get(0).getPropertyName());

        result = timersRegistry.getTimerMappings("timer2", entityWithTimer4Child);
        assertEquals("timer2", result.get(0).getPropertyName());

        result = timersRegistry.getTimerMappings("timer3", entityWithTimer4Child);
        assertEquals("timer3", result.get(0).getPropertyName());

        result = timersRegistry.getTimerMappings("timer4", entityWithTimer4Child);
        assertEquals("timer4", result.get(0).getPropertyName());

        result = timersRegistry.getTimerMappings("timer5", entityWithTimer4Child);
        assertEquals("timer5", result.get(0).getPropertyName());

        result = timersRegistry.getTimerMappings("timer6", entityWithTimer4Child);
        assertEquals("timer6", result.get(0).getPropertyName());

        result = timersRegistry.getTimerMappings("timer7", entityWithTimer4Child);
        assertEquals("timer7", result.get(0).getPropertyName());

        result = timersRegistry.getTimerMappings("timer8", entityWithTimer4Child);
        assertEquals("timer8", result.get(0).getPropertyName());

        assertEquals(null, result.get(0).getEntityProperty().value(entityWithTimer4Child));
        result.get(0).getEntityProperty().set(entityWithTimer4Child, 111L);
        assertEquals(Long.valueOf(111), entityWithTimer4Child.getTimer8());
    }
    @Test
    public void testEntityWrongTimerType() throws Exception {
        EntityWrongTimerType1 entityWrongTimerType1 = new EntityWrongTimerType1();
        EntityWrongTimerType2 entityWrongTimerType2 = new EntityWrongTimerType2();

        try {
            List<TimerMapping> result = timersRegistry.getTimerMappings(IEntityTimerManager.DEFAULT_TIMER, entityWrongTimerType1);
            fail();
        } catch (RuntimeException e) {
            if (!e.getMessage().contains("must have java.lang.Long type")) {
                fail();
            }
        }


        try {
            List<TimerMapping> result = timersRegistry.getTimerMappings(IEntityTimerManager.DEFAULT_TIMER, entityWrongTimerType2);
            fail();
        } catch (RuntimeException e) {
            if (!e.getMessage().contains("must have java.lang.Long type")) {
                fail();
            }
        }
    }

    @Test
    public void testEntityDifferentTypesGetSet() throws Exception {
        EntityDifferentTypesGetSet entityDifferentTypesGetSet = new EntityDifferentTypesGetSet();
        List<TimerMapping> result = timersRegistry.getTimerMappings(IEntityTimerManager.DEFAULT_TIMER, entityDifferentTypesGetSet);
        Property property = result.get(0).getEntityProperty();
        assertEquals(null, property.value(entityDifferentTypesGetSet));
        property.set(entityDifferentTypesGetSet, Long.valueOf(123));
        assertEquals(Long.valueOf(123L), entityDifferentTypesGetSet.getField());



    }

    /*
    @Test
    public void testEntityDifferentTypesGetSet2() throws Exception {
        EntityDifferentTypesGetSet2 entityDifferentTypesGetSet2 = new EntityDifferentTypesGetSet2();
        try {
            List<TimerMapping> result = timersRegistry.getTimerMappings(IEntityTimerManager.DEFAULT_TIMER, entityDifferentTypesGetSet2);
            fail();
        }catch (RuntimeException e){
            if (!e.getMessage().contains("not found for Class:")) {
                fail();
            }
        }
    }*/


    @Test
    public void testEntityDuplicateTimerName() throws Exception {

        internalTestDuplicateTimerName(new EntityDuplicateTimerName1(), "default");

        internalTestDuplicateTimerName(new EntityDuplicateTimerName2(), "timer");

        internalTestDuplicateTimerName(new EntityDuplicateTimerName3(), "timer");

        internalTestDuplicateTimerName(new EntityDuplicateTimerName4Child(), "timer");

        internalTestDuplicateTimerName(new EntityDuplicateTimerName5Child(), "timer");
    }

    private void internalTestDuplicateTimerName(Object object, String timerName) {
        try {
            List<TimerMapping> result = timersRegistry.getTimerMappings(IEntityTimerManager.DEFAULT_TIMER, object);
            fail();
        } catch (RuntimeException e) {
            if (!e.getMessage().contains("@EntityTimer with name '" + timerName)) {
                fail();
            }
        }
    }
    @Test
    public void testDuplicateTimerOnProperty() throws Exception {
        internalTestDuplicateTimerOnProperty(new EntityDuplicateTimerOnProperty1Child(), "timer1");
        internalTestDuplicateTimerOnProperty(new EntityDuplicateTimerOnProperty2Child(), "timer1");
    }

    private void internalTestDuplicateTimerOnProperty(Object object, String propertyName) {
        try {
            List<TimerMapping> result = timersRegistry.getTimerMappings(IEntityTimerManager.DEFAULT_TIMER, object);
            fail();
        } catch (RuntimeException e) {
            if (!e.getMessage().contains("Property '" + propertyName +"' has duplicate @EntityTimer annotation for class")) {
                fail();
            }
        }
    }




}
