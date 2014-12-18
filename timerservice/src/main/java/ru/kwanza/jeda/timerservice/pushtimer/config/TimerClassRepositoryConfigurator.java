package ru.kwanza.jeda.timerservice.pushtimer.config;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.Map;

/**
 * for manual registering timerClasses (if not using timers namespace spring integration)
 * @author Michael Yeskov
 */
public class TimerClassRepositoryConfigurator implements BeanPostProcessor{
    private Map<String, TimerClass> timerNameToClass;

    public Map<String, TimerClass> getTimerNameToClass() {
        return timerNameToClass;
    }

    @Required
    public void setTimerNameToClass(Map<String, TimerClass> timerNameToClass) {
        this.timerNameToClass = timerNameToClass;
    }

    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        return bean;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof TimerClassRepository) {
            TimerClassRepository repository = (TimerClassRepository)bean;
            for (Map.Entry<String, TimerClass> entry :  timerNameToClass.entrySet()) {
                repository.registerNameToClassBinding(entry.getKey(), entry.getValue());
            }
        }
        return bean;
    }
}
