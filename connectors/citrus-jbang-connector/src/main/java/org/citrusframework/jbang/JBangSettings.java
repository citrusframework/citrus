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

package org.citrusframework.jbang;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public final class JBangSettings {

    private static final String JBANG_PROPERTY_PREFIX = "citrus.jbang.";
    private static final String JBANG_ENV_PREFIX = "CITRUS_JBANG_";

    private static final String TRUST_URLS_PROPERTY = JBANG_PROPERTY_PREFIX + "trust.urls";
    private static final String TRUST_URLS_ENV = JBANG_ENV_PREFIX + "TRUST_URLS";

    private static final String JBANG_AUTO_DOWNLOAD_PROPERTY = JBANG_PROPERTY_PREFIX + "auto.download";
    private static final String JBANG_AUTO_DOWNLOAD_ENV = JBANG_ENV_PREFIX + "AUTO_DOWNLOAD";
    private static final String JBANG_AUTO_DOWNLOAD_DEFAULT = "true";

    private static final String JBANG_DOWNLOAD_URL_PROPERTY = JBANG_PROPERTY_PREFIX + "download.url";
    private static final String JBANG_DOWNLOAD_URL_ENV = JBANG_ENV_PREFIX + "DOWNLOAD_URL";
    private static final String JBANG_DOWNLOAD_URL_DEFAULT = "https://jbang.dev/releases/latest/download/jbang.zip";

    private static final String WORK_DIR_PROPERTY = JBANG_PROPERTY_PREFIX + "work.dir";
    private static final String WORK_DIR_ENV = JBANG_ENV_PREFIX + "WORK_DIR";
    private static final String WORK_DIR_DEFAULT = ".citrus-jbang";

    private static final String DUMP_PROCESS_OUTPUT_PROPERTY = JBANG_PROPERTY_PREFIX + "dump.process.output";
    private static final String DUMP_PROCESS_OUTPUT_ENV = JBANG_ENV_PREFIX + "DUMP_PROCESS_OUTPUT";
    private static final String DUMP_PROCESS_OUTPUT_DEFAULT = "false";

    private JBangSettings() {
        // prevent instantiation of utility class
    }

    /**
     * JBang download url.
     * @return
     */
    public static String getJBangDownloadUrl() {
        return System.getProperty(JBANG_DOWNLOAD_URL_PROPERTY,
                System.getenv(JBANG_DOWNLOAD_URL_ENV) != null ? System.getenv(JBANG_DOWNLOAD_URL_ENV) : JBANG_DOWNLOAD_URL_DEFAULT);
    }

    /**
     * JBang local work dir.
     * @return
     */
    public static Path getWorkDir() {
        String workDir = Optional.ofNullable(System.getProperty(WORK_DIR_PROPERTY, System.getenv(WORK_DIR_ENV))).orElse(WORK_DIR_DEFAULT);

        Path path = Paths.get(workDir);
        if (path.isAbsolute()) {
            return path.toAbsolutePath();
        } else {
            return Paths.get("").toAbsolutePath().resolve(workDir).toAbsolutePath();
        }
    }

    /**
     * JBang trust URLs.
     * @return
     */
    public static String[] getTrustUrls() {
        return Optional.ofNullable(System.getProperty(TRUST_URLS_PROPERTY, System.getenv(TRUST_URLS_ENV)))
                .map(urls -> urls.split(","))
                .orElseGet(() -> new String[]{});
    }

    /**
     * When set to true JBang binary is downloaded automatically when not present on host.
     * @return
     */
    public static boolean isAutoDownload() {
        return Boolean.parseBoolean(System.getProperty(JBANG_AUTO_DOWNLOAD_PROPERTY,
                System.getenv(JBANG_AUTO_DOWNLOAD_ENV) != null ? System.getenv(JBANG_AUTO_DOWNLOAD_ENV) : JBANG_AUTO_DOWNLOAD_DEFAULT));
    }

    /**
     * When set to true JBang process output will be redirected to a file in the current working directory.
     * @return
     */
    public static boolean isDumpProcessOutput() {
        return Boolean.parseBoolean(System.getProperty(DUMP_PROCESS_OUTPUT_PROPERTY,
                System.getenv(DUMP_PROCESS_OUTPUT_ENV) != null ? System.getenv(DUMP_PROCESS_OUTPUT_ENV) : DUMP_PROCESS_OUTPUT_DEFAULT));
    }

}
