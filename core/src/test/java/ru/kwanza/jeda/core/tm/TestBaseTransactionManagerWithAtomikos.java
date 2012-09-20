package ru.kwanza.jeda.core.tm;

import com.atomikos.icatch.jta.AtomikosTransactionManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.transaction.TransactionManager;

/**
 * @author Guzanov Alexander
 */
public class TestBaseTransactionManagerWithAtomikos extends TestBaseTransactionManager {

    public void setUp() throws Exception {
        ctx = new ClassPathXmlApplicationContext("application-context-atomikos.xml", TestBaseTransactionManager.class);
        tm = (BaseTransactionManager) ctx.getBean("baseTransactionManager");
        jtaTM = (TransactionManager) ctx.getBean("jtaTransactionManager");
    }

    public void tearDown() throws Exception {
        ((AtomikosTransactionManager) jtaTM).close();
    }
}
