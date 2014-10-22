package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.internal.IJedaManagerInternal;

/**
 * @author: Guzanov Alexander
 */
class FakeObjectHolder {
    public FakeObjectHolder(IJedaManagerInternal manager, String beanName, Object original) {
        manager.registerObject(beanName, original);
    }

}
