package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.ContextStoreException;
import ru.kwanza.jeda.api.IContext;
import ru.kwanza.jeda.api.IContextController;

import java.util.Collection;
import java.util.Map;

/**
 * @author Guzanov Alexander
 */
public class TestContextController implements IContextController {

    public IContext createEmptyValue(Object contextId) {
        return null;
    }

    public Map load(Collection contextIds) {
        return null;
    }

    public void store(Collection contextObjects) throws ContextStoreException {
    }

    public void remove(Collection contexts) throws ContextStoreException {
    }
}
