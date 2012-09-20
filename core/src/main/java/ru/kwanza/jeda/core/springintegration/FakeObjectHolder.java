package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.internal.ISystemManager;

/**
 * @author: Guzanov Alexander
 */
class FakeObjectHolder {
    public FakeObjectHolder(ISystemManager manager, String beanName, Object original) {
        manager.registerObject(beanName, original);
    }

}
