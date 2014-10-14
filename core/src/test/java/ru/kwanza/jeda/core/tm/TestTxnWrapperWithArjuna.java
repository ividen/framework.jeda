package ru.kwanza.jeda.core.tm;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.kwanza.jeda.api.IJedaManager;

/**
 * @author Guzanov Alexander
 */
public class TestTxnWrapperWithArjuna extends TestTxnWrapper {

    protected void setUp() throws Exception {
        ctx = new ClassPathXmlApplicationContext("application-context-tb-arjuna.xml", TestTxnWrapper.class);
        manager = ctx.getBean(TestTxnBean.class);
        sm = ctx.getBean(IJedaManager.class);
        tm = (BaseTransactionManager) sm.getTransactionManager();
    }

}
