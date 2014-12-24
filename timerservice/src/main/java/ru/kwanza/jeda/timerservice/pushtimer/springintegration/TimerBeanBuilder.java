package ru.kwanza.jeda.timerservice.pushtimer.springintegration;

import org.springframework.beans.factory.xml.ParserContext;
import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.api.internal.*;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import ru.kwanza.jeda.core.queue.ObjectCloneType;
import ru.kwanza.jeda.core.queue.TransactionalMemoryQueue;
import ru.kwanza.jeda.core.resourcecontroller.FixedBatchSizeResourceController;
import ru.kwanza.jeda.core.springintegration.JedaBeanDefinition;
import ru.kwanza.jeda.core.threadmanager.stage.StageThreadManager;
import ru.kwanza.jeda.timerservice.pushtimer.config.TimerClass;
import ru.kwanza.jeda.timerservice.pushtimer.processor.ExpireTimeProcessor;
import ru.kwanza.jeda.timerservice.pushtimer.springintegration.refs.TimerClassRef;

/**
 * @author Michael Yeskov
 */
class TimerBeanBuilder implements BeanBuilder{
    private static final String DEFAULT_TIMER_CLASS = "jeda.timerservice.default.DefaultTimerClass";

    private JedaBeanDefinition eventProcessorDef = null;
    private JedaBeanDefinition resourceControllerDef = null;
    private JedaBeanDefinition threadManagerDef = null;
    private JedaBeanDefinition timerClassDef = null;
    private JedaBeanDefinition timerClassRefHolderDef = null;
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
        } else if (TimerClassRef.class.isAssignableFrom(beanDefinition.getType())) {
            timerClassRefHolderDef = beanDefinition;
        } else {
            throw new RuntimeException("Unsupported bean definition!");
        }
    }

    public void build(BeanDefinitionBuilder definitionBuilder) {
        definitionBuilder.addPropertyReference("manager", "jeda.IJedaManager");
        definitionBuilder.addPropertyReference("timerManager", "jeda.ITimerManager");
        definitionBuilder.addPropertyReference("timerClassRepository", "jeda.timerservice.TimerClassRepository");

        addPropertyReference("eventProcessor", definitionBuilder, wrapProcessor(eventProcessorDef));
        addPropertyReference("queue", definitionBuilder, generateQueue());
        addPropertyReference("threadManager", definitionBuilder, applyDefaultThreadManager(threadManagerDef));
        addPropertyReference("resourceController", definitionBuilder, applyDefaultResourceController(resourceControllerDef));
        definitionBuilder.addPropertyValue("hasTransaction", "true");

        addTimerClass(definitionBuilder);
    }

    private void addTimerClass(BeanDefinitionBuilder definitionBuilder) {
        String classId = DEFAULT_TIMER_CLASS;
        if (timerClassDef != null) {
            classId = timerClassDef.getId();
        } else if (timerClassRefHolderDef != null) {
            classId = (String)timerClassRefHolderDef.getPropertyValues().getPropertyValue("ref").getValue();
        }
        definitionBuilder.addPropertyReference("timerClass", classId);
    }

    private JedaBeanDefinition applyDefaultThreadManager(JedaBeanDefinition threadManagerDef) {
        if (threadManagerDef != null) {
            return threadManagerDef;
        }
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(StageThreadManager.class);

        definitionBuilder.addConstructorArgValue("TimerProcessor-" + name.toUpperCase());
        definitionBuilder.addConstructorArgReference("jeda.IJedaManager");

        return ParseHelper.generateIdAndRegister(IThreadManager.class, definitionBuilder.getBeanDefinition(), parserContext);
    }



    private JedaBeanDefinition applyDefaultResourceController(JedaBeanDefinition resourceControllerDef) {
        if (resourceControllerDef != null) {
            return resourceControllerDef;
        }
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(FixedBatchSizeResourceController.class);
        definitionBuilder.addConstructorArgValue(1000);

        return  ParseHelper.generateIdAndRegister(IResourceController.class, definitionBuilder.getBeanDefinition(), parserContext);
    }

    private JedaBeanDefinition generateQueue() {
        BeanDefinitionBuilder definitionBuilder = BeanDefinitionBuilder
                .genericBeanDefinition(TransactionalMemoryQueue.class);
        definitionBuilder.addConstructorArgReference("jeda.IJedaManager");

        ObjectCloneType type = ObjectCloneType.SERIALIZE;
        definitionBuilder.addConstructorArgValue(type);
        definitionBuilder.addConstructorArgValue(Integer.MAX_VALUE);

        return ParseHelper.generateIdAndRegister(IQueue.class, definitionBuilder.getBeanDefinition(), parserContext);
    }

    private JedaBeanDefinition wrapProcessor(JedaBeanDefinition eventProcessorDef) {
        BeanDefinitionBuilder defBuilderExpireTimeProcessor = BeanDefinitionBuilder.genericBeanDefinition(ExpireTimeProcessor.class);
        defBuilderExpireTimeProcessor.addPropertyReference("delegate", getId(eventProcessorDef));

        return ParseHelper.generateIdAndRegister(IEventProcessor.class, defBuilderExpireTimeProcessor.getBeanDefinition(), parserContext);
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
