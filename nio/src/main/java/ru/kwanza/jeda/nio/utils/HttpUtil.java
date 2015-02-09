package ru.kwanza.jeda.nio.utils;

import ru.kwanza.jeda.nio.server.http.IHttpRequest;
import org.glassfish.grizzly.Buffer;
import org.glassfish.grizzly.http.*;
import org.glassfish.grizzly.http.util.Header;
import org.glassfish.grizzly.memory.Buffers;

import java.io.ByteArrayOutputStream;
import java.io.CharConversionException;
import java.io.PrintStream;
import java.net.URL;

/**
 * @author Guzanov Alexander
 */
public abstract class HttpUtil {

    public static boolean isMarkForClose(HttpContent content) {
        final HttpHeader httpHeader = content != null ? content.getHttpHeader() : null;
        final String header = httpHeader != null ? httpHeader.getHeader(Header.Connection) : null;
        return header == null || "CLOSE".equals(header.toUpperCase());
    }

    public static HttpPacket create500(IHttpRequest request, Throwable e) {
        return create500(getRequestPacket(request), e);
    }

    public static HttpPacket create500(HttpRequestPacket request, Throwable e) {
        final HttpResponsePacket responseHeader;
        responseHeader =
                HttpResponsePacket.builder(request).protocol(request.getProtocol()).status(500).contentLength(0)
                        .contentType("text/html")
                        .reasonPhrase("Internal Server Error: " + e.getLocalizedMessage()).build();

        final ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        final PrintStream printStream = new PrintStream(byteArrayOutputStream);
        e.printStackTrace(printStream);
        printStream.close();
        final HttpContent content =
                responseHeader.httpContentBuilder().content(Buffers.wrap(null, byteArrayOutputStream.toByteArray()))
                        .build();
        responseHeader.setContentLength(content.getContent().capacity());
        return content;
    }

    public static HttpPacket create500(HttpRequestPacket request, String responsePhrase) {
        return createResponse(request, 500, responsePhrase);
    }

    public static HttpPacket create500(IHttpRequest request, String responsePhrase) {
        return createResponse(getRequestPacket(request), 500, responsePhrase);
    }

    private static HttpRequestPacket getRequestPacket(IHttpRequest request) {
        return (HttpRequestPacket) request.getContent().getHttpHeader();
    }

    public static HttpPacket createRequest(URL url, Method method, boolean keepAlive, String contentType, String data) {
        final Buffer wrap = Buffers.wrap(null, data);
        return createRequest(url, method, wrap, contentType, keepAlive);
    }

    public static HttpPacket createRequest(URL url, Method method, boolean keepAlive, String contentType, byte[] data) {
        final Buffer wrap = Buffers.wrap(null, data);
        return createRequest(url, method, wrap, contentType, keepAlive);
    }

    public static HttpPacket createPOSTRequest(URL url, boolean keepAlive, String contentType, String data) {
        final Buffer wrap = Buffers.wrap(null, data);
        return createRequest(url, Method.POST, wrap, contentType, keepAlive);
    }

    public static HttpPacket createPOSTRequest(URL url, boolean keepAlive, String contentType, byte[] data) {
        final Buffer wrap = Buffers.wrap(null, data);
        return createRequest(url, Method.POST, wrap, contentType, keepAlive);
    }

    public static HttpPacket createGETRequest(URL url, boolean keepAlive, String contentType) {
        final HttpRequestPacket.Builder requestBuilder = HttpRequestPacket.builder();
        requestBuilder.method("POST").uri(url.getPath()).protocol(Protocol.HTTP_1_1).chunked(false).method(Method.POST)
                .contentLength(0).header(Header.Host, url.getHost() + ":" + url.getPort()).contentType(contentType);
        if (keepAlive) {
            requestBuilder.header(Header.Connection, "Keep-Alive");
        } else {
            requestBuilder.header(Header.Connection, "close");
        }
        return requestBuilder.build();
    }

    public static HttpPacket create404(HttpRequestPacket request) throws CharConversionException {
        return createResponse(request, 404,
                "Handler not registered for  corresponding URI: " + request.getRequestURIRef().getDecodedURI());
    }

    public static HttpResponsePacket createResponse(IHttpRequest request, int code, String responsePhrase) {
        return createResponse(getRequestPacket(request), code, responsePhrase);
    }

    public static HttpResponsePacket createResponse(HttpRequestPacket request, int code, String responsePhrase) {
        return HttpResponsePacket.builder(request).protocol(request.getProtocol()).status(code).contentLength(0)
                .reasonPhrase(responsePhrase).build();
    }

    public static HttpPacket createResponse(HttpRequestPacket request, int code, String responsePhrase,
                                            String contentType, String data) {
        final HttpResponsePacket responseHeader =
                HttpResponsePacket.builder(request).protocol(request.getProtocol()).status(code)
                        .contentType(contentType).reasonPhrase(responsePhrase).build();
        final HttpContent content = responseHeader.httpContentBuilder().content(Buffers.wrap(null, data)).build();
        responseHeader.setContentLength(content.getContent().capacity());
        return content;
    }

    public static HttpPacket createResponse(IHttpRequest request, int code, String responsePhrase, String contentType,
                                            String data) {
        return createResponse(getRequestPacket(request), code, responsePhrase, contentType, data);
    }

    public static HttpPacket createRedirect(HttpRequestPacket request, String location) {
        final HttpResponsePacket responseHeader =
                HttpResponsePacket.builder(request).protocol(request.getProtocol()).status(302).contentLength(0)
                        .header("location", location).build();
        return responseHeader.httpContentBuilder().build();
    }

    public static HttpPacket createRedirect(IHttpRequest request, String location) {
        return createRedirect(getRequestPacket(request), location);
    }

    public static HttpPacket createResponse(HttpRequestPacket request, String contentType, String charsetEncoding,
                                            byte[] data) {
        final HttpResponsePacket responseHeader =
                HttpResponsePacket.builder(request).protocol(request.getProtocol()).status(200).contentType(contentType)
                        .build();
        final HttpContent content = responseHeader.httpContentBuilder().content(Buffers.wrap(null, data)).build();
        responseHeader.setContentLength(content.getContent().capacity());
        if (charsetEncoding != null) {
            responseHeader.setCharacterEncoding(charsetEncoding);
        }
        return content;
    }

    public static HttpPacket createResponse(HttpRequestPacket request, String contentType, byte[] data) {
        return createResponse(request, contentType, null, data);
    }

    public static HttpPacket createResponse(IHttpRequest request, String contentType, String charsetEncoding,
                                            byte[] data) {
        return HttpUtil.createResponse(getRequestPacket(request), contentType, charsetEncoding, data);
    }

    public static HttpPacket createResponse(IHttpRequest request, String contentType, byte[] data) {
        return createResponse(request, contentType, null, data);
    }

    private static HttpPacket createRequest(URL url, Method method, Buffer wrap, String contentType,
                                            boolean keepAlive) {
        HttpRequestPacket.Builder requestBuilder = HttpRequestPacket.builder();

        requestBuilder.method("POST").uri(url.getPath()).protocol(Protocol.HTTP_1_1).chunked(false).method(method)
                .contentLength(wrap.capacity()).header(Header.Host, url.getHost() + ":" + url.getPort())
                .contentType(contentType);
        if (keepAlive) {
            requestBuilder.header(Header.Connection, "Keep-Alive");
        } else {
            requestBuilder.header(Header.Connection, "close");
        }
        return HttpContent.builder(requestBuilder.build()).content(wrap).last(true).build();
    }

    public static boolean isOk(int statusCode) {
        return statusCode == 200 || statusCode == 201;
    }

    private HttpUtil() {
    }
}
