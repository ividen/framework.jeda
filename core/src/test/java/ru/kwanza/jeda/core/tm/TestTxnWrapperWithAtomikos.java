package ru.kwanza.jeda.core.tm;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.internal.IJedaManagerInternal;

/**
 * @author Guzanov Alexander
 */
public class TestTxnWrapperWithAtomikos extends TestTxnWrapper {

    protected void setUp() throws Exception {
        ctx = new ClassPathXmlApplicationContext("application-context-tb-atomikos.xml", TestTxnWrapper.class);
        manager = ctx.getBean(TestTxnBean.class);
        sm = ctx.getBean(IJedaManager.class);
        tm = (BaseTransactionManager) sm.getTransactionManager();
    }

}
