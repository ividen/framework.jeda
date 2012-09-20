package ru.kwanza.jeda.timer;

/**
 * @author Guzanov Alexander
 */
public class TestBerkeleyControllerArjuna extends TestBerkleyController {
    @Override
    protected String getContextFileName() {
        return "berkeley-persistenttimer-config_atomikos.xml";
    }
}