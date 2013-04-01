package org.aksw.sparql_analytics.configuration;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.stereotype.Component;

// This class doesn't get called - I have no idea how to get the application context into the servlets...
@Component
public class SpringApplicationContext implements BeanFactoryAware {

    private static BeanFactory springBeanFactory;

    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        springBeanFactory = beanFactory;
        
        throw new RuntimeException("Actually got a beanFactory");
    }

    public static <T> T getBean(Class<? extends T> klass) {
        return springBeanFactory.getBean(klass);
    }
}