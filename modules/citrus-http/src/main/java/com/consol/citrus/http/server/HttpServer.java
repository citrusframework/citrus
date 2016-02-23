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

package com.consol.citrus.http.server;

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.http.message.HttpMessageConverter;
import com.consol.citrus.http.servlet.CitrusDispatcherServlet;
import com.consol.citrus.http.servlet.RequestCachingServletFilter;
import com.consol.citrus.server.AbstractServer;
import org.eclipse.jetty.security.SecurityHandler;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.servlet.*;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.context.*;
import org.springframework.core.ResolvableType;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import javax.servlet.ServletContext;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.*;

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
    private String contextConfigLocation = "classpath:com/consol/citrus/http/citrus-servlet-context.xml";

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

    /** Servlet mapping path */
    private String servletMappingPath = "/*";

    /** Optional servlet name customization */
    private String servletName;

    /** Context path */
    private String contextPath = "/";

    /** Optional security handler for basic auth */
    private SecurityHandler securityHandler;

    /** Optional servlet handler customization */
    private ServletHandler servletHandler;

    /** Message converter */
    private HttpMessageConverter messageConverter = new HttpMessageConverter();

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
            if (connectors != null && connectors.length > 0) {
                jettyServer = connectors[0].getServer();
                jettyServer.setConnectors(connectors);
            } else if (connector != null) {
                jettyServer = connector.getServer();
                jettyServer.addConnector(connector);
            } else {
                jettyServer = new Server(port);
            }
            
            HandlerCollection handlers = new HandlerCollection();
            
            ContextHandlerCollection contextCollection = new ContextHandlerCollection();
            
            ServletContextHandler contextHandler = new ServletContextHandler();
            contextHandler.setContextPath(contextPath);
            contextHandler.setResourceBase(resourceBase);

            //add the root application context as parent to the constructed WebApplicationContext
            if (useRootContextAsParent) {
                contextHandler.setAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE,
                        new SimpleDelegatingWebApplicationContext());
            }
            
            if (servletHandler == null) {
                servletHandler = new ServletHandler();
                addDispatcherServlet();
            }

            addRequestCachingFilter();

            contextHandler.setServletHandler(servletHandler);
            
            if (securityHandler != null) {
                contextHandler.setSecurityHandler(securityHandler);
            }
            
            contextCollection.addHandler(contextHandler);
            
            handlers.addHandler(contextCollection);
            
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
     * Adds default Spring dispatcher servlet with servlet mapping.
     */
    private void addDispatcherServlet() {
        ServletHolder servletHolder = new ServletHolder(getDispatherServlet());
        servletHolder.setName(getServletName());
        servletHolder.setInitParameter("contextConfigLocation", contextConfigLocation);

        servletHandler.addServlet(servletHolder);

        ServletMapping servletMapping = new ServletMapping();
        servletMapping.setServletName(getServletName());
        servletMapping.setPathSpec(servletMappingPath);

        servletHandler.addServletMapping(servletMapping);
    }

    /**
     * Gets the Citrus dispatcher servlet.
     * @return
     */
    protected DispatcherServlet getDispatherServlet() {
        return new CitrusDispatcherServlet(this);
    }

    /**
     * Adds request caching filter used for not using request data when
     * logging incoming requests
     */
    private void addRequestCachingFilter() {
        FilterMapping filterMapping = new FilterMapping();
        filterMapping.setFilterName("request-caching-filter");
        filterMapping.setPathSpec("/*");

        FilterHolder filterHolder = new FilterHolder(new RequestCachingServletFilter());
        filterHolder.setName("request-caching-filter");
        servletHandler.addFilter(filterHolder, filterMapping);
    }

    /**
     * Gets the customized servlet name or default name if not set.
     * @return the servletName
     */
    public String getServletName() {
        if (StringUtils.hasText(servletName)) {
            return servletName;
        } else {
            return getName() + "-servlet";
        }
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
        public void publishEvent(Object event) { applicationContext.publishEvent(event); }
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
        public String[] getBeanNamesForType(ResolvableType type) {
            return applicationContext.getBeanNamesForType(type);
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
        public String getApplicationName() {
            return applicationContext.getApplicationName();
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
        public String[] getBeanNamesForAnnotation(Class<? extends Annotation> annotationType) {
            return applicationContext.getBeanNamesForAnnotation(annotationType);
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
        public <T> T getBean(Class<T> requiredType, Object... args) throws BeansException {
            return applicationContext.getBean(requiredType, args);
        }
        public boolean isTypeMatch(String name, Class<?> targetType)
                throws NoSuchBeanDefinitionException {
            return applicationContext.isTypeMatch(name, targetType);
        }
        public boolean isTypeMatch(String name, ResolvableType targetType)
                throws NoSuchBeanDefinitionException {
            return applicationContext.isTypeMatch(name, targetType);
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
     * {@inheritDoc}
     */
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Gets the port.
     * @return the port the port to get.
     */
    public int getPort() {
        return port;
    }

    /**
     * Sets the port.
     * @param port the port to set
     */
    public void setPort(int port) {
        this.port = port;
    }

    /**
     * Gets the resourceBase.
     * @return the resourceBase the resourceBase to get.
     */
    public String getResourceBase() {
        return resourceBase;
    }

    /**
     * Sets the resourceBase.
     * @param resourceBase the resourceBase to set
     */
    public void setResourceBase(String resourceBase) {
        this.resourceBase = resourceBase;
    }

    /**
     * Gets the contextConfigLocation.
     * @return the contextConfigLocation the contextConfigLocation to get.
     */
    public String getContextConfigLocation() {
        return contextConfigLocation;
    }

    /**
     * Sets the contextConfigLocation.
     * @param contextConfigLocation the contextConfigLocation to set
     */
    public void setContextConfigLocation(String contextConfigLocation) {
        this.contextConfigLocation = contextConfigLocation;
    }

    /**
     * Gets the connector.
     * @return the connector the connector to get.
     */
    public Connector getConnector() {
        return connector;
    }

    /**
     * Sets the connector.
     * @param connector the connector to set
     */
    public void setConnector(Connector connector) {
        this.connector = connector;
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

    /**
     * Sets the connectors.
     * @param connectors the connectors to set
     */
    public void setConnectors(Connector[] connectors) {
        this.connectors = Arrays.copyOf(connectors, connectors.length);
    }

    /**
     * Gets the servletMappingPath.
     * @return the servletMappingPath the servletMappingPath to get.
     */
    public String getServletMappingPath() {
        return servletMappingPath;
    }

    /**
     * Sets the servletMappingPath.
     * @param servletMappingPath the servletMappingPath to set
     */
    public void setServletMappingPath(String servletMappingPath) {
        this.servletMappingPath = servletMappingPath;
    }

    /**
     * Gets the contextPath.
     * @return the contextPath the contextPath to get.
     */
    public String getContextPath() {
        return contextPath;
    }

    /**
     * Sets the contextPath.
     * @param contextPath the contextPath to set
     */
    public void setContextPath(String contextPath) {
        this.contextPath = contextPath;
    }

    /**
     * Gets the securityHandler.
     * @return the securityHandler the securityHandler to get.
     */
    public SecurityHandler getSecurityHandler() {
        return securityHandler;
    }

    /**
     * Sets the securityHandler.
     * @param securityHandler the securityHandler to set
     */
    public void setSecurityHandler(SecurityHandler securityHandler) {
        this.securityHandler = securityHandler;
    }

    /**
     * Gets the servletHandler.
     * @return the servletHandler the servletHandler to get.
     */
    public ServletHandler getServletHandler() {
        return servletHandler;
    }

    /**
     * Sets the servletHandler.
     * @param servletHandler the servletHandler to set
     */
    public void setServletHandler(ServletHandler servletHandler) {
        this.servletHandler = servletHandler;
    }

    /**
     * Sets the servletName.
     * @param servletName the servletName to set
     */
    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    /**
     * Gets the useRootContextAsParent.
     * @return the useRootContextAsParent the useRootContextAsParent to get.
     */
    public boolean isUseRootContextAsParent() {
        return useRootContextAsParent;
    }

    /**
     * Sets the useRootContextAsParent.
     * @param useRootContextAsParent the useRootContextAsParent to set
     */
    public void setUseRootContextAsParent(boolean useRootContextAsParent) {
        this.useRootContextAsParent = useRootContextAsParent;
    }

    /**
     * Gets the message converter.
     * @return
     */
    public HttpMessageConverter getMessageConverter() {
        return messageConverter;
    }

    /**
     * Sets the message converter.
     * @param messageConverter
     */
    public void setMessageConverter(HttpMessageConverter messageConverter) {
        this.messageConverter = messageConverter;
    }
}
