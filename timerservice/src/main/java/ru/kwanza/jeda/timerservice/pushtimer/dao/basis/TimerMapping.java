package ru.kwanza.jeda.timerservice.pushtimer.dao.basis;

import org.springframework.beans.factory.annotation.Required;

/**
 * @author Michael Yeskov
 */
public class TimerMapping {

    private String tableName;

    private String idField; //не константный тип поля зависит от имплементации делаем get Object Mapper пусть разбирается
    private String stateField;   //!active === (bucketId ==null && expire_time == null )//fired // interrupted
    private String bucketIdField;   // тип тот же
    private String expireTimeField;
    private String creationPointCountField;

    public String getListOfAll5Fields(){
        return idField + "," + stateField + "," + bucketIdField + "," + expireTimeField + "," + creationPointCountField;
    }


    public String getTableName() {
        return tableName;
    }
    @Required
    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getIdField() {
        return idField;
    }
    @Required
    public void setIdField(String idField) {
        this.idField = idField;
    }

    public String getStateField() {
        return stateField;
    }
    @Required
    public void setStateField(String stateField) {
        this.stateField = stateField;
    }

    public String getBucketIdField() {
        return bucketIdField;
    }
    @Required
    public void setBucketIdField(String bucketIdField) {
        this.bucketIdField = bucketIdField;
    }

    public String getExpireTimeField() {
        return expireTimeField;
    }
    @Required
    public void setExpireTimeField(String expireTimeField) {
        this.expireTimeField = expireTimeField;
    }

    public String getCreationPointCountField() {
        return creationPointCountField;
    }
    @Required
    public void setCreationPointCountField(String creationPointCountField) {
        this.creationPointCountField = creationPointCountField;
    }
}
