package ru.kwanza.jeda.mock;

import ru.kwanza.jeda.api.Manager;
import junit.framework.TestCase;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author Guzanov Alexander
 */
public class TestMockTransactionManagerWithDatasource extends TestCase {

    public void testMockTMWithDs() {
        ApplicationContext ctx = new ClassPathXmlApplicationContext("application-context-with-ds.xml", TestMocks.class);

        JdbcTemplate template = new JdbcTemplate((DataSource) ctx.getBean("dataSource"));

        template.execute("DELETE JEDA_1");
        template.execute("DELETE JEDA_2");

        assertEquals("Wrong tx count", 0, MockTransactionManagerInternal.getInstance().getTxs().size());
        Manager.getTM().begin();
        assertEquals("Wrong tx count", 0, MockTransactionManagerInternal.getInstance().getTxs().size());
        assertNotNull("Wrong tx count", MockTransactionManagerInternal.getInstance().getCurrentTx());
        template.execute("INSERT INTO JEDA_1(ID) VALUES('v_1')");

        Manager.getTM().commit();

        assertEquals("Wrong count", 1, template.queryForInt("SELECT count(*) FROM  JEDA_1"));

        Manager.getTM().begin();
        template.execute("INSERT INTO JEDA_1(ID) VALUES('v_2')");
        Manager.getTM().rollback();

        assertEquals("Wrong count", 1, template.queryForInt("SELECT count(*) FROM  JEDA_1"));

        Manager.getTM().begin();
        template.execute("INSERT INTO JEDA_1(ID) VALUES('v_2')");
        Manager.getTM().commit();

        Manager.getTM().begin();
        template.execute("INSERT INTO JEDA_1(ID) VALUES('v_3')");
        Manager.getTM().begin();
        template.execute("INSERT INTO JEDA_2(ID) VALUES('v_1')");
        Manager.getTM().commit();
        Manager.getTM().commit();

        assertEquals("Wrong count", 3, template.queryForInt("SELECT count(*) FROM  JEDA_1"));
        assertEquals("Wrong count", 1, template.queryForInt("SELECT count(*) FROM  JEDA_2"));


        Manager.getTM().begin();
        template.execute("INSERT INTO JEDA_1(ID) VALUES('v_4')");
        assertEquals("Wrong tx count", 0, MockTransactionManagerInternal.getInstance().getTxs().size());
        assertNotNull("Wrong tx count", MockTransactionManagerInternal.getInstance().getCurrentTx());
        Manager.getTM().begin();
        template.execute("INSERT INTO JEDA_2(ID) VALUES('v_2')");
        assertEquals("Wrong tx count", 1, MockTransactionManagerInternal.getInstance().getTxs().size());
        assertNotNull("Wrong tx count", MockTransactionManagerInternal.getInstance().getCurrentTx());
        Manager.getTM().rollback();
        assertEquals("Wrong tx count", 0, MockTransactionManagerInternal.getInstance().getTxs().size());
        assertNotNull("Wrong tx count", MockTransactionManagerInternal.getInstance().getCurrentTx());
        Manager.getTM().commit();
        assertEquals("Wrong tx count", 0, MockTransactionManagerInternal.getInstance().getTxs().size());
        assertNull("Wrong tx count", MockTransactionManagerInternal.getInstance().getCurrentTx());

        assertEquals("Wrong count", 4, template.queryForInt("SELECT count(*) FROM  JEDA_1"));
        assertEquals("Wrong count", 1, template.queryForInt("SELECT count(*) FROM  JEDA_2"));


        Manager.getTM().begin();
        template.execute("INSERT INTO JEDA_1(ID) VALUES('v_5')");
        assertEquals("Wrong tx count", 0, MockTransactionManagerInternal.getInstance().getTxs().size());
        assertNotNull("Wrong tx count", MockTransactionManagerInternal.getInstance().getCurrentTx());
        Manager.getTM().begin();
        template.execute("INSERT INTO JEDA_2(ID) VALUES('v_6')");
        assertEquals("Wrong tx count", 1, MockTransactionManagerInternal.getInstance().getTxs().size());
        assertNotNull("Wrong tx count", MockTransactionManagerInternal.getInstance().getCurrentTx());
        MockTransactionManagerInternal.getInstance().rollbackAllActive();

        assertEquals("Wrong tx count", 0, MockTransactionManagerInternal.getInstance().getTxs().size());
        assertNull("Wrong tx count", MockTransactionManagerInternal.getInstance().getCurrentTx());
    }
}
