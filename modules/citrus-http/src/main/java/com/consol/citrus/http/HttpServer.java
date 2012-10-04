/*
 * Copyright 2006-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.consol.citrus.http;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

import javax.servlet.ServletContext;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.servlet.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.*;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.servlet.RequestCachingServletFilter;
import com.consol.citrus.report.MessageTracingTestListener;
import com.consol.citrus.server.AbstractServer;

/**
 * Simple Http server implementation starting an embedded Jetty server instance with
 * Spring Application context support. Incoming requests are handled with Spring MVC.
 *
 * @author Christoph Deppisch
 * @since 2007
 */
public class HttpServer extends AbstractServer implements ApplicationContextAware {
    /** Server port */
    private int port = 8080;
    
    /** Server resource base */
    private String resourceBase = "src/main/resources";
    
    /** Application context location for request controllers */
    private String contextConfigLocation = "classpath:com/consol/citrus/http/citrus-http-servlet.xml";
    
    /** Server instance to be wrapped */
    private Server jettyServer;

    /** Application context used as delegate for parent WebApplicationContext in Jetty */
    private ApplicationContext applicationContext;
    
    /** Use root application context as parent to build WebApplicationContext */
    private boolean useRootContextAsParent = false;
    
    /** Do only start one instance after another so we need a static lock object */
    private static Object serverLock = new Object();
    
    /** Set custom connector with custom idle time and other configuration options */
    private Connector connector;
    
    /** Set list of custom connectors with custom configuration options */
    private Connector[] connectors;
    
    @Override
    protected void shutdown() {
        if (jettyServer != null) {
            try {
                synchronized (serverLock) {
                    jettyServer.stop();
                }
            } catch (Exception e) {
                throw new CitrusRuntimeException(e);
            }
        }
    }

    @Override
    protected void startup() {
        synchronized (serverLock) {
            if (connectors != null) {
                jettyServer = new Server();
                jettyServer.setConnectors(connectors);
            } else if (connector != null) {
                jettyServer = new Server();
                jettyServer.addConnector(connector);
            } else {
                jettyServer = new Server(port);
            }
            
            HandlerCollection handlers = new HandlerCollection();
            
            ContextHandlerCollection contexts = new ContextHandlerCollection();
            
            ServletContextHandler context = new ServletContextHandler();
            context.setContextPath("/");
            context.setResourceBase(resourceBase);
            
            //add the root application context as parent to the constructed WebApplicationContext
            if (useRootContextAsParent) {
                context.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                        new SimpleDelegatingWebApplicationContext());
            }
            
            ServletHandler servletHandler = new ServletHandler();
            
            ServletHolder servletHolder = new ServletHolder(new DispatcherServlet());
            servletHolder.setName("spring-servlet");
            servletHolder.setInitParameter("contextConfigLocation", contextConfigLocation);
            
            servletHandler.addServlet(servletHolder);
            
            ServletMapping servletMapping = new ServletMapping();
            servletMapping.setServletName("spring-servlet");
            servletMapping.setPathSpec("/*");
            
            servletHandler.addServletMapping(servletMapping);
            
            //Add request caching filter when message tracing is enabled
            if (applicationContext.getBeansOfType(MessageTracingTestListener.class).size() > 0) {
                FilterMapping filterMapping = new FilterMapping();
                filterMapping.setFilterName("request-caching-filter");
                filterMapping.setPathSpec("/*");
                
                FilterHolder filterHolder = new FilterHolder(new RequestCachingServletFilter());
                filterHolder.setName("request-caching-filter");
                servletHandler.addFilter(filterHolder, filterMapping);
            }
            
            context.setServletHandler(servletHandler);
            
            contexts.addHandler(context);
            
            handlers.addHandler(contexts);
            
            handlers.addHandler(new DefaultHandler());
            handlers.addHandler(new RequestLogHandler());
            
            jettyServer.setHandler(handlers);
            
            try {
                jettyServer.start();
            } catch (Exception e) {
                throw new CitrusRuntimeException(e);
            }
        }
    }

    /**
     * Get the server port.
     * @return the port
     */
    public int getPort() {
        return port;
    }

    /**
     * Set the server port.
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }
    
    /**
     * Set the server resource base.
     * @param resourceBase the resourceBase to set
     */
    public void setResourceBase(String resourceBase) {
        this.resourceBase = resourceBase;
    }

    /**
     * Set the context config location.
     * @param contextConfigLocation the contextConfigLocation to set
     */
    public void setContextConfigLocation(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }

    /** (non-Javadoc)
     * @see org.springframework.context.ApplicationContextAware#setApplicationContext(org.springframework.context.ApplicationContext)
     */
    public void setApplicationContext(ApplicationContext applicationContext) 
        throws BeansException {
        this.applicationContext = applicationContext;
    }
    
    /**
     * @param useRootContextAsParent the useRootContextAsParent to set
     */
    public void setUseRootContextAsParent(boolean useRootContextAsParent) {
        this.useRootContextAsParent = useRootContextAsParent;
    }
    
    /**
     * WebApplicationContext implementation that delegates method calls to parent ApplicationContext.
     */
    private final class SimpleDelegatingWebApplicationContext implements WebApplicationContext {
        public Resource getResource(String location) {
            return applicationContext.getResource(location);
        }
        public ClassLoader getClassLoader() {
            return applicationContext.getClassLoader();
        }
        public Resource[] getResources(String locationPattern) throws IOException {
            return applicationContext.getResources(locationPattern);
        }
        public void publishEvent(ApplicationEvent event) {
            applicationContext.publishEvent(event);
        }
        public String getMessage(String code, Object[] args, String defaultMessage,
                Locale locale) {
            return applicationContext.getMessage(code, args, defaultMessage, locale);
        }
        public String getMessage(String code, Object[] args, Locale locale)
                throws NoSuchMessageException {
            return applicationContext.getMessage(code, args, locale);
        }
        public String getMessage(MessageSourceResolvable resolvable, Locale locale)
                throws NoSuchMessageException {
            return applicationContext.getMessage(resolvable, locale);
        }
        public BeanFactory getParentBeanFactory() {
            return applicationContext.getParentBeanFactory();
        }
        public boolean containsLocalBean(String name) {
            return applicationContext.containsBean(name);
        }
        public boolean isSingleton(String name)
                throws NoSuchBeanDefinitionException {
            return applicationContext.isSingleton(name);
        }
        public boolean isPrototype(String name)
                throws NoSuchBeanDefinitionException {
            return applicationContext.isPrototype(name);
        }
        public Object getBean(String name) throws BeansException {
            return applicationContext.getBean(name);
        }
        public String[] getAliases(String name) {
            return applicationContext.getAliases(name);
        }
        public boolean containsBean(String name) {
            return applicationContext.containsBean(name);
        }
        public String[] getBeanDefinitionNames() {
            return applicationContext.getBeanDefinitionNames();
        }
        public int getBeanDefinitionCount() {
            return applicationContext.getBeanDefinitionCount();
        }
        public boolean containsBeanDefinition(String beanName) {
            return applicationContext.containsBeanDefinition(beanName);
        }
        public long getStartupDate() {
            return applicationContext.getStartupDate();
        }
        public ApplicationContext getParent() {
            return applicationContext.getParent();
        }
        public String getId() {
            return applicationContext.getId();
        }
        public String getDisplayName() {
            return applicationContext.getDisplayName();
        }
        public AutowireCapableBeanFactory getAutowireCapableBeanFactory()
                throws IllegalStateException {
            return applicationContext.getAutowireCapableBeanFactory();
        }
        public <T> Map<String, T> getBeansOfType(Class<T> type)
                throws BeansException {
            return applicationContext.getBeansOfType(type);
        }
        public <T> Map<String, T> getBeansOfType(Class<T> type,
                boolean includeNonSingletons, boolean allowEagerInit)
                throws BeansException {
            return applicationContext.getBeansOfType(type, includeNonSingletons, allowEagerInit);
        }
        public Map<String, Object> getBeansWithAnnotation(
                Class<? extends Annotation> annotationType)
                throws BeansException {
            return applicationContext.getBeansWithAnnotation(annotationType);
        }
        public <A extends Annotation> A findAnnotationOnBean(String beanName,
                Class<A> annotationType) {
            return applicationContext.findAnnotationOnBean(beanName, annotationType);
        }
        public <T> T getBean(String name, Class<T> requiredType)
                throws BeansException {
            return applicationContext.getBean(name, requiredType);
        }
        public <T> T getBean(Class<T> requiredType) throws BeansException {
            return applicationContext.getBean(requiredType);
        }
        public String[] getBeanNamesForType(Class<?> type) {
            return applicationContext.getBeanNamesForType(type);
        }
        public String[] getBeanNamesForType(Class<?> type,
                boolean includeNonSingletons, boolean allowEagerInit) {
            return applicationContext.getBeanNamesForType(type, includeNonSingletons, allowEagerInit);
        }
        public Object getBean(String name, Object... args)
                throws BeansException {
            return applicationContext.getBean(name, args);
        }
        public boolean isTypeMatch(String name, Class<?> targetType)
                throws NoSuchBeanDefinitionException {
            return false;
        }
        public Class<?> getType(String name)
                throws NoSuchBeanDefinitionException {
            return applicationContext.getType(name);
        }
        public ServletContext getServletContext() {
            return null;
        }
        public Environment getEnvironment() {
            return applicationContext.getEnvironment();
        }
    }

    /**
     * Sets the custom connector.
     * @param connector the connector to set
     */
    public void setConnector(Connector connector) {
        this.connector = connector;
    }

    /**
     * Sets a list of custom connectors.
     * @param connectors the connectors to set
     */
    public void setConnectors(Connector[] connectors) {
        this.connectors = Arrays.copyOf(connectors, connectors.length);
    }

    /**
     * Gets the resourceBase.
     * @return the resourceBase
     */
    public String getResourceBase() {
        return resourceBase;
    }

    /**
     * Gets the contextConfigLocation.
     * @return the contextConfigLocation
     */
    public String getContextConfigLocation() {
        return contextConfigLocation;
    }

    /**
     * Gets the useRootContextAsParent.
     * @return the useRootContextAsParent
     */
    public boolean isUseRootContextAsParent() {
        return useRootContextAsParent;
    }

    /**
     * Gets the connector.
     * @return the connector
     */
    public Connector getConnector() {
        return connector;
    }

    /**
     * Gets the connectors.
     * @return the connectors
     */
    public Connector[] getConnectors() {
        if (connectors != null) {
            return Arrays.copyOf(connectors, connectors.length);
        } else {
            return new Connector[]{};
        }
    }
}
