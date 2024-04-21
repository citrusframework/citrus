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

package org.citrusframework.spi;

import org.citrusframework.exceptions.CitrusRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class PropertiesLoader {

    private static final Logger logger = LoggerFactory.getLogger(PropertiesLoader.class);

    private PropertiesLoader() {
        // Not intended for instantiation
    }

    public static Properties loadProperties(Resource resource) {
        Properties properties = new Properties();
        try (InputStream inputStream = resource.getInputStream()) {
            logger.debug("Loading properties resource: {}", resource);
            loadProperties(resource.getLocation(), properties, inputStream);
        } catch (IOException e) {
            throwPropertiesLoadingFailedException(resource.getLocation(), e);
        }
        return properties;
    }

    public static Properties loadProperties(String path) {
        Properties properties = new Properties();
        try (InputStream in = ResourcePathTypeResolver.class.getClassLoader().getResourceAsStream(path)) {
            if (in == null) {
                throw new CitrusRuntimeException(String.format("Failed to locate resource path '%s'!", path));
            }

            loadProperties(path, properties, in);
        } catch (IOException e) {
            throwPropertiesLoadingFailedException(path, e);
        }

        return properties;
    }

    private static void throwPropertiesLoadingFailedException(String path, IOException e) {
        throw new CitrusRuntimeException(String.format("Unable to load properties from resource path configuration at '%s'", path), e);
    }

    private static void loadProperties(String path, Properties properties, InputStream in) throws IOException {
        if (path.endsWith(".xml")) {
            properties.loadFromXML(in);
        } else {
            properties.load(in);
        }
    }
}
