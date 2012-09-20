package ru.kwanza.jeda.core.manager;

import ru.kwanza.jeda.api.Manager;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Guzanov Alexander
 */
public class TestManagerInjection extends TestCase {
    public void testManagerInjection() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context.xml", TestManagerInjection.class);
        Manager manager = ctx.getBean(Manager.class);

        Manager.getTM().begin();

        Manager.getTM().commit();
    }
}
