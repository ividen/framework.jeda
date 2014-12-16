package ru.kwanza.jeda.timerservice.pushtimer.springintegration;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.internal.*;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.util.StringUtils;
import ru.kwanza.jeda.core.queue.ObjectCloneType;
import ru.kwanza.jeda.core.queue.TransactionalMemoryQueue;
import ru.kwanza.jeda.core.resourcecontroller.FixedBatchSizeResourceController;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinition;
import ru.kwanza.jeda.core.springintegration.SystemStageFactory;
import ru.kwanza.jeda.core.threadmanager.stage.StageThreadManager;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.processor.ExpireTimeProcessor;

/**
 * @author Michael Yeskov
 */
class TimerBeanBuilder {
    private JedaBeanDefinition eventProcessorDef = null;
    private JedaBeanDefinition resourceControllerDef = null;
    private JedaBeanDefinition threadManagerDef = null;
    private JedaBeanDefinition timerClassDef = null;
    private String name;
    private ParserContext parserContext;


    public TimerBeanBuilder(String name, ParserContext parserContext) {
        this.name = name;
        this.parserContext = parserContext;
    }

    public void addBean(JedaBeanDefinition beanDefinition) {
        if (IEventProcessor.class.isAssignableFrom(beanDefinition.getType())) {
            eventProcessorDef = beanDefinition;
        } else if (IResourceController.class.isAssignableFrom(beanDefinition.getType())) {
            resourceControllerDef = beanDefinition;
        } else if (IThreadManager.class.isAssignableFrom(beanDefinition.getType())) {
            threadManagerDef = beanDefinition;
        } else if (TimerClass.class.isAssignableFrom(beanDefinition.getType())) {
            timerClassDef = beanDefinition;
        }
        else {
            throw new RuntimeException("Unsupported bean definition!");
        }
    }

    public void build(BeanDefinitionBuilder definitionBuilder) {
        definitionBuilder.addPropertyReference("manager", "jeda.IJedaManager");
        definitionBuilder.addPropertyReference("timerClassRepository", "timerservice.TimerClassRepository");

        addPropertyReference("eventProcessor", definitionBuilder, wrapProcessor(eventProcessorDef));
        addPropertyReference("queue", definitionBuilder, generateQueue());
        addPropertyReference("threadManager", definitionBuilder, applyDefaultThreadManager(threadManagerDef));
        addPropertyReference("resourceController", definitionBuilder, applyDefaultResourceController(resourceControllerDef));
        definitionBuilder.addPropertyValue("hasTransaction", "true");

        addTimerClass(definitionBuilder);
    }

    private void addTimerClass(BeanDefinitionBuilder definitionBuilder) {
        String classId = "timerservice.default.DefaultTimerClass";
        if (timerClassDef != null) {
            classId = timerClassDef.getId();
        }
        definitionBuilder.addPropertyReference("timerClass", classId);
    }

    private JedaBeanDefinition applyDefaultThreadManager(JedaBeanDefinition threadManagerDef) {
        if (threadManagerDef != null) {
            return threadManagerDef;
        }
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(StageThreadManager.class);

        definitionBuilder.addConstructorArgValue(name.toLowerCase() + "-processor");
        definitionBuilder.addConstructorArgReference("jeda.IJedaManager");

        return register("THREAD_MANAGER", IThreadManager.class, definitionBuilder.getBeanDefinition());
    }



    private JedaBeanDefinition applyDefaultResourceController(JedaBeanDefinition resourceControllerDef) {
        if (resourceControllerDef != null) {
            return resourceControllerDef;
        }
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(FixedBatchSizeResourceController.class);
        definitionBuilder.addConstructorArgValue(1000);

        return  register("RESOURCE_CONTROLLER", IResourceController.class, definitionBuilder.getBeanDefinition());
    }

    private JedaBeanDefinition generateQueue() {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(TransactionalMemoryQueue.class);
        definitionBuilder.addConstructorArgReference("jeda.IJedaManager");

        ObjectCloneType type = ObjectCloneType.SERIALIZE;
        definitionBuilder.addConstructorArgValue(type);
        definitionBuilder.addConstructorArgValue(Integer.MAX_VALUE);

        return register("QUEUE", IQueue.class, definitionBuilder.getBeanDefinition());
    }

    private JedaBeanDefinition wrapProcessor(JedaBeanDefinition eventProcessorDef) {
        BeanDefinitionBuilder defBuilderExpireTimeProcessor = BeanDefinitionBuilder.genericBeanDefinition(ExpireTimeProcessor.class);
        defBuilderExpireTimeProcessor.addPropertyReference("delegate", getId(eventProcessorDef));

        return  register("PROCESSOR", IEventProcessor.class, defBuilderExpireTimeProcessor.getBeanDefinition());
    }

    private JedaBeanDefinition register(String namePostfix, Class clazz, AbstractBeanDefinition beanDefinition) {
        String beanName = name.toUpperCase() + "-" + namePostfix ;
        JedaBeanDefinition definition = new JedaBeanDefinition(beanName , clazz, beanDefinition);
        parserContext.getRegistry().registerBeanDefinition(beanName, definition);
        return definition;
    }

    private void addPropertyReference(String name, BeanDefinitionBuilder definitionBuilder, JedaBeanDefinition beanDefinition) {
        String id = getId(beanDefinition);
        if (id != null) {
            definitionBuilder.addPropertyReference(name, id);
        }
    }

    private String getId(JedaBeanDefinition beanDefinition) {
        return beanDefinition != null ? beanDefinition.getId() : null;
    }
}
