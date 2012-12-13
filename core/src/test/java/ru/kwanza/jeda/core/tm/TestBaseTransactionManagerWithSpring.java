package ru.kwanza.jeda.core.tm;

import com.atomikos.icatch.jta.AtomikosTransactionManager;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.transaction.TransactionManager;

/**
 * @author Alexander Guzanov
 */
public class TestBaseTransactionManagerWithSpring extends TestBaseTransactionManager {

    @Override
    protected String getContextPath() {
        return "application-context-ds.xml";
    }

}