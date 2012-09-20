package ru.kwanza.jeda.core.tm;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Guzanov Alexander
 */
public class TestBaseTransactionWithArjuna extends TestBaseTransactionManager {

    public void setUp() throws Exception {
        ctx = new ClassPathXmlApplicationContext("application-context-arjuna.xml", TestBaseTransactionManager.class);

        tm = (BaseTransactionManager) ctx.getBean("baseTransactionManager");
        jtaTM = (javax.transaction.TransactionManager) ctx.getBean("jtaTransactionManager");
    }

    public void tearDown() throws Exception {

    }
}
