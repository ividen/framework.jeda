package ru.kwanza.jeda.nio.server.http;

import ru.kwanza.jeda.api.IJedaManager;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;

/**
 * @author Guzanov Alexander
 */
public final class RequestID implements Serializable {
    String uniqueId;
    private String serverName;

    private static String encode(String str) {
        try {
            return URLEncoder.encode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    private static String decode(String str) {
        try {
            return URLDecoder.decode(str, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null;
        }
    }

    public static RequestID parse(String requestString) throws RequestIDException {
        String[] split = requestString.split("&");
        if (split.length != 2) {
            throw new RequestIDException("Wrong requestId!");
        }
        try {
            return new RequestID(decode(split[1]), decode(split[0]));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to parse orderParamsString", e);
        }
    }

    public static IHttpRequest findRequest(IJedaManager manager,RequestID requestID) throws RequestIDException {
        HttpServer httpServer = manager.<HttpServer>resolveObject(requestID.serverName);
        if (httpServer == null) {
            throw new RequestIDException("HTTP server not found in jeda structure!");
        }
        IHttpRequest result = httpServer.findSuspendedRequest(requestID.uniqueId);
        if (result == null) {
            throw new RequestIDException("Request doesn't exists!");
        }
        return result;
    }

    public static IHttpRequest findRequest(IJedaManager manager,String requestIDString) throws RequestIDException {
        return findRequest(manager,RequestID.parse(requestIDString));
    }

    protected RequestID(String uniqueId, String serverName) {
        this.uniqueId = uniqueId;
        this.serverName = serverName;
    }

    public String asString() {
        StringBuilder sb = new StringBuilder();
        return sb.append(encode(serverName)).append('&').append(encode(uniqueId)).toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        RequestID requestID = (RequestID) o;

        if (serverName != null ? !serverName.equals(requestID.serverName) : requestID.serverName != null) return false;
        if (uniqueId != null ? !uniqueId.equals(requestID.uniqueId) : requestID.uniqueId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = uniqueId != null ? uniqueId.hashCode() : 0;
        result = 31 * result + (serverName != null ? serverName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "RequestID{" +
                "uniqueId='" + uniqueId + '\'' +
                ", serverName='" + serverName + '\'' +
                '}';
    }
}
