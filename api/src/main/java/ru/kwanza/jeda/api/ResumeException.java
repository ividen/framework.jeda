package ru.kwanza.jeda.api;

import java.util.List;

/**
 * @author Dmitry Zagorovsky
 */
public class ResumeException extends Exception {

    private List<Long> unableToResumeEventIds;

    public ResumeException(List<Long> unableToResumeEventIds) {
        this.unableToResumeEventIds = unableToResumeEventIds;
    }

    public List<Long> getUnableToResumeEventIds() {
        return unableToResumeEventIds;
    }

}
