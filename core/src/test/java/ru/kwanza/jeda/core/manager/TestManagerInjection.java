package ru.kwanza.jeda.core.manager;

import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.kwanza.jeda.api.IJedaManager;

/**
 * @author Guzanov Alexander
 */
public class TestManagerInjection extends TestCase {
    public void testManagerInjection() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml", TestManagerInjection.class);
        IJedaManager manager = ctx.getBean(IJedaManager.class);

        manager.getTransactionManager().begin();

        manager.getTransactionManager().commit();
    }
}
