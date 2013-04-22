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

import com.consol.citrus.model.spring.beans.Beans;

import java.io.File;
import java.util.List;

/**
 * Helper class for loading and persisting the citrus config as well as locating elements within the config.
 *
 * @author Martin.Maher@consol.de
 * @since 2013.04.20
 */
public interface CitrusConfigHelper {

    /**
     * Returns all top level config elements, matching the supplied <code>clazz</code> parameter.
     * <br />
     * Top level config elements are elements that are a direct child of the root beans element. E.g.
     * <pre>
     * <beans>
     *     <element-one>
     *         <element-two></element-two>
     *     </element-one>
     *     <element-three></element-three>
     * </beans>
     * </pre>
     *
     * Both element-one and element-three are top level config elements, whereas element-two is not.
     *
     * @param jaxbConfig the root element of the citrus-config. Cannot be null.
     * @param clazz the element class to search for
     * @param <T> the type of the class to search for
     * @return any matching config elements or an empty list
     */
    <T> List<T> getConfigElementsByType(Beans jaxbConfig, Class<T> clazz);

    /**
     * Loads the citrus config for the supplied <code>path</code>.
     *
     * @param path the path to the config file
     * @return the loaded config
     */
    Beans loadCitrusConfig(File path);

    /**
     * Persists the config, saving it to the supplied <code>path</code> location. If the file already
     * exists, it's overwritten. Otherwise a new file is created.
     *
     * @param path
     * @param jaxbConfig
     */
    void persistCitrusConfig(File path, Beans jaxbConfig);

}
