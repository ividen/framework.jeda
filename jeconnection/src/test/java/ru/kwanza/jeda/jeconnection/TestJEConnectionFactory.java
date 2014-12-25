package ru.kwanza.jeda.jeconnection;


import com.sleepycat.je.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.toolbox.SerializationHelper;

import javax.transaction.RollbackException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

import static junit.framework.Assert.*;

/**
 * @author Kiryl Karatsetski
 */
@RunWith(SpringJUnit4ClassRunner.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
public abstract class TestJEConnectionFactory {

    @Autowired
    @Qualifier("connectionFactory")
    private JEConnectionFactory factoryJE;
    @Autowired
    @Qualifier("connectionFactory2")
    private JEConnectionFactory factoryJE2;
    @Autowired
    private IJedaManager manager;

    @Before
    public void setUp() throws Exception {
        delete(new File("./target/berkeley_db"));
    }

    @Test
    public void testCommitLoad() throws Exception, RollbackException {
        TransactionStatus status1 = manager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Database db = factoryJE.getConnection(0).openDatabase("test",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        String test = "Test";
        db.put(null, new DatabaseEntry(SerializationHelper.longToBytes(10)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test)));
        Database db1 = factoryJE.getConnection(0).openDatabase("test_1",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        String test1 = "Test1";
        db1.put(null, new DatabaseEntry(SerializationHelper.longToBytes(20)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test1)));
        manager.getTransactionManager().commit(status1);

        TransactionStatus status2 = manager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        db = factoryJE.getConnection(0).openDatabase("test", new DatabaseConfig()
                .setAllowCreate(true).setTransactional(true));
        Cursor cursor = db.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        assertEquals(OperationStatus.SUCCESS, cursor.getNext(key, value, LockMode.DEFAULT));
        assertEquals(10l, SerializationHelper.bytesToLong(key.getData()));
        assertEquals("Test", SerializationHelper.bytesToObject(value.getData()));

        db1 = factoryJE.getConnection(0).openDatabase("test_1",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        Cursor cursor1 = db1.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key1 = new DatabaseEntry();
        DatabaseEntry value1 = new DatabaseEntry();
        assertEquals(OperationStatus.SUCCESS, cursor1.getNext(key1, value1, LockMode.DEFAULT));
        assertEquals(20l, SerializationHelper.bytesToLong(key1.getData()));
        assertEquals("Test1", SerializationHelper.bytesToObject(value1.getData()));
        cursor.close();
        cursor1.close();
        manager.getTransactionManager().commit(status2);
    }

//    public void testCommitLoad_1() throws Exception, RollbackException {
//        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(getConfigName(), TestJEConnectionFactory.class);
//        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");
//
//
//        manager.getTransactionManager().begin();
//        Database db = factoryJE.getTxConnection(0l).openDatabase("test",
//                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
//        String test = "Test";
//        db.put(null, new DatabaseEntry(SerializationHelper.longToBytes(10)),
//                new DatabaseEntry(SerializationHelper.objectToBytes(test)));
//        Database db1 = factoryJE.getTxConnection(1l).openDatabase("test_1",
//                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
//        String test1 = "Test1";
//        db1.put(null, new DatabaseEntry(SerializationHelper.longToBytes(20)),
//                new DatabaseEntry(SerializationHelper.objectToBytes(test1)));
//        manager.getTransactionManager().commit();
//
//        FileLock lock0 = new RandomAccessFile(new File("./target/berkeley_db/0/fileLock.lock"), "rw").getChannel().lock();
//        FileLock lock1 = new RandomAccessFile(new File("./target/berkeley_db/1/fileLock.lock"), "rw").getChannel().lock();
//
//        lock0.release();
//        lock1.release();
//
//        manager.getTransactionManager().begin();
//        db = factoryJE.getTxConnection(0l).openDatabase("test", new DatabaseConfig()
//                .setAllowCreate(true).setTransactional(true));
//        Cursor cursor = db.openCursor(null, new CursorConfig().setReadCommitted(true));
//        DatabaseEntry key = new DatabaseEntry();
//        DatabaseEntry value = new DatabaseEntry();
//        assertEquals(OperationStatus.SUCCESS, cursor.getNext(key, value, LockMode.DEFAULT));
//        assertEquals(10l, SerializationHelper.bytesToLong(key.getData()));
//        assertEquals("Test", SerializationHelper.bytesToObject(value.getData()));
//
//        db1 = factoryJE.getTxConnection(1l).openDatabase("test_1",
//                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
//        Cursor cursor1 = db1.openCursor(null, new CursorConfig().setReadCommitted(true));
//        DatabaseEntry key1 = new DatabaseEntry();
//        DatabaseEntry value1 = new DatabaseEntry();
//        assertEquals(OperationStatus.SUCCESS, cursor1.getNext(key1, value1, LockMode.DEFAULT));
//        assertEquals(20l, SerializationHelper.bytesToLong(key1.getData()));
//        assertEquals("Test1", SerializationHelper.bytesToObject(value1.getData()));
//        cursor.close();
//        cursor1.close();
//        manager.getTransactionManager().commit();
//        lock0 = new RandomAccessFile(new File("./target/berkeley_db/0/fileLock.lock"), "rw").getChannel().lock();
//        lock1 = new RandomAccessFile(new File("./target/berkeley_db/1/fileLock.lock"), "rw").getChannel().lock();
//
//        lock0.release();
//        lock1.release();
//
//        ctx.close();
//    }

    @Test
    public void testCommitLoad_2() throws Exception, RollbackException {
        TransactionStatus status1 = manager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Database db = factoryJE.getTxConnection(0).openDatabase("test",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        String test = "Test";
        db.put(null, new DatabaseEntry(SerializationHelper.longToBytes(10)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test)));
        Database db1 = factoryJE.getTxConnection(0).openDatabase("test_1",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        String test1 = "Test1";
        db1.put(null, new DatabaseEntry(SerializationHelper.longToBytes(20)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test1)));
        manager.getTransactionManager().commit(status1);

        FileLock lock0 = new RandomAccessFile(new File("./target/berkeley_db/0/fileLock.lock"), "rw").getChannel().lock();

        lock0.release();

        TransactionStatus status2 = manager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        db = factoryJE.getTxConnection(0).openDatabase("test", new DatabaseConfig()
                .setAllowCreate(true).setTransactional(true));
        Cursor cursor = db.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        assertEquals(OperationStatus.SUCCESS, cursor.getNext(key, value, LockMode.DEFAULT));
        assertEquals(10l, SerializationHelper.bytesToLong(key.getData()));
        assertEquals("Test", SerializationHelper.bytesToObject(value.getData()));

        db1 = factoryJE.getTxConnection(0).openDatabase("test_1",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        Cursor cursor1 = db1.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key1 = new DatabaseEntry();
        DatabaseEntry value1 = new DatabaseEntry();
        assertEquals(OperationStatus.SUCCESS, cursor1.getNext(key1, value1, LockMode.DEFAULT));
        assertEquals(20l, SerializationHelper.bytesToLong(key1.getData()));
        assertEquals("Test1", SerializationHelper.bytesToObject(value1.getData()));
        cursor.close();
        cursor1.close();
        manager.getTransactionManager().commit(status2);
        lock0 = new RandomAccessFile(new File("./target/berkeley_db/0/fileLock.lock"), "rw").getChannel().lock();

        lock0.release();
    }

    @Test
    public void testCommitLoad_3() throws Exception, RollbackException {
        Database db = factoryJE.getConnection(0).openDatabase("test",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        Database db1 = factoryJE.getConnection(0).openDatabase("test_1",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));

        TransactionStatus status1 = manager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));

        String test = "Test";
        db.put(null, new DatabaseEntry(SerializationHelper.longToBytes(10)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test)));

        String test1 = "Test1";
        db1.put(null, new DatabaseEntry(SerializationHelper.longToBytes(20)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test1)));
        manager.getTransactionManager().commit(status1);

        TransactionStatus status2 = manager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        db = factoryJE.getConnection(0).openDatabase("test", new DatabaseConfig()
                .setAllowCreate(true).setTransactional(true));
        Cursor cursor = db.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        assertEquals(OperationStatus.SUCCESS, cursor.getNext(key, value, LockMode.DEFAULT));
        assertEquals(10l, SerializationHelper.bytesToLong(key.getData()));
        assertEquals("Test", SerializationHelper.bytesToObject(value.getData()));

        db1 = factoryJE.getConnection(0).openDatabase("test_1",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        Cursor cursor1 = db1.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key1 = new DatabaseEntry();
        DatabaseEntry value1 = new DatabaseEntry();
        assertEquals(OperationStatus.SUCCESS, cursor1.getNext(key1, value1, LockMode.DEFAULT));
        assertEquals(20l, SerializationHelper.bytesToLong(key1.getData()));
        assertEquals("Test1", SerializationHelper.bytesToObject(value1.getData()));
        cursor.close();
        cursor1.close();
        manager.getTransactionManager().commit(status2);

        factoryJE.closeConnection(0);
        factoryJE.closeConnection(1);
    }


    @Test
    public void testRollbacktLoad() throws Exception, RollbackException {
        TransactionStatus status1 = manager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Database db = factoryJE.getConnection(0).openDatabase("test",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        String test = "Test";
        db.put(null, new DatabaseEntry(SerializationHelper.longToBytes(10)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test)));
        Database db1 = factoryJE.getConnection(0).openDatabase("test_1",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        String test1 = "Test1";
        db1.put(null, new DatabaseEntry(SerializationHelper.longToBytes(20)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test1)));
        manager.getTransactionManager().rollback(status1);

        TransactionStatus status2 = manager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        db = factoryJE.getConnection(0).openDatabase("test", new DatabaseConfig()
                .setAllowCreate(true).setTransactional(true));
        Cursor cursor = db.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        assertEquals(OperationStatus.NOTFOUND, cursor.getNext(key, value, LockMode.DEFAULT));

        db1 = factoryJE.getConnection(0).openDatabase("test_1",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        Cursor cursor1 = db1.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key1 = new DatabaseEntry();
        DatabaseEntry value1 = new DatabaseEntry();
        assertEquals(OperationStatus.NOTFOUND, cursor1.getNext(key1, value1, LockMode.DEFAULT));
        cursor.close();
        cursor1.close();
        manager.getTransactionManager().commit(status2);

        factoryJE.closeConnection(0);
        factoryJE.closeConnection(1);
    }

    @Test
    public void testInnerTransaction_CommitCommit() throws Exception, RollbackException {
        TransactionStatus status1 = manager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Database db = factoryJE.getConnection(0).openDatabase("test",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        String test = "Test";
        db.put(null, new DatabaseEntry(SerializationHelper.longToBytes(10)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test)));

        {
            TransactionStatus status2 = manager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
            Database db1 = factoryJE.getConnection(0).openDatabase("test",
                    new DatabaseConfig().setAllowCreate(true).setTransactional(true));
            String test1 = "Test1";
            db1.put(null, new DatabaseEntry(SerializationHelper.longToBytes(20)),
                    new DatabaseEntry(SerializationHelper.objectToBytes(test1)));
            manager.getTransactionManager().commit(status2);
        }

        manager.getTransactionManager().commit(status1);

        TransactionStatus status3 = manager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        db = factoryJE.getConnection(0).openDatabase("test", new DatabaseConfig()
                .setAllowCreate(true).setTransactional(true));
        Cursor cursor = db.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        assertEquals(OperationStatus.SUCCESS, cursor.getNext(key, value, LockMode.DEFAULT));
        assertEquals(10l, SerializationHelper.bytesToLong(key.getData()));
        assertEquals("Test", SerializationHelper.bytesToObject(value.getData()));
        assertEquals(OperationStatus.SUCCESS, cursor.getNext(key, value, LockMode.DEFAULT));
        assertEquals(20l, SerializationHelper.bytesToLong(key.getData()));
        assertEquals("Test1", SerializationHelper.bytesToObject(value.getData()));


        cursor.close();

        manager.getTransactionManager().commit(status3);

        factoryJE.closeConnection(0);
        factoryJE.closeConnection(1);
    }

    @Test
    public void testInnerTransaction_CommitRollback() throws Exception, RollbackException {
        TransactionStatus status1 = manager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Database db = factoryJE.getConnection(0).openDatabase("test",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        String test = "Test";
        db.put(null, new DatabaseEntry(SerializationHelper.longToBytes(10)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test)));

        {
            TransactionStatus status2 = manager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
            Database db1 = factoryJE.getConnection(0).openDatabase("test",
                    new DatabaseConfig().setAllowCreate(true).setTransactional(true));
            String test1 = "Test1";
            db1.put(null, new DatabaseEntry(SerializationHelper.longToBytes(20)),
                    new DatabaseEntry(SerializationHelper.objectToBytes(test1)));
            manager.getTransactionManager().rollback(status2);
        }

        manager.getTransactionManager().commit(status1);

        TransactionStatus status3 = manager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        db = factoryJE.getConnection(0).openDatabase("test", new DatabaseConfig()
                .setAllowCreate(true).setTransactional(true));
        Cursor cursor = db.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        assertEquals(OperationStatus.SUCCESS, cursor.getNext(key, value, LockMode.DEFAULT));
        assertEquals(10l, SerializationHelper.bytesToLong(key.getData()));
        assertEquals("Test", SerializationHelper.bytesToObject(value.getData()));
        assertEquals(OperationStatus.NOTFOUND, cursor.getNext(key, value, LockMode.DEFAULT));
        cursor.close();

        manager.getTransactionManager().commit(status3);

        factoryJE.closeConnection(0);
        factoryJE.closeConnection(1);
    }

    @Test
    public void testInnerTransaction_RollbackCommit() throws Exception, RollbackException {
        TransactionStatus status1 = manager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        Database db = factoryJE.getConnection(0).openDatabase("test",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        String test = "Test";
        db.put(null, new DatabaseEntry(SerializationHelper.longToBytes(10)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test)));

        {
            TransactionStatus status2 = manager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
            Database db1 = factoryJE.getConnection(0).openDatabase("test",
                    new DatabaseConfig().setAllowCreate(true).setTransactional(true));
            String test1 = "Test1";
            db1.put(null, new DatabaseEntry(SerializationHelper.longToBytes(20)),
                    new DatabaseEntry(SerializationHelper.objectToBytes(test1)));
            manager.getTransactionManager().commit(status2);
        }

        manager.getTransactionManager().rollback(status1);

        TransactionStatus status3 = manager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        db = factoryJE.getConnection(0).openDatabase("test", new DatabaseConfig()
                .setAllowCreate(true).setTransactional(true));
        Cursor cursor = db.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        assertEquals(OperationStatus.SUCCESS, cursor.getNext(key, value, LockMode.DEFAULT));
        assertEquals(20l, SerializationHelper.bytesToLong(key.getData()));
        assertEquals("Test1", SerializationHelper.bytesToObject(value.getData()));
        assertEquals(OperationStatus.NOTFOUND, cursor.getNext(key, value, LockMode.DEFAULT));
        cursor.close();

        manager.getTransactionManager().commit(status3);

        factoryJE.closeConnection(0);
        factoryJE.closeConnection(1);
    }

    @Test
    public void testJEConnectionFactory1() {
        factoryJE.setLockingTimeout(1000);
        assertEquals(1000, factoryJE.getLockingTimeout());
        assertEquals("./target/berkeley_db/", factoryJE.getPath());
        assertTrue(factoryJE.getEnvironmentConfig().getAllowCreate());
        assertEquals(factoryJE.getTransactionConfig().getDurability(), Durability.COMMIT_SYNC);

        factoryJE.getConnection(1);
        factoryJE2.getConnection(0);

        try {
            factoryJE.getConnection(0);
            fail("Extected " + JEConnectionException.class);
        } catch (JEConnectionException e) {

        }

        factoryJE.setEnvironmentConfig(new EnvironmentConfig().setAllowCreate(false));
        assertFalse(factoryJE.getEnvironmentConfig().getAllowCreate());
        factoryJE.setTransactionConfig(new TransactionConfig().setDurability(Durability.READ_ONLY_TXN));
        assertEquals(factoryJE.getTransactionConfig().getDurability(), Durability.READ_ONLY_TXN);

        factoryJE.closeConnection(0);
        factoryJE2.closeConnection(0);
        factoryJE.closeConnection(1);
        factoryJE2.closeConnection(1);
    }

    @Test
    public void testActive() throws Exception, RollbackException {
        factoryJE.destroy();
        try {
            factoryJE.getConnection(0);
            fail("Expected " + JEConnectionException.class);
        } catch (JEConnectionException e) {
        }
        TransactionStatus status1 = manager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        try {
            factoryJE.getTxConnection(0);
            fail("Expected " + JEConnectionException.class);
        } catch (JEConnectionException e) {
        }

        manager.getTransactionManager().commit(status1);
    }

    @Test
    public void testGetTxConection() throws Exception, RollbackException {

        try {
            factoryJE.getTxConnection(0);
            fail("Expected " + JEConnectionException.class);
        } catch (JEConnectionException e) {
        }
    }

    private void delete(File file) throws IOException {
        if (file.isDirectory()) {
            for (File item : file.listFiles()) {
                delete(item);
            }
        }
        file.delete();
    }
}
