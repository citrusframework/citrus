/*
 * Copyright 2006-2014 the original author or authors.
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

package com.consol.citrus.endpoint;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import com.consol.citrus.annotations.CitrusEndpointConfig;
import com.consol.citrus.config.annotation.AnnotationConfigParser;
import com.consol.citrus.context.ReferenceResolver;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PropertiesLoaderUtils;

/**
 * Default endpoint factory implementation uses registered endpoint components in Spring application context to create endpoint
 * from given endpoint uri. If endpoint bean name is given factory directly creates from application context. If endpoint uri is given
 * factory tries to find proper endpoint component in application context and in default endpoint component configuration.
 *
 * Default endpoint components are listed in property file reference where key is the component name and value is the fully qualified class name
 * of the implementing endpoint component class.
 *
 * @author Christoph Deppisch
 * @since 1.4.1
 */
public class DefaultEndpointFactory implements EndpointFactory {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(DefaultEndpointFactory.class);

    /** Default Citrus endpoint components from classpath resource properties */
    private static Properties endpointComponentProperties;

    /** Default Citrus endpoint annotation parsers from classpath resource properties */
    private static Properties endpointParserProperties;

    /** Endpoint cache for endpoint reuse */
    private Map<String, Endpoint> endpointCache = new ConcurrentHashMap<>();

    static {
        try {
            // Loads property file from classpath holding default endpoint component definitions in Citrus.
            endpointComponentProperties = PropertiesLoaderUtils.loadProperties(new ClassPathResource("com/consol/citrus/endpoint/endpoint.components"));
        } catch (IOException e) {
            log.warn("Unable to laod default endpoint components from resource '%s'", e);
        }

        try {
            // Loads property file from classpath holding default endpoint annotation parser definitions in Citrus.
            endpointParserProperties = PropertiesLoaderUtils.loadProperties(new ClassPathResource("com/consol/citrus/endpoint/endpoint.parser"));
        } catch (IOException e) {
            log.warn("Unable to laod default endpoint annotation parsers from resource '%s'", e);
        }
    }

    @Override
    public Endpoint create(String endpointName, Annotation endpointConfig, TestContext context) {
        String qualifier = endpointConfig.annotationType().getAnnotation(CitrusEndpointConfig.class).qualifier();
        AnnotationConfigParser<Annotation, ? extends Endpoint> parser = getAnnotationParser(context.getReferenceResolver()).get(qualifier);

        if (parser == null) {
            // try to get parser from default Citrus modules
            parser = resolveDefaultAnnotationParser(qualifier, context.getReferenceResolver());
        }

        if (parser == null) {
            throw new CitrusRuntimeException(String.format("Unable to create endpoint annotation parser with name '%s'", qualifier));
        }

        Endpoint endpoint = parser.parse(endpointConfig);
        endpoint.setName(endpointName);
        return endpoint;
    }

    @Override
    public Endpoint create(String uri, TestContext context) {
        String endpointUri = context.replaceDynamicContentInString(uri);
        if (!endpointUri.contains(":")) {
            return context.getReferenceResolver().resolve(endpointUri, Endpoint.class);
        }

        StringTokenizer tok = new StringTokenizer(endpointUri, ":");
        if (tok.countTokens() < 2) {
            throw new CitrusRuntimeException(String.format("Invalid endpoint uri '%s'", endpointUri));
        }

        String componentName = tok.nextToken();
        EndpointComponent component = getEndpointComponents(context.getReferenceResolver()).get(componentName);

        if (component == null) {
            // try to get component from default Citrus modules
            component = resolveDefaultComponent(componentName);
        }

        if (component == null) {
            throw new CitrusRuntimeException(String.format("Unable to create endpoint component with name '%s'", componentName));
        }

        Map<String, String> parameters = component.getParameters(endpointUri);
        String cachedEndpointName;
        if (parameters.containsKey(EndpointComponent.ENDPOINT_NAME)) {
            cachedEndpointName = parameters.remove(EndpointComponent.ENDPOINT_NAME);
        } else {
            cachedEndpointName = endpointUri;
        }

        synchronized (endpointCache) {
            if (endpointCache.containsKey(cachedEndpointName)) {
                if (log.isDebugEnabled()) {
                    log.debug(String.format("Found cached endpoint for uri '%s'", cachedEndpointName));
                }
                return endpointCache.get(cachedEndpointName);
            } else {
                Endpoint endpoint = component.createEndpoint(endpointUri, context);
                endpointCache.put(cachedEndpointName, endpoint);
                return endpoint;
            }
        }
    }

    private Map<String, EndpointComponent> getEndpointComponents(ReferenceResolver referenceResolver) {
        return referenceResolver.resolveAll(EndpointComponent.class);
    }

    private EndpointComponent resolveDefaultComponent(String componentName) {
        String endpointComponentClassName = endpointComponentProperties.getProperty(componentName);

        try {
            if (endpointComponentClassName != null) {
                Class<EndpointComponent> endpointComponentClass = (Class<EndpointComponent>) Class.forName(endpointComponentClassName);
                EndpointComponent endpointComponent = endpointComponentClass.newInstance();
                endpointComponent.setName(componentName);
                return endpointComponent;
            }
        } catch (ClassNotFoundException e) {
            log.warn(String.format("Unable to find default Citrus endpoint component '%s' in classpath", endpointComponentClassName), e);
        } catch (InstantiationException e) {
            log.warn(String.format("Unable to instantiate Citrus endpoint component '%s'", endpointComponentClassName), e);
        } catch (IllegalAccessException e) {
            log.warn(String.format("Unable to access Citrus endpoint component '%s'", endpointComponentClassName), e);
        }

        return null;
    }

    private Map<String, AnnotationConfigParser> getAnnotationParser(ReferenceResolver referenceResolver) {
        return referenceResolver.resolveAll(AnnotationConfigParser.class);
    }

    private AnnotationConfigParser<Annotation, ? extends Endpoint> resolveDefaultAnnotationParser(String qualifier, ReferenceResolver referenceResolver) {
        String annotationParserClassName = endpointParserProperties.getProperty(qualifier);

        try {
            if (annotationParserClassName != null) {
                Class<AnnotationConfigParser<Annotation, Endpoint>> annotationParserClass = (Class<AnnotationConfigParser<Annotation, Endpoint>>) Class.forName(annotationParserClassName);
                return annotationParserClass.getConstructor(ReferenceResolver.class).newInstance(referenceResolver);
            }
        } catch (ClassNotFoundException e) {
            log.warn(String.format("Unable to find default Citrus endpoint parser '%s' in classpath", annotationParserClassName), e);
        } catch (InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            log.warn(String.format("Unable to instantiate Citrus endpoint parser '%s'", annotationParserClassName), e);
        } catch (IllegalAccessException e) {
            log.warn(String.format("Unable to access Citrus endpoint parser '%s'", annotationParserClassName), e);
        }

        return null;
    }
}
