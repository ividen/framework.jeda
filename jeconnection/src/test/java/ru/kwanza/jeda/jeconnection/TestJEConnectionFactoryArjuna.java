package ru.kwanza.jeda.jeconnection;

/**
 * @author Guzanov Alexander
 */
public class TestJEConnectionFactoryArjuna extends TestJEConnectionFactory {
    protected String getConfigName() {
        return "application-config-arjuna.xml";
    }
}
