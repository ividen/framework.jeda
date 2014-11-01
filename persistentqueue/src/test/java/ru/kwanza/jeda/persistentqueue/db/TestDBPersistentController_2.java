package ru.kwanza.jeda.persistentqueue.db;

import junit.framework.Assert;
import org.dbunit.Assertion;
import org.dbunit.database.DatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.SortedDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import ru.kwanza.dbtool.core.DBTool;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.persistentqueue.DefaultPersistableEvent;
import ru.kwanza.jeda.persistentqueue.IQueuePersistenceController;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Alexander Guzanov
 */
@ContextConfiguration("test-config_2.xml")
public class TestDBPersistentController_2 extends AbstractTransactionalJUnit4SpringContextTests {
    @Resource(name = "defaultQueueController1")
    private IQueuePersistenceController<DefaultPersistableEvent> controller1;
    @Resource(name = "defaultQueueController2")
    private IQueuePersistenceController<DefaultPersistableEvent> controller2;
    private Node node1 = new Node(1);
    private Node node2 = new Node(2);

    @Resource(name = "dbtool.DBTool")
    protected DBTool dbTool;

    protected IDataSet getActualDataSet(String tablename) throws Exception {
        return new SortedDataSet(new DatabaseConnection(dbTool.getJDBCConnection())
                .createDataSet(new String[]{tablename}));
    }

    protected IDataSet getResourceSet(String fileName) throws DataSetException {
        return new SortedDataSet(new FlatXmlDataSetBuilder().build(this.getClass().getResourceAsStream(fileName)));
    }

    @Test
    public void testQueueName() {
        Assert.assertEquals("DBQueuePersistenceController." +
                        "ru.kwanza.jeda.persistentqueue.db.queue.NamedEventQueue:queueName=queue1",
                controller1.getQueueName());
        Assert.assertEquals("DBQueuePersistenceController" +
                        ".ru.kwanza.jeda.persistentqueue.db.queue.NamedEventQueue:queueName=queue2",
                controller2.getQueueName());
    }

    @Test
    public void testPersist() throws Exception {
        controller1.persist(Arrays.asList(new DefaultPersistableEvent(1l)), node1);
        controller1.persist(Arrays.asList(new DefaultPersistableEvent(2l)), node2);
        controller2.persist(Arrays.asList(new DefaultPersistableEvent(21l)), node1);
        controller2.persist(Arrays.asList(new DefaultPersistableEvent(22l)), node2);

        Assertion.assertEqualsIgnoreCols(getResourceSet("dataset_2_1.xml"),
                getActualDataSet("jeda_jdbc_event_nqueue"), "jeda_jdbc_event_nqueue", new String[]{"data"});
    }

    @Test
    public void testDelete() throws Exception {
        controller1.delete(Arrays.asList(new DefaultPersistableEvent(3l)), node1);
        controller2.delete(Arrays.asList(new DefaultPersistableEvent(7l)), node1);
        controller1.delete(Arrays.asList(new DefaultPersistableEvent(13l)), node2);
        controller2.delete(Arrays.asList(new DefaultPersistableEvent(17l)), node2);
        Assertion.assertEqualsIgnoreCols(getResourceSet("dataset_2_2.xml"),
                getActualDataSet("jeda_jdbc_event_nqueue"), "jeda_jdbc_event_nqueue", new String[]{"data"});
    }

    @Test
    public void testLoad() {
        Collection<DefaultPersistableEvent> load = controller1.load(10, node1);
        Assert.assertEquals(4, load.size());

        load = controller1.load(2, node1);
        Assert.assertEquals(2, load.size());
        load = controller1.load(100, node2);
        Assert.assertEquals(4, load.size());

        load = controller2.load(10, node1);
        Assert.assertEquals(4, load.size());

        load = controller2.load(2, node1);
        Assert.assertEquals(2, load.size());
        load = controller2.load(100, node2);
        Assert.assertEquals(4, load.size());

    }

    @Test
    public void testTransfer_1() throws Exception {
        Assert.assertEquals(1, controller1.transfer(1, node1, node2));
        Assert.assertEquals(2, controller2.transfer(2, node1, node2));
        Assertion.assertEqualsIgnoreCols(getResourceSet("dataset_2_3.xml"),
                getActualDataSet("jeda_jdbc_event_nqueue"), "jeda_jdbc_event_nqueue", new String[]{"data"});
    }

    @Test
    public void testTransfer_2() throws Exception {
        Assert.assertEquals(4, controller1.transfer(4, node1, node2));
        Assert.assertEquals(4, controller2.transfer(4, node1, node2));
        Assertion.assertEqualsIgnoreCols(getResourceSet("dataset_2_4.xml"),
                getActualDataSet("jeda_jdbc_event_nqueue"), "jeda_jdbc_event_nqueue", new String[]{"data"});
    }

    @Test
    public void testTransfer_3() throws Exception {
        Assert.assertEquals(4, controller1.transfer(100, node1, node2));
        Assert.assertEquals(4, controller2.transfer(100, node1, node2));
        Assertion.assertEqualsIgnoreCols(getResourceSet("dataset_2_5.xml"),
                getActualDataSet("jeda_jdbc_event_nqueue"), "jeda_jdbc_event_nqueue", new String[]{"data"});
    }
}

