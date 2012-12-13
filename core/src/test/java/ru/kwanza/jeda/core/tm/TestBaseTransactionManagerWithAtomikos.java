package ru.kwanza.jeda.core.tm;

import com.atomikos.icatch.jta.AtomikosTransactionManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.transaction.TransactionManager;

/**
 * @author Guzanov Alexander
 */
public class TestBaseTransactionManagerWithAtomikos extends TestBaseTransactionManager {

    public String getContextPath() {
        return "application-context-atomikos.xml";
    }

}
