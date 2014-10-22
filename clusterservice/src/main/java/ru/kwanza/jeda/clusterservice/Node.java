package ru.kwanza.jeda.clusterservice;

/**
 * @author Alexander Guzanov
 */
public class Node {
    protected Integer id;
    protected volatile Long lastActivity;

    public Integer getId() {
        return id;
    }

    public Long getLastActivity() {
        return lastActivity;
    }


}
