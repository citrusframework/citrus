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

package com.consol.citrus.util;

import com.consol.citrus.Citrus;
import com.consol.citrus.context.TestContext;
import com.consol.citrus.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.SimpleTypeConverter;
import org.springframework.core.io.InputStreamSource;
import org.springframework.util.StringUtils;
import org.springframework.xml.transform.StringSource;
import org.w3c.dom.Node;

import javax.xml.transform.Source;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamSource;
import java.io.*;
import java.util.*;

/**
 * @author Christoph Deppisch
 * @since 2.5
 */
public abstract class TypeConversionUtils {

    /** Logger */
    private static Logger log = LoggerFactory.getLogger(TypeConversionUtils.class);

    /**
     * Prevent instantiation.
     */
    private TypeConversionUtils() {
        super();
    }

    /**
     * Converts target object to required type if necessary.
     *
     * @param target
     * @param type
     * @param <T>
     * @return
     */
    public static <T> T convertIfNecessary(Object target, Class<T> type) {
        if (type.isInstance(target)) {
            return type.cast(target);
        }

        if (Source.class.isAssignableFrom(type)) {
            if (target.getClass().isAssignableFrom(String.class)) {
                return (T) new StringSource(String.valueOf(target));
            } else if (target.getClass().isAssignableFrom(Node.class)) {
                return (T) new DOMSource((Node) target);
            } else if (target.getClass().isAssignableFrom(InputStreamSource.class)) {
                try {
                    return (T) new StreamSource(((InputStreamSource)target).getInputStream());
                } catch (IOException e) {
                    log.warn("Failed to create stream source from object", e);
                }
            }
        }

        if (Map.class.isAssignableFrom(type)) {
            String mapString = String.valueOf(target);

            Properties props = new Properties();
            try {
                props.load(new StringReader(mapString.substring(1, mapString.length() - 1).replace(", ", "\n")));
            } catch (IOException e) {
                throw new CitrusRuntimeException("Failed to reconstruct object of type map", e);
            }
            Map<String, Object> map = new LinkedHashMap<>();
            for (Map.Entry<Object, Object> entry : props.entrySet()) {
                map.put(entry.getKey().toString(), entry.getValue());
            }

            return (T) map;
        }

        if (String[].class.isAssignableFrom(type)) {
            String arrayString = String.valueOf(target).replaceAll("^\\[", "").replaceAll("\\]$", "").replaceAll(",\\s", ",");
            return (T) StringUtils.commaDelimitedListToStringArray(String.valueOf(arrayString));
        }

        if (List.class.isAssignableFrom(type)) {
            String listString = String.valueOf(target).replaceAll("^\\[", "").replaceAll("\\]$", "").replaceAll(",\\s", ",");
            return (T) Arrays.asList(StringUtils.commaDelimitedListToStringArray(String.valueOf(listString)));
        }

        if (byte[].class.isAssignableFrom(type) && target instanceof String) {
            try {
                return (T) String.valueOf(target).getBytes(Citrus.CITRUS_FILE_ENCODING);
            } catch (UnsupportedEncodingException e) {
                return (T) String.valueOf(target).getBytes();
            }
        }

        try {
            return new SimpleTypeConverter().convertIfNecessary(target, type);
        } catch (ConversionNotSupportedException e) {
            if (String.class.equals(type)) {
                log.warn(String.format("Using object toString representation: %s", e.getMessage()));
                return (T) target.toString();
            }

            throw e;
        }
    }

    /**
     * Convert value string to required type.
     * @param value
     * @param type
     * @return
     */
    public static <T> T convertStringToType(String value, Class<T> type) {
        if (type.isAssignableFrom(String.class)) {
            return (T) value;
        } else if (type.isAssignableFrom(int.class) || type.isAssignableFrom(Integer.class)) {
            return (T) Integer.valueOf(value);
        } else if (type.isAssignableFrom(short.class) || type.isAssignableFrom(Short.class)) {
            return (T) Short.valueOf(value);
        }  else if (type.isAssignableFrom(byte.class) || type.isAssignableFrom(Byte.class)) {
            return (T) Byte.valueOf(value);
        }  else if (type.isAssignableFrom(long.class) || type.isAssignableFrom(Long.class)) {
            return (T) Long.valueOf(value);
        } else if (type.isAssignableFrom(boolean.class) || type.isAssignableFrom(Boolean.class)) {
            return (T) Boolean.valueOf(value);
        } else if (type.isAssignableFrom(float.class) || type.isAssignableFrom(Float.class)) {
            return (T) Float.valueOf(value);
        } else if (type.isAssignableFrom(double.class) || type.isAssignableFrom(Double.class)) {
            return (T) Double.valueOf(value);
        }

        throw new CitrusRuntimeException(String.format("Unable to convert '%s' to required type '%s'", value, type.getName()));
    }

    /**
     * Convert value string to required type or read bean of type from application context.
     * @param value
     * @param type
     * @param context
     * @return
     */
    public static <T> T convertStringToType(String value, Class<T> type, TestContext context) {
        try {
            return convertStringToType(value, type);
        } catch (CitrusRuntimeException e) {
            // try to resolve bean in application context
            if (context.getApplicationContext() != null && context.getApplicationContext().containsBean(value)) {
                Object bean = context.getApplicationContext().getBean(value);
                if (type.isAssignableFrom(bean.getClass())) {
                    return (T) bean;
                }
            }

            throw new CitrusRuntimeException(String.format("Unable to convert '%s' to required type '%s' - also no bean of required type available in application context", value, type.getName()), e.getCause());
        }
    }
}
