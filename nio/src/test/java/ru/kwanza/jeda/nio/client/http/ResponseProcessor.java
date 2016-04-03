package ru.kwanza.jeda.nio.client.http;

import ru.kwanza.jeda.api.IEventProcessor;

import java.util.Collection;

/**
 * @author Michael Yeskov
 */
public class ResponseProcessor implements IEventProcessor<HttpResponseEvent> {

    @Override
    public void process(Collection<HttpResponseEvent> events) {
        for (HttpResponseEvent event : events) {
            if (event.hasException()) {
                System.out.println(event.getException().getMessage());
            } else {
                System.out.println(event.getHttpContent().getContent().toStringContent().length());
            }

        }

    }
}
