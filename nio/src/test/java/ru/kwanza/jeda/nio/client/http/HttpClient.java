package ru.kwanza.jeda.nio.client.http;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import ru.kwanza.jeda.api.IFlowBus;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.SinkException;

import java.util.Arrays;

/**
 * @author Michael Yeskov
 */
public class HttpClient {
    public static void main(String[] args) throws SinkException, InterruptedException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("test-app-config.xml", HttpClient.class);
        IJedaManager jedaManager = ctx.getBean(IJedaManager.class);
        IFlowBus flowBus = jedaManager.getFlowBus("client-transport-flow-bus");
        HttpFilterChainHolder filterChainHolder = (HttpFilterChainHolder)ctx.getBean("httpFilterChainHolder");


        HttpRequestEvent requestEvent = new HttpRequestEvent(filterChainHolder.getHttpFilterChain(), "Msg 2" );

        final TransactionStatus status1 = jedaManager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        flowBus.put(Arrays.asList(requestEvent));
        jedaManager.getTransactionManager().commit(status1);

        Thread.sleep(10000);


        IDelegatingTransportEvent soapEvent = new ApplicationSoapEvent("Test message", filterChainHolder.getHttpFilterChain());

        final TransactionStatus status2 = jedaManager.getTransactionManager().getTransaction(new DefaultTransactionDefinition(TransactionDefinition.PROPAGATION_REQUIRES_NEW));
        flowBus.put(Arrays.asList(soapEvent));
        jedaManager.getTransactionManager().commit(status2);

        while (true) {
            Thread.currentThread().join(1000);
        }



    }
}
