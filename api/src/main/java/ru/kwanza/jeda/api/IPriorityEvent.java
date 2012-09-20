package ru.kwanza.jeda.api;

/**
 * @author Guzanov Alexander
 */
public interface IPriorityEvent extends IEvent {

    public Priority getPriority();

    public enum Priority {
        CRITICAL(5),
        HIGHEST(4),
        HIGH(3),
        NORMAL(2),
        LOW(1);

        public int getCode() {
            return code;
        }

        int code;

        Priority(int code) {
            this.code = code;
        }

        public static Priority findByCode(int code) {
            for (Priority p : Priority.values()) {
                if (p.code == code) {
                    return p;
                }
            }

            return null;
        }
    }
}
