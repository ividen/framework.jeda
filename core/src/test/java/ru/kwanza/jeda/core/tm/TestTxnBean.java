package ru.kwanza.jeda.core.tm;

import ru.kwanza.txn.api.Transactional;
import ru.kwanza.txn.api.TransactionalType;

import javax.annotation.Resource;
import java.io.IOException;

/**
 * @author Guzanov Alexander
 */
public class TestTxnBean {
    @Resource(name = "testTransactionalBean")
    private TestTxnBean bean;


    @Transactional(TransactionalType.REQUIRES_NEW)
    public void method1() {
        throw new RuntimeException("Test With Rollback");
    }

    @Transactional(value = TransactionalType.MANDATORY)
    public int method2() {
        return 1;
    }

    @Transactional()
    public void method3() {
        System.out.println("Test" + bean.method2());
    }

    @Transactional(value = TransactionalType.REQUIRED,
            applicationExceptions = {IOException.class, RuntimeException.class})
    public void method4() throws TestIOException {
        throw new TestIOException();
    }
}