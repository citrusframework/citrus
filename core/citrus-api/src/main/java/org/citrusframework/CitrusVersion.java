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

package org.citrusframework;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class CitrusVersion {

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(CitrusVersion.class);

    /** Citrus version */
    private static String version;

    /* Load Citrus version */
    static {
        try (final InputStream in = CitrusVersion.class.getClassLoader().getResourceAsStream("META-INF/citrus.version")) {
            Properties versionProperties = new Properties();
            versionProperties.load(in);
            version = versionProperties.get("citrus.version").toString();

            if (version.equals("${project.version}")) {
                logger.warn("Citrus version has not been filtered with Maven project properties yet");
                version = "";
            }
        } catch (IOException e) {
            logger.warn("Unable to read Citrus version information", e);
            version = "";
        }
    }

    /**
     * Prevent instantiation.
     */
    private CitrusVersion() {
        super();
    }

    /**
     * Gets the Citrus version.
     * @return
     */
    public static String version() {
        return version;
    }
}
