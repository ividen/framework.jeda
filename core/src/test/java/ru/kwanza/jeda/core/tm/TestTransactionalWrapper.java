package ru.kwanza.jeda.core.tm;

import ru.kwanza.jeda.api.Manager;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;

/**
 * @author Guzanov Alexander
 */
public abstract class TestTransactionalWrapper extends TestCase {
    protected ApplicationContext ctx;
    protected TestTransactionalBean manager;
    protected BaseTransactionManager tm;


    public void test1() {
        try {
            manager.method1();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void test2() {
        Manager.getTM().begin();
        manager.method2();
        Manager.getTM().commit();
    }

    public void test3() {
        manager.method3();
    }

    public void test4() throws TestIOException {
        try {
            manager.method4();
        } catch (TestIOException e) {
            e.printStackTrace();
        }
    }

    public void test5() {
        try {
            manager.method2();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
    }
}
