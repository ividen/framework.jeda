package ru.kwanza.jeda.core.manager;

import ru.kwanza.jeda.api.ContextStoreException;
import ru.kwanza.jeda.api.IContext;
import ru.kwanza.jeda.api.IContextController;

import java.util.Collection;
import java.util.Map;

/**
 * @author Guzanov Alexander
 */
public class SystemContextController implements IContextController {
    private IContextController contextController;
    private String name;

    public SystemContextController(String name, IContextController contextController) {
        this.contextController = contextController;
        this.name = name;
    }

    public IContext createEmptyValue(Object contextId) {
        return contextController.createEmptyValue(contextId);
    }

    public Map load(Collection contextIds) {
        return contextController.load(contextIds);
    }

    public void store(Collection contextObjects) throws ContextStoreException {
        contextController.store(contextObjects);
    }

    public void remove(Collection contexts) throws ContextStoreException {
        contextController.remove(contexts);
    }

    public String getName() {
        return name;
    }
}
