package ru.kwanza.jeda.core.springintegration;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Guzanov Alexander
 */
public class JedaCoreNamespaceHandler extends NamespaceHandlerSupport {

    public void init() {
        registerBeanDefinitionParser("stage", new StageParser());

        registerBeanDefinitionParser("queue", new CustomQueueParser());
        registerBeanDefinitionParser("memory-queue", new MemoryQueueParser());
        registerBeanDefinitionParser("priority-memory-queue", new PriorityMemoryQueueParser());
        registerBeanDefinitionParser("tx-memory-queue", new TxMemoryQueueParser());
        registerBeanDefinitionParser("tx-priority-memory-queue", new TxPriorityMemoryQueueParser());

        registerBeanDefinitionParser("resource-controller", new CustomResourceControllerParser());
        registerBeanDefinitionParser("static-resource-controller", new StaticResourceControllerParser());
        registerBeanDefinitionParser("fixed-batch-size-resource-controller",
                new FixedBatchSizeResourceControllerParser());
        registerBeanDefinitionParser("smart-resource-controller", new SmartResourceControllerParser());


        registerBeanDefinitionParser("admission-controller", new AdmissionControllerParser());
        registerBeanDefinitionParser("event-processor", new EventProcessorParser());

        registerBeanDefinitionParser("stage-thread-manager", new StageThreadManagerParser());
        registerBeanDefinitionParser("thread-manager", new ThreadManagerParser());
        registerBeanDefinitionParser("shared-thread-manager", new SharedThreadManagerParser());
        registerBeanDefinitionParser("share-strategy-by-input-rate",
                new SharedThreadManagerParser.InputRateStrategyParser());
        registerBeanDefinitionParser("share-strategy-by-thread-count",
                new SharedThreadManagerParser.ThreadCountStrategyParser());
        registerBeanDefinitionParser("share-strategy-by-waiting-time",
                new SharedThreadManagerParser.WaitingTimeStrategyParser());
        registerBeanDefinitionParser("share-strategy-by-input-rate-and-waiting-time",
                new SharedThreadManagerParser.InputRateAndWaitingTimeStrategyParser());
        registerBeanDefinitionParser("share-strategy-by-thread-count-and-waiting-time",
                new SharedThreadManagerParser.ThreadCountAndWaitingTimeStrategyParser());
        registerBeanDefinitionParser("share-strategy-by-queue-size",
                new SharedThreadManagerParser.QueueSizeStrategyParser());
        registerBeanDefinitionParser("share-strategy-by-round-robin",
                new SharedThreadManagerParser.RoundRobinStrategyParser());
        registerBeanDefinitionParser("share-strategy",
                new SharedThreadManagerParser.CustomStrategyParser());

        registerBeanDefinitionParser("flow-bus", new FlowBusParser());
        registerBeanDefinitionParser("context-controller", new ContextControllerParser());

        registerBeanDefinitionDecoratorForAttribute("registered", new RegisterBeanDecorator());
    }
}
