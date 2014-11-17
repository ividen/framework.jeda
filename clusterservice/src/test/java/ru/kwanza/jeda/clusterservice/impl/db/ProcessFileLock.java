package ru.kwanza.jeda.clusterservice.impl.db;

import ru.kwanza.jeda.clusterservice.IClusteredComponent;
import ru.kwanza.jeda.clusterservice.Node;
import ru.kwanza.jeda.clusterservice.impl.db.orm.BaseComponentEntity;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Alexander Guzanov
 */
public class ProcessFileLock {
    ConcurrentHashMap<String, FileLock> locks = new ConcurrentHashMap<String, FileLock>();
    ConcurrentHashMap<String, RandomAccessFile> files = new ConcurrentHashMap<String, RandomAccessFile>();

    public void lock(IClusteredComponent cmp, Node node) {
        String id = BaseComponentEntity.createId(node, cmp);

        if (locks.get(id) != null) {
            throw new RuntimeException("LockExists!");
        }


        if (!files.containsKey(id)) {
            final File file = new File(id);
            if (!file.exists()) {
                try {
                    file.createNewFile();

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            try {

                files.put(id, new RandomAccessFile(file, "rw"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        final RandomAccessFile raf = files.get(id);

        final FileLock fileLock;
        try {
            fileLock = raf.getChannel().tryLock();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        if (fileLock == null) {
            throw new RuntimeException("Can't lock file " + id);
        }
        locks.put(id, fileLock);

    }


    public void unlock(IClusteredComponent cmp, Node node) {
        String id = BaseComponentEntity.createId(node, cmp);

        final FileLock fl = locks.get(id);
        if (fl == null) {
            throw new RuntimeException("LockNOTExists!");
        }

        locks.remove(id);

        final FileChannel channel = fl.channel();

        try {
            fl.release();
            channel.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


}
