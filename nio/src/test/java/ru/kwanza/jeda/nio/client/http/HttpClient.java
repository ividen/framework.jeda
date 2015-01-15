package ru.kwanza.jeda.nio.client.http;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.kwanza.jeda.api.IFlowBus;
import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.nio.client.ClientMain;
import ru.kwanza.jeda.nio.client.ITransportEvent;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author Michael Yeskov
 */
public class HttpClient {
    public static void main(String[] args) throws SinkException, InterruptedException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("test-app-config.xml", HttpClient.class);
        IJedaManager systemManager = ctx.getBean(IJedaManager.class);
        IFlowBus flowBus = systemManager.getFlowBus("client-transport-flow-bus");
        HttpFilterChainBuilder filterChainBuilder = (HttpFilterChainBuilder)ctx.getBean("filterChainBuilder");

        HttpRequestEvent requestEvent = new HttpRequestEvent(filterChainBuilder, "Test message");

        systemManager.getTransactionManager().begin();
        flowBus.put(Arrays.asList(requestEvent));
        systemManager.getTransactionManager().commit();

        Thread.sleep(10000);

        systemManager.getTransactionManager().begin();
        flowBus.put(Arrays.asList(requestEvent));
        systemManager.getTransactionManager().commit();


        while (true) {
            Thread.currentThread().join(1000);
        }



    }
}
