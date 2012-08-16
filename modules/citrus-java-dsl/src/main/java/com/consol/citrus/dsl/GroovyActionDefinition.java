/*
 * Copyright 2006-2012 the original author or authors.
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

package com.consol.citrus.dsl;

import org.springframework.core.io.Resource;

import com.consol.citrus.script.GroovyAction;
import com.consol.citrus.util.FileUtils;

/**
 * Action executes groovy scripts either specified inline or from external file resource.
 * 
 * @author Max Argyo, Giulia DelBravo
 * @since 1.3
 */
public class GroovyActionDefinition extends AbstractActionDefinition<GroovyAction> {

	public GroovyActionDefinition(GroovyAction action) {
	    super(action);
    }
	
	/**
     * Use a script template from file path.
     * @param scriptTemplate the scriptTemplate to set
     */
    public GroovyActionDefinition template(String scriptTemplatePath) {
        action.setScriptTemplateResource(FileUtils.getResourceFromFilePath(scriptTemplatePath));
        return this;
    }
	
	/**
     * Use a script template resource.
     * @param scriptTemplate the scriptTemplate to set
     */
	public GroovyActionDefinition template(Resource scriptTemplate) {
		action.setScriptTemplateResource(scriptTemplate);
		return this;
	}
	
	/**
     * Prevent script template usage.
     * @param useScriptTemplate the useScriptTemplate to set
     */
	public GroovyActionDefinition skipTemplate() {
		action.setUseScriptTemplate(false);
		return this;
	}
}
