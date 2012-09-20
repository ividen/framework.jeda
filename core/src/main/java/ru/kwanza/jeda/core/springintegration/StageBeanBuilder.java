package ru.kwanza.jeda.core.springintegration;

import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.internal.*;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.StringUtils;

/**
 * @author Guzanov Alexander
 */
class StageBeanBuilder {
    private FlexFlowBeanDefinition admissionControllerDef = null;
    private FlexFlowBeanDefinition eventProcessorDef = null;
    private FlexFlowBeanDefinition queueDef = null;
    private FlexFlowBeanDefinition resourceControllerDef = null;
    private FlexFlowBeanDefinition threadManagerDef = null;
    private String transaction;

    public StageBeanBuilder(String transaction) {
        this.transaction = transaction;
    }

    public void addBean(FlexFlowBeanDefinition beanDefinition) {
        if (IQueue.class.isAssignableFrom(beanDefinition.getType())) {
            queueDef = beanDefinition;
        } else if (IAdmissionController.class.isAssignableFrom(beanDefinition.getType())) {
            admissionControllerDef = beanDefinition;
        } else if (IEventProcessor.class.isAssignableFrom(beanDefinition.getType())) {
            eventProcessorDef = beanDefinition;
        } else if (IResourceController.class.isAssignableFrom(beanDefinition.getType())) {
            resourceControllerDef = beanDefinition;
        } else if (IThreadManager.class.isAssignableFrom(beanDefinition.getType())) {
            threadManagerDef = beanDefinition;
        } else {
            throw new RuntimeException("Unsupported bean definition!");
        }
    }

    public void build(BeanDefinitionBuilder definitionBuilder) {
        definitionBuilder.addPropertyReference("manager", ISystemManager.class.getName());
        addPropertyReference("eventProcessor", definitionBuilder, eventProcessorDef);
        addPropertyReference("queue", definitionBuilder, queueDef);
        addPropertyReference("threadManager", definitionBuilder, threadManagerDef);
        addPropertyReference("admissionController", definitionBuilder, admissionControllerDef);
        addPropertyReference("resourceController", definitionBuilder, resourceControllerDef);
        definitionBuilder.addPropertyValue("hasTransaction",
                Boolean.valueOf(StringUtils.hasText(transaction) ? "true" : transaction));
    }

    private void addPropertyReference(String name, BeanDefinitionBuilder definitionBuilder, FlexFlowBeanDefinition beanDefinition) {
        String id = getId(beanDefinition);
        if (id != null) {
            definitionBuilder.addPropertyReference(name, id);
        }
    }

    private String getId(FlexFlowBeanDefinition beanDefinition) {
        return beanDefinition != null ? beanDefinition.getId() : null;
    }
}
