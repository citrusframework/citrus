package com.consol.citrus.ws.context;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.context.NoSuchMessageException;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;

/**
 * WebApplicationContext implementation that delegates method calls to parent ApplicationContext.
 */
public final class ParentDelegatingWebApplicationContext implements WebApplicationContext {
    private final ApplicationContext delegate;

    public ParentDelegatingWebApplicationContext(ApplicationContext applicationContext) {
        this.delegate = applicationContext;
    }

    public Resource getResource(String location) {
        return delegate.getResource(location);
    }
    public ClassLoader getClassLoader() {
        return delegate.getClassLoader();
    }
    public Resource[] getResources(String locationPattern) throws IOException {
        return delegate.getResources(locationPattern);
    }
    public void publishEvent(ApplicationEvent event) {
        delegate.publishEvent(event);
    }
    public void publishEvent(Object event) { delegate.publishEvent(event); }
    public String getMessage(String code, Object[] args, String defaultMessage,
            Locale locale) {
        return delegate.getMessage(code, args, defaultMessage, locale);
    }
    public String getMessage(String code, Object[] args, Locale locale)
            throws NoSuchMessageException {
        return delegate.getMessage(code, args, locale);
    }
    public String getMessage(MessageSourceResolvable resolvable, Locale locale)
            throws NoSuchMessageException {
        return delegate.getMessage(resolvable, locale);
    }
    public BeanFactory getParentBeanFactory() {
        return delegate.getParentBeanFactory();
    }
    public boolean containsLocalBean(String name) {
        return delegate.containsBean(name);
    }
    public boolean isSingleton(String name)
            throws NoSuchBeanDefinitionException {
        return delegate.isSingleton(name);
    }
    public boolean isPrototype(String name)
            throws NoSuchBeanDefinitionException {
        return delegate.isPrototype(name);
    }
    public Object getBean(String name) throws BeansException {
        return delegate.getBean(name);
    }
    public String[] getAliases(String name) {
        return delegate.getAliases(name);
    }
    public boolean containsBean(String name) {
        return delegate.containsBean(name);
    }
    public String[] getBeanDefinitionNames() {
        return delegate.getBeanDefinitionNames();
    }
    public String[] getBeanNamesForType(ResolvableType type) {
        return delegate.getBeanNamesForType(type);
    }
    public String[] getBeanNamesForType(ResolvableType resolvableType, boolean b, boolean b1) {
        return delegate.getBeanNamesForType(resolvableType, b, b1);
    }
    public int getBeanDefinitionCount() {
        return delegate.getBeanDefinitionCount();
    }
    public boolean containsBeanDefinition(String beanName) {
        return delegate.containsBeanDefinition(beanName);
    }
    public long getStartupDate() {
        return delegate.getStartupDate();
    }
    public ApplicationContext getParent() {
        return delegate.getParent();
    }
    public String getId() {
        return delegate.getId();
    }
    public String getApplicationName() {
        return delegate.getApplicationName();
    }
    public String getDisplayName() {
        return delegate.getDisplayName();
    }
    public AutowireCapableBeanFactory getAutowireCapableBeanFactory()
            throws IllegalStateException {
        return delegate.getAutowireCapableBeanFactory();
    }
    public <T> Map<String, T> getBeansOfType(Class<T> type)
            throws BeansException {
        return delegate.getBeansOfType(type);
    }
    public <T> Map<String, T> getBeansOfType(Class<T> type,
            boolean includeNonSingletons, boolean allowEagerInit)
            throws BeansException {
        return delegate.getBeansOfType(type, includeNonSingletons, allowEagerInit);
    }
    public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
        return delegate.getBeanNamesForAnnotation(annotationType);
    }
    public Map<String, Object> getBeansWithAnnotation(
            Class<? extends Annotation> annotationType)
            throws BeansException {
        return delegate.getBeansWithAnnotation(annotationType);
    }
    public <A extends Annotation> A findAnnotationOnBean(String beanName,
            Class<A> annotationType) {
        return delegate.findAnnotationOnBean(beanName, annotationType);
    }
    public <T> T getBean(String name, Class<T> requiredType)
            throws BeansException {
        return delegate.getBean(name, requiredType);
    }
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        return delegate.getBean(requiredType);
    }
    public String[] getBeanNamesForType(Class<?> type) {
        return delegate.getBeanNamesForType(type);
    }
    public String[] getBeanNamesForType(Class<?> type,
            boolean includeNonSingletons, boolean allowEagerInit) {
        return delegate.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
    }
    public Object getBean(String name, Object... args)
            throws BeansException {
        return delegate.getBean(name, args);
    }
    public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
        return delegate.getBean(requiredType, args);
    }
    public <T> ObjectProvider<T> getBeanProvider(Class<T> aClass) {
        return delegate.getBeanProvider(aClass);
    }
    public <T> ObjectProvider<T> getBeanProvider(ResolvableType resolvableType) {
        return delegate.getBeanProvider(resolvableType);
    }
    public boolean isTypeMatch(String name, Class<?> targetType)
            throws NoSuchBeanDefinitionException {
        return delegate.isTypeMatch(name, targetType);
    }
    public boolean isTypeMatch(String name, ResolvableType targetType)
            throws NoSuchBeanDefinitionException {
        return delegate.isTypeMatch(name, targetType);
    }
    public Class<?> getType(String name)
            throws NoSuchBeanDefinitionException {
        return delegate.getType(name);
    }
    public Class<?> getType(String s, boolean b) throws NoSuchBeanDefinitionException {
        return delegate.getType(s, b);
    }
    public ServletContext getServletContext() {
        return null;
    }
    public Environment getEnvironment() {
        return delegate.getEnvironment();
    }
}
