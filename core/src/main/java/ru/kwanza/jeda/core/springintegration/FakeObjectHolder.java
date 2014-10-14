package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.internal.ISystemManagerInternal;

/**
 * @author: Guzanov Alexander
 */
class FakeObjectHolder {
    public FakeObjectHolder(ISystemManagerInternal manager, String beanName, Object original) {
        manager.registerObject(beanName, original);
    }

}
