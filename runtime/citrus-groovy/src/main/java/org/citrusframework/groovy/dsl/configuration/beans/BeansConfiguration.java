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

package org.citrusframework.groovy.dsl.configuration.beans;

import java.lang.reflect.InvocationTargetException;
import java.util.Locale;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.lang.GroovyObjectSupport;
import groovy.lang.GroovyRuntimeException;
import groovy.lang.MissingMethodException;
import org.citrusframework.Citrus;
import org.citrusframework.CitrusContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.message.DefaultMessageQueue;
import org.citrusframework.spi.ReferenceResolver;

public class BeansConfiguration extends GroovyObjectSupport {

    private final ReferenceResolver referenceResolver;

    public BeansConfiguration(Citrus citrus) {
        this(citrus.getCitrusContext());
    }

    public BeansConfiguration(CitrusContext citrusContext) {
        this(citrusContext.getReferenceResolver());
    }

    public BeansConfiguration(ReferenceResolver referenceResolver) {
        this.referenceResolver = referenceResolver;
    }

    public void beans(@DelegatesTo(BeansConfiguration.class) Closure<?> callable) {
        callable.setResolveStrategy(Closure.DELEGATE_FIRST);
        callable.setDelegate(this);
        callable.call();
    }

    public void bean(Class<?> type) {
        bean(sanitizeBeanName(type.getSimpleName()), type);
    }

    public void bean(String name, Class<?> type) {
        try {
            referenceResolver.bind(name, type.getConstructor().newInstance());
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new CitrusRuntimeException(String.format("Failed to instantiate bean of type '%s' - no default constructor available", type));
        }
    }

    public void bean(String name, Class<?> type, Closure<?> callable) {
        try {
            Object bean = type.getDeclaredConstructor().newInstance();
            callable.setResolveStrategy(Closure.DELEGATE_ONLY);
            callable.setDelegate(bean);
            callable.call();

            referenceResolver.bind(name, bean);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new CitrusRuntimeException(String.format("Failed to instantiate bean of type '%s' - no default constructor available", type));
        }
    }

    public void bean(String name, Closure<?> callable) {
        Object bean = callable.call();
        if (bean == null) {
            throw new CitrusRuntimeException("Failed to instantiate bean from closure - expected bean instance return value but was null");
        }
        referenceResolver.bind(name, bean);
    }

    public void queue(String name) {
        referenceResolver.bind(name, new DefaultMessageQueue(name));
    }

    public void propertyMissing(String name, Object value) {
        referenceResolver.bind(name, value);
    }

    public Object methodMissing(String name, Object argLine) {
        if (argLine == null) {
            throw new MissingMethodException(name, BeansConfiguration.class, null);
        }

        Object[] args = (Object[]) argLine;
        if (args.length == 2) {
            Class<?> type = (Class<?>) args[0];
            if (args[1] instanceof Closure<?> closure) {

                try {
                    Object bean = type.getDeclaredConstructor().newInstance();
                    closure.setResolveStrategy(Closure.DELEGATE_ONLY);
                    closure.setDelegate(bean);
                    closure.call();

                    referenceResolver.bind(name, bean);
                    return bean;
                } catch (IllegalAccessException | InstantiationException | InvocationTargetException | NoSuchMethodException e) {
                    throw new GroovyRuntimeException(String.format("Failed to instantiate bean of type '%s'", type), e);
                }
            }
        } else if (args.length == 1) {
            if (args[0] instanceof Closure<?> closure) {
                closure.setResolveStrategy(Closure.DELEGATE_ONLY);

                Object bean = closure.call();
                referenceResolver.bind(name, bean);
            }
        }

        throw new MissingMethodException(name, BeansConfiguration.class, args);
    }

    /**
     * Uncapitalize given bean name.
     * @param name
     * @return
     */
    private static String sanitizeBeanName(String name) {
        return name.substring(0, 1).toLowerCase(Locale.US) + name.substring(1);
    }
}
