package ru.kwanza.jeda.nio.server.http;

import org.glassfish.grizzly.http.HttpContent;
import org.glassfish.grizzly.http.HttpHeader;
import org.glassfish.grizzly.http.HttpPacket;
import org.glassfish.grizzly.http.util.MimeHeaders;

/**
 * @author Guzanov Alexander
 */
abstract class TraceUtil {
    static String getDescription(HttpPacket packet) {
        StringBuilder result = new StringBuilder();
        if (packet instanceof HttpHeader) {
            HttpHeader headerPacket = (HttpHeader) packet;
            result.append("\n----------Headers :");
            result.append("\n\tProtocol:").append(headerPacket.getProtocol().getProtocolString());
            result.append("\n\tContent-Type:").append(headerPacket.getContentType());
            result.append("\n\tCharacter-Encoding:").append(headerPacket.getCharacterEncoding());
            result.append("\n\tContent-Length:").append(headerPacket.getContentLength());
            MimeHeaders headers = headerPacket.getHeaders();
            Iterable<String> names = headers.names();
            for (String name : names) {
                result.append("\n\tHeader[").append(name).append("] : ").append(headers.getHeader(name));
            }
        } else if (packet instanceof HttpContent) {
            HttpContent contentPacket = (HttpContent) packet;
            result.append(getDescription(contentPacket.getHttpHeader()));
            result.append("\n----------Content : \n").append(contentPacket.getContent().toStringContent());
        }

        return result.toString();
    }
}
