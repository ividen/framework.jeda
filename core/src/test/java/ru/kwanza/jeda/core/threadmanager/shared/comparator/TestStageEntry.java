package ru.kwanza.jeda.core.threadmanager.shared.comparator;

import ru.kwanza.jeda.core.threadmanager.shared.StageEntry;

/**
 * @author Guzanov Alexander
 */
public class TestStageEntry extends StageEntry {
    public TestStageEntry() {
        super(new TestStage());
    }

    @Override
    public TestStage getStage() {
        return (TestStage) super.getStage();
    }
}
