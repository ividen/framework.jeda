package ru.kwanza.jeda.clusterservice;

/**
 * @author Alexander Guzanov
 */
public class Node {
    protected Integer id;
    protected Long lastActivity;
    protected String pid;
    protected String ipAddress;

    public Node(int id) {
        this.id = id;
    }

    protected Node() {
    }

    public Integer getId() {
        return id;
    }

    public Long getLastActivity() {
        return lastActivity;
    }

    public String getPid() {
        return pid;
    }

    public String getIpAddress() {
        return ipAddress;
    }
}
