package ru.kwanza.jeda.nio.server.http;

/**
 * //todo aguzanov убрать это, чтобы не конфигурировать много хэндлеров вподряд
 *
 * @author Guzanov Alexander
 */
public interface ITimedOutHandler {

    public void onTimedOut(IHttpRequest request);
}
