package ru.kwanza.jeda.core.tm;

import ru.kwanza.jeda.api.Manager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Guzanov Alexander
 */
public class TestTxnWrapperWithArjuna extends TestTxnWrapper {

    protected void setUp() throws Exception {
        ctx = new ClassPathXmlApplicationContext("application-context-tb-arjuna.xml", TestTxnWrapper.class);
        manager = ctx.getBean(TestTxnBean.class);
        tm = (BaseTransactionManager) Manager.getTM();
    }

}
