package ru.kwanza.jeda.clusterservice.impl.db;

import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.impl.db.orm.AlienComponent;
import ru.kwanza.jeda.clusterservice.impl.db.orm.ComponentEntity;
import ru.kwanza.toolbox.fieldhelper.FieldHelper;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author Alexander Guzanov
 */
public class ComponentRepository {
    private ConcurrentMap<String, IClusteredComponent> components = new ConcurrentHashMap<String, IClusteredComponent>();
    private ConcurrentMap<String, ComponentEntry> activeComponents = new ConcurrentHashMap<String, ComponentEntry>();
    private ConcurrentMap<String, ComponentEntry> passiveCoomponents = new ConcurrentHashMap<String, ComponentEntry>();
    private ConcurrentMap<String, AlienComponent> alienComponent = new ConcurrentHashMap<String, AlienComponent>();
    private ConcurrentMap<String,AlienComponent>  stopRepair = new ConcurrentHashMap<String,AlienComponent>();
    private ConcurrentMap<String,AlienComponent>  stopingRepair = new ConcurrentHashMap<String,AlienComponent>();


    public Map<String, IClusteredComponent> getComponents() {
        return Collections.unmodifiableMap(components);
    }

    public void registerComponent(IClusteredComponent component) {
        if (components.putIfAbsent(component.getName(), component) != null) {
            throw new IllegalStateException("Component " + component.getName() + " already exists! Can't register!");
        }
    }

    public Map<String, IClusteredComponent> getActiveComponents() {
        return FieldHelper.getValueFieldMap(activeComponents, ComponentEntry.componentField);
    }

    public boolean isActive(String name) {
        return activeComponents.containsKey(name);
    }

    public Map<String, IClusteredComponent> getPassiveComponents() {
        return FieldHelper.getValueFieldMap(passiveCoomponents, ComponentEntry.componentField);
    }

    public Collection<ComponentEntity> getActiveEntities() {
        return FieldHelper.getFieldCollection(activeComponents.values(), ComponentEntry.entityField);
    }

    public Collection<ComponentEntity> getPassiveEntities() {
        return FieldHelper.getFieldCollection(passiveCoomponents.values(), ComponentEntry.entityField);
    }

    public Map<String, AlienComponent> getAlienEntities() {
        return Collections.unmodifiableMap(alienComponent);
    }

    public Map<String, AlienComponent> getStopRepairEntities() {
        return Collections.unmodifiableMap(stopRepair);
    }

    public Map<String, AlienComponent> getStopigRepairEntities() {
        return Collections.unmodifiableMap(stopingRepair);
    }

    public void addActiveComponent(ComponentEntity componentEntity) {
        activeComponents.put(componentEntity.getName(), new ComponentEntry(components.get(componentEntity.getName()), componentEntity));
    }

    public void addPassiveComponent(ComponentEntity componentEntity) {
        passiveCoomponents.put(componentEntity.getName(), new ComponentEntry(components.get(componentEntity.getName()), componentEntity));
    }

    public void addAlientComponent(AlienComponent componentEntity) {
        alienComponent.put(componentEntity.getId(), componentEntity);
    }

    public void addStopRepair(AlienComponent component){
        stopRepair.put(component.getId(),component);
    }
    public void addStopingRepair(AlienComponent component){
        stopingRepair.put(component.getId(),component);
    }

    public boolean removeActiveComponent(String name) {
        return activeComponents.remove(name) != null;
    }

    public boolean removePassiveComponent(String name) {
        return passiveCoomponents.remove(name) != null;
    }

    public boolean removeAlienComponent(String id) {
        return alienComponent.remove(id) != null;
    }

    public boolean removeStopRepairComponent(String id) {
        return stopRepair.remove(id) != null;
    }
    public boolean removeStopingRepairComponent(String id) {
        return stopingRepair.remove(id) != null;
    }

    public IClusteredComponent getComponent(String name) {
        return components.get(name);
    }

}
