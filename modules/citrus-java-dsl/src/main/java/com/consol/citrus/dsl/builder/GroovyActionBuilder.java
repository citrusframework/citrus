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

import com.consol.citrus.exceptions.CitrusRuntimeException;
import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.util.FileUtils;
import org.springframework.core.io.Resource;

import java.io.IOException;

/**
 * Action executes groovy scripts either specified inline or from external file resource.
 * 
 * @author Christoph Deppisch
 * @since 2.3
 */
public class GroovyActionBuilder extends AbstractTestActionBuilder<GroovyAction> {

	/**
	 * Constructor using action field.
	 * @param action
	 */
	public GroovyActionBuilder(GroovyAction action) {
	    super(action);
    }

	/**
	 * Default constructor.
	 */
	public GroovyActionBuilder() {
		super(new GroovyAction());
	}

	/**
	 * Sets the Groovy script to execute.
	 * @param script
	 * @return
	 */
	public GroovyActionBuilder script(String script) {
		action.setScript(script);
		return this;
	}

	/**
	 * Sets the Groovy script to execute.
	 * @param scriptResource
	 * @return
	 */
	public GroovyActionBuilder script(Resource scriptResource) {
		try {
			action.setScript(FileUtils.readToString(scriptResource));
		} catch (IOException e) {
			throw new CitrusRuntimeException("Failed to read script resource file", e);
		}
		return this;
	}

	/**
	 * Use a script template from file path.
	 * @param scriptTemplatePath the scriptTemplate to set
	 */
	public GroovyActionBuilder template(String scriptTemplatePath) {
		action.setScriptTemplatePath(scriptTemplatePath);
		return this;
	}
	
	/**
     * Use a script template resource.
     * @param scriptTemplate the scriptTemplate to set
     */
	public GroovyActionBuilder template(Resource scriptTemplate) {
		try {
            action.setScriptTemplate(FileUtils.readToString(scriptTemplate));
        } catch (IOException e) {
            throw new CitrusRuntimeException("Failed to read script template file", e);
        }
		return this;
	}
	
	/**
     * Prevent script template usage.
     */
	public GroovyActionBuilder skipTemplate() {
		action.setUseScriptTemplate(false);
		return this;
	}
}
