package ru.kwanza.jeda.nio.client;

import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.filterchain.*;
import org.glassfish.grizzly.http.*;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.memory.Buffers;
import org.glassfish.grizzly.ssl.SSLEngineConfigurator;
import org.glassfish.grizzly.ssl.SSLFilter;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import ru.kwanza.jeda.api.AbstractEvent;
import ru.kwanza.jeda.api.IFlowBus;
import ru.kwanza.jeda.api.Manager;
import ru.kwanza.jeda.api.SinkException;
import ru.kwanza.jeda.nio.server.http.JKSEntryPointKeystore;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Guzanov Alexander
 */
public class Main {

    static AtomicLong counter = new AtomicLong(0);
    static volatile long ts;
    public static final int BATCH_SIZE = 100;
    public static final int ITER = 2;
    public static final long INT = BATCH_SIZE * ITER;
    public static long M = 1;

    public static class ResponseHandle extends AbstractFilter {

        public NextAction handleRead(FilterChainContext ctx) throws IOException {
            Object message = ctx.getMessage();
            if (message instanceof HttpContent) {
                HttpContent content = (HttpContent) message;

                if (content.isLast() == true) {
                    long l = counter.incrementAndGet();
                    if (l == 1) {
                        ts = System.currentTimeMillis();
                    } else if (l == M * INT) {
                        ts = System.currentTimeMillis() - ts;
                        System.out.println(M * INT * 1000 / ts);
                    }

                    System.out.println(content.getContent().toStringContent());
                    // todo aguzanov Работа с client http обобшить код в части определения того, что делать с соединением: закрывать или возвращать  пул
//                    if (HttpUtil.isMarkForClose(content)) {
//                        closeConnection(ctx);
//                    } else {
//                    System.out.println(l);
                    releaseConnection(ctx);
//                    }

                }

            } else {
                System.out.println("not last");
            }

            return super.handleRead(ctx);
        }

        @Override
        public void exceptionOccurred(FilterChainContext ctx, Throwable error) {
            super.exceptionOccurred(ctx, error);
            error.printStackTrace();
            long l = counter.incrementAndGet();
            if (l == 1) {
                ts = System.currentTimeMillis();
            } else if (l == M * INT) {
                ts = System.currentTimeMillis() - ts;
                System.out.println(M * INT * 1000 / ts);
            }
        }

        public void handleConnectError(ITransportEvent event, Throwable e) {
            System.out.println("Connect Error for " + event);
            e.printStackTrace();
            long l = counter.incrementAndGet();
            if (l == 1) {
                ts = System.currentTimeMillis();
            } else if (l == M * INT) {
                ts = System.currentTimeMillis() - ts;
                System.out.println(M * INT * 1000 / ts);
            }
        }
    }

    public static class TestTransportEvent extends AbstractEvent implements ITransportEvent {
        private String uri;
        public static final String txt =
                "<?xml version='1.0' encoding='utf-8'?><soapenv:Envelope xmlns:soapenv=\"http://www.w3.org/2003/05/soap-envelope\"><soapenv:Body><ns1:CPAReq xmlns:ns1=\"http://twophaseinteraction.mts.ru/xsd\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:type=\"ns1:CPAReqMessage\" ns1:messageId=\"0\" ns1:version=\"1.5\"><ns1:Client /><ns1:Provider><ns1:id>3868638A697715F0F186DA2C7CB1CDC3</ns1:id></ns1:Provider><ns1:Payment><ns1:date>2012-03-06T01:21:58.052+03:00</ns1:date><ns1:amount>1000</ns1:amount><ns1:currency>810</ns1:currency><ns1:exponent>2</ns1:exponent><ns1:Params><ns1:Param ns1:name=\"currency\">810</ns1:Param></ns1:Params></ns1:Payment><ns1:pcentreTrxId>E4131D2C074AEFB292DE87EE800E625A</ns1:pcentreTrxId></ns1:CPAReq></soapenv:Body></soapenv:Envelope>";
        private InetSocketAddress endpoint;

        public static FilterChain chain;
        private ConnectionConfig config;
        private boolean isSSL;

        public TestTransportEvent(URL url, boolean iskeepAlive) {
            this.endpoint = new InetSocketAddress(url.getHost(), url.getPort() == -1 ? 80 : url.getPort());
            this.uri = url.getPath();
            this.isSSL = url.getProtocol().equals("https");
            this.config = new ConnectionConfig(endpoint,/*iskeepAlive*/true, true, 1000);
        }

        public ConnectionConfig getConnectionConfig() {
            return config;
        }

        public FilterChain getProcessingFilterChain() {
//            return chain;

            FilterChainBuilder builder = FilterChainBuilder.stateless();

            builder.add(new TransportFilter());
            if (isSSL) {
                try {
                    SSLContext sslContext = SSLContext.getInstance("TLS");

                    JKSEntryPointKeystore keystore = new JKSEntryPointKeystore("J:/mykeystore.jks", "a12345678");
                    keystore.init(null, null);
                    sslContext.init(keystore.getKeyManager(), keystore.getTrustManagers(), null);

                    SSLEngineConfigurator engineConfigurator = new SSLEngineConfigurator(sslContext, true, true, true);
                    SSLFilter filter = new SSLFilter(null, engineConfigurator);
                    builder.add(filter);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }

            HttpClientFilter httpClientFilter = new HttpClientFilter() {

            };
            httpClientFilter.addTransferEncoding(new FixedLengthTransferEncoding());
            builder.add(httpClientFilter);
            builder.add(new ResponseHandle());

            return builder.build();
        }

        public Object getContent() {
            Buffer wrap = Buffers.wrap(null, txt);
            HttpRequestPacket.Builder requestBuilder = HttpRequestPacket.builder();

            requestBuilder.method("POST").uri(uri).protocol(Protocol.HTTP_1_1).chunked(false).contentLength(wrap.capacity())
                    .header(Header.Host, config.getEndpoint().getHostName() + ":" + config.getEndpoint().getPort())
                    .contentType("application/soap+xml");
            if (config.isKeepAlive()) {
                requestBuilder.header(Header.Connection, "Keep-Alive");
            } else {
                requestBuilder.header(Header.Connection, "close");
            }
            return HttpContent.builder(requestBuilder.build()).content(wrap).last(true).build();
        }

        public String getContextId() {
            return null;
        }

        @Override
        public String toString() {
            return "TestTransportEvent{" +
                    "uri='" + uri + '\'' +
                    ", endpoint=" + endpoint +
                    '}';
        }

    }

    public static void main(String[] args) throws InterruptedException, IOException, SinkException {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("application.xml", Main.class);

        IFlowBus flowBus = Manager.getFlowBus("client-transport-flow-bus");

        URL url1 = new URL("http://localhost:88/console/ttp-ticketservice-controller/security/config");
//        URL url1 = new URL("http://wwww.lenta.ru/");
//        URL url1 = new URL("http://10.1.3.145:8080/agregator-1.5/emulator/");
//        URL url2 = new URL("http://10.1.2.246:8080/agregator-1.5/emulator/");
        M = 1;

        for (int i = 0; i < ITER; i++) {
            ArrayList<ITransportEvent> events = new ArrayList<ITransportEvent>(BATCH_SIZE);
            for (int j = 0; j < BATCH_SIZE; j++) {
//                events.add(new TestTransportEvent(url0));
                events.add(new TestTransportEvent(url1, true));
//                events.add(new TestTransportEvent(url2, true));
            }

            Collection collection = flowBus.tryPut(events);

            if (collection != null) {
                System.out.println("clogged");
            }

//            Thread.sleep(1000);
        }

        System.out.println("All putted");

        while (true) {
            Thread.currentThread().join(10000);
        }

    }
}
