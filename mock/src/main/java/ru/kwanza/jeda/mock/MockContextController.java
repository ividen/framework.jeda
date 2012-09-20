package ru.kwanza.jeda.mock;

import ru.kwanza.jeda.api.ContextStoreException;
import ru.kwanza.jeda.api.IContext;
import ru.kwanza.jeda.api.IContextController;

import java.util.Collection;
import java.util.Map;

/**
 * @author Guzanov Alexander
 */
public class MockContextController implements IContextController<Object, IContext<Object, Object>> {


    public IContext<Object, Object> createEmptyValue(Object contextId) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public Map<Object, IContext<Object, Object>> load(Collection<Object> contextIds) {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void store(Collection<IContext<Object, Object>> contextObjects) throws ContextStoreException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void remove(Collection<IContext<Object, Object>> contexts) throws ContextStoreException {
        //To change body of implemented methods use File | Settings | File Templates.
    }
}
