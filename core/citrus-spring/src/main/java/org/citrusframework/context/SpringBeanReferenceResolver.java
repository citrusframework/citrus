/*
 * Copyright 2006-2016 the original author or authors.
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

package org.citrusframework.context;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.citrusframework.context.resolver.TypeAliasResolver;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.ReferenceResolver;
import org.citrusframework.spi.SimpleReferenceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * Spring bean reference resolver operates on given application context to resolve bean references.
 *
 * @author Christoph Deppisch
 * @since 2.5
 */
public class SpringBeanReferenceResolver implements ReferenceResolver, ApplicationContextAware {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(SpringBeanReferenceResolver.class);

    private ApplicationContext applicationContext;

    private ReferenceResolver fallback = new SimpleReferenceResolver();

    private final Map<String, TypeAliasResolver<?, ?>> typeAliasResolvers = new HashMap<>();

    /**
     * Default constructor.
     */
    public SpringBeanReferenceResolver() {
        super();
    }

    /**
     * Constructor initializes with given application context.
     * @param applicationContext
     */
    public SpringBeanReferenceResolver(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public <T> T resolve(Class<T> requiredType) {
        try {
            return applicationContext.getBean(requiredType);
        } catch (NoSuchBeanDefinitionException e) {
            if (fallback.isResolvable(requiredType)) {
                return fallback.resolve(requiredType);
            }

            return resolveAlias(requiredType, this::resolve)
                    .orElseThrow(() -> new CitrusRuntimeException(String.format("Unable to find bean reference for type '%s'", requiredType), e));
        }
    }

    @Override
    public <T> T resolve(String name, Class<T> type) {
        try {
            return applicationContext.getBean(name, type);
        } catch (NoSuchBeanDefinitionException e) {
            if (fallback.isResolvable(name, type)) {
                return fallback.resolve(name, type);
            }

            return resolveAlias(type, clazz -> resolve(name, clazz))
                    .orElseThrow(() -> new CitrusRuntimeException(String.format("Unable to find bean reference for name '%s'", name), e));
        }
    }

    @Override
    public Object resolve(String name) {
        try {
            return applicationContext.getBean(name);
        } catch (NoSuchBeanDefinitionException e) {
            if (fallback.isResolvable(name)) {
                return fallback.resolve(name);
            }

            throw new CitrusRuntimeException(String.format("Unable to find bean reference for name '%s'", name), e);
        }
    }

    @Override
    public <T> Map<String, T> resolveAll(Class<T> requiredType) {
        Map<String, T>  beans = applicationContext.getBeansOfType(requiredType);

        if (beans.isEmpty()) {
            if (fallback.isResolvable(requiredType)) {
                return fallback.resolveAll(requiredType);
            }

            return resolveAllAlias(requiredType, this::resolveAll)
                    .orElseGet(HashMap::new);
        }

        return beans;
    }

    @Override
    public boolean isResolvable(String name) {
        return applicationContext.containsBean(name) || fallback.isResolvable(name);
    }

    @Override
    public boolean isResolvable(Class<?> type) {
        boolean canResolve = applicationContext.getBeanNamesForType(type).length > 0 || fallback.isResolvable(type);

        if (!canResolve) {
            Optional<TypeAliasResolver<?, ?>> aliasResolver = typeAliasResolvers.values().stream()
                    .filter(resolver -> resolver.isAliasFor(type))
                    .findFirst();

            if (aliasResolver.isEmpty()) {
                aliasResolver = TypeAliasResolver.lookup().values().stream()
                        .filter(resolver -> resolver.isAliasFor(type))
                        .findFirst();
            }

            if (aliasResolver.isPresent()) {
                canResolve = applicationContext.getBeanNamesForType(aliasResolver.get().getAliasType()).length > 0 || fallback.isResolvable(aliasResolver.get().getAliasType());
            }
        }

        return canResolve;
    }

    @Override
    public boolean isResolvable(String name, Class<?> type) {
        boolean canResolve = Arrays.asList(applicationContext.getBeanNamesForType(type)).contains(name) || fallback.isResolvable(name, type);

        if (!canResolve) {
            if (typeAliasResolvers.containsKey(name) && typeAliasResolvers.get(name).isAliasFor(type)) {
                canResolve = Arrays.asList(applicationContext.getBeanNamesForType(typeAliasResolvers.get(name).getAliasType())).contains(name) || fallback.isResolvable(name, typeAliasResolvers.get(name).getAliasType());
            }

            Optional<TypeAliasResolver<?, ?>> aliasResolver = typeAliasResolvers.values().stream()
                    .filter(resolver -> resolver.isAliasFor(type))
                    .findFirst();

            if (aliasResolver.isEmpty()) {
                aliasResolver = TypeAliasResolver.lookup().values().stream()
                        .filter(resolver -> resolver.isAliasFor(type))
                        .findFirst();
            }

            if (aliasResolver.isPresent()) {
                canResolve = Arrays.asList(applicationContext.getBeanNamesForType(aliasResolver.get().getAliasType())).contains(name) || fallback.isResolvable(name, aliasResolver.get().getAliasType());
            }
        }

        return canResolve;
    }

    /**
     * Specifies the fallback.
     * @param fallback
     */
    public SpringBeanReferenceResolver withFallback(ReferenceResolver fallback) {
        this.fallback = fallback;
        return this;
    }

    @Override
    public void bind(String name, Object value) {
        fallback.bind(name, value);
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<T> resolveAlias(Class<T> source, Function<Class<?>, ?> supplier) {
        Optional<TypeAliasResolver<?, ?>> aliasResolver = typeAliasResolvers.values().stream()
                .filter(resolver -> resolver.isAliasFor(source))
                .findFirst();

        if (aliasResolver.isEmpty()) {
            aliasResolver = TypeAliasResolver.lookup().values().stream()
                    .filter(resolver -> resolver.isAliasFor(source))
                    .findFirst();
        }

        if (aliasResolver.isPresent()) {
            TypeAliasResolver<T, ?> resolver = (TypeAliasResolver<T, ?>) aliasResolver.get();

            try {
                return Optional.of(resolver.adapt(supplier.apply(resolver.getAliasType())));
            } catch (Exception e) {
                logger.warn(String.format("Unable to resolve alias type %s for required source %s", resolver.getAliasType(), source));
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    @SuppressWarnings("unchecked")
    private <T> Optional<Map<String, T>> resolveAllAlias(Class<T> source, Function<Class<?>, Map<String, ?>> supplier) {
        Optional<TypeAliasResolver<?, ?>> aliasResolver = typeAliasResolvers.values().stream()
                .filter(resolver -> resolver.isAliasFor(source))
                .findFirst();

        if (aliasResolver.isEmpty()) {
            aliasResolver = TypeAliasResolver.lookup().values().stream()
                    .filter(resolver -> resolver.isAliasFor(source))
                    .findFirst();
        }

        if (aliasResolver.isPresent()) {
            TypeAliasResolver<T, ?> resolver = (TypeAliasResolver<T, ?>) aliasResolver.get();

            try {
                return Optional.of(supplier.apply(resolver.getAliasType())
                        .entrySet()
                        .stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, v -> resolver.adapt(v.getValue()))));
            } catch (Exception e) {
                logger.warn(String.format("Unable to resolve alias type %s for required source %s", resolver.getAliasType(), source));
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    public void registerTypeAliasResolver(String name, TypeAliasResolver<?, ?> aliasResolver) {
        this.typeAliasResolvers.put(name, aliasResolver);
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    /**
     * Specifies the fallback.
     * @param fallback
     */
    public void setFallback(ReferenceResolver fallback) {
        this.fallback = fallback;
    }

    /**
     * Obtains the applicationContext.
     * @return
     */
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
