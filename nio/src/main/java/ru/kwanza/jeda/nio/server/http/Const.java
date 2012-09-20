package ru.kwanza.jeda.nio.server.http;

/**
 * @author Guzanov Alexander
 */
public interface Const {
    String SERVER_SLOT_ALIAS = "server";
    int DEFAULT_CONNECTION_IDLE_TIMEOUT = 10000;
    int DEFAULT_FLEX_FLOW_RESPONSE_TIMEOUT = 60 * 1000 * 3;
    int DEFAULT_KEEP_ALIVE_IDLE_TIMEOUT = 30;
    int DEFAULT_KEEP_ALIVE_MAX_REQUESTS_COUNT = 256;
    int DEFAULT_SERVER_CONNECTION_BACKLOG = 10000;
    int DEFUALT_SERVER_SOCKET_SO_TIMEOUT = 10000;
}
