/*
 * Copyright the original author or authors.
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

package org.citrusframework.endpoint;

import java.lang.annotation.Annotation;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import org.citrusframework.CitrusSettings;
import org.citrusframework.annotations.CitrusEndpoint;
import org.citrusframework.annotations.CitrusEndpointConfig;
import org.citrusframework.common.InitializingPhase;
import org.citrusframework.common.ShutdownPhase;
import org.citrusframework.config.annotation.AnnotationConfigParser;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.ReferenceResolverAware;
import org.citrusframework.util.PropertyUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Default endpoint factory implementation uses registered endpoint components in Spring application context to create endpoint
 * from given endpoint uri. If endpoint bean name is given factory directly creates from application context. If endpoint uri is given
 * factory tries to find proper endpoint component in application context and in default endpoint component configuration.
 * <p>
 * Default endpoint components are listed in property file reference where key is the component name and value is the fully qualified class name
 * of the implementing endpoint component class.
 *
 * @since 1.4.1
 */
public class DefaultEndpointFactory implements EndpointFactory {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(DefaultEndpointFactory.class);

    /** Endpoint cache for endpoint reuse */
    private final Map<String, Endpoint> endpointCache = new ConcurrentHashMap<>();

    @Override
    public Endpoint create(String endpointName, Annotation endpointConfig, TestContext context) {
        String qualifier = endpointConfig.annotationType().getAnnotation(CitrusEndpointConfig.class).qualifier();
        AnnotationConfigParser parser = Optional.ofNullable(context.getReferenceResolver().resolveAll(AnnotationConfigParser.class).get(qualifier))
                .or(() -> AnnotationConfigParser.lookup(qualifier))
                .orElseThrow(() -> new CitrusRuntimeException(String.format("Unable to create endpoint annotation parser with name '%s'", qualifier)));

        Endpoint endpoint = parser.parse(endpointConfig, context.getReferenceResolver());
        endpoint.setName(endpointName);

        if (endpoint instanceof ReferenceResolverAware referenceResolverAware) {
            referenceResolverAware.setReferenceResolver(context.getReferenceResolver());
        }

        if (endpoint instanceof InitializingPhase initializingBean) {
            initializingBean.initialize();
        }

        PropertyUtils.configure(endpointName, endpoint, context.getReferenceResolver());
        return endpoint;
    }

    @Override
    public Endpoint create(String endpointName, CitrusEndpoint endpointConfig, Class<?> endpointType, TestContext context) {
        EndpointBuilder<?> builder = context.getReferenceResolver().resolveAll(EndpointBuilder.class)
                .values()
                .stream()
                .filter(endpointBuilder -> endpointBuilder.supports(endpointType))
                .findFirst()
                .or(() -> EndpointBuilder.lookup()
                        .values()
                        .stream()
                        .filter(endpointBuilder -> endpointBuilder.supports(endpointType))
                        .findFirst())
                .orElseThrow(() -> new CitrusRuntimeException(String.format("Unable to create endpoint builder for type '%s'", endpointType.getName())));

        Endpoint endpoint = builder.build(endpointConfig, context.getReferenceResolver());
        endpoint.setName(endpointName);
        return endpoint;
    }

    @Override
    public Endpoint create(String uri, TestContext context) {
        String endpointUri = context.replaceDynamicContentInString(uri);
        if (!endpointUri.contains(":")) {
            if (context.getReferenceResolver().isResolvable(endpointUri, Endpoint.class)) {
                return context.getReferenceResolver().resolve(endpointUri, Endpoint.class);
            } else {
                synchronized (endpointCache) {
                    if (endpointCache.containsKey(endpointUri)) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Found cached endpoint for uri '{}'", endpointUri);
                        }
                        return endpointCache.get(endpointUri);
                    }
                }
            }
        }

        String componentName = getComponentName(endpointUri);
        EndpointComponent component = Optional.ofNullable(getEndpointComponents(context.getReferenceResolver()).get(componentName))
                .or(() -> EndpointComponent.lookup(componentName))
                .orElseThrow(() -> new CitrusRuntimeException(String.format("Unable to create endpoint component with name '%s'", componentName)));

        Map<String, String> parameters = component.getParameters(endpointUri);
        String cachedEndpointName = parameters.getOrDefault(EndpointComponent.ENDPOINT_NAME, endpointUri);

        synchronized (endpointCache) {
            if (endpointCache.containsKey(cachedEndpointName)) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Found cached endpoint for uri '{}'", cachedEndpointName);
                }
                return endpointCache.get(cachedEndpointName);
            } else {
                Endpoint endpoint = component.createEndpoint(endpointUri, context);

                boolean endpointCacheEnabled = Optional.ofNullable(parameters.get(EndpointComponent.ENDPOINT_CACHE))
                        .map(Boolean::parseBoolean)
                        .orElse(component.supportsEndpointCaching());

                if (endpointCacheEnabled) {
                    endpointCache.put(cachedEndpointName, endpoint);

                    boolean autoRemove = Optional.ofNullable(parameters.get(EndpointComponent.AUTO_REMOVE))
                            .map(Boolean::parseBoolean)
                            .orElseGet(CitrusSettings::isAutoRemoveDynamicEndpoints);
                    if (autoRemove) {
                        context.doFinally(() -> ctx -> {
                            logger.info("Stopping and removing endpoint '{}' due to auto remove setting", endpoint.getName());
                            if (endpoint instanceof ShutdownPhase destroyable) {
                                destroyable.destroy();
                            }

                            synchronized (endpointCache) {
                                endpointCache.remove(cachedEndpointName);
                            }
                        });
                    }
                }
                return endpoint;
            }
        }
    }

    private static String getComponentName(String endpointUri) {
        String componentName = endpointUri;
        if (componentName.contains("?")) {
            componentName = componentName.substring(0, componentName.indexOf("?"));
        }

        if (componentName.contains(":")) {
            componentName = componentName.substring(0, componentName.indexOf(":"));
        }

        if (componentName.isEmpty() || endpointUri.endsWith(":") || endpointUri.endsWith("?")) {
            throw new CitrusRuntimeException(String.format("Invalid endpoint uri '%s'", endpointUri));
        }
        return componentName;
    }

    @Override
    public void destroy() {
        endpointCache.forEach((name, endpoint) -> {
            if (endpoint instanceof ShutdownPhase destroyable) {
                try {
                    logger.debug("Destroying endpoint '{}'", name);
                    destroyable.destroy();
                } catch (Exception e) {
                    logger.warn("Failed to shutdown bean {}: {}", name, e.getMessage());
                    if (logger.isDebugEnabled()) {
                        logger.debug(e.getMessage(), e);
                    }
                }
            }
        });

        endpointCache.clear();
    }

    private Map<String, EndpointComponent> getEndpointComponents(ReferenceResolver referenceResolver) {
        return referenceResolver.resolveAll(EndpointComponent.class);
    }
}
