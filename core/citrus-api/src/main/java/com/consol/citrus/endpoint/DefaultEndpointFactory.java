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

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import com.consol.citrus.annotations.CitrusEndpoint;
import com.consol.citrus.annotations.CitrusEndpointConfig;
import com.consol.citrus.config.annotation.AnnotationConfigParser;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.spi.ReferenceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    /** Endpoint cache for endpoint reuse */
    private final Map<String, Endpoint> endpointCache = new ConcurrentHashMap<>();

    @Override
    public Endpoint create(String endpointName, Annotation endpointConfig, TestContext context) {
        String qualifier = endpointConfig.annotationType().getAnnotation(CitrusEndpointConfig.class).qualifier();
        Optional<AnnotationConfigParser> parser = Optional.ofNullable(context.getReferenceResolver().resolveAll(AnnotationConfigParser.class).get(qualifier));

        if (!parser.isPresent()) {
            // try to get parser from default Citrus modules
            parser = AnnotationConfigParser.lookup(qualifier);
        }

        if (parser.isPresent()) {
            Endpoint endpoint = parser.get().parse(endpointConfig, context.getReferenceResolver());
            endpoint.setName(endpointName);
            return endpoint;
        }

        throw new CitrusRuntimeException(String.format("Unable to create endpoint annotation parser with name '%s'", qualifier));
    }

    @Override
    public Endpoint create(String endpointName, CitrusEndpoint endpointConfig, Class<?> endpointType, TestContext context) {
        Optional<EndpointBuilder> builder = context.getReferenceResolver().resolveAll(EndpointBuilder.class)
                .values()
                .stream()
                .filter(endpointBuilder -> endpointBuilder.supports(endpointType))
                .findFirst();

        if (!builder.isPresent()) {
            // try to get builder from default Citrus modules
            builder = EndpointBuilder.lookup()
                    .values()
                    .stream()
                    .filter(endpointBuilder -> endpointBuilder.supports(endpointType))
                    .findFirst();
        }

        if (builder.isPresent()) {
            Endpoint endpoint = builder.get().build(endpointConfig, context.getReferenceResolver());
            endpoint.setName(endpointName);
            return endpoint;
        }

        throw new CitrusRuntimeException(String.format("Unable to create endpoint builder for type '%s'", endpointType.getName()));
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
        Optional<EndpointComponent> component = Optional.ofNullable(getEndpointComponents(context.getReferenceResolver()).get(componentName));

        if (!component.isPresent()) {
            // try to get component from default Citrus modules
            component = EndpointComponent.lookup(componentName);
        }

        if (!component.isPresent()) {
            throw new CitrusRuntimeException(String.format("Unable to create endpoint component with name '%s'", componentName));
        }

        Map<String, String> parameters = component.get().getParameters(endpointUri);
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
                Endpoint endpoint = component.get().createEndpoint(endpointUri, context);
                endpointCache.put(cachedEndpointName, endpoint);
                return endpoint;
            }
        }
    }

    private Map<String, EndpointComponent> getEndpointComponents(ReferenceResolver referenceResolver) {
        return referenceResolver.resolveAll(EndpointComponent.class);
    }
}
