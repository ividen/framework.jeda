package ru.kwanza.jeda.nio.client.http;

import ru.kwanza.jeda.nio.client.ITransportEvent;

/**
 * @author Michael Yeskov
 */
public interface IDelegatingTransportEvent extends ITransportEvent{

    public String getResponseStageName();


}
