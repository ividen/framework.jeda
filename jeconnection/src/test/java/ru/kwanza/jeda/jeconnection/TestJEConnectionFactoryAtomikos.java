package ru.kwanza.jeda.jeconnection;

/**
 * @author Guzanov Alexander
 */
public class TestJEConnectionFactoryAtomikos extends TestJEConnectionFactory {

    @Override
    protected String getConfigName() {
        return "application-config-atomikos.xml";
    }
}

