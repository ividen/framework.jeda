package ru.kwanza.jeda.context.berkeley;

/**
 * @author Dmitry Zagorovsky
 */
public class TestBerkeleyBlobContextController_Arjuna extends AbstractBerkeleyBlobContextControllerTest {

    @Override
    protected String getContextFileName() {
        return "berkeley-blob-context-controller-test-config-arjuna.xml";
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ctxController = ctx.getBean("berkeleyBlobContextController", BerkeleyBlobContextController.class);
    }

}
