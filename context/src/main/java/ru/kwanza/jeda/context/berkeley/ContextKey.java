package ru.kwanza.jeda.context.berkeley;

import ru.kwanza.jeda.context.MapContextImpl;

import java.io.Serializable;

public class ContextKey implements Serializable {

    private String contextId;
    private String terminator;

    public ContextKey(String contextId, String terminator) {
        this.contextId = contextId;
        this.terminator = terminator;
    }

    public ContextKey(MapContextImpl ctx) {
        this.contextId = ctx.getId();
        this.terminator = ctx.getTerminator();
    }

    public String getContextId() {
        return contextId;
    }

    public String getTerminator() {
        return terminator;
    }

}
