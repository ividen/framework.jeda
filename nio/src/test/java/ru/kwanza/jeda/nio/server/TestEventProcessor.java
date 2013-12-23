package ru.kwanza.jeda.nio.server;

import ru.kwanza.jeda.api.IEventProcessor;
import ru.kwanza.jeda.nio.server.http.IHttpRequest;
import ru.kwanza.jeda.nio.server.http.IHttpEvent;
import ru.kwanza.jeda.nio.server.http.RequestID;
import ru.kwanza.jeda.nio.server.http.RequestIDException;
import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.http.HttpResponsePacket;
import org.glassfish.grizzly.memory.Buffers;

import java.util.Collection;

/**
 * @author Guzanov Alexander
 */
public class TestEventProcessor implements IEventProcessor<IHttpEvent> {
    public void process(Collection<IHttpEvent> events) {

        for (IHttpEvent e : events) {
            IHttpRequest httpRequest = e.getHttpRequest();
            HttpContent content = httpRequest.getContent();
            HttpRequestPacket request = (HttpRequestPacket) content.getHttpHeader();
            HttpResponsePacket responseHeader = HttpResponsePacket
                    .builder(request).protocol(request.getProtocol()).status(200).contentType("application+xml").build();

            HttpContent build = responseHeader.httpContentBuilder().
                    content(Buffers.wrap(null,
                            "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                                    "<beans xmlns=\"http://www.springframework.org/schema/beans\"\n" +
                                    "       xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                                    "       xmlns:jeda-core=\"http://www.intervale.ru/schema/jeda-core\"\n" +
                                    "       xsi:schemaLocation=\"http://www.springframework.org/schema/beans\n" +
                                    "               http://www.springframework.org/schema/beans/spring-beans-3.0.xsd\n" +
                                    "\n" +
                                    "               http://www.intervale.ru/schema/jeda-core http://www.intervale.ru/schema/jeda-core.xsd\">\n" +
                                    "\n" +
                                    "    <import resource=\"classpath:jeda-core-config.xml\"/>\n" +
                                    "\n" +
                                    "\n" +
                                    "    <bean id=\"jtaTransactionManager\"\n" +
                                    "          class=\"com.atomikos.icatch.jta.AtomikosTransactionManager\"\n" +
                                    "          init-method=\"init\" destroy-method=\"close\">\n" +
                                    "        <property name=\"forceShutdown\" value=\"true\"/>\n" +
                                    "    </bean>\n" +
                                    "\n" +
                                    "    <jeda-core:stage name=\"TestStage1\">\n" +
                                    "        <jeda-core:memory-queue/>\n" +
                                    "        <jeda-core:static-resource-controller/>\n" +
                                    "        <jeda-core:admission-controller\n" +
                                    "                class=\"ru.kwanza.jeda.core.springintegration.TestAdmissionController\"/>\n" +
                                    "        <jeda-core:event-processor class=\"ru.kwanza.jeda.core.springintegration.TestEventProcessor\"/>\n" +
                                    "        <jeda-core:stage-thread-manager threadNamePrefix=\"TestStage1\"/>\n" +
                                    "    </jeda-core:stage>\n" +
                                    "\n" +
                                    "    <jeda-core:stage name=\"TestStage2\">\n" +
                                    "        <jeda-core:memory-queue maxSize=\"10000\"/>\n" +
                                    "        <jeda-core:static-resource-controller batchSize=\"1000\" adjustmentCount=\"1000\" adjustmentInterval=\"2000\"/>\n" +
                                    "        <jeda-core:event-processor class=\"ru.kwanza.jeda.core.springintegration.TestEventProcessor\"/>\n" +
                                    "        <jeda-core:stage-thread-manager threadNamePrefix=\"TestStage2\"/>\n" +
                                    "    </jeda-core:stage>\n" +
                                    "\n" +
                                    "    <jeda-core:stage name=\"TestStage3\">\n" +
                                    "        <jeda-core:priority-memory-queue/>\n" +
                                    "        <jeda-core:static-resource-controller batchSize=\"1000\" threadCount=\"10\"/>\n" +
                                    "        <jeda-core:event-processor class=\"ru.kwanza.jeda.core.springintegration.TestEventProcessor\"/>\n" +
                                    "        <jeda-core:stage-thread-manager threadNamePrefix=\"TestStage3\"/>\n" +
                                    "    </jeda-core:stage>\n" +
                                    "\n" +
                                    "    <jeda-core:stage name=\"TestStage4\">\n" +
                                    "        <jeda-core:priority-memory-queue maxSize=\"10000\"/>\n" +
                                    "        <jeda-core:smart-resource-controller/>\n" +
                                    "        <jeda-core:event-processor class=\"ru.kwanza.jeda.core.springintegration.TestEventProcessor\"/>\n" +
                                    "        <jeda-core:stage-thread-manager threadNamePrefix=\"TestStage4\"/>\n" +
                                    "    </jeda-core:stage>\n" +
                                    "\n" +
                                    "    <jeda-core:stage name=\"TestStage5\" transaction=\"true\">\n" +
                                    "        <jeda-core:tx-memory-queue/>\n" +
                                    "        <jeda-core:smart-resource-controller startBatchSize=\"1\"/>\n" +
                                    "        <jeda-core:event-processor class=\"ru.kwanza.jeda.core.springintegration.TestEventProcessor\"/>\n" +
                                    "        <jeda-core:stage-thread-manager threadNamePrefix=\"TestStage5\"/>\n" +
                                    "    </jeda-core:stage>\n" +
                                    "\n" +
                                    "    <jeda-core:stage name=\"TestStage6\" transaction=\"true\">\n" +
                                    "        <jeda-core:tx-memory-queue maxSize=\"1000\" cloneType=\"SERIALIZE\"/>\n" +
                                    "        <jeda-core:smart-resource-controller startBatchSize=\"1\" maxBatchSize=\"500\"\n" +
                                    "                                                 maxThreadCount=\"10\" processingTimeThreshold=\"8000\"\n" +
                                    "                                                 adjustmentCount=\"2000\" adjustmentInterval=\"2000\" maxElementCount=\"100\"\n" +
                                    "                                                 waitForFillingTimeout=\"2000\"/>\n" +
                                    "        <jeda-core:event-processor class=\"ru.kwanza.jeda.core.springintegration.TestEventProcessor\"/>\n" +
                                    "        <jeda-core:stage-thread-manager threadNamePrefix=\"TestStage6\"/>\n" +
                                    "    </jeda-core:stage>\n" +
                                    "\n" +
                                    "    <jeda-core:stage name=\"TestStage7\" transaction=\"true\">\n" +
                                    "        <jeda-core:tx-memory-queue maxSize=\"1000\" cloneType=\"CLONE\"/>\n" +
                                    "        <jeda-core:smart-resource-controller/>\n" +
                                    "        <jeda-core:event-processor class=\"ru.kwanza.jeda.core.springintegration.TestEventProcessor\"/>\n" +
                                    "        <jeda-core:stage-thread-manager threadNamePrefix=\"TestStage7\"/>\n" +
                                    "    </jeda-core:stage>\n" +
                                    "\n" +
                                    "    <jeda-core:stage name=\"TestStage8\" transaction=\"true\">\n" +
                                    "        <jeda-core:tx-memory-queue maxSize=\"1000\"/>\n" +
                                    "        <jeda-core:smart-resource-controller startBatchSize=\"1\"/>\n" +
                                    "        <jeda-core:event-processor class=\"ru.kwanza.jeda.core.springintegration.TestEventProcessor\"/>\n" +
                                    "        <jeda-core:stage-thread-manager threadNamePrefix=\"TestStage8\"/>\n" +
                                    "    </jeda-core:stage>\n" +
                                    "\n" +
                                    "    <jeda-core:stage name=\"TestStage9\" transaction=\"true\">\n" +
                                    "        <jeda-core:tx-memory-queue cloneType=\"SERIALIZE\"/>\n" +
                                    "        <jeda-core:smart-resource-controller startBatchSize=\"1\" maxBatchSize=\"500\"\n" +
                                    "                                                 maxThreadCount=\"10\" processingTimeThreshold=\"9000\"/>\n" +
                                    "        <jeda-core:event-processor class=\"ru.kwanza.jeda.core.springintegration.TestEventProcessor\"/>\n" +
                                    "        <jeda-core:stage-thread-manager threadNamePrefix=\"TestStage9\"/>\n" +
                                    "\n" +
                                    "    </jeda-core:stage>\n" +
                                    "\n" +
                                    "    <jeda-core:stage name=\"TestStage10\" transaction=\"true\">\n" +
                                    "        <jeda-core:tx-priority-memory-queue/>\n" +
                                    "        <jeda-core:static-resource-controller/>\n" +
                                    "        <jeda-core:event-processor class=\"ru.kwanza.jeda.core.springintegration.TestEventProcessor\"/>\n" +
                                    "        <jeda-core:stage-thread-manager threadNamePrefix=\"TestStage10\"/>\n" +
                                    "    </jeda-core:stage>\n" +
                                    "\n" +
                                    "    <jeda-core:stage name=\"TestStage11\" transaction=\"true\">\n" +
                                    "        <jeda-core:tx-priority-memory-queue maxSize=\"1000\" cloneType=\"SERIALIZE\"/>\n" +
                                    "        <jeda-core:static-resource-controller/>\n" +
                                    "        <jeda-core:event-processor class=\"ru.kwanza.jeda.core.springintegration.TestEventProcessor\"/>\n" +
                                    "        <jeda-core:stage-thread-manager threadNamePrefix=\"TestStage11\"/>\n" +
                                    "    </jeda-core:stage>\n" +
                                    "\n" +
                                    "    <jeda-core:stage name=\"TestStage12\" transaction=\"true\">\n" +
                                    "        <jeda-core:tx-priority-memory-queue maxSize=\"1000\" cloneType=\"CLONE\"/>\n" +
                                    "        <jeda-core:static-resource-controller/>\n" +
                                    "        <jeda-core:event-processor class=\"ru.kwanza.jeda.core.springintegration.TestEventProcessor\"/>\n" +
                                    "        <jeda-core:stage-thread-manager threadNamePrefix=\"TestStage12\"/>\n" +
                                    "    </jeda-core:stage>\n" +
                                    "\n" +
                                    "    <jeda-core:stage name=\"TestStage13\" transaction=\"true\">\n" +
                                    "        <jeda-core:tx-priority-memory-queue maxSize=\"1000\"/>\n" +
                                    "        <jeda-core:static-resource-controller/>\n" +
                                    "        <jeda-core:event-processor class=\"ru.kwanza.jeda.core.springintegration.TestEventProcessor\"/>\n" +
                                    "        <jeda-core:stage-thread-manager threadNamePrefix=\"TestStage13\"/>\n" +
                                    "    </jeda-core:stage>\n" +
                                    "\n" +
                                    "    <jeda-core:stage name=\"TestStage14\" transaction=\"true\">\n" +
                                    "        <jeda-core:tx-priority-memory-queue cloneType=\"SERIALIZE\"/>\n" +
                                    "        <jeda-core:static-resource-controller/>\n" +
                                    "        <jeda-core:event-processor class=\"ru.kwanza.jeda.core.springintegration.TestEventProcessor\"/>\n" +
                                    "        <jeda-core:stage-thread-manager threadNamePrefix=\"TestStage14\"/>\n" +
                                    "    </jeda-core:stage>\n" +
                                    "\n" +
                                    "    <jeda-core:stage name=\"TestStage15\">\n" +
                                    "        <jeda-core:queue id=\"TestQueue2\" class=\"ru.kwanza.jeda.core.springintegration.TestQueue2\"\n" +
                                    "                             factory-method=\"create\">\n" +
                                    "            <constructor-arg index=\"0\" value=\"1\"/>\n" +
                                    "            <constructor-arg index=\"1\" value=\"2\"/>\n" +
                                    "        </jeda-core:queue>\n" +
                                    "        <jeda-core:resource-controller id=\"ResourceController1\"\n" +
                                    "                                           class=\"ru.kwanza.jeda.core.springintegration.ResourceController1\">\n" +
                                    "            <constructor-arg index=\"0\" value=\"1\"/>\n" +
                                    "            <constructor-arg index=\"1\" value=\"2\"/>\n" +
                                    "        </jeda-core:resource-controller>\n" +
                                    "        <jeda-core:event-processor class=\"ru.kwanza.jeda.core.springintegration.TestEventProcessor\"/>\n" +
                                    "        <jeda-core:stage-thread-manager threadNamePrefix=\"TestStage1\"/>\n" +
                                    "    </jeda-core:stage>\n" +
                                    "\n" +
                                    "    <jeda-core:stage name=\"TestStage16\">\n" +
                                    "        <jeda-core:queue id=\"TestQueue1\" class=\"ru.kwanza.jeda.core.springintegration.TestQueue1\">\n" +
                                    "            <constructor-arg index=\"0\" value=\"1\"/>\n" +
                                    "            <constructor-arg index=\"1\" value=\"2\"/>\n" +
                                    "        </jeda-core:queue>\n" +
                                    "        <jeda-core:resource-controller id=\"ResourceController2\"\n" +
                                    "                                           class=\"ru.kwanza.jeda.core.springintegration.ResourceController2\"\n" +
                                    "                                           factory-method=\"create\">\n" +
                                    "            <constructor-arg index=\"0\" value=\"1\" type=\"java.lang.Long\"/>\n" +
                                    "            <constructor-arg index=\"1\" value=\"2\" type=\"java.lang.Long\"/>\n" +
                                    "        </jeda-core:resource-controller>\n" +
                                    "        <jeda-core:event-processor class=\"ru.kwanza.jeda.core.springintegration.TestEventProcessor\"/>\n" +
                                    "        <jeda-core:stage-thread-manager threadNamePrefix=\"TestStage1\" idleTimeout=\"1000\"\n" +
                                    "                                            maxSingleEventAttempt=\"100\"\n" +
                                    "                                            maxThreadCount=\"10\"/>\n" +
                                    "    </jeda-core:stage>\n" +
                                    "\n" +
                                    "    <jeda-core:stage name=\"TestStage17\" transaction=\"true\">\n" +
                                    "        <jeda-core:tx-priority-memory-queue maxSize=\"1000\"/>\n" +
                                    "        <jeda-core:fixed-batch-size-resource-controller batchSize=\"1000\"/>\n" +
                                    "        <jeda-core:event-processor class=\"ru.kwanza.jeda.core.springintegration.TestEventProcessor\"/>\n" +
                                    "        <jeda-core:stage-thread-manager threadNamePrefix=\"TestStage13\"/>\n" +
                                    "    </jeda-core:stage>\n" +
                                    "\n" +
                                    "    <jeda-core:stage name=\"TestStage18\" transaction=\"true\">\n" +
                                    "        <jeda-core:tx-priority-memory-queue maxSize=\"1000\"/>\n" +
                                    "        <jeda-core:fixed-batch-size-resource-controller batchSize=\"1000\" adjustmentCount=\"1000\"\n" +
                                    "                                                            adjustmentInterval=\"2000\" waitForFillingTimeout=\"1000\"/>\n" +
                                    "        <jeda-core:event-processor class=\"ru.kwanza.jeda.core.springintegration.TestEventProcessor\"/>\n" +
                                    "        <jeda-core:stage-thread-manager threadNamePrefix=\"TestStage13\"/>\n" +
                                    "    </jeda-core:stage>\n" +
                                    "\n" +
                                    "</beans>")).
                    build();
            responseHeader.setContentLength(build.getContent().capacity());
            try {
                String requestString = httpRequest.getID().asString();
//                System.out.println(requestString);
                RequestID.findRequest(requestString).finish(build);
            } catch (RequestIDException e1) {
                e1.printStackTrace();
            }

        }
    }
}
