package ru.kwanza.jeda.jeconnection.springintegration;

import ru.kwanza.jeda.jeconnection.JEConnectionFactory;
import junit.framework.TestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Guzanov Alexander
 */
public class TestSpringIntegration extends TestCase {

    public void test() throws InterruptedException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("application-config.xml", TestSpringIntegration.class);
        assertNotNull(ctx.getBean("berkeleyFactory", JEConnectionFactory.class));
        assertNotNull(ctx.getBean("berkeleyFactory2", JEConnectionFactory.class));
        ctx.close();
    }
}
