package ru.kwanza.jeda.core.tm;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.kwanza.jeda.api.ISystemManager;
import ru.kwanza.jeda.api.internal.ISystemManagerInternal;

/**
 * @author Guzanov Alexander
 */
public class TestTxnWrapperWithArjuna extends TestTxnWrapper {

    protected void setUp() throws Exception {
        ctx = new ClassPathXmlApplicationContext("application-context-tb-arjuna.xml", TestTxnWrapper.class);
        manager = ctx.getBean(TestTxnBean.class);
        sm = ctx.getBean(ISystemManagerInternal.class);
        tm = (BaseTransactionManager) sm.getTransactionManager();
    }

}
