/*
 * Copyright 2006-2015 the original author or authors.
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

package com.consol.citrus.dsl.builder;

import com.consol.citrus.actions.AntRunAction;
import org.apache.tools.ant.BuildListener;
import org.springframework.util.StringUtils;

import java.util.Arrays;

/**
 * Action running ANT build targets during test.
 * 
 * @author Christoph Deppisch
 * @since 2.3
 */
public class AntRunBuilder extends AbstractTestActionBuilder<AntRunAction> {

    /**
     * Constructor using action field.
     * @param action
     */
	public AntRunBuilder(AntRunAction action) {
	    super(action);
    }

    /**
     * Default constructor.
     */
    public AntRunBuilder() {
        super(new AntRunAction());
    }

    /**
     * Sets the build file path.
     * @param buildFilePath
     * @return
     */
    public AntRunBuilder buildFilePath(String buildFilePath) {
        action.setBuildFilePath(buildFilePath);
        return this;
    }
	
	/**
     * Build target name to call.
     * @param target
     */
	public AntRunBuilder target(String target) {
		action.setTarget(target);
		return this;
	}
	
	/**
     * Multiple build target names to call.
     * @param targets
     */
    public AntRunBuilder targets(String ... targets) {
        action.setTargets(StringUtils.collectionToCommaDelimitedString(Arrays.asList(targets)));
        return this;
    }
    
    /**
     * Adds a build property by name and value.
     * @param name
     * @param value
     */
    public AntRunBuilder property(String name, Object value) {
        action.getProperties().put(name, value);
        return this;
    }
    
    /**
     * Adds a build property file reference by file path.
     * @param filePath
     */
    public AntRunBuilder propertyFile(String filePath) {
        action.setPropertyFilePath(filePath);
        return this;
    }
    
    /**
     * Adds custom build listener implementation.
     * @param buildListener
     */
    public AntRunBuilder listener(BuildListener buildListener) {
        action.setBuildListener(buildListener);
        return this;
    }
}
