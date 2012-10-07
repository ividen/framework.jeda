package ru.kwanza.jeda.nio.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

/**
 * //todo aguzanov возможность устанавливать через системные свойства параметры соединения к конкретному адресу через системные свойства
 *
 * @author Guzanov Alexander
 */
class DefaultConnectionPoolConfigurator implements IConnectionPoolConfigurator {
    public static final String KEEP_ALIVE_TIMEOUT = DefaultConnectionPoolConfigurator.class.getName() + ".KEEP_ALIVE_TIMEOUT";
    public static final String POOL_SIZE = DefaultConnectionPoolConfigurator.class.getName() + ".POOL_SIZE";
    public static final String MAX_REQUEST_COUNT = DefaultConnectionPoolConfigurator.class.getName() + ".MAX_REQUEST_COUNT";

    public static final int DEFAULT_CONNECTION_POOL_SIZE = 1000;
    public static final long DEFAULT_MAX_REQUEST_COUNT = -1;
    public static final int DEFAULT_KEEP_ALIVE_TIMEOUT = 60 * 1000;
    private static final Logger logger = LoggerFactory.getLogger(DefaultConnectionPoolConfigurator.class);

    private int poolSize = DEFAULT_CONNECTION_POOL_SIZE;
    private long keepAliveTimeout = DEFAULT_KEEP_ALIVE_TIMEOUT;
    private long maxRequestCount = DEFAULT_MAX_REQUEST_COUNT;

    DefaultConnectionPoolConfigurator() {
        String sPoolSize = System.getProperty(POOL_SIZE);
        if (sPoolSize != null) {
            try {
                poolSize = Integer.valueOf(sPoolSize);
            } catch (NumberFormatException e) {
                logger.warn("Value {} of property {} must be interger!", sPoolSize, POOL_SIZE);

            }
        }

        String sKeepAliveTimeout = System.getProperty(KEEP_ALIVE_TIMEOUT);
        if (sKeepAliveTimeout != null) {
            try {
                keepAliveTimeout = Long.valueOf(keepAliveTimeout);
            } catch (NumberFormatException e) {
                logger.warn("Value {} of property {} must be long!", sKeepAliveTimeout, KEEP_ALIVE_TIMEOUT);
            }
        }

        String sMaxRequestCount = System.getProperty(MAX_REQUEST_COUNT);
        if (sMaxRequestCount != null) {
            try {
                maxRequestCount = Long.valueOf(keepAliveTimeout);
            } catch (NumberFormatException e) {
                logger.warn("Value {} of property {} must be long!", sMaxRequestCount, MAX_REQUEST_COUNT);
            }
        }
    }

    public long getKeepAliveTimeout(InetSocketAddress address) {
        return keepAliveTimeout;
    }

    public long getMaxRequestCount(InetSocketAddress address) {
        return maxRequestCount;
    }

    public int getPoolSize(InetSocketAddress address) {
        return poolSize;
    }


}
