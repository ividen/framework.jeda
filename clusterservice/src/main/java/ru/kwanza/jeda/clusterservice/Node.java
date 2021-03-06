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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || !(o instanceof Node)) return false;

        Node node = (Node) o;

        if (!getId().equals(node.getId())) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public String toString() {
        return "Node{" +
                "id=" + id +
                ", lastActivity=" + lastActivity +
                ", pid='" + pid + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}
