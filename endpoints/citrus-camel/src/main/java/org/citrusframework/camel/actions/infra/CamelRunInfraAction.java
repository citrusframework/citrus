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

package org.citrusframework.camel.actions.infra;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.camel.catalog.CamelCatalog;
import org.apache.camel.catalog.DefaultCamelCatalog;
import org.apache.camel.impl.DefaultCamelContext;
import org.citrusframework.actions.camel.CamelInfraRunActionBuilder;
import org.citrusframework.camel.actions.AbstractCamelAction;
import org.citrusframework.camel.jbang.CamelJBangSettings;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.util.ReflectionHelper;
import org.citrusframework.util.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.citrusframework.camel.dsl.CamelSupport.camel;

/**
 * Starts Camel infra service from Camel catalog.
 * Uses Java reflection to initialize the service and calls initialize method on the infrastructure service to start the service.
 * Exposes service properties such as host, port, serviceUrl as test variables.
 * Saves service instance and metadata to the test context for later reference (e.g. when stopping the service.
 * Supports auto removal of the service after the test has finished.
 */
public class CamelRunInfraAction extends AbstractCamelAction {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CamelRunInfraAction.class);

    private final CamelCatalog catalog;

    private final String serviceName;
    private final String implementation;

    private final boolean autoRemove;
    private final boolean dumpServiceOutput;

    protected CamelRunInfraAction(Builder builder) {
        super("run-infra", builder);

        this.catalog = builder.catalog;
        this.serviceName = builder.serviceName;
        this.implementation = builder.implementation;
        this.autoRemove = builder.autoRemove;
        this.dumpServiceOutput = builder.dumpServiceOutput;
    }

    @Override
    public void doExecute(TestContext context) {
        String resolvedServiceName = context.replaceDynamicContentInString(serviceName);
        String resolvedImplementation = Optional.ofNullable(implementation).map(context::replaceDynamicContentInString).orElse(null);
        String fullServiceName = StringUtils.hasText(resolvedImplementation) ? resolvedServiceName + "." + resolvedImplementation : resolvedServiceName;

        try {
            Optional<InfraService> infraService = resolveInfraService(catalog, resolvedServiceName, resolvedImplementation);

            if (infraService.isEmpty()) {
                throw new CitrusRuntimeException("No Camel infra service found for '%s'".formatted(fullServiceName));
            }

            logger.info("Starting Camel infra service '{}' ...", fullServiceName);

            Class<?> serviceType = Class.forName(infraService.get().service());
            Object instance = Class.forName(infraService.get().implementation()).getDeclaredConstructor().newInstance();

            if (isNotInfrastructureService(instance)) {
                throw new CitrusRuntimeException("Camel infra service '%s' is not an infrastructure service".formatted(instance.getClass().getName()));
            }

            if (dumpServiceOutput) {
                if (Arrays.stream(instance.getClass().getInterfaces()).anyMatch(c -> c.getName().contains("ContainerService"))) {
                    Path workDir = CamelJBangSettings.getWorkDir();
                    Path logFile = workDir.resolve(String.format("camel-infra-%s-output.txt", fullServiceName));
                    Object containerLogConsumer = Class.forName("org.apache.camel.test.infra.common.CamelLogConsumer")
                            .getConstructor(Path.class, boolean.class).newInstance(logFile, true);

                    instance.getClass()
                            .getMethod("followLog", Class.forName("org.testcontainers.containers.output.BaseConsumer"))
                            .invoke(instance, containerLogConsumer);
                }
            }

            // Start the service now
            instance.getClass().getMethod("initialize").invoke(instance);

            context.setVariable("%s%s:meta".formatted(CamelInfraSettings.CAMEL_INFRA_PROPERTY_PREFIX, fullServiceName.toLowerCase()), infraService.get());
            context.setVariable("%s%s".formatted(CamelInfraSettings.CAMEL_INFRA_PROPERTY_PREFIX, fullServiceName.toLowerCase()), instance);

            HashMap<String, Object> serviceProperties = new HashMap<>();
            ReflectionHelper.doWithMethods(serviceType, method -> {
                if (method.getParameterCount() == 0 &&
                        !method.getName().equals("initialize") &&
                        !method.getName().equals("close") &&
                        !method.getName().equals("shutdown") &&
                        !method.getName().contains("registerProperties")) {
                    try {
                        serviceProperties.put(method.getName(), method.invoke(instance));
                    } catch (InvocationTargetException e) {
                        // do nothing ignore
                    }
                }
            });

            if (logger.isDebugEnabled()) {
                logger.debug("Camel infra service properties: {}", serviceProperties.entrySet()
                        .stream()
                        .map((entry) -> context.getLogModifier().mask("%s='%s'".formatted(entry.getKey(), entry.getValue().toString())))
                        .collect(Collectors.joining(System.lineSeparator())));
            }

            String serviceVariableName = fullServiceName.replaceAll("[^a-zA-Z0-9]", "_").toUpperCase();
            exposeServiceProperties(serviceProperties, serviceVariableName, context);

            if (autoRemove) {
                context.doFinally(camel()
                        .infra()
                        .stop()
                        .meta(infraService.get())
                        .instance(instance)
                );
            }

            logger.info("Successfully started Camel infra service '{}'", fullServiceName);
        } catch (IOException | ClassNotFoundException | NoSuchMethodException | InstantiationException |
                 IllegalAccessException | InvocationTargetException e) {
            throw new CitrusRuntimeException("Failed to run Camel infra service '%s'".formatted(fullServiceName), e);
        }
    }

    private void exposeServiceProperties(Map<String, Object> serviceProperties, String serviceVariableName, TestContext context) {
        serviceProperties.forEach((key, value) -> {
            if (value instanceof Map valueMap) {
                exposeServiceProperties(valueMap, serviceVariableName, context);
            }

            String name = "%s%s_%s".formatted(CamelInfraSettings.CAMEL_INFRA_ENV_PREFIX, serviceVariableName, normalizeKey(key));
            context.setVariable(name, Optional.ofNullable(value).orElse(""));
            logger.info("Exposing service property {}",
                    context.getLogModifier().mask("%s='%s'".formatted(name, Optional.ofNullable(value).orElse(""))));
        });
    }

    private static Optional<InfraService> resolveInfraService(CamelCatalog catalog, String serviceName, String implementation) throws IOException {
        List<InfraService> services = InfraServiceUtils.getInfraServiceMetadata(catalog);
        return services
                .stream()
                .filter(service -> {
                    if (implementation != null && !implementation.isEmpty()
                            && service.aliasImplementation() != null) {
                        return service.alias().contains(serviceName)
                                && service.aliasImplementation().contains(implementation);
                    } else if (implementation == null) {
                        return service.alias().contains(serviceName)
                                && (service.aliasImplementation() == null || service.aliasImplementation().isEmpty());
                    }

                    return false;
                })
                .findFirst();
    }

    private static String normalizeKey(String key) {
        String result =  key.replaceAll("([A-Z])", "_$1");

        if (result.startsWith("get_")) {
            result = result.replaceFirst("get_", "").toUpperCase();
        }

        if (result.startsWith("is_")) {
            result = result.replaceFirst("is_", "").toUpperCase();
        }

        return result.replaceAll("\\.", "_").toUpperCase();
    }

    private static boolean isNotInfrastructureService(Object instance) {
        AtomicBoolean matchesCriteria = new AtomicBoolean(false);
        ReflectionHelper.doWithMethods(instance.getClass(), method -> {
            if (method.getName().contains("initialize")) {
                matchesCriteria.set(true);
            }
        });

        return !matchesCriteria.get();
    }

    /**
     * Action builder.
     */
    public static class Builder extends AbstractCamelAction.Builder<CamelRunInfraAction, Builder>
            implements CamelInfraRunActionBuilder<CamelRunInfraAction, Builder> {

        private String serviceName;
        private String implementation;

        private String catalogName;
        private CamelCatalog catalog;

        private boolean autoRemove = CamelInfraSettings.isAutoRemoveServices();
        private boolean dumpServiceOutput = CamelInfraSettings.isDumpServiceOutput();

        public Builder() {
            // Camel context is optional for this action - set a default context so users do not have to set it explicitly
            camelContext = new DefaultCamelContext();
        }

        @Override
        public Builder service(String serviceName, String implementation) {
            this.serviceName = serviceName;
            this.implementation = implementation;
            return this;
        }

        @Override
        public Builder service(String serviceName) {
            this.serviceName = serviceName;
            return this;
        }

        @Override
        public Builder implementation(String implementation) {
            this.implementation = implementation;
            return this;
        }

        @Override
        public Builder autoRemove(boolean autoRemove) {
            this.autoRemove = autoRemove;
            return this;
        }

        @Override
        public Builder dumpServiceOutput(boolean dumpServiceOutput) {
            this.dumpServiceOutput = dumpServiceOutput;
            return this;
        }

        @Override
        public Builder catalog(String name) {
            this.catalogName = name;
            return this;
        }

        @Override
        public Builder catalog(Object o) {
            if (o instanceof CamelCatalog catalog) {
                this.catalog = catalog;
            } else {
                throw new CitrusRuntimeException("Invalid catalog object, expected CamelCatalog, but got %s".formatted(o.getClass().getName()));
            }

            return this;
        }

        public Builder catalog(CamelCatalog catalog) {
            this.catalog = catalog;
            return this;
        }

        @Override
        protected CamelRunInfraAction doBuild() {
            if (StringUtils.hasText(catalogName) && referenceResolver != null) {
                if (referenceResolver.isResolvable(catalogName, CamelCatalog.class))  {
                    catalog = referenceResolver.resolve(catalogName, CamelCatalog.class);
                } else {
                    throw new CitrusRuntimeException("Missing Camel catalog for name '%s'");
                }
            }

            if (catalog == null) {
                if (referenceResolver != null && referenceResolver.isResolvable(CamelCatalog.class)) {
                    catalog = referenceResolver.resolve(CamelCatalog.class);
                } else {
                    catalog = new DefaultCamelCatalog();
                }
            }

            return new CamelRunInfraAction(this);
        }
    }
}
