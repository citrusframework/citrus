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
            String arrayString = String.valueOf(target).replaceAll("^\\[", "").replaceAll("\\]$", "");
            return (T) StringUtils.commaDelimitedListToStringArray(String.valueOf(arrayString));
        }

        if (List.class.isAssignableFrom(type)) {
            String listString = String.valueOf(target).replaceAll("^\\[", "").replaceAll("\\]$", "");
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
}
