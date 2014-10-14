package ru.kwanza.jeda.core.tm;

import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import ru.kwanza.jeda.api.internal.IJedaManagerInternal;

/**
 * @author Guzanov Alexander
 */
public abstract class TestTxnWrapper extends TestCase {
    protected ApplicationContext ctx;
    protected TestTxnBean manager;
    protected BaseTransactionManager tm;
    protected IJedaManagerInternal sm;


    public void test1() {
        try {
            manager.method1();
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }

    public void test2() {
        sm.getTransactionManager().begin();
        manager.method2();
        sm.getTransactionManager().commit();
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
