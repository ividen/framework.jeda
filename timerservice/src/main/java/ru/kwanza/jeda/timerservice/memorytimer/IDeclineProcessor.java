package ru.kwanza.jeda.timerservice.memorytimer;

import ru.kwanza.jeda.api.helper.FlushResult;
import ru.kwanza.jeda.api.helper.SinkHelper;

/**
 * @author Michael Yeskov
 */
public interface IDeclineProcessor {
    public void processFlushResult(FlushResult flushResult, SinkHelper responseSink);
}
