package ru.kwanza.jeda.core.queue;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

/**
 * @author Guzanov Alexander
 */
class ObjectOutputStreamEx extends ObjectOutputStream {
    private int objCount = 0;

    public ObjectOutputStreamEx(OutputStream out) throws IOException {
        super(out);
    }

    public int getObjCount() {
        return objCount;
    }

    public void writeObjectAndCount(Object obj) throws IOException {
        super.writeObject(obj);
        objCount++;
    }
}
