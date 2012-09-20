package ru.kwanza.jeda.api;

import java.util.Collection;
import java.util.Map;

/**
 * @author Guzanov Alexander
 */
public interface IContextController<ID, C extends IContext<ID, ?>> {

    public C createEmptyValue(ID contextId);

    public Map<ID, C> load(Collection<ID> contextIds);

    public void store(Collection<C> contextObjects) throws ContextStoreException;

    public void remove(Collection<C> contexts) throws ContextStoreException;

}
