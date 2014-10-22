package ru.kwanza.jeda.nio.server.http;

import ru.kwanza.jeda.api.IJedaManager;
import ru.kwanza.jeda.api.internal.IJedaManagerInternal;
import ru.kwanza.jeda.nio.utils.HttpUtil;
import org.glassfish.grizzly.http.HttpPacket;
import org.glassfish.grizzly.http.HttpRequestPacket;
import org.glassfish.grizzly.threadpool.GrizzlyExecutorService;
import org.glassfish.grizzly.threadpool.ThreadPoolConfig;
import org.glassfish.grizzly.utils.DelayedExecutor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Guzanov Alexander
 */
public class HttpServer implements IHttpServer {
    public static final Logger logger = LoggerFactory.getLogger(HttpServer.class);

    private AtomicLong counter = new AtomicLong(0l);
    private ConcurrentMap<Pattern, IHttpHandler> regexpToHandlers = new ConcurrentHashMap<Pattern, IHttpHandler>();
    private ConcurrentMap<String, EntryPoint> entryPoints = new ConcurrentHashMap<String, EntryPoint>();
    private ConcurrentMap<String, IHttpHandler> uriToHandlers = new ConcurrentHashMap<String, IHttpHandler>();
    private ConcurrentMap<String, RequestImpl> requests = new ConcurrentHashMap<String, RequestImpl>();
    private DelayedExecutor.DelayQueue<RequestImpl> suspendedRequests;

    private DelayedExecutor suspendedDelayExecutor;
    private ExecutorService executorService;

    private String name;
    private ThreadPoolConfig threadPoolConfig;
    private int keepAliveIdleTimeout = Const.DEFAULT_KEEP_ALIVE_IDLE_TIMEOUT;
    private int keepAliveMaxRequestsCount = Const.DEFAULT_KEEP_ALIVE_MAX_REQUESTS_COUNT;


    private class SuspendedRequestWorker implements
            DelayedExecutor.Worker<RequestImpl> {
        public boolean doWork(final RequestImpl element) {
            try {
                if (element.timedOutHandler != null) {
                    try {
                        element.timedOutHandler.onTimedOut(element);
                    } catch (Throwable e) {
                        logger.error("Error invoking ITimedOutHandler", e);
                        element.finish(HttpUtil.create500((HttpRequestPacket) element.getContent().getHttpHeader(), e));
                    }

                    if (!element.finished) {
                        element.finish(createDefaultTimeoutResponse(element));
                    }
                }
                element.finish(createDefaultTimeoutResponse(element));
            } finally {
                requests.remove(element.getID().uniqueId);
            }

            return true;
        }

        private HttpPacket createDefaultTimeoutResponse(RequestImpl element) {
            return HttpUtil.create500((HttpRequestPacket) element.getContent().getHttpHeader(),
                    "Timed out processing request!");
        }
    }

    private class SuspendedRequestResolver implements
            DelayedExecutor.Resolver<RequestImpl> {
        public boolean removeTimeout(final RequestImpl element) {
            if (element.suspendedTimestamp != DelayedExecutor.UNSET_TIMEOUT) {
                element.suspendedTimestamp = DelayedExecutor.UNSET_TIMEOUT;
                return true;
            }

            return false;
        }

        public Long getTimeoutMillis(final RequestImpl element) {
            return element.suspendedTimestamp;
        }

        public void setTimeoutMillis(final RequestImpl element, final long timeoutMillis) {
            element.suspendedTimestamp = timeoutMillis;
        }
    }

    public HttpServer(IJedaManagerInternal manager, String name) {
        this.name = name;
        this.threadPoolConfig = ThreadPoolConfig.defaultConfig();
        this.threadPoolConfig.setCorePoolSize(1);
        this.threadPoolConfig.setMaxPoolSize(Runtime.getRuntime().availableProcessors());
        this.threadPoolConfig.setPoolName("HttpServer(" + name + ") Servants");
        manager.registerObject(name, this);
    }

    public String getName() {
        return name;
    }

    public boolean registerHandler(String uri, IHttpHandler handler) {
        if (null == uriToHandlers.putIfAbsent(uri, handler)) {
            return true;
        }

        return false;
    }

    public boolean registerHandler(Pattern pattern, IHttpHandler handler) {
        if (null == regexpToHandlers.putIfAbsent(pattern, handler)) {
            return true;
        }

        return false;
    }

    public ExecutorService getExecutorService() {
        return executorService;
    }

    public int getKeepAliveIdleTimeout() {
        return keepAliveIdleTimeout;
    }

    public void setKeepAliveIdleTimeout(int keepAliveIdleTimeout) {
        this.keepAliveIdleTimeout = keepAliveIdleTimeout;
    }

    public int getKeepAliveMaxRequestsCount() {
        return keepAliveMaxRequestsCount;
    }

    public void setKeepAliveMaxRequestsCount(int keepAliveMaxRequestsCount) {
        this.keepAliveMaxRequestsCount = keepAliveMaxRequestsCount;
    }

    public ThreadPoolConfig getThreadPoolConfig() {
        return threadPoolConfig;
    }

    public void setThreadPoolConfig(ThreadPoolConfig threadPoolConfig) {
        this.threadPoolConfig = threadPoolConfig;
    }

    public void destroy() {
        for (EntryPoint entryPoint : entryPoints.values()) {
            try {
                entryPoint.destroy();
            } catch (Throwable e) {
                logger.error("HttpServer(" + getName() +
                        ":" + entryPoint.getName() + ") Error destroying entry point.", e);
            }
        }
        try {
            suspendedDelayExecutor.stop();
        } catch (Throwable e) {
            logger.error("Error stoping delay executor!", e);
        }
        this.executorService.shutdown();
    }

    public IHttpRequest findSuspendedRequest(String uniqueId) {
        return requests.get(uniqueId);
    }

    public void init() {
        this.executorService = GrizzlyExecutorService.createInstance(this.threadPoolConfig);
        this.suspendedDelayExecutor = new DelayedExecutor(this.executorService);
        this.suspendedRequests = suspendedDelayExecutor.createDelayQueue(new SuspendedRequestWorker(),
                new SuspendedRequestResolver());
        suspendedDelayExecutor.start();
    }

    public boolean registerEntryPoint(EntryPoint entryPoint) {
        if (null == entryPoints.putIfAbsent(entryPoint.getName(), entryPoint)) {
            entryPoint.init(this);
            return true;
        }

        return false;
    }

    IHttpHandler findHandler(String uri) {
        IHttpHandler result = uriToHandlers.get(uri);
        if (result == null) {
            for (Map.Entry<Pattern, IHttpHandler> e : regexpToHandlers.entrySet()) {
                Pattern key = e.getKey();
                Matcher matcher = key.matcher(uri);
                if (matcher.find()) {
                    IHttpHandler putIfAbsent;
                    if ((putIfAbsent = uriToHandlers.putIfAbsent(uri, e.getValue())) == null) {
                        result = e.getValue();
                    } else {
                        result = putIfAbsent;
                    }
                }
            }
        }

        return result;
    }

    void finish(RequestImpl request, HttpPacket result) {
        suspendedRequests.remove(request);
        RequestImpl remove = requests.remove(request.getID().uniqueId);
        if (remove != null) {
            remove.finished = true;
            remove.context.write(result, remove);
        }
    }

    String nextUID() {
        return String.valueOf(System.currentTimeMillis()) + "-" + String.valueOf(counter.incrementAndGet());
    }

    void suspend(RequestImpl request) {
        request.context.suspend();
        suspendedRequests.add(request, request.timeout, TimeUnit.MILLISECONDS);
        requests.put(request.getID().uniqueId, request);
    }
}
