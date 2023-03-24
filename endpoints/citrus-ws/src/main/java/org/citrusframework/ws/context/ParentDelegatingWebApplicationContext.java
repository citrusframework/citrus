package org.citrusframework.ws.context;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import jakarta.servlet.ServletContext;
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

    @Override
    public Resource getResource(String location) {
        return delegate.getResource(location);
    }
    @Override
    public ClassLoader getClassLoader() {
        return delegate.getClassLoader();
    }
    @Override
    public Resource[] getResources(String locationPattern) throws IOException {
        return delegate.getResources(locationPattern);
    }
    @Override
    public void publishEvent(ApplicationEvent event) {
        delegate.publishEvent(event);
    }
    @Override
    public void publishEvent(Object event) { delegate.publishEvent(event); }
    @Override
    public String getMessage(String code, Object[] args, String defaultMessage,
            Locale locale) {
        return delegate.getMessage(code, args, defaultMessage, locale);
    }
    @Override
    public String getMessage(String code, Object[] args, Locale locale)
            throws NoSuchMessageException {
        return delegate.getMessage(code, args, locale);
    }
    @Override
    public String getMessage(MessageSourceResolvable resolvable, Locale locale)
            throws NoSuchMessageException {
        return delegate.getMessage(resolvable, locale);
    }
    @Override
    public BeanFactory getParentBeanFactory() {
        return delegate.getParentBeanFactory();
    }
    @Override
    public boolean containsLocalBean(String name) {
        return delegate.containsBean(name);
    }
    @Override
    public boolean isSingleton(String name)
            throws NoSuchBeanDefinitionException {
        return delegate.isSingleton(name);
    }
    @Override
    public boolean isPrototype(String name)
            throws NoSuchBeanDefinitionException {
        return delegate.isPrototype(name);
    }
    @Override
    public Object getBean(String name) throws BeansException {
        return delegate.getBean(name);
    }
    @Override
    public String[] getAliases(String name) {
        return delegate.getAliases(name);
    }
    @Override
    public boolean containsBean(String name) {
        return delegate.containsBean(name);
    }
    @Override
    public String[] getBeanDefinitionNames() {
        return delegate.getBeanDefinitionNames();
    }
    @Override
    public <T> ObjectProvider<T> getBeanProvider(Class<T> aClass, boolean b) {
        return delegate.getBeanProvider(aClass, b);
    }
    @Override
    public <T> ObjectProvider<T> getBeanProvider(ResolvableType resolvableType, boolean b) {
        return delegate.getBeanProvider(resolvableType, b);
    }
    @Override
    public String[] getBeanNamesForType(ResolvableType type) {
        return delegate.getBeanNamesForType(type);
    }
    @Override
    public String[] getBeanNamesForType(ResolvableType resolvableType, boolean b, boolean b1) {
        return delegate.getBeanNamesForType(resolvableType, b, b1);
    }
    @Override
    public int getBeanDefinitionCount() {
        return delegate.getBeanDefinitionCount();
    }
    @Override
    public boolean containsBeanDefinition(String beanName) {
        return delegate.containsBeanDefinition(beanName);
    }
    @Override
    public long getStartupDate() {
        return delegate.getStartupDate();
    }
    @Override
    public ApplicationContext getParent() {
        return delegate.getParent();
    }
    @Override
    public String getId() {
        return delegate.getId();
    }
    @Override
    public String getApplicationName() {
        return delegate.getApplicationName();
    }
    @Override
    public String getDisplayName() {
        return delegate.getDisplayName();
    }
    @Override
    public AutowireCapableBeanFactory getAutowireCapableBeanFactory()
            throws IllegalStateException {
        return delegate.getAutowireCapableBeanFactory();
    }
    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type)
            throws BeansException {
        return delegate.getBeansOfType(type);
    }
    @Override
    public <T> Map<String, T> getBeansOfType(Class<T> type,
            boolean includeNonSingletons, boolean allowEagerInit)
            throws BeansException {
        return delegate.getBeansOfType(type, includeNonSingletons, allowEagerInit);
    }
    @Override
    public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
        return delegate.getBeanNamesForAnnotation(annotationType);
    }
    @Override
    public Map<String, Object> getBeansWithAnnotation(
            Class<? extends Annotation> annotationType)
            throws BeansException {
        return delegate.getBeansWithAnnotation(annotationType);
    }
    @Override
    public <A extends Annotation> A findAnnotationOnBean(String beanName,
            Class<A> annotationType) {
        return delegate.findAnnotationOnBean(beanName, annotationType);
    }

    @Override
    public <A extends Annotation> A findAnnotationOnBean(String beanName, Class<A> annotationType, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
        return delegate.findAnnotationOnBean(beanName, annotationType, allowFactoryBeanInit);
    }

    @Override
    public <A extends Annotation> Set<A> findAllAnnotationsOnBean(String beanName, Class<A> annotationType, boolean allowFactoryBeanInit) throws NoSuchBeanDefinitionException {
        return delegate.findAllAnnotationsOnBean(beanName, annotationType, allowFactoryBeanInit);
    }

    @Override
    public <T> T getBean(String name, Class<T> requiredType)
            throws BeansException {
        return delegate.getBean(name, requiredType);
    }
    @Override
    public <T> T getBean(Class<T> requiredType) throws BeansException {
        return delegate.getBean(requiredType);
    }
    @Override
    public String[] getBeanNamesForType(Class<?> type) {
        return delegate.getBeanNamesForType(type);
    }
    @Override
    public String[] getBeanNamesForType(Class<?> type,
            boolean includeNonSingletons, boolean allowEagerInit) {
        return delegate.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
    }
    @Override
    public Object getBean(String name, Object... args)
            throws BeansException {
        return delegate.getBean(name, args);
    }
    @Override
    public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
        return delegate.getBean(requiredType, args);
    }
    @Override
    public <T> ObjectProvider<T> getBeanProvider(Class<T> aClass) {
        return delegate.getBeanProvider(aClass);
    }
    @Override
    public <T> ObjectProvider<T> getBeanProvider(ResolvableType resolvableType) {
        return delegate.getBeanProvider(resolvableType);
    }
    @Override
    public boolean isTypeMatch(String name, Class<?> targetType)
            throws NoSuchBeanDefinitionException {
        return delegate.isTypeMatch(name, targetType);
    }
    @Override
    public boolean isTypeMatch(String name, ResolvableType targetType)
            throws NoSuchBeanDefinitionException {
        return delegate.isTypeMatch(name, targetType);
    }
    @Override
    public Class<?> getType(String name)
            throws NoSuchBeanDefinitionException {
        return delegate.getType(name);
    }
    @Override
    public Class<?> getType(String s, boolean b) throws NoSuchBeanDefinitionException {
        return delegate.getType(s, b);
    }
    @Override
    public ServletContext getServletContext() {
        return null;
    }
    @Override
    public Environment getEnvironment() {
        return delegate.getEnvironment();
    }
}
