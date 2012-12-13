package ru.kwanza.jeda.core.tm;

import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Guzanov Alexander
 */
public class TestBaseTransactionWithArjuna extends TestBaseTransactionManager {

    @Override
    protected String getContextPath() {
        return "application-context-arjuna.xml";  //To change body of implemented methods use File | Settings | File Templates.
    }
}
