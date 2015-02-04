package ru.kwanza.jeda.api.helper;

/**
 * @author Michael Yeskov
 */
public interface IDeclineProcessor {
    public void processFlushResult(FlushResult flushResult, SinkHelper responseSink);
}
