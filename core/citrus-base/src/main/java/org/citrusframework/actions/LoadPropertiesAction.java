/*
 * Copyright 2006-2010 the original author or authors.
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

package org.citrusframework.actions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.citrusframework.AbstractTestActionBuilder;
import org.citrusframework.context.TestContext;
import org.citrusframework.exceptions.CitrusRuntimeException;
import org.citrusframework.spi.Resource;
import org.citrusframework.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Action reads property files and creates test variables for every property entry. File
 * resource path can define a resource located on classpath or file system.
 *
 * @author Christoph Deppisch
 */
public class LoadPropertiesAction extends AbstractTestAction {

    /** File resource path */
    private final String filePath;

    /** Logger */
    private static final Logger logger = LoggerFactory.getLogger(LoadPropertiesAction.class);

    /**
     * Default constructor.
     */
    public LoadPropertiesAction(Builder builder) {
        super("load", builder);

        this.filePath = builder.filePath;
    }

    @Override
    public void doExecute(TestContext context) {
        Resource resource = FileUtils.getFileResource(filePath, context);

        if (logger.isDebugEnabled()) {
            logger.debug("Reading property file " + FileUtils.getFileName(resource.getLocation()));
        }

        Properties props = FileUtils.loadAsProperties(resource);

        Map<String, Object> unresolved = new LinkedHashMap<>();
        for (Entry<Object, Object> entry : props.entrySet()) {
            String key = entry.getKey().toString();

            if (logger.isDebugEnabled()) {
                logger.debug("Loading property: " + key + "=" + props.getProperty(key) + " into variables");
            }

            if (logger.isDebugEnabled() && context.getVariables().containsKey(key)) {
                logger.debug("Overwriting property " + key + " old value:" + context.getVariable(key)
                        + " new value:" + props.getProperty(key));
            }

            try {
                context.setVariable(key, context.replaceDynamicContentInString(props.getProperty(key)));
            } catch (CitrusRuntimeException e) {
                unresolved.put(key, props.getProperty(key));
            }
        }

        context.resolveDynamicValuesInMap(unresolved).forEach(context::setVariable);

        logger.info("Loaded property file " + FileUtils.getFileName(resource.getLocation()));
    }

    /**
     * Gets the file.
     * @return the file
     */
    public String getFilePath() {
        return filePath;
    }

    /**
     * Action builder.
     */
    public static final class Builder extends AbstractTestActionBuilder<LoadPropertiesAction, Builder> {

        private String filePath;

        /**
         * Fluent API action building entry method used in Java DSL.
         * @return
         */
        public static Builder load() {
            return new Builder();
        }

        /**
         * Fluent API action building entry method used in Java DSL.
         * @param filePath
         * @return
         */
        public static Builder load(String filePath) {
            Builder builder = new Builder();
            builder.filePath(filePath);
            return builder;
        }

        public Builder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        @Override
        public LoadPropertiesAction build() {
            return new LoadPropertiesAction(this);
        }
    }
}
