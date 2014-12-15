package ru.kwanza.jeda.timerservice.pushtimer.monitoring;

import org.springframework.stereotype.Component;

import javax.management.*;
import java.lang.management.ManagementFactory;

/**
 * @author Michael Yeskov
 */
@Component
public class JMXRegistry {
    private static final String PACKAGE = "ru.kwanza.jeda.timerservice";


    public String registerByClass(String className, String componentName,  Object mbean) {
        String fullName = PACKAGE + ".byclass:type=" + className + "-" + componentName;
        return register(fullName, mbean);
    }

    public String registerInTotal(String componentName, Object mbean) {
        String fullName = PACKAGE + ".total:type=" + componentName;
        return register(fullName, mbean);
    }

    public String register(String fullName, Object mbean) {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName(fullName);
            mbs.registerMBean(mbean, objectName);
            return fullName;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public void unregister(String fullName) {
        try {
            MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName(fullName);
            mbs.unregisterMBean(objectName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
