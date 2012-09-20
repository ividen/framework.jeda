package ru.kwanza.jeda.core.springintegration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;

/**
 * @author Guzanov Alexander
 */
public class FlexFlowBeanDefinition extends AbstractBeanDefinition {
    private Class type;
    private String id;
    private String parentName;

    public FlexFlowBeanDefinition(String id, Class type, AbstractBeanDefinition beanDefinition) {
        super((BeanDefinition) beanDefinition);
        this.type = type;
        this.id = id;
    }

    public String getParentName() {
        return parentName;
    }

    public void setParentName(String parentName) {
        this.parentName = parentName;
    }

    public String getId() {
        return id;
    }

    public Class getType() {
        return type;
    }

    public AbstractBeanDefinition cloneBeanDefinition() {
        return new FlexFlowBeanDefinition(id, type, this);
    }
}
