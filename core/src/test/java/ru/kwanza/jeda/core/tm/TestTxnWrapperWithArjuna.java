package ru.kwanza.jeda.core.tm;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.kwanza.jeda.api.internal.IJedaManagerInternal;

/**
 * @author Guzanov Alexander
 */
public class TestTxnWrapperWithArjuna extends TestTxnWrapper {

    protected void setUp() throws Exception {
        ctx = new ClassPathXmlApplicationContext("application-context-tb-arjuna.xml", TestTxnWrapper.class);
        manager = ctx.getBean(TestTxnBean.class);
        sm = ctx.getBean(IJedaManagerInternal.class);
        tm = (BaseTransactionManager) sm.getTransactionManager();
    }

}
