package ru.kwanza.jeda.timerservice.pushtimer.consuming;

/**
 * @author Michael Yeskov
 */
public class NodeId {
    private Integer currentNodeId;
    private Integer repairedNodeId;

    public NodeId(Integer currentNodeId, Integer repairedNodeId) {
        this.currentNodeId = currentNodeId;
        this.repairedNodeId = repairedNodeId;
    }

    public Integer getCurrentNodeId() {
        return currentNodeId;
    }

    public Integer getRepairedNodeId() {
        return repairedNodeId;
    }

    public Integer getEffectiveNodeId(){
        return repairedNodeId == null ? currentNodeId : repairedNodeId;
    }

    public boolean isInRepairMode() {
        return repairedNodeId != null;
    }

    @Override
    public String toString() {
        return "Node" + currentNodeId + (repairedNodeId == null ? "" : "_Repair" + repairedNodeId);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NodeId)) return false;

        NodeId nodeId = (NodeId) o;

        if (!currentNodeId.equals(nodeId.currentNodeId)) return false;
        if (repairedNodeId != null ? !repairedNodeId.equals(nodeId.repairedNodeId) : nodeId.repairedNodeId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = currentNodeId.hashCode();
        result = 31 * result + (repairedNodeId != null ? repairedNodeId.hashCode() : 0);
        return result;
    }
}
