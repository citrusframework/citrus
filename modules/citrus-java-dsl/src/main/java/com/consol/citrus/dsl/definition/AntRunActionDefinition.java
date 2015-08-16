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

package com.consol.citrus.dsl.definition;

import com.consol.citrus.actions.AntRunAction;
import org.apache.tools.ant.BuildListener;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * Action running ANT build targets during test.
 * 
 * @author Christoph Deppisch
 * @since 1.3
 * @deprecated since 2.3 in favor of using {@link com.consol.citrus.dsl.builder.AntRunBuilder}
 */
public class AntRunActionDefinition extends AbstractActionDefinition<AntRunAction> {

    /**
     * Constructor using action field.
     * @param action
     */
	public AntRunActionDefinition(AntRunAction action) {
	    super(action);
    }

    /**
     * Default constructor.
     */
    public AntRunActionDefinition() {
        super(new AntRunAction());
    }

    public AntRunActionDefinition buildFilePath(String buildFilePath) {
        action.setBuildFilePath(buildFilePath);
        return this;
    }

	/**
     * Build target name to call.
     * @param target
     */
	public AntRunActionDefinition target(String target) {
		action.setTarget(target);
		return this;
	}

	/**
     * Multiple build target names to call.
     * @param targets
     */
    public AntRunActionDefinition targets(String ... targets) {
        action.setTargets(StringUtils.collectionToCommaDelimitedString(Arrays.asList(targets)));
        return this;
    }

    /**
     * Adds a build property by name and value.
     * @param name
     * @param value
     */
    public AntRunActionDefinition property(String name, Object value) {
        action.getProperties().put(name, value);
        return this;
    }

    /**
     * Adds a build property file reference by file path.
     * @param filePath
     */
    public AntRunActionDefinition propertyFile(String filePath) {
        action.setPropertyFilePath(filePath);
        return this;
    }

    /**
     * Adds custom build listener implementation.
     * @param buildListener
     */
    public AntRunActionDefinition listener(BuildListener buildListener) {
        action.setBuildListener(buildListener);
        return this;
    }
}
