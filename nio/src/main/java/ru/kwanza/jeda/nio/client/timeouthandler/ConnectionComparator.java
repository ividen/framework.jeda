package ru.kwanza.jeda.nio.client.timeouthandler;

import org.glassfish.grizzly.Connection;
import ru.kwanza.jeda.nio.client.AbstractFilter;

import java.util.Comparator;

/**
* @author Alexander Guzanov
*/
public final class ConnectionComparator implements Comparator<Connection> {

    public int compare(Connection o1, Connection o2) {
        final long l = TimeoutHandler.TIMEOUT.get(o1).timestamp.get() - TimeoutHandler.TIMEOUT.get(o2).timestamp.get();
        return l > 0 ? 1 : l < 0 ? -1 : 0;
    }
}
