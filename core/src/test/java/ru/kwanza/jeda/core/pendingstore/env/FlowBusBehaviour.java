package ru.kwanza.jeda.core.pendingstore.env;

/**
 * @author Dmitry Zagorovsky
 */
public class FlowBusBehaviour {

    private static FlowBusBehaviour instance = new FlowBusBehaviour();
    private SinkExceptionType sinkExceptionType;
    private int remainTryPutCount = 0;


    private FlowBusBehaviour() {
        sinkExceptionType = null;
    }

    public static SinkExceptionType getSinkExceptionType() {
        return instance.sinkExceptionType;
    }

    public static void setSinkExceptionType(SinkExceptionType sinkExceptionType) {
        instance.sinkExceptionType = sinkExceptionType;
    }

    public static int getRemainTryPutCount() {
        return instance.remainTryPutCount;
    }

    public static void setRemainTryPutCount(int remainTryPutCount) {
        instance.remainTryPutCount = remainTryPutCount;
    }

    public static void reset() {
        instance = new FlowBusBehaviour();
    }

    public static enum SinkExceptionType {
        CLOSED, CLOGGED, OTHER
    }

}
