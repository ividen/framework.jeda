package ru.kwanza.jeda.timerservice.pushtimer.dao.basis;

import org.springframework.beans.factory.annotation.Required;

/**
 * @author Michael Yeskov
 */
public class TimerMapping {

    private String tableName;  //required field

    private String idField = "id"; //не константный тип поля зависит от имплементации делаем get Object Mapper пусть разбирается
    private String stateField = "state";   //!active === (bucketId ==null && expire_time == null )//fired // interrupted
    private String bucketIdField = "bucket_id";   // тип тот же
    private String expireTimeField = "expire_time";
    private String creationPointCountField = "creation_point_count";

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

    public void setIdField(String idField) {
        this.idField = idField;
    }

    public String getStateField() {
        return stateField;
    }

    public void setStateField(String stateField) {
        this.stateField = stateField;
    }

    public String getBucketIdField() {
        return bucketIdField;
    }

    public void setBucketIdField(String bucketIdField) {
        this.bucketIdField = bucketIdField;
    }

    public String getExpireTimeField() {
        return expireTimeField;
    }

    public void setExpireTimeField(String expireTimeField) {
        this.expireTimeField = expireTimeField;
    }

    public String getCreationPointCountField() {
        return creationPointCountField;
    }

    public void setCreationPointCountField(String creationPointCountField) {
        this.creationPointCountField = creationPointCountField;
    }
}
