package ru.kwanza.jeda.core.threadmanager.shared.comparator;

import ru.kwanza.jeda.core.threadmanager.shared.StageEntry;

import java.util.Comparator;

/**
 * @author Guzanov Alexander
 */
public class InputRateComparator implements Comparator<StageEntry> {
    public int compare(StageEntry o1, StageEntry o2) {
        double inputRate1 = o1.getStage().getResourceController().getInputRate();
        double inputRate2 = o2.getStage().getResourceController().getInputRate();
        if (inputRate1 > inputRate2) {
            return -1;
        }

        if (inputRate1 < inputRate2) {
            return 1;
        }

        return 0;
    }
}
