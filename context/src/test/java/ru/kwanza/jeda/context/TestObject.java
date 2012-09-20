package ru.kwanza.jeda.context;

import java.io.Serializable;

/**
 * @author Dmitry Zagorovsky
 */
public class TestObject extends ObjectContext implements Serializable {

    private String data1 = "testData1";
    private Long data2 = 1240000l;

    public TestObject() {
    }

    public TestObject(String data1) {
        this.data1 = data1;
    }

    public TestObject(Long id, Long version, String data1, Long data2) {
        super(id, version);
        this.data1 = data1;
        this.data2 = data2;
    }

    public String getData1() {
        return data1;
    }

    public void setData1(String data1) {
        this.data1 = data1;
    }

    public Long getData2() {
        return data2;
    }

    public void setData2(Long data2) {
        this.data2 = data2;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TestObject that = (TestObject) o;

        return !(data1 != null ? !data1.equals(that.data1) : that.data1 != null) &&
                !(data2 != null ? !data2.equals(that.data2) : that.data2 != null);

    }

    @Override
    public int hashCode() {
        int result = data1 != null ? data1.hashCode() : 0;
        result = 31 * result + (data2 != null ? data2.hashCode() : 0);
        return result;
    }

}
