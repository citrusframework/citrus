/*
 * Copyright 2022 the original author or authors.
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.citrusframework.groovy.dsl.configuration.endpoints;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.citrusframework.endpoint.Endpoint;
import org.citrusframework.endpoint.EndpointBuilder;
import groovy.lang.MissingPropertyException;
import org.citrusframework.util.ReflectionHelper;

/**
 * @author Christoph Deppisch
 */
public class EndpointBuilderConfiguration<T extends Endpoint> implements Supplier<T> {

    private final EndpointBuilder<T> builder;

    private static final Map<Class<?>, Class<?>> WRAPPER_TO_PRIMITIVE = new HashMap<>();

    static {
        WRAPPER_TO_PRIMITIVE.put(Boolean.class, boolean.class);
        WRAPPER_TO_PRIMITIVE.put(Byte.class, byte.class);
        WRAPPER_TO_PRIMITIVE.put(Character.class, char.class);
        WRAPPER_TO_PRIMITIVE.put(Double.class, double.class);
        WRAPPER_TO_PRIMITIVE.put(Float.class, float.class);
        WRAPPER_TO_PRIMITIVE.put(Integer.class, int.class);
        WRAPPER_TO_PRIMITIVE.put(Long.class, long.class);
        WRAPPER_TO_PRIMITIVE.put(Short.class, short.class);
        WRAPPER_TO_PRIMITIVE.put(Void.class, void.class);
    }

    public EndpointBuilderConfiguration(EndpointBuilder<T> builder) {
        this.builder = builder;
    }

    public void propertyMissing(String name, Object value) {
        Method m = ReflectionHelper.findMethod(builder.getClass(), name, value.getClass());

        if (m == null) {
            if (isWrapperForPrimitive(value.getClass())) {
                m = ReflectionHelper.findMethod(builder.getClass(), name, WRAPPER_TO_PRIMITIVE.get(value.getClass()));
            }

            if (m == null) {
                throw new MissingPropertyException(name, this.builder.getClass());
            }
        }

        try {
            m.invoke(builder, value);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new MissingPropertyException(name, this.builder.getClass(), e);
        }
    }

    private static boolean isWrapperForPrimitive(Class<?> type) {
        return WRAPPER_TO_PRIMITIVE.containsKey(type);
    }

    @Override
    public T get() {
        return builder.build();
    }
}
