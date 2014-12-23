package ru.kwanza.jeda.jeconnection.springintegration;

import junit.framework.TestCase;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import ru.kwanza.jeda.jeconnection.JEConnectionFactory;

/**
 * @author Guzanov Alexander
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("application-config.xml")
public class TestSpringIntegration extends TestCase {

    @Autowired
    private ApplicationContext ctx;

    @Test
    public void test() throws InterruptedException {
        assertNotNull(ctx.getBean("berkeleyFactory", JEConnectionFactory.class));
        assertNotNull(ctx.getBean("berkeleyFactory2", JEConnectionFactory.class));
    }
}
