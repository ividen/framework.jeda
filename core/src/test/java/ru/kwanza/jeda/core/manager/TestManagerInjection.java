package ru.kwanza.jeda.core.manager;

import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import ru.kwanza.jeda.api.IJedaManager;

/**
 * @author Guzanov Alexander
 */
public class TestManagerInjection extends TestCase {
    public void testManagerInjection() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml", TestManagerInjection.class);
        IJedaManager manager = ctx.getBean(IJedaManager.class);

        final TransactionStatus status = manager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        manager.getTransactionManager().commit(status);
    }
}
