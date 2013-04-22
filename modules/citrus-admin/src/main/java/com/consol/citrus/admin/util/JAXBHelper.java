/*
 * Copyright 2006-2013 the original author or authors.
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
package com.consol.citrus.admin.util;

import javax.xml.bind.JAXBContext;
import javax.xml.transform.Result;
import java.io.File;

/**
 * Utility class for creating {@link JAXBContext} as well as for marshaling and un-marshaling xml.
 *
 * @author Martin.Maher@consol.de
 * @since 2013.04.20
 */
public interface JAXBHelper {

    /**
     * Creates a new JAXB context based on the supplied <code>paths</code>
     *
     * @param paths the paths or packages to use when (un-)marshaling
     * @return the JAXB Context
     */
    JAXBContext createJAXBContextByPath(String... paths);

    /**
     * Marshal JAXB element
     *
     * @param element the element to marshal
     * @return the string representation
     */
    <T> String marshal(JAXBContext context, T element);

    /**
     * Marshal JAXB Element
     *
     * @param context the JAXBContext
     * @param element the element to marshal
     * @param result the result of the marshaling
     */
    <T> void marshal(JAXBContext context, T element, Result result);

    /**
     *
     * @param context
     * @param element
     * @param file
     * @param <T>
     */
    <T> void marshal(JAXBContext context, T element, File file);

    /**
     * Un-marshal XML to JAXB representation.
     *
     * @param clazz the JAXB element to unmarshal
     * @param raw the raw xml
     * @return the JAXB element
     */
    <T> T unmarshal(JAXBContext context, Class<T> clazz, String raw);

    /**
     *
     * @param context
     * @param clazz
     * @param file
     * @param <T>
     * @return
     */
    <T> T unmarshal(JAXBContext context, Class<T> clazz, File file);
}
