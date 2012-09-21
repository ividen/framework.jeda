package ru.kwanza.jeda.context.berkeley;

public class TestBerkeleyBlobContextControllerWithDictionary_Atomikos extends AbstractBerkeleyBlobContextControllerTest {

    @Override
    protected String getContextFileName() {
        return "berkeley-blob-context-controller-test-config-atomikos.xml";
    }

    @Override
    public void setUp() throws Exception {
        super.setUp();
        ctxController = ctx.getBean("berkeleyBlobContextControllerWithDict", BerkeleyBlobContextController.class);
    }

}
