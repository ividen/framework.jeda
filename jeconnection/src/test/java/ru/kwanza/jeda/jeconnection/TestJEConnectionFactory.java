package ru.kwanza.jeda.jeconnection;


import ru.kwanza.jeda.api.Manager;
import ru.kwanza.toolbox.SerializationHelper;
import com.sleepycat.je.*;
import junit.framework.TestCase;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.transaction.RollbackException;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileLock;

/**
 * @author Kiryl Karatsetski
 */
public abstract class TestJEConnectionFactory extends TestCase {

    @Override
    public void setUp() throws Exception {
        delete(new File("./target/berkeley_db"));
    }

    public void testCommitLoad() throws Exception, RollbackException {
        ClassPathXmlApplicationContext ctx;
        ctx = new ClassPathXmlApplicationContext(getConfigName(), TestJEConnectionFactory.class);
        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");


        Manager.getTM().begin();
        Database db = factoryJE.getConnection(0l).openDatabase("test",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        String test = "Test";
        db.put(null, new DatabaseEntry(SerializationHelper.longToBytes(10)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test)));
        Database db1 = factoryJE.getConnection(0l).openDatabase("test_1",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        String test1 = "Test1";
        db1.put(null, new DatabaseEntry(SerializationHelper.longToBytes(20)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test1)));
        Manager.getTM().commit();

        Manager.getTM().begin();
        db = factoryJE.getConnection(0l).openDatabase("test", new DatabaseConfig()
                .setAllowCreate(true).setTransactional(true));
        Cursor cursor = db.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        assertEquals(OperationStatus.SUCCESS, cursor.getNext(key, value, LockMode.DEFAULT));
        assertEquals(10l, SerializationHelper.bytesToLong(key.getData()));
        assertEquals("Test", SerializationHelper.bytesToObject(value.getData()));

        db1 = factoryJE.getConnection(0l).openDatabase("test_1",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        Cursor cursor1 = db1.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key1 = new DatabaseEntry();
        DatabaseEntry value1 = new DatabaseEntry();
        assertEquals(OperationStatus.SUCCESS, cursor1.getNext(key1, value1, LockMode.DEFAULT));
        assertEquals(20l, SerializationHelper.bytesToLong(key1.getData()));
        assertEquals("Test1", SerializationHelper.bytesToObject(value1.getData()));
        cursor.close();
        cursor1.close();
        Manager.getTM().commit();

        ctx.close();
    }

    protected abstract String getConfigName();

//    public void testCommitLoad_1() throws Exception, RollbackException {
//        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(getConfigName(), TestJEConnectionFactory.class);
//        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");
//
//
//        Manager.getTM().begin();
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
//        Manager.getTM().commit();
//
//        FileLock lock0 = new RandomAccessFile(new File("./target/berkeley_db/0/fileLock.lock"), "rw").getChannel().lock();
//        FileLock lock1 = new RandomAccessFile(new File("./target/berkeley_db/1/fileLock.lock"), "rw").getChannel().lock();
//
//        lock0.release();
//        lock1.release();
//
//        Manager.getTM().begin();
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
//        Manager.getTM().commit();
//        lock0 = new RandomAccessFile(new File("./target/berkeley_db/0/fileLock.lock"), "rw").getChannel().lock();
//        lock1 = new RandomAccessFile(new File("./target/berkeley_db/1/fileLock.lock"), "rw").getChannel().lock();
//
//        lock0.release();
//        lock1.release();
//
//        ctx.close();
//    }

    public void testCommitLoad_2() throws Exception, RollbackException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(getConfigName(), TestJEConnectionFactory.class);
        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");


        Manager.getTM().begin();
        Database db = factoryJE.getTxConnection(0l).openDatabase("test",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        String test = "Test";
        db.put(null, new DatabaseEntry(SerializationHelper.longToBytes(10)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test)));
        Database db1 = factoryJE.getTxConnection(0l).openDatabase("test_1",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        String test1 = "Test1";
        db1.put(null, new DatabaseEntry(SerializationHelper.longToBytes(20)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test1)));
        Manager.getTM().commit();

        FileLock lock0 = new RandomAccessFile(new File("./target/berkeley_db/0/fileLock.lock"), "rw").getChannel().lock();

        lock0.release();

        Manager.getTM().begin();
        db = factoryJE.getTxConnection(0l).openDatabase("test", new DatabaseConfig()
                .setAllowCreate(true).setTransactional(true));
        Cursor cursor = db.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        assertEquals(OperationStatus.SUCCESS, cursor.getNext(key, value, LockMode.DEFAULT));
        assertEquals(10l, SerializationHelper.bytesToLong(key.getData()));
        assertEquals("Test", SerializationHelper.bytesToObject(value.getData()));

        db1 = factoryJE.getTxConnection(0l).openDatabase("test_1",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        Cursor cursor1 = db1.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key1 = new DatabaseEntry();
        DatabaseEntry value1 = new DatabaseEntry();
        assertEquals(OperationStatus.SUCCESS, cursor1.getNext(key1, value1, LockMode.DEFAULT));
        assertEquals(20l, SerializationHelper.bytesToLong(key1.getData()));
        assertEquals("Test1", SerializationHelper.bytesToObject(value1.getData()));
        cursor.close();
        cursor1.close();
        Manager.getTM().commit();
        lock0 = new RandomAccessFile(new File("./target/berkeley_db/0/fileLock.lock"), "rw").getChannel().lock();

        lock0.release();

        ctx.close();
    }

    public void testCommitLoad_3() throws Exception, RollbackException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(getConfigName(), TestJEConnectionFactory.class);
        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");

        Database db = factoryJE.getConnection(0l).openDatabase("test",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        Database db1 = factoryJE.getConnection(0l).openDatabase("test_1",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));

        Manager.getTM().begin();

        String test = "Test";
        db.put(null, new DatabaseEntry(SerializationHelper.longToBytes(10)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test)));

        String test1 = "Test1";
        db1.put(null, new DatabaseEntry(SerializationHelper.longToBytes(20)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test1)));
        Manager.getTM().commit();

        Manager.getTM().begin();
        db = factoryJE.getConnection(0l).openDatabase("test", new DatabaseConfig()
                .setAllowCreate(true).setTransactional(true));
        Cursor cursor = db.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        assertEquals(OperationStatus.SUCCESS, cursor.getNext(key, value, LockMode.DEFAULT));
        assertEquals(10l, SerializationHelper.bytesToLong(key.getData()));
        assertEquals("Test", SerializationHelper.bytesToObject(value.getData()));

        db1 = factoryJE.getConnection(0l).openDatabase("test_1",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        Cursor cursor1 = db1.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key1 = new DatabaseEntry();
        DatabaseEntry value1 = new DatabaseEntry();
        assertEquals(OperationStatus.SUCCESS, cursor1.getNext(key1, value1, LockMode.DEFAULT));
        assertEquals(20l, SerializationHelper.bytesToLong(key1.getData()));
        assertEquals("Test1", SerializationHelper.bytesToObject(value1.getData()));
        cursor.close();
        cursor1.close();
        Manager.getTM().commit();

        factoryJE.closeConnection(0l);
        factoryJE.closeConnection(1l);
        ctx.close();
    }


    public void testRollbacktLoad() throws Exception, RollbackException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(getConfigName(), TestJEConnectionFactory.class);
        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");


        Manager.getTM().begin();
        Database db = factoryJE.getConnection(0l).openDatabase("test",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        String test = "Test";
        db.put(null, new DatabaseEntry(SerializationHelper.longToBytes(10)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test)));
        Database db1 = factoryJE.getConnection(0l).openDatabase("test_1",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        String test1 = "Test1";
        db1.put(null, new DatabaseEntry(SerializationHelper.longToBytes(20)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test1)));
        Manager.getTM().rollback();

        Manager.getTM().begin();
        db = factoryJE.getConnection(0l).openDatabase("test", new DatabaseConfig()
                .setAllowCreate(true).setTransactional(true));
        Cursor cursor = db.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        assertEquals(OperationStatus.NOTFOUND, cursor.getNext(key, value, LockMode.DEFAULT));

        db1 = factoryJE.getConnection(0l).openDatabase("test_1",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        Cursor cursor1 = db1.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key1 = new DatabaseEntry();
        DatabaseEntry value1 = new DatabaseEntry();
        assertEquals(OperationStatus.NOTFOUND, cursor1.getNext(key1, value1, LockMode.DEFAULT));
        cursor.close();
        cursor1.close();
        Manager.getTM().commit();

        factoryJE.closeConnection(0l);
        factoryJE.closeConnection(1l);
        ctx.close();
    }

    public void testInnerTransaction_CommitCommit() throws Exception, RollbackException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(getConfigName(), TestJEConnectionFactory.class);
        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");


        Manager.getTM().begin();
        Database db = factoryJE.getConnection(0l).openDatabase("test",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        String test = "Test";
        db.put(null, new DatabaseEntry(SerializationHelper.longToBytes(10)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test)));

        {
            Manager.getTM().begin();
            Database db1 = factoryJE.getConnection(0l).openDatabase("test",
                    new DatabaseConfig().setAllowCreate(true).setTransactional(true));
            String test1 = "Test1";
            db1.put(null, new DatabaseEntry(SerializationHelper.longToBytes(20)),
                    new DatabaseEntry(SerializationHelper.objectToBytes(test1)));
            Manager.getTM().commit();
        }

        Manager.getTM().commit();

        Manager.getTM().begin();
        db = factoryJE.getConnection(0l).openDatabase("test", new DatabaseConfig()
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

        Manager.getTM().commit();

        factoryJE.closeConnection(0l);
        factoryJE.closeConnection(1l);
        ctx.close();
    }

    public void testInnerTransaction_CommitRollback() throws Exception, RollbackException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(getConfigName(), TestJEConnectionFactory.class);
        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");


        Manager.getTM().begin();
        Database db = factoryJE.getConnection(0l).openDatabase("test",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        String test = "Test";
        db.put(null, new DatabaseEntry(SerializationHelper.longToBytes(10)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test)));

        {
            Manager.getTM().begin();
            Database db1 = factoryJE.getConnection(0l).openDatabase("test",
                    new DatabaseConfig().setAllowCreate(true).setTransactional(true));
            String test1 = "Test1";
            db1.put(null, new DatabaseEntry(SerializationHelper.longToBytes(20)),
                    new DatabaseEntry(SerializationHelper.objectToBytes(test1)));
            Manager.getTM().rollback();
        }

        Manager.getTM().commit();

        Manager.getTM().begin();
        db = factoryJE.getConnection(0l).openDatabase("test", new DatabaseConfig()
                .setAllowCreate(true).setTransactional(true));
        Cursor cursor = db.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        assertEquals(OperationStatus.SUCCESS, cursor.getNext(key, value, LockMode.DEFAULT));
        assertEquals(10l, SerializationHelper.bytesToLong(key.getData()));
        assertEquals("Test", SerializationHelper.bytesToObject(value.getData()));
        assertEquals(OperationStatus.NOTFOUND, cursor.getNext(key, value, LockMode.DEFAULT));
        cursor.close();

        Manager.getTM().commit();

        factoryJE.closeConnection(0l);
        factoryJE.closeConnection(1l);
        ctx.close();
    }

    public void testInnerTransaction_RollbackCommit() throws Exception, RollbackException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(getConfigName(), TestJEConnectionFactory.class);
        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");


        Manager.getTM().begin();
        Database db = factoryJE.getConnection(0l).openDatabase("test",
                new DatabaseConfig().setAllowCreate(true).setTransactional(true));
        String test = "Test";
        db.put(null, new DatabaseEntry(SerializationHelper.longToBytes(10)),
                new DatabaseEntry(SerializationHelper.objectToBytes(test)));

        {
            Manager.getTM().begin();
            Database db1 = factoryJE.getConnection(0l).openDatabase("test",
                    new DatabaseConfig().setAllowCreate(true).setTransactional(true));
            String test1 = "Test1";
            db1.put(null, new DatabaseEntry(SerializationHelper.longToBytes(20)),
                    new DatabaseEntry(SerializationHelper.objectToBytes(test1)));
            Manager.getTM().commit();
        }

        Manager.getTM().rollback();

        Manager.getTM().begin();
        db = factoryJE.getConnection(0l).openDatabase("test", new DatabaseConfig()
                .setAllowCreate(true).setTransactional(true));
        Cursor cursor = db.openCursor(null, new CursorConfig().setReadCommitted(true));
        DatabaseEntry key = new DatabaseEntry();
        DatabaseEntry value = new DatabaseEntry();
        assertEquals(OperationStatus.SUCCESS, cursor.getNext(key, value, LockMode.DEFAULT));
        assertEquals(20l, SerializationHelper.bytesToLong(key.getData()));
        assertEquals("Test1", SerializationHelper.bytesToObject(value.getData()));
        assertEquals(OperationStatus.NOTFOUND, cursor.getNext(key, value, LockMode.DEFAULT));
        cursor.close();

        Manager.getTM().commit();

        factoryJE.closeConnection(0l);
        factoryJE.closeConnection(1l);
        ctx.close();
    }

    public void testJEConnectionFactory1() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(getConfigName(), TestJEConnectionFactory.class);

        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");
        JEConnectionFactory factoryJE2 = (JEConnectionFactory) ctx.getBean("connectionFactory2");

        factoryJE.setLockingTimeout(1000);
        assertEquals(1000, factoryJE.getLockingTimeout());
        assertEquals("./target/berkeley_db/", factoryJE.getPath());
        assertTrue(factoryJE.getEnvironmentConfig().getAllowCreate());
        assertEquals(factoryJE.getTransactionConfig().getDurability(), Durability.COMMIT_SYNC);

        factoryJE.getConnection(1l);
        factoryJE2.getConnection(0l);

        try {
            factoryJE.getConnection(0l);
            fail("Extected " + JEConnectionException.class);
        } catch (JEConnectionException e) {

        }

        factoryJE.setEnvironmentConfig(new EnvironmentConfig().setAllowCreate(false));
        assertFalse(factoryJE.getEnvironmentConfig().getAllowCreate());
        factoryJE.setTransactionConfig(new TransactionConfig().setDurability(Durability.READ_ONLY_TXN));
        assertEquals(factoryJE.getTransactionConfig().getDurability(), Durability.READ_ONLY_TXN);

        factoryJE.closeConnection(0l);
        factoryJE2.closeConnection(0l);
        factoryJE.closeConnection(1l);
        factoryJE2.closeConnection(1l);
        ctx.close();
    }

    public void testActive() throws Exception, RollbackException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(getConfigName(), TestJEConnectionFactory.class);
        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");

        factoryJE.destroy();
        try {
            factoryJE.getConnection(0l);
            fail("Expected " + JEConnectionException.class);
        } catch (JEConnectionException e) {
        }
        Manager.getTM().begin();
        try {
            factoryJE.getTxConnection(0l);
            fail("Expected " + JEConnectionException.class);
        } catch (JEConnectionException e) {
        }

        Manager.getTM().commit();
    }

    public void testGetTxConection() throws Exception, RollbackException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext(getConfigName(), TestJEConnectionFactory.class);
        JEConnectionFactory factoryJE = (JEConnectionFactory) ctx.getBean("connectionFactory");


        try {
            factoryJE.getTxConnection(0l);
            fail("Expected " + JEConnectionException.class);
        } catch (JEConnectionException e) {
        }

        ctx.close();
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
