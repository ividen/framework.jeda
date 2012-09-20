package ru.kwanza.jeda.core.tm;

import ru.kwanza.jeda.api.Manager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Guzanov Alexander
 */
public class TestTransactionalWrapperWithArjuna extends TestTransactionalWrapper {

    protected void setUp() throws Exception {
        ctx = new ClassPathXmlApplicationContext("application-context-tb-arjuna.xml", TestTransactionalWrapper.class);
        manager = ctx.getBean(TestTransactionalBean.class);
        tm = (BaseTransactionManager) Manager.getTM();
    }

}
